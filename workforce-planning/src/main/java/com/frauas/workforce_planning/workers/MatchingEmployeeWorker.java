package com.frauas.workforce_planning.workers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.frauas.workforce_planning.repository.EmployeeMatchingRepository;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class MatchingEmployeeWorker {

  private final EmployeeMatchingRepository matchingRepository;

  public MatchingEmployeeWorker(EmployeeMatchingRepository matchingRepository) {
    this.matchingRepository = matchingRepository;
  }

  @JobWorker(type = "resourceMatcher")
  public void matchEmployees(final JobClient client, final ActivatedJob job) {

    Map<String, Object> variables = job.getVariablesAsMap();

    // Expect requiredSkills from BPMN (CSV string)
    String requiredSkillsRaw = (String) variables.getOrDefault("requiredSkills", "");

    // Optional: keep reading workload if your BPMN uses it (not used in DB query yet)
    // Object workloadObj = variables.get("workload");

    List<String> skills = Arrays.stream(requiredSkillsRaw.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(String::toLowerCase)
        .collect(Collectors.toList());

    // min 2 matches if 2+ skills provided, else 1
    int minMatches = skills.size() >= 2 ? 2 : 1;

    Map<String, Object> resultVars = new HashMap<>();

    if (skills.isEmpty()) {
      // No skills provided => treat as not found
      resultVars.put("suitableResourceFound", false);
      resultVars.put("matchedEmployeeId", null);
      resultVars.put("matchedSkillCount", 0);
      resultVars.put("matchScore", 0);

      client.newCompleteCommand(job.getKey()).variables(resultVars).send().join();
      return;
    }

    // Query DB for best matches (already sorted best-first)
    List<EmployeeMatchingRepository.MatchResult> matches =
        matchingRepository.findBestMatches(skills, minMatches);

    if (matches.isEmpty()) {
      // Not found -> BPMN should route to TriggerExternalProvider
      resultVars.put("suitableResourceFound", false);
      resultVars.put("matchedEmployeeId", null);
      resultVars.put("matchedSkillCount", 0);
      resultVars.put("matchScore", 0);
    } else {
      // Pick best match (first one)
      EmployeeMatchingRepository.MatchResult best = matches.get(0);

      resultVars.put("suitableResourceFound", true);
      resultVars.put("matchedEmployeeId", best.getEmployeeId());
      resultVars.put("matchedSkillCount", best.getMatchedSkillCount());
      resultVars.put("matchScore", best.getScore());
    }

    client.newCompleteCommand(job.getKey()).variables(resultVars).send().join();
  }
}
