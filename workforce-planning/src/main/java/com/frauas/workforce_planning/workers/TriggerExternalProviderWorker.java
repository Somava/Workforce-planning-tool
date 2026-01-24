package com.frauas.workforce_planning.workers;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.frauas.workforce_planning.config.Team3bConfig;
import com.frauas.workforce_planning.dto.ExternalWorkforce3BRequestDTO;
import com.frauas.workforce_planning.repository.RequestRepository;      

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class TriggerExternalProviderWorker {

    private final Team3bConfig team3bConfig;
    private final RestTemplate restTemplate;
    private final RequestRepository requestRepository; // Added Repository

    // Spring injects all three dependencies here
    public TriggerExternalProviderWorker(Team3bConfig team3bConfig, 
                                          RestTemplate restTemplate, 
                                          RequestRepository requestRepository) {
        this.team3bConfig = team3bConfig;
        this.restTemplate = restTemplate;
        this.requestRepository = requestRepository;
    }

    @JobWorker(type = "notify-group-3b")
    public void triggerExternalProvider(final JobClient client, final ActivatedJob job) {
        String targetUrl = team3bConfig.getFullUrl();
        
        // Inside TriggerExternalProviderWorker
        // 1. Get the data from Camunda (This has your projectName if it's in the variables)
        ExternalWorkforce3BRequestDTO camundaPayload = job.getVariablesAsType(ExternalWorkforce3BRequestDTO.class);
        Long requestId = camundaPayload.requestId();

        // 2. Try to find the DB record, but don't crash if it's missing
        var dbEntityOpt = requestRepository.findByRequestId(requestId);

        // 3. Logic: If DB exists, use its project info. Otherwise, use Camunda's info.
        Long finalProjectId = dbEntityOpt.isPresent() ? dbEntityOpt.get().getProjectId() : camundaPayload.projectId();
        String finalProjectName = dbEntityOpt.isPresent() ? dbEntityOpt.get().getProjectName() : camundaPayload.projectName();

        // 4. Construct the DTO using the "Best Available" data
        ExternalWorkforce3BRequestDTO enrichedPayload = new ExternalWorkforce3BRequestDTO(
            requestId,
            finalProjectId,
            finalProjectName,
            camundaPayload.jobTitle(),
            camundaPayload.description(),
            camundaPayload.availabilityHoursPerWeek(),
            camundaPayload.wagePerHour(),
            camundaPayload.skills(),
            camundaPayload.experienceYears(),
            camundaPayload.location(),
            camundaPayload.startDate(),
            camundaPayload.endDate()
        );

        // 5. Send
        try {
            restTemplate.postForEntity(targetUrl, enrichedPayload, String.class);
            System.out.println(">>> [WORKER] Sent JSON. Data source: " + (dbEntityOpt.isPresent() ? "Database" : "Camunda Variables"));
        } catch (Exception e) {
            throw new RuntimeException("External Provider Communication Failed: " + e.getMessage());
        }
    }
}