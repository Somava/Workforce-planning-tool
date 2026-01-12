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
    private static final double W_SKILLS = 0.45;
    private static final double W_HOURS  = 0.20;
    private static final double W_WAGE   = 0.15;
    private static final double W_EXP    = 0.15;
    private static final double W_LOC    = 0.05;

    private static final double APPLIED_BONUS = 0.05; // bonus if employee applied

    public double score(Employee e, StaffingRequest r, boolean applied) {
        double skills = skillsScore(e.getSkills(), r.getRequiredSkills());
        double hours  = hoursScore(e.getRemainingHoursPerWeek(), r.getAvailabilityHoursPerWeek());
        double wage   = wageScore(e.getWagePerHour(), r.getWagePerHour());
        double exp    = experienceScore(e.getExperienceYears(), r.getExperienceYears());
        double loc    = locationScore(e.getPrimaryLocation(), pickRequestLocation(r));

        double total =
                (skills * W_SKILLS) +
                (hours  * W_HOURS) +
                (wage   * W_WAGE) +
                (exp    * W_EXP) +
                (loc    * W_LOC);

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

    private double hoursScore(Integer remaining, Integer required) {
        if (required == null || required <= 0) return 1.0;
        if (remaining == null || remaining <= 0) return 0.0;

        // If hard filter passed, remaining >= required, so score can reward extra capacity slightly
        // Remaining exactly required => ~0.8 ; extra => goes up toward 1.0
        double ratio = (double) remaining / (double) required;
        double score = 0.8 + Math.min(0.2, (ratio - 1.0) * 0.2); // cap bonus
        return clamp01(score);
    }

    private double wageScore(BigDecimal employeeWage, BigDecimal requestWage) {
        // If request wage is null, don't penalize
        if (requestWage == null || requestWage.compareTo(BigDecimal.ZERO) <= 0) return 0.7;
        if (employeeWage == null || employeeWage.compareTo(BigDecimal.ZERO) <= 0) return 0.4;

        // closeness: exact match => 1.0; farther => lower
        BigDecimal diff = employeeWage.subtract(requestWage).abs();
        BigDecimal ratio = diff.divide(requestWage, 6, RoundingMode.HALF_UP);

        double s = 1.0 - ratio.doubleValue(); // 0 diff => 1 ; 100% diff => 0
        return clamp01(s);
    }

    private double experienceScore(Integer empYears, Integer reqYears) {
        if (reqYears == null || reqYears <= 0) return 0.7;
        if (empYears == null || empYears <= 0) return 0.0;

        if (empYears < reqYears) return 0.0; // normally filtered out already
        int extra = empYears - reqYears;

        // req met => 0.8, then + up to 0.2 for extra experience
        double s = 0.8 + Math.min(0.2, extra * 0.02); // +0.02 per extra year up to +0.2
        return clamp01(s);
    }

    private double locationScore(String employeeLocation, String requestLocation) {
        if (isBlank(requestLocation)) return 0.7; // unknown => neutral
        if (isBlank(employeeLocation)) return 0.0;

        String e = employeeLocation.trim().toLowerCase();
        String r = requestLocation.trim().toLowerCase();

        if (r.equals("remote")) {
            return e.equals("remote") ? 1.0 : 0.6; // allow non-remote but lower
        }
        return e.equals(r) ? 1.0 : 0.0;
    }

    private String pickRequestLocation(StaffingRequest r) {
        // prefer workLocation, else projectLocation
        if (!isBlank(r.getWorkLocation())) return r.getWorkLocation();
        return r.getProjectLocation();
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
