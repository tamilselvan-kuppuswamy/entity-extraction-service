package com.jarvis.entityextraction.controller;

import com.jarvis.entityextraction.dto.EntityExtractionRequest;
import com.jarvis.entityextraction.dto.EntityExtractionResponse;
import com.jarvis.entityextraction.service.EntityExtractionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/extract-entities")
@RequiredArgsConstructor
@Slf4j
public class EntityExtractionController {

  private final EntityExtractionService service;

  @PostMapping
  public ResponseEntity<EntityExtractionResponse> extract(
      @Valid @RequestBody EntityExtractionRequest request,
      @RequestHeader(value = "x-correlation-id", required = false) String correlationIdHeader) {
    String correlationId =
        (correlationIdHeader != null) ? correlationIdHeader : UUID.randomUUID().toString();
    MDC.put("correlationId", correlationId);
    log.info("Received extract-entities request: {}", request);

    EntityExtractionResponse response = service.extractEntities(request, correlationId);
    MDC.remove("correlationId");
    return ResponseEntity.ok(response);
  }
}
