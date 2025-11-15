package com.example.RecipeBook.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private Key getSigningKey(){
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername() , refreshExpiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), refreshExpiration);
    }

    private String generateToken(String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("type", expiration == jwtExpiration ? "access" : "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }

        catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        }

        catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        }

        catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        }

        catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        }

        catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;

    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Failed to extract expiration from token", e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
