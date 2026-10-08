package com.example.delivery.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증되지 않은 요청을 401 ProblemDetail로 응답한다.
 * 필터 단계의 예외는 @RestControllerAdvice까지 전달되지 않으므로 여기서 직접 응답을 작성한다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 토큰 없이 보호 API에 접근하면 InsufficientAuthenticationException이 전달된다.
        String detail = authException instanceof InsufficientAuthenticationException
                ? "인증이 필요합니다."
                : authException.getMessage();

        SecurityProblemDetailWriter.write(request, response, HttpStatus.UNAUTHORIZED, detail);
    }
}
