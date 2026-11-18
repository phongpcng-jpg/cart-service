package io.github.nguyenquephong13062003.cart_service.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long itemId,
        Long cartId,
        Long productId,
        String skuId,
        int quantity,
        /** Đơn giá đang lưu trong giỏ (lần thêm/cộng dồn gần nhất). */
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        /** Thông tin hiển thị lấy từ product-service; null nếu không lấy được. */
        ProductSummary product) {

    public record ProductSummary(String name, String imageUrl, BigDecimal currentPrice) {
    }
}
