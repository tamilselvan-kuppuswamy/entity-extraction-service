package com.jarvis.entityextraction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntityExtractionRequest {
  @NotBlank(message = "Input text must not be blank")
  private String text;

  @NotBlank(message = "Business use case/intent must be specified")
  private String useCase; // e.g., "CreateShipment"

  private String language; // optional
}
