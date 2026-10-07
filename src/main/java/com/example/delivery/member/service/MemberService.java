package com.example.delivery.member.service;

import com.example.delivery.common.exception.DuplicateUsernameException;
import com.example.delivery.common.exception.InvalidPasswordException;
import com.example.delivery.member.dto.MemberResponse;
import com.example.delivery.member.dto.SignupRequest;
import com.example.delivery.member.entity.Member;
import com.example.delivery.member.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    // BCrypt는 72바이트까지만 사용하며, 초과하면 encode()에서 예외가 발생한다.
    private static final int MAX_PASSWORD_BYTES = 72;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new InvalidPasswordException("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        if (memberRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException();
        }

        Member member = new Member(request.username(), passwordEncoder.encode(request.password()), request.role());
        return MemberResponse.from(memberRepository.save(member));
    }
}
