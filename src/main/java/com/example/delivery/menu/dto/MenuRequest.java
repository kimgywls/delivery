package com.example.delivery.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 메뉴 등록·수정 요청. 수정(PUT)은 전체 교체이므로 description이 없으면 null로 저장된다.
 */
public record MenuRequest(
        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 1원 이상이어야 합니다.")
        Long price,

        String description
) {
}
