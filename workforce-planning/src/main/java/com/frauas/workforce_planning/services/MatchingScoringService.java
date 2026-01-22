package com.frauas.workforce_planning.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.StaffingRequest;

@Service
public class MatchingScoringService {

    // Weights: Skills and Hours are most important
    private static final double W_SKILLS = 0.45; // Increased weight for skills
    private static final double W_HOURS  = 0.15;
    private static final double W_WAGE   = 0.10;
    private static final double W_EXP    = 0.15; // Increased weight for senior experience
    private static final double W_LOC    = 0.05;
    private static final double W_PERF   = 0.10;

    private static final double APPLIED_BONUS = 0.05; // 5% bonus for showing initiative

    public double score(Employee e, StaffingRequest r, boolean applied) {
        double skills = skillsScore(e.getSkills(), r.getRequiredSkills());
        double hours  = hoursScore(e.getTotalHoursPerWeek(), r.getAvailabilityHoursPerWeek());
        double wage   = wageScore(e.getWagePerHour(), r.getWagePerHour());
        double exp    = experienceScore(e.getExperienceYears(), r.getExperienceYears());
        double loc    = locationScore(e.getPrimaryLocation(), r.getWorkLocation(), r.getProjectLocation());
        double perf   = performanceScore(e.getPerformanceRating());

        double total = (skills * W_SKILLS) +
                       (hours  * W_HOURS) +
                       (wage   * W_WAGE) +
                       (exp    * W_EXP) +
                       (loc    * W_LOC) +
                       (perf   * W_PERF);

        if (applied) total += APPLIED_BONUS;

        return clamp01(total);
    }

    // ---------------- HELPER METHODS ----------------

    private double skillsScore(List<String> employeeSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return 1.0;
        if (employeeSkills == null || employeeSkills.isEmpty()) return 0.0;

        Set<String> emp = normalizeSet(employeeSkills);
        Set<String> req = normalizeSet(requiredSkills);

        long matched = req.stream().filter(emp::contains).count();
        double ratio = (double) matched / (double) req.size();
        
        // Quadratic boost: Having 3/3 is significantly better than 1/3
        return Math.pow(ratio, 1.5); 
    }

    private double hoursScore(Integer empHours, Integer reqHours) {
        if (reqHours == null) return 1.0;
        if (empHours == null) return 0.0;
        // Strict match requested: If they meet or exceed, full points
        return empHours >= reqHours ? 1.0 : (double) empHours / (double) reqHours;
    }

    private double wageScore(BigDecimal empWage, BigDecimal reqWage) {
        if (reqWage == null || empWage == null) return 1.0;
        
        // If within budget, score is higher the cheaper they are (efficiency)
        if (empWage.compareTo(reqWage) <= 0) {
            BigDecimal diff = reqWage.subtract(empWage);
            double savingsRatio = diff.divide(reqWage, 4, RoundingMode.HALF_UP).doubleValue();
            return 0.8 + (savingsRatio * 0.2); // Base 0.8 for being in budget + savings bonus
        }
        
        // Over budget (Note: The Hard Filter in MatchingService will usually catch this)
        return 0.0; 
    }

    private double experienceScore(Integer empYears, Integer reqYears) {
        if (reqYears == null) return 1.0;
        if (empYears == null) return 0.0;

        if (empYears >= reqYears) {
            // Reward "Over-qualification" up to a certain point
            int extra = empYears - reqYears;
            return clamp01(0.9 + (extra * 0.01)); 
        }
        return (double) empYears / (double) reqYears * 0.7; // Penalty for being under-experienced
    }

    private double performanceScore(Double rating) {
        if (rating == null) return 0.5; // Average
        return clamp01(rating / 5.0);
    }

    private double locationScore(String empLoc, String workLoc, String projLoc) {
        if ("Remote".equalsIgnoreCase(workLoc)) return 1.0;
        if (empLoc == null || projLoc == null) return 0.0;
        return empLoc.equalsIgnoreCase(projLoc) ? 1.0 : 0.0;
    }

    // CRITICAL: Space-removal normalization to fix "SpringBoot" vs "Spring Boot"
    private Set<String> normalizeSet(List<String> list) {
        if (list == null) return Set.of();
        return list.stream()
                .filter(Objects::nonNull)
                .map(s -> s.replaceAll("\\s+", "").toLowerCase().trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private double clamp01(double x) {
        return Math.min(1.0, Math.max(0.0, x));
    }
}