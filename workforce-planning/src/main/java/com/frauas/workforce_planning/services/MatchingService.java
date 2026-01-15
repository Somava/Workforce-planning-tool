package com.frauas.workforce_planning.services;
import com.frauas.workforce_planning.model.enums.RequestStatus;

import com.frauas.workforce_planning.dto.MatchedEmployeeDTO;
import com.frauas.workforce_planning.model.entity.Certification;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.model.entity.EmployeeCertification;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;
import com.frauas.workforce_planning.repository.EmployeeApplicationRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeApplicationRepository employeeApplicationRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final MatchingScoringService scoringService;
    private final UserRepository userRepository;
    

    public MatchingService(
            EmployeeRepository employeeRepository,
            EmployeeApplicationRepository employeeApplicationRepository,
            StaffingRequestRepository staffingRequestRepository,
            MatchingScoringService scoringService,
            UserRepository userRepository     
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeApplicationRepository = employeeApplicationRepository;
        this.staffingRequestRepository = staffingRequestRepository;
        this.scoringService = scoringService;
        this.userRepository = userRepository; 
    }

    public List<MatchedEmployeeDTO> matchEmployees(Long requestId, int topN) {

        StaffingRequest request = staffingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("StaffingRequest not found: " + requestId));

       //logs
       System.out.println("DEBUG: requestId = " + requestId);
      System.out.println("DEBUG: requestStatus = " + request.getStatus());
      System.out.println("DEBUG: requestExp = " + request.getExperienceYears());
      System.out.println("DEBUG: requestWage = " + request.getWagePerHour());
     System.out.println("DEBUG: requestWorkLoc = " + request.getWorkLocation());
     System.out.println("DEBUG: requestProjectLoc = " + request.getProjectLocation());


        // ✅ Add the status check RIGHT HERE
    if (request.getStatus() != RequestStatus.APPROVED) {
        throw new IllegalStateException(
                "Matching is allowed only for APPROVED requests. Current status = " + request.getStatus()
        );
    }

    int requiredHours = safeInt(request.getAvailabilityHoursPerWeek(), 0);

    //logs added
    System.out.println("DEBUG: requiredHours = " + requiredHours);


        // 1) Get all "applied" employees for this request (subset of internal employees)
        Set<Long> appliedEmployeeDbIds = employeeApplicationRepository
                .findByStaffingRequest_RequestId(requestId)
                .stream()
                .map(EmployeeApplication::getEmployee)
                .filter(Objects::nonNull)
                .map(Employee::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    //logs
    System.out.println("DEBUG: appliedEmployeeDbIds size = " + appliedEmployeeDbIds.size());


        // 2) Candidate pool from DB: start with strongest DB filter first (availability + capacity)
        List<Employee> candidates = employeeRepository
    .findByMatchingAvailability(MatchingAvailability.AVAILABLE);
        // TEMP DISABLED: capacity check will be enabled after hours subtraction logic
        //List<Employee> candidates = employeeRepository
                //.findByMatchingAvailabilityAndRemainingHoursPerWeekGreaterThanEqual(
                       // MatchingAvailability.AVAILABLE,
                       // requiredHours
                //);

    //logs
    System.out.println("DEBUG: candidates size (after DB query) = " + candidates.size());
    // NEW logs: how many pass each hard filter
long expPass = candidates.stream().filter(e -> passesExperience(e, request)).count();
long wagePass = candidates.stream().filter(e -> passesWage(e, request)).count();
long locPass  = candidates.stream().filter(e -> passesLocation(e, request)).count();

System.out.println("DEBUG: expPass = " + expPass);
System.out.println("DEBUG: wagePass = " + wagePass);
System.out.println("DEBUG: locPass = " + locPass);

// NEW log: show first few employees and why they fail
for (int i = 0; i < Math.min(5, candidates.size()); i++) {
    Employee e = candidates.get(i);
    System.out.println("DEBUG EMP[" + i + "] id=" + e.getEmployeeId()
            + " hours=" + e.getRemainingHoursPerWeek()
            + " exp=" + e.getExperienceYears()
            + " wage=" + e.getWagePerHour()
            + " loc=" + e.getPrimaryLocation()
            + " | expOk=" + passesExperience(e, request)
            + " wageOk=" + passesWage(e, request)
            + " locOk=" + passesLocation(e, request));
}



// 2.5) Exclude leadership employees
Set<Long> leadershipEmployeeIds = userRepository.findLeadershipEmployeeIds();

// 3) Hard filters (NO scoring here)
List<Employee> filtered = candidates.stream()
        .filter(e -> !leadershipEmployeeIds.contains(e.getId())) // 🚫 exclude managers / heads / planners
        .filter(e -> passesExperience(e, request))
        .filter(e -> passesWage(e, request))
        .filter(e -> passesLocation(e, request))
        .toList();

        //logs
        System.out.println("DEBUG: filtered size (after hard filters) = " + filtered.size());


        // 4) Score + map to DTO
        List<MatchedEmployeeDTO> scored = filtered.stream()
                .map(e -> {
                    boolean applied = appliedEmployeeDbIds.contains(e.getId());
                    double score = scoringService.score(e, request, applied);
                    return toDto(e, score, applied);
                })
                .sorted(Comparator.comparingDouble(MatchedEmployeeDTO::score).reversed())
                .limit(Math.max(topN, 0))
                .toList();

        return scored;
    }

    // ---------------- Hard filters ----------------

    private boolean passesExperience(Employee e, StaffingRequest r) {
        Integer req = r.getExperienceYears();
        if (req == null) return true; // no requirement
        Integer emp = e.getExperienceYears();
        if (emp == null) return false;
        return emp >= req;
    }

    /**
     * Wage rule assumption:
     * staffing_requests.wage_per_hour = max budget.
     * If you mean "offered wage", then remove this hard filter and score only.
     */
    private boolean passesWage(Employee e, StaffingRequest r) {
        BigDecimal budget = r.getWagePerHour();
        if (budget == null) return true; // no budget
        BigDecimal empWage = e.getWagePerHour();
        if (empWage == null) return false;
        return empWage.compareTo(budget) <= 0;
    }

    private boolean passesLocation(Employee e, StaffingRequest r) {
        String reqLoc = pickRequestLocation(r);
        if (isBlank(reqLoc)) return true; // not specified => don't filter

        String empLoc = e.getPrimaryLocation();
        if (isBlank(empLoc)) return false;

        String rl = reqLoc.trim().toLowerCase();
        String el = empLoc.trim().toLowerCase();

        // If request is remote, allow remote + non-remote (scoring will rank)
        if (rl.equals("remote")) return true;

        return el.equals(rl);
    }

    private String pickRequestLocation(StaffingRequest r) {
        if (!isBlank(r.getWorkLocation())) return r.getWorkLocation();
        return r.getProjectLocation();
    }

    // ---------------- DTO mapping ----------------

  // ---------------- DTO mapping ----------------
private MatchedEmployeeDTO toDto(Employee e, double score, boolean applied) {
    String seniority = seniorityFromExperience(e.getExperienceYears());
    Double performanceRating = e.getPerformanceRating();
    String performanceGrade = (performanceRating != null) ? String.format(java.util.Locale.US, "%.2f/5", performanceRating) : "N/A";

    List<String> certifications = e.getCertifications().stream()
            .map(ec -> ec.getCertification().getName())
            .distinct()
            .toList();

    double scorePercent = Math.round(score * 10000.0) / 100.0;

    return new MatchedEmployeeDTO(
            e.getId(),
            e.getEmployeeId(),
            e.getFirstName(),
            e.getLastName(),
            e.getEmail(),
            e.getPrimaryLocation(),
            e.getRemainingHoursPerWeek(),
            e.getWagePerHour(),
            e.getExperienceYears(),
            scorePercent,
            applied,
            seniority,
            performanceGrade,
            certifications
    );
}

 // ---------------- Merit helpers ----------------

    private String seniorityFromExperience(Integer years) {
        if (years == null) return "Unknown";
        if (years <= 2) return "Junior";
        if (years <= 5) return "Mid-Level";
        return "Senior";
    }


    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private int safeInt(Integer x, int def) {
        return x == null ? def : x;
    }
}
