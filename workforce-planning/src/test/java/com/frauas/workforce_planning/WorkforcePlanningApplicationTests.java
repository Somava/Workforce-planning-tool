package com.frauas.workforce_planning;

// Ensure this import is present for Spring testing tools
import org.springframework.boot.test.context.SpringBootTest; 
import org.springframework.test.context.TestPropertySource; 
import org.junit.jupiter.api.Test;

// Imports for the Mock
import org.springframework.boot.test.mock.mockito.MockBean;
import io.camunda.zeebe.client.ZeebeClient; // <-- THIS is the critical import

@TestPropertySource(properties = {
    // Keep these properties to cover other aspects like JavaMailSender
    "spring.mail.host=test-smtp-server",
    "spring.mail.port=25",
    // We still keep the Zeebe client property as a safeguard
    "zeebe.client.enabled=false" 
})
@SpringBootTest
class WorkforcePlanningApplicationTests {

    // 1. ANNOTATE THE MOCK BEAN
    // This tells Spring Boot: "Before loading the context, create a mock object
    // of type ZeebeClient and place it into the application context."
    // This satisfies every single Camunda component that asks for a ZeebeClient.
    @MockBean
    private ZeebeClient zeebeClient;

    @Test
    void contextLoads() {
        // Simple test to check if the Spring context loads
        // It will now succeed because the ZeebeClient dependency is satisfied by the mock.
    }
}