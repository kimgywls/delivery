package com.example.delivery.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security 방화벽(StrictHttpFirewall)이 거절한 요청(예: //, ;, 인코딩된 경로)을 400 ProblemDetail로 응답한다.
 * 기본 처리기는 sendError(400)를 호출해 /error로 다시 전달되므로, 응답을 직접 작성해 원래 상태 코드를 유지한다.
 * Bean으로 등록하면 WebSecurity가 이 처리기를 사용한다.
 */
@Component
public class ProblemDetailRequestRejectedHandler implements RequestRejectedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       RequestRejectedException requestRejectedException) throws IOException {
        SecurityProblemDetailWriter.write(request, response, HttpStatus.BAD_REQUEST, "요청 경로 형식이 올바르지 않습니다.");
    }
}
