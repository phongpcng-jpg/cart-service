package io.github.nguyenquephong13062003.cart_service.dto.request;

import io.github.nguyenquephong13062003.cart_service.entity.CartItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Không cho phép đổi productId. */
public record UpdateCartItemRequest(

        @NotBlank(message = "skuId is required")
        @Size(max = 100, message = "skuId must not exceed 100 characters")
        String skuId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than 0")
        @Max(value = CartItem.MAX_QUANTITY, message = "quantity must not exceed " + CartItem.MAX_QUANTITY)
        Integer quantity) {

    public UpdateCartItemRequest {
        skuId = skuId == null ? null : skuId.trim();
    }
}
