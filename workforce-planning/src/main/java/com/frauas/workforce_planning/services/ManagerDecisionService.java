package com.frauas.workforce_planning.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.camunda.zeebe.client.ZeebeClient;

/**
 * Service to handle Camunda process flow for Manager decisions 
 * after a request has been rejected or failed.
 */
@Service
public class ManagerDecisionService {

    private static final Logger log = LoggerFactory.getLogger(ManagerDecisionService.class);

    @Autowired
    private ZeebeClient zeebeClient;

        public void completeReviewTask(Long requestId, String decision) {
        log.info("Publishing message for Request ID: {} with decision: {}", requestId, decision);

        // This variable tells the Gateway which path to take after the Receive Task
        Map<String, Object> variables = Map.of(
            "managerDecision", decision.toLowerCase().trim()
        );

        zeebeClient.newPublishMessageCommand()
                .messageName("Review Request Failure") // Matches 'Name' in Modeler
                .correlationKey(String.valueOf(requestId)) // Matches 'Subscription correlation key'
                .variables(variables)
                .send()
                .join();

        log.info("Message correlated successfully for Request {}", requestId);
    }
}