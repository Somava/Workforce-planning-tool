package com.frauas.workforce_planning.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TriggerExternalProviderWorker {

    // job type MUST match the type configured on the BPMN service task
    @JobWorker(type = "trigger-external-provider")
    public void triggerExternalProvider(final JobClient client, final ActivatedJob job) {

        // 1. Read current process variables (including output from MatchingEmployeeWorker)
        Map<String, Object> vars = job.getVariablesAsMap();
        System.out.println("[TriggerExternalProvider] Incoming vars: " + vars);

        String requiredSkill  = (String) vars.get("requiredSkills");
        String workloadString = (String) vars.get("workload");
        Boolean matchFound    = (Boolean) vars.get("matchFound");  // should be false on this path

        // (Optional safety check – just to be sure)
        if (Boolean.TRUE.equals(matchFound)) {
            System.out.println("[TriggerExternalProvider] WARNING: matchFound was true but process came here.");
        }

        int requiredWorkload = 0;
        if (workloadString != null && !workloadString.isBlank()) {
            requiredWorkload = Integer.parseInt(workloadString);
        }

        // 2. Simulate calling an external provider (NO internal matching code here)
        boolean externalResourceFound = false;
        String externalEmployeeId = null;

        // Super simple dummy rule: if we know the skill and workload > 0,
        // we pretend an external provider always finds someone.
        if (requiredSkill != null && !requiredSkill.isBlank() && requiredWorkload > 0) {
            externalResourceFound = true;
            externalEmployeeId = "EXT-" + requiredSkill.toUpperCase() + "-001";
        }

        System.out.println("[TriggerExternalProvider] externalResourceFound=" 
                + externalResourceFound + ", externalEmployeeId=" + externalEmployeeId);

        // 3. Prepare result variables for the next gateway
        Map<String, Object> resultVars = new HashMap<>();
        resultVars.put("externalResourceFound", externalResourceFound);

        if (externalResourceFound) {
            resultVars.put("externalEmployeeId", externalEmployeeId);
            resultVars.put("assignmentSource", "EXTERNAL");
        }

        // 4. Complete the job
        client
            .newCompleteCommand(job.getKey())
            .variables(resultVars)
            .send()
            .join();
    }
}
