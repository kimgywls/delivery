package com.example.delivery.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 인증은 되었지만 역할이 맞지 않는 요청을 403 ProblemDetail로 응답한다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        SecurityProblemDetailWriter.write(request, response, HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
}
