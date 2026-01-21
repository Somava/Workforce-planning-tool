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
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;


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

        // ✅ PUT THE DEBUG LOG RIGHT HERE
    System.out.println(
        "DEBUG requestId=" + requestId
        + " status=" + request.getStatus()
        + " hours=" + request.getAvailabilityHoursPerWeek()
        + " workLoc=" + request.getWorkLocation()
        + " projectLoc=" + request.getProjectLocation()
        + " deptId=" + (request.getDepartment() != null ? request.getDepartment().getId() : null)
        + " skills=" + request.getRequiredSkills()
    );


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

            

        // 2) Candidate pool from DB: start with strongest DB filter first (availability + capacity)
       List<Employee> availableEmployees =
        employeeRepository.findByMatchingAvailability(MatchingAvailability.AVAILABLE);

    // Employees who applied for THIS request (employee_application table)
     List<Employee> appliedEmployees = employeeApplicationRepository
        .findByStaffingRequest_RequestId(requestId)
        .stream()
        .map(EmployeeApplication::getEmployee)
        .filter(Objects::nonNull)
        .toList();


   // Merge AVAILABLE + APPLIED employees (dedupe by employee id)
   Map<Long, Employee> merged = new LinkedHashMap<>();
   for (Employee e : availableEmployees) merged.put(e.getId(), e);
   for (Employee e : appliedEmployees) merged.put(e.getId(), e);
    List<Employee> candidates = new ArrayList<>(merged.values());




/// 2.5) Exclude leadership employees
Set<Long> leadershipEmployeeIds = userRepository.findLeadershipEmployeeIds();

// 3) Hard filters (TRUE constraints only)
for (Employee emp : candidates) {
    boolean idOk = emp.getId() != null;
    boolean leaderOk = emp.getId() != null && !leadershipEmployeeIds.contains(emp.getId());
    boolean deptOk = passesDepartment(emp, request);
    boolean hoursOk = passesContractHours(emp, request);
    boolean locOk = passesLocation(emp, request);
    boolean wageOk = passesWage(emp, request);
    boolean skillsOk = passesSkillsAnyMatch(emp, request);

    // Print only the employee you care about (id=21) to avoid huge logs
    if (emp.getId() != null && emp.getId().equals(21L)) {
        System.out.println(
            "DEBUG EMP 21 => " +
            "idOk=" + idOk +
            " leaderOk=" + leaderOk +
            " deptOk=" + deptOk +
            " hoursOk=" + hoursOk +
            " locOk=" + locOk +
            " wageOk=" + wageOk +
            " skillsOk=" + skillsOk +
            " | empDept=" + (emp.getDepartment()!=null ? emp.getDepartment().getId() : null) +
            " empHours=" + emp.getTotalHoursPerWeek() +
            " empLoc=" + emp.getPrimaryLocation() +
            " empWage=" + emp.getWagePerHour() +
            " empSkills=" + emp.getSkills()
        );
    }
}

List<Employee> baseFiltered = candidates.stream()
        .filter(e -> e.getId() != null)
        .filter(e -> !leadershipEmployeeIds.contains(e.getId()))
        .filter(e -> passesDepartment(e, request)) 
        .filter(e -> passesContractHours(e, request))
        .filter(e -> passesLocation(e, request))  
        .filter(e -> passesWage(e, request))        
        .toList();

System.out.println("DEBUG baseFiltered (available/applied + no leadership + dept) = " + baseFiltered.size());

// 4) Skill gate: must match at least 1 required skill (prevents Python-only)
List<Employee> skillFiltered = baseFiltered.stream()
        .filter(e -> passesSkillsAnyMatch(e, request))  // your method already does ">=1 overlap"
        .toList();

System.out.println("DEBUG skillFiltered (>=1 skill overlap) = " + skillFiltered.size());

// If request has required skills and nobody matches ANY => return empty (trigger external worker)
if (request.getRequiredSkills() != null
        && !request.getRequiredSkills().isEmpty()
        && skillFiltered.isEmpty()) {
    return List.of();
}

// 5) Order: overlap bucket first (2 > 1), then score (waivers), then applied first
List<Employee> ordered = skillFiltered.stream()
        .sorted(Comparator
                .comparingInt((Employee e) -> matchedSkillCount(e, request)).reversed()
                .thenComparingDouble(e ->
                        scoringService.score(e, request, appliedEmployeeDbIds.contains(e.getId()))
                ).reversed()
                .thenComparing(e -> appliedEmployeeDbIds.contains(e.getId()), Comparator.reverseOrder())
        )
        .toList();

System.out.println("DEBUG ordered size = " + ordered.size());

// 6) Map to DTO ONCE + limit + return
return ordered.stream()
        .map(e -> {
            boolean applied = appliedEmployeeDbIds.contains(e.getId());
            double score = scoringService.score(e, request, applied);
            return toDto(requestId, e, score, applied);
        })
        .limit(Math.max(topN, 0))
        .toList();
    }


    // ---------------- Hard filters ----------------

// ---------------- Department grouping ----------------

private static final Map<Long, Set<Long>> GROUPS = Map.ofEntries(
    Map.entry(1L, Set.of(1L,2L,3L,4L)),
    Map.entry(2L, Set.of(1L,2L,3L,4L)),
    Map.entry(3L, Set.of(1L,2L,3L,4L)),
    Map.entry(4L, Set.of(1L,2L,3L,4L)),

    Map.entry(5L, Set.of(5L,6L,7L,8L)),
    Map.entry(6L, Set.of(5L,6L,7L,8L)),
    Map.entry(7L, Set.of(5L,6L,7L,8L)),
    Map.entry(8L, Set.of(5L,6L,7L,8L)),

    Map.entry(9L, Set.of(9L,10L,11L,12L)),
    Map.entry(10L, Set.of(9L,10L,11L,12L)),
    Map.entry(11L, Set.of(9L,10L,11L,12L)),
    Map.entry(12L, Set.of(9L,10L,11L,12L))
);

private boolean passesDepartment(Employee e, StaffingRequest r) {

    // request department_id
    Long reqDeptId = (r.getDepartment() != null)
            ? r.getDepartment().getId()
            : null;

    // employee department_id
    Long empDeptId = (e.getDepartment() != null)
            ? e.getDepartment().getId()
            : null;

    if (reqDeptId == null) return true;
    if (empDeptId == null) return false;

    boolean ok = GROUPS
            .getOrDefault(reqDeptId, Set.of(reqDeptId))
            .contains(empDeptId);

    System.out.println(
        "DEPT CHECK -> requestDept=" + reqDeptId +
        " employeeDept=" + empDeptId +
        " result=" + ok
    );

    return ok;
}


    private boolean passesExperience(Employee e, StaffingRequest r) {
        Integer req = r.getExperienceYears();
        if (req == null) return true; // no requirement
        Integer emp = e.getExperienceYears();
        if (emp == null) return false;
        return emp >= req;
    }

   private boolean passesSkillsStrict(Employee e, StaffingRequest r) {
    List<String> req = r.getRequiredSkills();
    if (req == null || req.isEmpty()) return true;

    List<String> empSkills = e.getSkills();
    if (empSkills == null || empSkills.isEmpty()) return false;

    Set<String> emp = normalizeSkills(empSkills);
    Set<String> required = normalizeSkills(req);

    return emp.containsAll(required);
}
   private boolean passesSkillsAnyMatch(Employee e, StaffingRequest r) {
    List<String> req = r.getRequiredSkills();
    if (req == null || req.isEmpty()) return true;

    List<String> empSkills = e.getSkills();
    if (empSkills == null || empSkills.isEmpty()) return false;

    Set<String> emp = normalizeSkills(empSkills);
    Set<String> required = normalizeSkills(req);

    return required.stream().anyMatch(emp::contains);
}
private Set<String> normalizeSkills(List<String> list) {
    if (list == null) return Set.of();
    return list.stream()
            .filter(Objects::nonNull)
            .map(s -> s.trim().toLowerCase())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
}

private int matchedSkillCount(Employee e, StaffingRequest r) {
    List<String> req = r.getRequiredSkills();
    if (req == null || req.isEmpty()) return 0;

    List<String> empSkills = e.getSkills();
    if (empSkills == null || empSkills.isEmpty()) return 0;

    Set<String> emp = normalizeSkills(empSkills);
    Set<String> required = normalizeSkills(req);

    return (int) required.stream().filter(emp::contains).count();
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
    String workLoc = r.getWorkLocation();       // only "Remote" or "Onsite"
    String projectLoc = r.getProjectLocation(); // city (meaningful)

    // Remote => anyone
    if (workLoc != null && workLoc.trim().equalsIgnoreCase("Remote")) {
        return true;
    }

    // Onsite => employee must be in project city
    if (workLoc != null && workLoc.trim().equalsIgnoreCase("Onsite")) {
        if (projectLoc == null || projectLoc.trim().isEmpty()) return true; // or false if strict
        String empCity = e.getPrimaryLocation();
        if (empCity == null || empCity.trim().isEmpty()) return false;
        return empCity.trim().equalsIgnoreCase(projectLoc.trim());
    }

    // unknown / null => don't block
    return true;
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
