package io.github.nguyenquephong13062003.cart_service.dto.response;

import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponse(
        Long cartId,
        String userId,
        String guestSessionId,
        CartStatus status,
        BigDecimal subtotal,
        int totalQuantity,
        List<CartItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {
}
