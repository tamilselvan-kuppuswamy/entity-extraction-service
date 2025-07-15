package com.jarvis.entityextraction.exception.handler;

import com.jarvis.entityextraction.exception.EntityExtractionException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(EntityExtractionException.class)
  public ResponseEntity<Map<String, String>> handleEntityExtractionException(
          EntityExtractionException ex) {
    log.error("Entity Extraction error: {}", ex.getMessage());
    return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleOtherExceptions(Exception ex) {
    log.error("Unhandled exception: ", ex);
    return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
  }
}