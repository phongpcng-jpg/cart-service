package io.github.nguyenquephong13062003.cart_service.client;

import java.math.BigDecimal;

/** Thông tin sản phẩm do product-service cung cấp (chỉ dùng để hiển thị, không lưu vào DB của cart-service). */
public record ProductInfo(Long productId, String name, String imageUrl, BigDecimal price) {
}
