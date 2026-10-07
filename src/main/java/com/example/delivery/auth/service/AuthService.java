package com.example.delivery.auth.service;

import com.example.delivery.auth.dto.LoginRequest;
import com.example.delivery.auth.dto.LoginResponse;
import com.example.delivery.member.entity.Member;
import com.example.delivery.member.repository.MemberRepository;
import com.example.delivery.security.JwtProvider;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // BCrypt matches()는 72바이트를 넘는 입력을 앞 72바이트만 비교하므로 먼저 거절한다.
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final String LOGIN_FAILED_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BadCredentialsException(LOGIN_FAILED_MESSAGE);
        }

        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException(LOGIN_FAILED_MESSAGE));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException(LOGIN_FAILED_MESSAGE);
        }

        return new LoginResponse(jwtProvider.createToken(member));
    }
}
