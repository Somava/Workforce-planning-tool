package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.dto.ExternalDecisionRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DecisionIntegrationService {

  @Value("${integration.team3b.base-url:}")
  private String baseUrl;

  @Value("${integration.team3b.decision-path:}")
  private String decisionPath;

  private final RestTemplate restTemplate = new RestTemplate();

  public void sendDecision(ExternalDecisionRequestDTO payload) {
    if (baseUrl == null || baseUrl.isBlank() || decisionPath == null || decisionPath.isBlank()) {
      System.out.println("[3B Decision] Skipping (no decision endpoint configured yet). Payload=" + payload);
      return;
    }
    String url = baseUrl + decisionPath;
    restTemplate.postForEntity(url, payload, String.class);
  }
}
