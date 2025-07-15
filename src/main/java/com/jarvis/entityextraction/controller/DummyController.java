package com.jarvis.entityextraction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DummyController {
  @GetMapping("/")
  public ResponseEntity<String> home() {
    return ResponseEntity.ok("API is up and running");
  }
}
