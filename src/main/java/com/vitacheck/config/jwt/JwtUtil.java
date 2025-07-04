package com.vitacheck.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long accessTokenExpTime;
    private final long refreshTokenExpTime;
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration-minutes}") long accessTokenExpTime,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpTime = accessTokenExpTime * 60 * 1000; // 분 -> 밀리초
        this.refreshTokenExpTime = refreshTokenExpTime * 24 * 60 * 60 * 1000; // 일 -> 밀리초
    }

    public String createAccessToken(String email) {
        return createToken(email, accessTokenExpTime);
    }

    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenExpTime);
    }

    private String createToken(String email, long expirationTime) {
        Claims claims = Jwts.claims();
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("email", String.class);
    }

    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 시도
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // MalformedJwtException: JWT 형식이 잘못되었을 때
            // SecurityException: 서명이 올바르지 않거나(위조), JWT 구조에 문제가 있을 때
            log.warn("잘못된 JWT 서명입니다. (Invalid JWT signature)");
        } catch (ExpiredJwtException e) {
            // 토큰의 유효기간이 만료되었을 때
            // 이 로그는 매우 흔하게 발생하므로 ERROR나 WARN 대신 INFO 레벨로 기록하는 것이 좋습니다.
            log.info("만료된 JWT 토큰입니다. (Expired JWT token)");
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 형식의 JWT일 때
            log.warn("지원되지 않는 JWT 토큰입니다. (Unsupported JWT token)");
        } catch (IllegalArgumentException e) {
            // JWT 클레임 문자열이 비어있거나, 토큰 값이 null일 때
            log.warn("JWT 토큰이 잘못되었습니다. (JWT token is malformed or empty)");
        }
        // 위 예외 중 하나라도 발생하면 유효하지 않은 토큰이므로 false 반환
        return false;
    }
}
