package com.frauas.workforce_planning.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mock3b")
public class Mock3BController {

  @PostMapping("/api/group1/workforce-request")
  public ResponseEntity<String> receiveRequest(@RequestBody Map<String,Object> body) {
    return ResponseEntity.ok("OK");
  }

  @PostMapping("/api/group1/workforce-decision")
  public ResponseEntity<String> receiveDecision(@RequestBody Map<String,Object> body) {
    return ResponseEntity.ok("OK");
  }
}


