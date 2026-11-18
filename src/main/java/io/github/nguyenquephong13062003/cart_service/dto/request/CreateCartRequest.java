package io.github.nguyenquephong13062003.cart_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Danh sách item ban đầu có thể rỗng/null -> giỏ được tạo với trạng thái EMPTY. */
public record CreateCartRequest(

        @Size(max = 100, message = "items must not exceed 100 entries")
        List<@Valid CartItemRequest> items) {
}
