package com.jarvis.entityextraction.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarvis.entityextraction.config.EntityExtractionConfig;
import com.jarvis.entityextraction.dto.EntityExtractionRequest;
import com.jarvis.entityextraction.dto.EntityExtractionResponse;
import com.jarvis.entityextraction.exception.EntityExtractionException;
import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityExtractionService {

  private final EntityExtractionConfig config;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OpenAIClient openAIClient;

  @PostConstruct
  public void init() {
    log.info(
        "Entity Extraction Service initialized with use cases: {}", config.getCases().keySet());
    this.openAIClient =
        new OpenAIClientBuilder()
            .credential(new AzureKeyCredential(config.getAzureOpenAiKey()))
            .endpoint(config.getAzureOpenAiEndpoint())
            .buildClient();
  }

  public EntityExtractionResponse extractEntities(
      EntityExtractionRequest request, String correlationId) {
    MDC.put("correlationId", correlationId);
    log.info(
        "Extracting entities for use case [{}], input: {}",
        request.getUseCase(),
        request.getText());

    try {
      EntityExtractionConfig.UseCaseFields caseConfig = config.getCases().get(request.getUseCase());
      if (caseConfig == null) {
        throw new EntityExtractionException("Unknown business use case: " + request.getUseCase());
      }

      Set<String> allowedFields = caseConfig.getAllowedFields();
      Set<String> requiredFields = caseConfig.getRequiredFields();

      Map<String, Object> extracted =
          callAzureOpenAiExtraction(
              request.getText(),
              allowedFields,
              requiredFields,
              request.getLanguage(),
              correlationId);

      Set<String> missing = new HashSet<>(requiredFields);
      missing.removeAll(extracted.keySet());

      return EntityExtractionResponse.builder()
          .extractedEntities(extracted)
          .missingFields(missing)
          .reason("Azure OpenAI extraction")
          .language(StringUtils.hasText(request.getLanguage()) ? request.getLanguage() : "en")
          .build();

    } catch (Exception e) {
      log.error("Entity extraction failed", e);
      throw new EntityExtractionException("Entity extraction failed: " + e.getMessage());
    } finally {
      MDC.remove("correlationId");
    }
  }

  private Map<String, Object> callAzureOpenAiExtraction(
      String text,
      Set<String> allowedFields,
      Set<String> requiredFields,
      String language,
      String correlationId)
      throws Exception {

    log.info("Calling Azure OpenAI with correlationId: {}", correlationId);

    FunctionDefinition functionDef =
        new FunctionDefinition("extract_shipment_entities")
            .setDescription("Extract shipment-related entities from user input.")
            .setParameters(
                BinaryData.fromObject(buildFunctionSchema(allowedFields, requiredFields)));

    List<ChatRequestMessage> messages =
        List.of(
            new ChatRequestSystemMessage(
                "You are a logistics assistant. Always extract shipment-related entities by calling the function `extract_shipment_entities`. "
                    + "Do not respond with plain text. Return partial values if needed."),
            new ChatRequestUserMessage(text));

    ChatCompletionsOptions options =
        new ChatCompletionsOptions(messages)
            .setFunctions(List.of(functionDef))
            .setFunctionCall(FunctionCallConfig.AUTO)
            .setTemperature(0.0);

    ChatCompletions completions = openAIClient.getChatCompletions(config.getModel(), options);

    if (completions == null || completions.getChoices().isEmpty()) {
      throw new EntityExtractionException("No response from OpenAI.");
    }

    ChatChoice firstChoice = completions.getChoices().getFirst();
    ChatResponseMessage message = firstChoice.getMessage();

    if (message == null || message.getFunctionCall() == null) {
      log.warn("GPT response missing function call: {}", message);
      throw new EntityExtractionException("No function call result in OpenAI response.");
    }

    String functionCallArguments = message.getFunctionCall().getArguments();
    log.info("Function call arguments: {}", functionCallArguments);

    @SuppressWarnings("unchecked")
    Map<String, Object> extracted = objectMapper.readValue(functionCallArguments, Map.class);

    extracted.keySet().retainAll(allowedFields);
    log.info("Extracted entities: {}", extracted);

    return extracted;
  }

  private Map<String, Object> buildFunctionSchema(
      Set<String> allowedFields, Set<String> requiredFields) {
    Map<String, Object> properties = new LinkedHashMap<>();
    for (String field : allowedFields) {
      Map<String, Object> prop = new LinkedHashMap<>();
      prop.put("type", "string");
      prop.put("description", "Extracted field: " + field);
      properties.put(field, prop);
    }

    Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("type", "object");
    parameters.put("properties", properties);
    parameters.put("required", new ArrayList<>(requiredFields));

    return parameters;
  }
}
