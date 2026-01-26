package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.frauas.workforce_planning.model.entity.Department;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.entity.User;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.security.JwtAuthFilter;
import com.frauas.workforce_planning.services.MatchingService;
import com.frauas.workforce_planning.services.StaffingDecisionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/resource-planner")
public class ResourcePlannerController {

  private final MatchingService matchingService;
  private final StaffingDecisionService decisionService;
  private final UserRepository userRepository;
  private final StaffingRequestRepository staffingRequestRepository;

  public ResourcePlannerController(MatchingService matchingService,
                                    StaffingDecisionService decisionService,
                                    UserRepository userRepository,
                                    StaffingRequestRepository staffingRequestRepository) {
    this.matchingService = matchingService;
    this.decisionService = decisionService;
    this.userRepository = userRepository; 
    this.staffingRequestRepository = staffingRequestRepository;
  }

  @GetMapping("/staffing-requests/employee-matches")
  public ResponseEntity<MatchResponseDTO> getMatches(@RequestParam Long requestId,
                                                    @RequestParam(defaultValue = "10") int topN) {
        
    JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();   
    String role = p.selectedRole();
    if(!"ROLE_RESOURCE_PLNR".equals(role)) {
        
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "You are not authorized to perform this action"
        );
    }

    System.out.println("Fetching matches for requestId: " + requestId + " with topN: " + topN);
                                                       
    List<MatchedEmployeeDTO> matches = matchingService.matchEmployees(requestId, topN);

    if (matches.isEmpty()) {
      return ResponseEntity.ok(new MatchResponseDTO(
          "Unable to get employees for this role in our organisation",
          List.of()
      ));
    }

    return ResponseEntity.ok(new MatchResponseDTO(null, matches));
  }

  @PostMapping("/staffing-requests/employee-reserve-decision")
  public ResponseEntity<String> reserve(@RequestParam Long requestId,
                                        @RequestParam boolean internalFound,
                                        @RequestBody(required = false) CandidateActionRequest body) {

    JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();   
    String role = p.selectedRole();
    if(!"ROLE_RESOURCE_PLNR".equals(role)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "You are not authorized to perform this action"
        );
    }

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

  @GetMapping("/approved-requests")
    public ResponseEntity<List<StaffingRequest>> getApprovedForResourcePlanner() {
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
        String email = p.email();
        String role = p.selectedRole();
        if(!"ROLE_RESOURCE_PLNR".equals(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to perform this action"
            );
        }
        log.info("Fetching approved requests for resource planner email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found for email: " + email
                ));

        Department department = user.getEmployee().getDepartment();                
        
        List<StaffingRequest> pending = staffingRequestRepository.findApprovedForResourcePlanner(
            RequestStatus.APPROVED,
            department.getId()
        );

        if (pending.isEmpty()) {
            return ResponseEntity.ok()
                .header("X-Info", "No approved staffing requests found")
                .body(List.of());
        }

        return ResponseEntity.ok(pending);
    }

  
}