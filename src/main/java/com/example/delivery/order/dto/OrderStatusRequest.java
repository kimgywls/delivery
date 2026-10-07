package com.example.delivery.order.dto;

import com.example.delivery.order.entity.OrderStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * 사장님의 주문 상태 변경 요청. ACCEPTED와 DELIVERED만 입력할 수 있다.
 */
public record OrderStatusRequest(
        @NotNull(message = "변경할 상태는 필수입니다.")
        OrderStatus status
) {

    // 다른 상태값은 현재 주문 상태와 관계없이 잘못된 입력이므로 400으로 거절한다.
    @AssertTrue(message = "상태는 ACCEPTED 또는 DELIVERED만 입력할 수 있습니다.")
    public boolean isChangeableStatus() {
        return status == null || status == OrderStatus.ACCEPTED || status == OrderStatus.DELIVERED;
    }
}
