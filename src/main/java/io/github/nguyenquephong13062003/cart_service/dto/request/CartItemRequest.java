package io.github.nguyenquephong13062003.cart_service.dto.request;

import io.github.nguyenquephong13062003.cart_service.entity.CartItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Dùng cho cả "tạo giỏ hàng" (danh sách ban đầu) và "thêm CartItem". */
public record CartItemRequest(

        @NotNull(message = "productId is required")
        @Positive(message = "productId must be positive")
        Long productId,

        @NotBlank(message = "skuId is required")
        @Size(max = 100, message = "skuId must not exceed 100 characters")
        String skuId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than 0")
        @Max(value = CartItem.MAX_QUANTITY, message = "quantity must not exceed " + CartItem.MAX_QUANTITY)
        Integer quantity,

        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.00", message = "unitPrice must not be negative")
        @Digits(integer = 17, fraction = 2, message = "unitPrice must have at most 2 decimal places")
        BigDecimal unitPrice) {

    public CartItemRequest {
        skuId = skuId == null ? null : skuId.trim();
    }
}
