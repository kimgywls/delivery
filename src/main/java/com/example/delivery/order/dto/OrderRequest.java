package com.example.delivery.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * 주문 생성 요청. 한 주문에 같은 가게의 메뉴를 여러 개 담을 수 있다.
 * 주문자·가게·가격·총액·상태는 요청으로 받지 않는다.
 */
public record OrderRequest(
        @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다.")
        List<@NotNull(message = "주문 상품은 비어 있을 수 없습니다.") @Valid OrderItemRequest> items,

        @NotBlank(message = "배송 주소는 필수입니다.")
        @Size(max = 500, message = "배송 주소는 500자 이하여야 합니다.")
        String deliveryAddress
) {

    // 같은 메뉴를 여러 항목으로 나눠 담지 않는다. 다른 검증에서 걸리는 경우는 여기서 중복 오류를 내지 않는다.
    @AssertTrue(message = "같은 메뉴를 중복해서 담을 수 없습니다.")
    public boolean isMenuIdsUnique() {
        if (items == null) {
            return true;
        }
        List<Long> menuIds = items.stream()
                .filter(Objects::nonNull)
                .map(OrderItemRequest::menuId)
                .filter(Objects::nonNull)
                .toList();
        return menuIds.size() == menuIds.stream().distinct().count();
    }
}
