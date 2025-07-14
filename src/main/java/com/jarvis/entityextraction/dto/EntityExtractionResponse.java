package com.jarvis.entityextraction.dto;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityExtractionResponse {
  private Map<String, Object> extractedEntities;
  private Set<String> missingFields;
  private String reason;
  private String language;
}
