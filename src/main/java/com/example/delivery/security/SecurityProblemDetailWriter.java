package com.example.delivery.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

/**
 * Security 필터 단계의 오류를 MVC의 @RestControllerAdvice와 같은 ProblemDetail 형식으로 응답한다.
 * 필터에서 발생한 오류는 DispatcherServlet에 도달하지 않으므로 응답을 직접 작성한다.
 */
final class SecurityProblemDetailWriter {

    // Spring MVC와 같은 방식으로 ProblemDetail을 직렬화한다.
    private static final JacksonJsonHttpMessageConverter CONVERTER = new JacksonJsonHttpMessageConverter();

    private SecurityProblemDetailWriter() {
    }

    static void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String detail) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        try {
            problem.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException e) {
            // 방화벽이 거절한 비정상 경로처럼 URI로 해석할 수 없으면 instance를 생략한다.
        }

        response.setStatus(status.value());
        CONVERTER.write(problem, MediaType.APPLICATION_PROBLEM_JSON, new ServletServerHttpResponse(response));
    }
}
