package com.example.delivery.security;

import com.example.delivery.member.entity.Role;

/**
 * JWT에서 꺼낸 로그인 회원 정보. Authentication의 principal로 저장되며
 * Controller에서 @AuthenticationPrincipal로 받아 Service에 전달한다.
 */
public record AuthMember(Long id, String username, Role role) {
}
