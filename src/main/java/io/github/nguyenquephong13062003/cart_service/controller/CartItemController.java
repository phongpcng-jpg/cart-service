package io.github.nguyenquephong13062003.cart_service.controller;

import io.github.nguyenquephong13062003.cart_service.dto.request.UpdateCartItemRequest;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartItemResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartResponse;
import io.github.nguyenquephong13062003.cart_service.identity.CartOwner;
import io.github.nguyenquephong13062003.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Thao tác trên CartItem chỉ cần itemId; cart được suy ra từ item. */
@RestController
@RequestMapping("/api/v1/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartService cartService;

    /** Thông tin 1 cart item. */
    @GetMapping("/{itemId}")
    public CartItemResponse getCartItem(CartOwner owner, @PathVariable("itemId") Long itemId) {
        return cartService.getCartItem(owner, itemId);
    }

    @PutMapping("/{itemId}")
    public CartResponse updateItem(
            CartOwner owner,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(owner, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public CartResponse removeItem(CartOwner owner, @PathVariable("itemId") Long itemId) {
        return cartService.removeItem(owner, itemId);
    }
}
