package com.example.delivery.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization: Bearer {토큰} 헤더를 검증해 SecurityContext에 인증 정보를 저장한다.
 * Bean으로 등록하면 서블릿 필터로도 자동 등록되어 두 번 실행되므로 SecurityConfig에서 직접 생성한다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final RequestMatcher publicRequestMatcher;

    // 공개 API는 토큰이 있어도 검사하지 않는다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return publicRequestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토큰이 없으면 인증 없이 진행하고, 보호 API라면 이후 AuthorizationFilter가 401로 거절한다.
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthMember authMember;
        try {
            authMember = jwtProvider.parseToken(header.substring(BEARER_PREFIX.length()));
        } catch (ExpiredJwtException e) {
            authenticationEntryPoint.commence(request, response, new CredentialsExpiredException("만료된 토큰입니다."));
            return;
        } catch (JwtException | IllegalArgumentException e) {
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("유효하지 않은 토큰입니다."));
            return;
        }

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                authMember, null, List.of(new SimpleGrantedAuthority("ROLE_" + authMember.role().name())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }
}
