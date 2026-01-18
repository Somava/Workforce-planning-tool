package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.frauas.workforce_planning.dto.CandidateActionRequest;
import com.frauas.workforce_planning.dto.MatchedEmployeeDTO;
import com.frauas.workforce_planning.services.MatchingService;
import com.frauas.workforce_planning.services.StaffingDecisionService;

@RestController
@RequestMapping("/api")
public class StaffingMatchingController {

  private final MatchingService matchingService;
  private final StaffingDecisionService decisionService;

  public StaffingMatchingController(MatchingService matchingService,
                                    StaffingDecisionService decisionService) {
    this.matchingService = matchingService;
    this.decisionService = decisionService;
  }

  @GetMapping("/resource-planner/staffing-requests/{requestId}/matches")
  public List<MatchedEmployeeDTO> getMatches(@PathVariable Long requestId,
                                             @RequestParam(defaultValue = "10") int topN) {
    return matchingService.matchEmployees(requestId, topN);
  }

  @PostMapping("/resource-planner/staffing-requests/{requestId}/reserve")
  public ResponseEntity<String> reserve(@RequestParam Long requestId,
                                        @RequestParam boolean internalFound,
                                        @RequestBody CandidateActionRequest body) {
    decisionService.reserve(requestId, internalFound, body.employeeDbId());
    return ResponseEntity.ok("Request " + requestId + " has been processed");
  }

  @PostMapping("/department-head/staffing-requests/{requestId}/assign")
  public ResponseEntity<Void> assign(@PathVariable Long requestId,
                                     @RequestBody CandidateActionRequest body) {
    decisionService.assign(requestId, body.employeeDbId());
    return ResponseEntity.ok().build();
  }
}
