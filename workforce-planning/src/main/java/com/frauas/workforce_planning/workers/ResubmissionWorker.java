package com.frauas.workforce_planning.workers;

import org.springframework.stereotype.Component;

import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class ResubmissionWorker {

    @JobWorker(type = "send-resubmission-notification", autoComplete = true)
    public void handleResubmission() {
        // This is where your "send message" logic lives
        System.out.println("LOG: The process has ended with a Resubmission event.");
    }
}