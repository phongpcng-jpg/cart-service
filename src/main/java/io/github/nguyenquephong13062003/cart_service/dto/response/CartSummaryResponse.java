package io.github.nguyenquephong13062003.cart_service.dto.response;

import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** Thông tin giỏ hàng, không kèm danh sách CartItem. */
public record CartSummaryResponse(
        Long cartId,
        String userId,
        String guestSessionId,
        CartStatus status,
        BigDecimal subtotal,
        int itemCount,
        int totalQuantity,
        Instant createdAt,
        Instant updatedAt) {
}
