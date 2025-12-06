package com.frauas.workforce_planning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.spring.client.annotation.Deployment;

@SpringBootApplication
@Deployment(resources = "classpath:resources/bpmn/workforce_planning.bpmn")
public class WorkforcePlanningApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkforcePlanningApplication.class, args);
	}

}
