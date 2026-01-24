package com.frauas.workforce_planning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "integration.team3b")
public class Team3bConfig {
    private String baseUrl;
    private String requestPath;
    private String decisionPath; // 1. Add this field

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }

    public String getDecisionPath() { return decisionPath; } // 2. Add Getter
    public void setDecisionPath(String decisionPath) { this.decisionPath = decisionPath; } // 2. Add Setter

    public String getFullUrl() {
        return baseUrl + requestPath;
    }

    public String getDecisionUrl() { // 3. Add this helper method
        return baseUrl + decisionPath;
    }
}