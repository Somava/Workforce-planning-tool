package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.CandidateActionRequest;
import com.frauas.workforce_planning.dto.MatchResponseDTO;
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
  public ResponseEntity<MatchResponseDTO> getMatches(@RequestParam Long requestId,
                                                    @RequestParam(defaultValue = "10") int topN) {

    List<MatchedEmployeeDTO> matches = matchingService.matchEmployees(requestId, topN);

    if (matches.isEmpty()) {
      return ResponseEntity.ok(new MatchResponseDTO(
          "Unable to get employees for this role in our organisation",
          List.of()
      ));
    }

    return ResponseEntity.ok(new MatchResponseDTO(null, matches));
  }

  @PostMapping("/resource-planner/staffing-requests/reserve")
  public ResponseEntity<String> reserve(@RequestParam Long requestId,
                                        @RequestParam boolean internalFound,
                                        @RequestBody(required = false) CandidateActionRequest body) {
    if (internalFound && body == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "employeeDbId is required when internalFound=true"
        );
    }
    Long employeeDbId = body != null ? body.employeeDbId() : null;
    decisionService.reserve(requestId, internalFound, employeeDbId);
    return ResponseEntity.ok("Request " + requestId + " has been processed");
  }

  
}