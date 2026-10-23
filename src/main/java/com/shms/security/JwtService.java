package com.shms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(
            @Value("${shms.jwt.secret:SuperSecretDemonstrationKeyForCollegeProjectSmartHealthMonitoringSystem2026!}") String secret,
            @Value("${shms.jwt.expiration-ms:86400000}") long expirationMillis // 24 hours
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        if (user.getPatientId() != null) {
            claims.put("patientId", user.getPatientId());
        }
        if (user.getDoctorId() != null) {
            claims.put("doctorId", user.getDoctorId());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public Long extractPatientId(String token) {
        Number pid = extractClaims(token).get("patientId", Number.class);
        return pid != null ? pid.longValue() : null;
    }

    public Long extractDoctorId(String token) {
        Number did = extractClaims(token).get("doctorId", Number.class);
        return did != null ? did.longValue() : null;
    }

    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = extractClaims(token);
            boolean notExpired = claims.getExpiration().after(new Date());
            return notExpired && claims.getSubject().equals(username);
        } catch (Exception e) {
            return false;
        }
    }
}
