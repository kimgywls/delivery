package com.example.delivery.security;

import com.example.delivery.member.entity.Member;
import com.example.delivery.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String USERNAME_CLAIM = "username";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long expirationMs;

    public JwtProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        // Base64로 디코딩한 키가 256비트 미만이면 WeakKeyException으로 애플리케이션 시작이 실패한다.
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String createToken(Member member) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim(USERNAME_CLAIM, member.getUsername())
                .claim(ROLE_CLAIM, member.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 서명과 만료 시간을 검증하고 회원 정보를 꺼낸다.
     * 만료되면 ExpiredJwtException, 서명·형식이 잘못되면 JwtException을 던진다.
     */
    public AuthMember parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthMember(
                Long.valueOf(claims.getSubject()),
                claims.get(USERNAME_CLAIM, String.class),
                Role.valueOf(claims.get(ROLE_CLAIM, String.class)));
    }
}
