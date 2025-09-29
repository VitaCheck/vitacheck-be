package com.vitacheck.auth.config.jwt;

import com.vitacheck.auth.dto.AuthDto;
import com.vitacheck.auth.dto.AuthUserDto;
import com.vitacheck.auth.dto.OAuthAttributes;
import com.vitacheck.common.enums.Gender;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDate;
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

    public String createAccessToken(AuthUserDto user) {
        Claims claims = Jwts.claims();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("gender", user.getGender());
        claims.put("birthDate", user.getBirthDate().toString());

        return createToken(claims, accessTokenExpTime);
    }

    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims();
        claims.put("email", email);
        return createToken(claims, refreshTokenExpTime);
    }

    private String createToken(Claims claims, long expirationTime) {
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
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다. (Invalid JWT signature)");
        } catch (DecodingException e) { // 👈 [2/2] 이 catch 문을 한 줄 추가하세요!
            log.warn("잘못된 Base64 인코딩 토큰입니다. (Invalid Base64 token)");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다. (Expired JWT token)");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다. (Unsupported JWT token)");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다. (JWT token is malformed or empty)");
        }
        return false;
    }

    public String createTempSocialToken(OAuthAttributes attributes) {
        Claims claims = Jwts.claims();
        claims.put("email", attributes.getEmail());
        claims.put("name", attributes.getName());
        claims.put("provider", attributes.getProvider());
        claims.put("providerId", attributes.getProviderId());

        // LocalDate, Gender 등 복잡한 객체는 문자열로 변환하여 저장
        if (attributes.getBirthDate() != null) {
            claims.put("birthDate", attributes.getBirthDate().toString());
        }
        if (attributes.getGender() != null) {
            claims.put("gender", attributes.getGender().name());
        }
        if (attributes.getPhoneNumber() != null) {
            claims.put("phoneNumber", attributes.getPhoneNumber());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) // ✅ 10분 유효
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createPreSignupToken(AuthDto.PreSignUpRequest request) {
        Claims claims = Jwts.claims();
        claims.put("email", request.getEmail());
        claims.put("password", request.getPassword());
        claims.put("nickname", request.getNickname());
        claims.put("agreedTermIds", request.getAgreedTermIds());
        claims.put("type", "PRE_SIGNUP"); // 토큰 종류 명시

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 10 * 60 * 1000)) // 10분 유효
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaimsFromPreSignupToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        if (!"PRE_SIGNUP".equals(claims.get("type", String.class))) {
            throw new SecurityException("Invalid token type for signup");
        }
        return claims;
    }


    public OAuthAttributes getSocialAttributesFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        return OAuthAttributes.builder()
                .email(claims.get("email", String.class))
                .name(claims.get("name", String.class))
                .provider(claims.get("provider", String.class))
                .providerId(claims.get("providerId", String.class))
                .birthDate(claims.containsKey("birthDate") ? LocalDate.parse(claims.get("birthDate", String.class)) : null)
                .gender(claims.containsKey("gender") ? Gender.valueOf(claims.get("gender", String.class)) : null)
                .phoneNumber(claims.get("phoneNumber", String.class))
                .build();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}