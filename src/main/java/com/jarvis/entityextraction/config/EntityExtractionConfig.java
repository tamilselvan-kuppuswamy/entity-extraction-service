package com.jarvis.entityextraction.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "entity-config")
public class EntityExtractionConfig {
  private Map<String, UseCaseFields> cases = new HashMap<>();
  private double confidenceThreshold;
  private String azureOpenAiEndpoint;
  private String azureOpenAiKey;
  private String model;

  @Data
  public static class UseCaseFields {
    private Set<String> allowedFields;
    private Set<String> requiredFields;
  }
}
