package io.github.nguyenquephong13062003.cart_service.service;

import io.github.nguyenquephong13062003.cart_service.dto.request.CartItemRequest;
import io.github.nguyenquephong13062003.cart_service.dto.request.CreateCartRequest;
import io.github.nguyenquephong13062003.cart_service.dto.request.UpdateCartItemRequest;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartItemResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartSummaryResponse;
import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import io.github.nguyenquephong13062003.cart_service.identity.CartOwner;

import java.util.List;

public interface CartService {

    // ---- truy vấn
    /** Thông tin giỏ hàng, không kèm items. */
    CartSummaryResponse getCartSummary(CartOwner owner, Long cartId);

    /** Thông tin giỏ hàng đầy đủ, kèm danh sách items. */
    CartResponse getCartDetail(CartOwner owner, Long cartId);

    /** Danh sách các giỏ của người gọi (không kèm items), có thể lọc theo status. */
    List<CartSummaryResponse> listCarts(CartOwner owner, CartStatus status);

    List<CartItemResponse> listCartItems(CartOwner owner, Long cartId);

    CartItemResponse getCartItem(CartOwner owner, Long itemId);

    // ---- thay đổi
    CartResponse createCart(CartOwner owner, CreateCartRequest request);

    CartResponse updateStatus(CartOwner owner, Long cartId, CartStatus targetStatus);

    CartResponse reactivateCart(CartOwner owner, Long cartId);

    CartResponse addItem(CartOwner owner, Long cartId, CartItemRequest request);

    CartResponse updateItem(CartOwner owner, Long itemId, UpdateCartItemRequest request);

    CartResponse removeItem(CartOwner owner, Long itemId);

    void deleteCart(CartOwner owner, Long cartId);
}
