package com.example.gateway.jwt;


import com.example.gateway.jwt.exception.TokenValidException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
@Component
public class JwtValidator {
    private static final String TOKEN_TYPE = "Bearer ";
    private final Key secretKey;

    public JwtValidator(
        @Value("${jwt.secret.key}") final String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public void validateToken(final String token) {
        try {
            getClaimsJws(token);
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("유효하지 않은 JWT 서명 입니다.");
            throw new TokenValidException("유효하지 않은 JWT 서명 입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 입니다.");
            throw new TokenValidException("만료된 JWT 입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 입니다.");
            throw new TokenValidException("지원되지 않는 JWT 입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 입니다.");
            throw new TokenValidException("유효하지 않은 JWT 입니다.", e);
        } catch (RuntimeException e){
            log.error("알 수 없는 오류가 발생했습니다.");
            throw new TokenValidException("알 수 없는 오류가 발생했습니다.", e);
        }
    }

    public Long getId(final String token) {
        String idValue = getClaimsJws(token)
            .getBody()
            .getSubject();
        return Long.parseLong(idValue);
    }

    private Jws<Claims> getClaimsJws(final String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token);
    }

    public String extractToken(final String token) {
        return token.substring(TOKEN_TYPE.length());
    }

    public String getToken(final ServerHttpRequest request) {
        return request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
    }
}
