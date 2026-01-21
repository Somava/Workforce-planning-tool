package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingScoringService {

    // Weights (sum ~ 1.0; applied bonus is extra)
    private static final double W_SKILLS = 0.40;
    private static final double W_HOURS  = 0.20;
    private static final double W_WAGE   = 0.15;
    private static final double W_EXP    = 0.10;
    private static final double W_LOC    = 0.05;
    private static final double W_PERF   = 0.10;


    private static final double APPLIED_BONUS = 0.0; // bonus if employee applied

    public double score(Employee e, StaffingRequest r, boolean applied) {
        double skills = skillsScore(e.getSkills(), r.getRequiredSkills());
        double hours  = hoursScore(e.getTotalHoursPerWeek(), r.getAvailabilityHoursPerWeek());
        double wage   = wageScore(e.getWagePerHour(), r.getWagePerHour());
        double exp    = experienceScore(e.getExperienceYears(), r.getExperienceYears());
        double loc    = locationScore(e.getPrimaryLocation(), r.getWorkLocation(), r.getProjectLocation());
        double perf   = performanceScore(e.getPerformanceRating()); // ✅ ADD

double total =
        (skills * W_SKILLS) +
        (hours  * W_HOURS) +
        (wage   * W_WAGE) +
        (exp    * W_EXP) +
        (loc    * W_LOC) +
        (perf   * W_PERF); // ✅ ADD
 

        if (applied) total += APPLIED_BONUS;

        // keep in [0..1] range (applied bonus may push slightly above 1)
        return Math.min(1.0, Math.max(0.0, total));
    }

    // ---------------- helpers ----------------

    private double skillsScore(List<String> employeeSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return 1.0; // no required skills => perfect
        if (employeeSkills == null || employeeSkills.isEmpty()) return 0.0;

        Set<String> emp = normalizeSet(employeeSkills);
        Set<String> req = normalizeSet(requiredSkills);

        long matched = req.stream().filter(emp::contains).count();
        return clamp01((double) matched / (double) req.size());
    }
    
    private double performanceScore(Double ratingOutOf5) {
    // rating is like 0.0 .. 5.0 (ex: 4.6)
    if (ratingOutOf5 == null) return 0.6; // neutral default if missing
    double normalized = ratingOutOf5 / 5.0; // -> 0..1
    return clamp01(normalized);
    }


    private double hoursScore(Integer employeeTotal, Integer required) {
    if (required == null) return 0.7;      // neutral if request doesn't specify
    if (employeeTotal == null) return 0.4; // mild penalty for missing data

    if (employeeTotal >= required) return 1.0; // can cover required hours

    double ratio = (double) employeeTotal / (double) required; // partial coverage
    return clamp01(0.2 + 0.8 * ratio);
}



    private double wageScore(BigDecimal employeeWage, BigDecimal requestWage) {
    if (requestWage == null || requestWage.compareTo(BigDecimal.ZERO) <= 0) return 0.7;
    if (employeeWage == null || employeeWage.compareTo(BigDecimal.ZERO) <= 0) return 0.4;

    int cmp = employeeWage.compareTo(requestWage);

    // If employee is within budget, reward closeness (budget - employee)
    if (cmp <= 0) {
        BigDecimal diff = requestWage.subtract(employeeWage); // >= 0
        BigDecimal ratio = diff.divide(requestWage, 6, RoundingMode.HALF_UP);
        return clamp01(1.0 - ratio.doubleValue()); // exact budget => 1.0, cheaper => slightly lower
    }

    // If employee is over budget, penalize harder
    BigDecimal over = employeeWage.subtract(requestWage); // > 0
    BigDecimal ratio = over.divide(requestWage, 6, RoundingMode.HALF_UP);

    // Example: 10% over budget => 0.5, 20% => 0.0 (tune if needed)
    double s = 0.5 - (ratio.doubleValue() * 5.0);
    return clamp01(s);
 }

   private double experienceScore(Integer empYears, Integer reqYears) {
    if (reqYears == null || reqYears <= 0) return 0.7;
    if (empYears == null || empYears <= 0) return 0.4;

    if (empYears >= reqYears) {
        int extra = empYears - reqYears;
        return clamp01(0.8 + Math.min(0.2, extra * 0.02));
    }

    double ratio = (double) empYears / (double) reqYears;
    return clamp01(0.2 + 0.6 * ratio);
}

 private double locationScore(String employeeLocation, String workLoc, String projectLoc) {
    if (workLoc != null && workLoc.trim().equalsIgnoreCase("Remote")) return 1.0;

    if (workLoc != null && workLoc.trim().equalsIgnoreCase("Onsite")) {
        if (projectLoc == null || projectLoc.trim().isEmpty()) return 0.7;
        if (employeeLocation == null || employeeLocation.trim().isEmpty()) return 0.0;
        return employeeLocation.trim().equalsIgnoreCase(projectLoc.trim()) ? 1.0 : 0.0;
    }

    return 0.7;
}


    private Set<String> normalizeSet(List<String> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private double clamp01(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return x;
    }
}
