package com.healthdekho.security;

import com.healthdekho.entity.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(10);

    private final SecretKey signingKey;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${healthdekho.security.jwt.secret}") String encodedSecret,
            @Value("${healthdekho.security.jwt.issuer}") String issuer,
            @Value("${healthdekho.security.jwt.audience}") String audience) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(encodedSecret);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT signing secret must be base64-encoded", exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT signing secret must contain at least 256 bits");
        }
        if (issuer.isBlank() || audience.isBlank()) {
            throw new IllegalStateException("JWT issuer and audience must be configured");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.audience = audience;
    }

    public String issueAccessToken(Customer customer) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(customer.getId().toString())
                .claim("email", customer.getNormalizedEmail())
                .claim("role", "CUSTOMER")
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(ACCESS_TOKEN_LIFETIME)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public CustomerPrincipal validateAndLoadPrincipal(String token, CustomUserDetailsService userDetailsService) {
        Jws<Claims> signedClaims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(signedClaims.getHeader().getAlgorithm())) {
            throw new JwtException("Unsupported JWT algorithm");
        }
        Claims claims = signedClaims.getPayload();
        if (claims.getExpiration() == null) {
            throw new JwtException("JWT expiration is required");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(audience)) {
            throw new JwtException("Invalid JWT audience");
        }
        if (!"CUSTOMER".equals(claims.get("role", String.class))) {
            throw new JwtException("Invalid JWT role");
        }
        UUID subject;
        try {
            subject = UUID.fromString(claims.getSubject());
        } catch (RuntimeException exception) {
            throw new JwtException("Invalid JWT subject", exception);
        }
        CustomerPrincipal principal = userDetailsService.loadPrincipalById(subject);
        if (!principal.isEnabled() || !principal.getUsername().equals(claims.get("email", String.class))) {
            throw new JwtException("Customer is not permitted to use this token");
        }
        return principal;
    }

    public long getAccessTokenLifetimeSeconds() {
        return ACCESS_TOKEN_LIFETIME.toSeconds();
    }
}
