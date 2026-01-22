package com.frauas.workforce_planning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "integration.team3b")
public class Team3bConfig {
    private String baseUrl;
    private String requestPath;

    // Getters and Setters are MANDATORY for Spring to bind the YAML values
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }

    public String getFullUrl() {
        return baseUrl + requestPath;
    }
}