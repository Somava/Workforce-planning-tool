package com.frauas.workforce_planning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.camunda.zeebe.spring.client.annotation.Deployment;

@SpringBootApplication
@Deployment(resources = "classpath:bpmn/workforce_planning.bpmn")
public class WorkforcePlanningApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkforcePlanningApplication.class, args);
    }

    /**
     * This bean allows the TriggerExternalProviderWorker to inject 
     * a RestTemplate to communicate with Group 3b's API.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}