package com.frauas.workforce_planning.services;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.frauas.workforce_planning.dto.EmployeeLanguageDTO;
import com.frauas.workforce_planning.dto.MatchedEmployeeDTO;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.EmployeeApplicationRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;

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
long hoursPass = candidates.stream().filter(e -> passesContractHours(e, request)).count();
System.out.println("DEBUG: hoursPass = " + hoursPass);


System.out.println("DEBUG: expPass = " + expPass);
System.out.println("DEBUG: wagePass = " + wagePass);
System.out.println("DEBUG: locPass = " + locPass);
System.out.println("DEBUG: locPass = " + locPass);

// NEW log: show first few employees and why they fail
for (int i = 0; i < Math.min(5, candidates.size()); i++) {
    Employee e = candidates.get(i);
    System.out.println("DEBUG EMP[" + i + "] id=" + e.getEmployeeId()
            + " totalHours=" + e.getTotalHoursPerWeek()
            + " exp=" + e.getExperienceYears()
            + " wage=" + e.getWagePerHour()
            + " loc=" + e.getPrimaryLocation()
            + " | hoursOk=" + passesContractHours(e, request)
            + " | expOk=" + passesExperience(e, request)
            + " wageOk=" + passesWage(e, request)
            + " locOk=" + passesLocation(e, request));
}



// 2.5) Exclude leadership employees
Set<Long> leadershipEmployeeIds = userRepository.findLeadershipEmployeeIds();

// 3) Hard filters (NO scoring here)
List<Employee> filtered = candidates.stream()
        .filter(e -> !leadershipEmployeeIds.contains(e.getId())) //  exclude managers / heads / planners
        .filter(e -> passesContractHours(e, request))   
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
                    return toDto(requestId, e, score, applied);
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

    // 1) If staffing request is Remote => allow employees from ANY city
    String workLocMode = r.getWorkLocation(); // expected: "Remote" or "Onsite"
    if (!isBlank(workLocMode) && workLocMode.trim().equalsIgnoreCase("Remote")) {
        return true;
    }

    // 2) If not Remote, match employee primary location with request's project location (city)
    String reqCity = r.getProjectLocation(); // expected: "Berlin", "Frankfurt", etc.
    if (isBlank(reqCity)) return true; // no city specified => don't filter

    String empCity = e.getPrimaryLocation();
    if (isBlank(empCity)) return false;

    return empCity.trim().equalsIgnoreCase(reqCity.trim());
}


    private boolean passesContractHours(Employee e, StaffingRequest r) {
    Integer reqHours = r.getAvailabilityHoursPerWeek(); // 20 or 40
    if (reqHours == null) return true;

    Integer empTotal = e.getTotalHoursPerWeek(); // employee total hours = 20 or 40
    if (empTotal == null) return false;

    return empTotal.equals(reqHours);
}

  // ---------------- DTO mapping ----------------
private MatchedEmployeeDTO toDto(Long requestId, Employee e, double score, boolean applied) {
    String seniority = seniorityFromExperience(e.getExperienceYears());

    Double performanceRating = e.getPerformanceRating();
    String performanceGrade = (performanceRating != null)
            ? String.format(java.util.Locale.US, "%.2f/5", performanceRating)
            : "N/A";

    String emergencyContact = e.getEmergencyContact();

    List<String> skills = (e.getSkills() != null) ? e.getSkills() : List.of();

    

    double scorePercent = Math.round(score * 10000.0) / 100.0;
    List<EmployeeLanguageDTO> languages =
        employeeRepository.findLanguagesWithProficiency(e.getId())
                .stream()
                .map(row -> new EmployeeLanguageDTO(
                        (String) row[0], // language name
                        (String) row[1]  // proficiency
                ))
                .toList();

System.out.println(
    "Employee " + e.getEmployeeId() + " languages = " + languages
);

    return new MatchedEmployeeDTO(
            requestId,
            e.getId(),
            e.getEmployeeId(),
            e.getFirstName(),
            e.getLastName(),
            e.getEmail(),
            e.getPrimaryLocation(),

            e.getTotalHoursPerWeek(),  
            e.getWagePerHour(),
            e.getExperienceYears(),

            scorePercent,
            applied,

            seniority,
            performanceGrade,

            emergencyContact,
            skills,
            languages
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
