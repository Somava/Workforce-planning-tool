package com.frauas.workforce_planning.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatchingEmployeeWorker {

    // Simple in-memory employee model (no DB yet)
    static class Employee {
        String id;
        String skill;
        int availableWorkload; // e.g. hours per week

        Employee(String id, String skill, int availableWorkload) {
            this.id = id;
            this.skill = skill;
            this.availableWorkload = availableWorkload;
        }
    }

    // Dummy employees – for now this replaces the database
    private final List<Employee> employees = List.of(
            new Employee("EMP001", "Java", 40),
            new Employee("EMP002", "Python", 20),
            new Employee("EMP003", "Java", 10),
            new Employee("EMP004", "Data Science", 30),
            new Employee("EMP005", "Java", 25)
    );

    // This worker is bound to the BPMN service task with type "match-employees"
    @JobWorker(type = "match-employees")
    public void matchEmployees(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variables = job.getVariablesAsMap();

        // ⚠ Use same variable names as in your BPMN & ValidateRequestDataWorker
        String requiredSkill = (String) variables.get("requiredSkills"); // or "requiredSkill" if BPMN uses that
        String workloadString = (String) variables.get("workload");      // requested hours

        int requiredWorkload = Integer.parseInt(workloadString);

        // -------- Matching logic (simple version) ----------
        Employee selected = null;

        for (Employee e : employees) {
            boolean skillMatches = e.skill.equalsIgnoreCase(requiredSkill);
            boolean capacityEnough = e.availableWorkload >= requiredWorkload;

            if (skillMatches && capacityEnough) {
                // simple rule: pick the first employee that fits
                selected = e;
                break;
            }
        }

        String matchedEmployeeId = (selected != null) ? selected.id : null;

        Map<String, Object> resultVars = new HashMap<>();
        resultVars.put("matchedEmployeeId", matchedEmployeeId);
        resultVars.put("matchFound", selected != null);

        System.out.println("Matched employee: " + matchedEmployeeId);

        client
            .newCompleteCommand(job.getKey())
            .variables(resultVars)
            .send()
            .join();
    }
}

