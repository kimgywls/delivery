package com.example.delivery.member.dto;

import com.example.delivery.member.entity.Member;
import com.example.delivery.member.entity.Role;

public record MemberResponse(Long id, String username, Role role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getUsername(), member.getRole());
    }
}
