package com.frauas.workforce_planning.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private final Key key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(
            Long userId,
            String email,
            String selectedRole,
            List<String> roles,
            String employeeHrId,
            Long employeeDbId // nullable for external
    ) {
        long now = System.currentTimeMillis();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("selectedRole", selectedRole);
        claims.put("roles", roles);
        claims.put("employeeHrId", employeeHrId);

        if (employeeDbId != null) {
            claims.put("employeeDbId", employeeDbId);
        }

        return Jwts.builder()
                .setSubject(email) // OK to keep email as subject
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // parses + checks signature + exp automatically
            return true;
        } catch (Exception e) {
            return false;
        }
    }
        
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object v = parseClaims(token).get("userId");
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Long l) return l;
        return Long.valueOf(v.toString());
    }
    
    public String extractSelectedRole(String token) {
        Object v = parseClaims(token).get("selectedRole");
        return v != null ? v.toString() : null;
    }

    public String extractEmployeeHrId(String token) {
        Object v = parseClaims(token).get("employeeHrId");
        return v != null ? v.toString() : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }
}
