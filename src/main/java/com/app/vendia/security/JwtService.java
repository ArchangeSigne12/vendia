package com.app.vendia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;


@Service
public class JwtService {

    private final SecretKey cle;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${vendia.jwt.secret}") String secret,
            @Value("${vendia.jwt.access-token-expiration-ms}") long accessExpirationMs,
            @Value("${vendia.jwt.refresh-token-expiration-ms}") long refreshExpirationMs
    ) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String genererAccessToken(UserDetails user) {
        return construire(user, accessExpirationMs, "ACCESS");
    }

    public String genererRefreshToken(UserDetails user) {
        return construire(user, refreshExpirationMs, "REFRESH");
    }

    private String construire(UserDetails user, long expirationMs, String type) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", type)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cle)
                .compact();
    }

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public boolean estValide(String token, UserDetails user) {
        String email = extraireEmail(token);
        return email.equals(user.getUsername()) && !estExpire(token);
    }

    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(cle).build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
