package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.frauas.workforce_planning.dto.StaffingDecisionRequest;


//import com.frauas.workforce_planning.dto.CandidateActionRequest;
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

  @GetMapping("/resource-planner/staffing-requests/matches")
  public ResponseEntity<?> getMatches(@RequestParam Long requestId,
                                    @RequestParam(defaultValue = "10") int topN) {

    List<MatchedEmployeeDTO> list = matchingService.matchEmployees(requestId, topN);

    // Ensure we never return null
    if (list == null || list.isEmpty()) {
        return ResponseEntity.ok(java.util.Map.of(
        "message", "Unable to get employees for this role in our organisation"
));

    }

    // Same response as before when employees exist
    return ResponseEntity.ok(list);
  }

  @PostMapping("/resource-planner/staffing-requests/decision")
  public ResponseEntity<Void> decision(@RequestParam Long requestId,
                                      @RequestBody StaffingDecisionRequest body) {
    decisionService.decide(requestId, body.accept(), body.employeeDbId());

    return ResponseEntity.ok().build();
  }
}
