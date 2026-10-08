package com.example.delivery.menu.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 메뉴 목록 페이징 응답. Page 객체를 그대로 응답하지 않고 필요한 값만 담는다.
 */
public record MenuPageResponse(
        List<MenuResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static MenuPageResponse from(Page<MenuResponse> menus) {
        return new MenuPageResponse(
                menus.getContent(),
                menus.getNumber(),
                menus.getSize(),
                menus.getTotalElements(),
                menus.getTotalPages());
    }
}
