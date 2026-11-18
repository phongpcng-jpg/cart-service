package io.github.nguyenquephong13062003.cart_service.dto.request;

import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCartStatusRequest(

        @NotNull(message = "status is required")
        CartStatus status) {
}
