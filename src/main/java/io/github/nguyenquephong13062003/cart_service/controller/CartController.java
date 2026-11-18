package io.github.nguyenquephong13062003.cart_service.controller;

import io.github.nguyenquephong13062003.cart_service.dto.request.CartItemRequest;
import io.github.nguyenquephong13062003.cart_service.dto.request.CreateCartRequest;
import io.github.nguyenquephong13062003.cart_service.dto.request.UpdateCartStatusRequest;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartItemResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartSummaryResponse;
import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import io.github.nguyenquephong13062003.cart_service.identity.CartOwner;
import io.github.nguyenquephong13062003.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Danh tính (X-User-Id / X-Guest-Session-Id) được resolve thành {@link CartOwner} ở mọi endpoint. */
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ---------------------------------------------------------------- queries

    /** Danh sách giỏ của người gọi (không kèm items). */
    @GetMapping
    public List<CartSummaryResponse> listCarts(
            CartOwner owner,
            @RequestParam(name = "status", required = false) CartStatus status) {
        return cartService.listCarts(owner, status);
    }

    /** Thông tin giỏ hàng (không kèm items). */
    @GetMapping("/{cartId}")
    public CartSummaryResponse getCart(CartOwner owner, @PathVariable("cartId") Long cartId) {
        return cartService.getCartSummary(owner, cartId);
    }

    /** Thông tin giỏ hàng đầy đủ, kèm danh sách items. */
    @GetMapping("/{cartId}/detail")
    public CartResponse getCartDetail(CartOwner owner, @PathVariable("cartId") Long cartId) {
        return cartService.getCartDetail(owner, cartId);
    }

    /** Danh sách items của giỏ. */
    @GetMapping("/{cartId}/items")
    public List<CartItemResponse> listCartItems(CartOwner owner, @PathVariable("cartId") Long cartId) {
        return cartService.listCartItems(owner, cartId);
    }

    // ---------------------------------------------------------------- commands

    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            CartOwner owner,
            @Valid @RequestBody(required = false) CreateCartRequest request) {
        CartResponse response = cartService.createCart(owner, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{cartId}").buildAndExpand(response.cartId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{cartId}/status")
    public CartResponse updateStatus(
            CartOwner owner,
            @PathVariable("cartId") Long cartId,
            @Valid @RequestBody UpdateCartStatusRequest request) {
        return cartService.updateStatus(owner, cartId, request.status());
    }

    @PostMapping("/{cartId}/reactivate")
    public CartResponse reactivate(CartOwner owner, @PathVariable("cartId") Long cartId) {
        return cartService.reactivateCart(owner, cartId);
    }

    @PostMapping("/{cartId}/items")
    public CartResponse addItem(
            CartOwner owner,
            @PathVariable("cartId") Long cartId,
            @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(owner, cartId, request);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(CartOwner owner, @PathVariable("cartId") Long cartId) {
        cartService.deleteCart(owner, cartId);
        return ResponseEntity.noContent().build();
    }
}
