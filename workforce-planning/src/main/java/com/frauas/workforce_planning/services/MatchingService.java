package com.frauas.workforce_planning.services;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new IllegalStateException("Matching allowed only for APPROVED requests.");
        }

        // 1. Prepare Pool
        Set<Long> appliedIds = employeeApplicationRepository.findByStaffingRequest_RequestId(requestId)
                .stream().map(app -> app.getEmployee().getId()).collect(Collectors.toSet());

        List<Employee> available = employeeRepository.findByMatchingAvailability(MatchingAvailability.AVAILABLE);
        List<Employee> applied = employeeApplicationRepository.findByStaffingRequest_RequestId(requestId)
                .stream().map(EmployeeApplication::getEmployee).filter(Objects::nonNull).toList();

        Map<Long, Employee> merged = new LinkedHashMap<>();
        available.forEach(e -> merged.put(e.getId(), e));
        applied.forEach(e -> merged.put(e.getId(), e));
        
        Set<Long> leadershipIds = userRepository.findLeadershipEmployeeIds();

        // 2. Filter and Rank
        return merged.values().stream()
                .filter(e -> e.getId() != null)
                .filter(e -> !leadershipIds.contains(e.getId()))
                .filter(e -> passesDepartmentGroup(e, request))
                .filter(e -> passesHoursStrict(e, request))
                .filter(e -> passesWageStrict(e, request))
                .filter(e -> passesLocation(e, request))
                .filter(e -> passesExperienceStrict(e, request)) // NEW: Experience Filter
                .filter(e -> hasAtLeastOneSkill(e, request))
                .sorted(Comparator
                        .comparingInt((Employee e) -> getMatchedSkillCount(e, request)).reversed()
                        .thenComparingDouble(e -> scoringService.score(e, request, appliedIds.contains(e.getId()))).reversed()
                )
                .limit(Math.max(topN, 0))
                .map(e -> toDto(requestId, e, scoringService.score(e, request, appliedIds.contains(e.getId())), appliedIds.contains(e.getId())))
                .toList();
    }

    // ---------------- Hard Filters ----------------

    private boolean passesExperienceStrict(Employee e, StaffingRequest r) {
        if (r.getExperienceYears() == null) return true;
        if (e.getExperienceYears() == null) return false;
        // Rule: Employee must have EQUAL OR MORE years than requested
        return e.getExperienceYears() >= r.getExperienceYears();
    }

    private boolean passesDepartmentGroup(Employee e, StaffingRequest r) {
        if (r.getDepartment() == null || e.getDepartment() == null) {
            return false;
        }
        return Objects.equals(r.getDepartment().getId(), e.getDepartment().getId());
    }

    private boolean passesHoursStrict(Employee e, StaffingRequest r) {
        if (r.getAvailabilityHoursPerWeek() == null) return true;
        return Objects.equals(r.getAvailabilityHoursPerWeek(), e.getTotalHoursPerWeek());
    }

    private boolean passesWageStrict(Employee e, StaffingRequest r) {
        if (r.getWagePerHour() == null || e.getWagePerHour() == null) return true;
        return e.getWagePerHour().compareTo(r.getWagePerHour()) <= 0;
    }

    private boolean passesLocation(Employee e, StaffingRequest r) {
        if ("Remote".equalsIgnoreCase(r.getWorkLocation())) return true;
        if ("Onsite".equalsIgnoreCase(r.getWorkLocation())) {
            return r.getProjectLocation() != null && r.getProjectLocation().equalsIgnoreCase(e.getPrimaryLocation());
        }
        return true;
    }

    private boolean hasAtLeastOneSkill(Employee e, StaffingRequest r) {
        return getMatchedSkillCount(e, r) > 0;
    }

    private int getMatchedSkillCount(Employee e, StaffingRequest r) {
        if (r.getRequiredSkills() == null || e.getSkills() == null) return 0;
        
        // We use the new normalization that handles spaces
        Set<String> empSkills = normalizeSkills(e.getSkills());
        Set<String> reqSkills = normalizeSkills(r.getRequiredSkills());
        
        return (int) reqSkills.stream()
                .filter(empSkills::contains)
                .count();
    }

    private Set<String> normalizeSkills(List<String> list) {
        if (list == null) return Set.of();
        return list.stream()
                .filter(Objects::nonNull)
                .map(s -> s.replaceAll("\\s+", "").toLowerCase().trim()) 
                // The replaceAll("\\s+", "") is the magic part! It removes all spaces.
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // ---------------- DTO Mapper ----------------

    private MatchedEmployeeDTO toDto(Long requestId, Employee e, double score, boolean applied) {
        List<EmployeeLanguageDTO> langs = employeeRepository.findLanguagesWithProficiency(e.getId())
                .stream().map(row -> new EmployeeLanguageDTO((String) row[0], (String) row[1])).toList();

        return new MatchedEmployeeDTO(
                requestId, e.getId(), e.getEmployeeId(), e.getFirstName(), e.getLastName(),
                e.getEmail(), e.getPrimaryLocation(), e.getTotalHoursPerWeek(),
                e.getWagePerHour(), e.getExperienceYears(), Math.round(score * 10000.0) / 100.0,
                applied, seniorityFromExperience(e.getExperienceYears()),
                (e.getPerformanceRating() != null ? e.getPerformanceRating() + "/5" : "N/A"),
                e.getEmergencyContact(), (e.getSkills() != null ? e.getSkills() : List.of()), langs
        );
    }

    private String seniorityFromExperience(Integer years) {
        if (years == null) return "Unknown";
        if (years <= 2) return "Junior";
        if (years <= 5) return "Mid-Level";
        return "Senior";
    }
}