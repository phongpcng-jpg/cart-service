package io.github.nguyenquephong13062003.cart_service.mapper;

import io.github.nguyenquephong13062003.cart_service.client.ProductInfo;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartItemResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartItemResponse.ProductSummary;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartSummaryResponse;
import io.github.nguyenquephong13062003.cart_service.entity.Cart;
import io.github.nguyenquephong13062003.cart_service.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Mapper thủ công Entity -> Response DTO (không phụ thuộc MapStruct). */
@Component
public class CartMapper {

    /** Giỏ hàng đầy đủ, gồm danh sách item. */
    public CartResponse toResponse(Cart cart, Map<Long, ProductInfo> products) {
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getGuestSessionId(),
                cart.getStatus(),
                cart.getSubtotal(),
                totalQuantity(cart),
                toItemResponses(cart, products),
                cart.getCreatedAt(),
                cart.getUpdatedAt());
    }

    /** Giỏ hàng không kèm item. */
    public CartSummaryResponse toSummary(Cart cart) {
        return new CartSummaryResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getGuestSessionId(),
                cart.getStatus(),
                cart.getSubtotal(),
                cart.getItems().size(),
                totalQuantity(cart),
                cart.getCreatedAt(),
                cart.getUpdatedAt());
    }

    public List<CartItemResponse> toItemResponses(Cart cart, Map<Long, ProductInfo> products) {
        return cart.getItems().stream()
                .map(item -> toItemResponse(item, products.get(item.getProductId())))
                .toList();
    }

    public CartItemResponse toItemResponse(CartItem item, ProductInfo product) {
        ProductSummary summary = product == null
                ? null
                : new ProductSummary(product.name(), product.imageUrl(), product.price());
        return new CartItemResponse(
                item.getId(),
                item.getCart().getId(),
                item.getProductId(),
                item.getSkuId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal(),
                summary);
    }

    private int totalQuantity(Cart cart) {
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }
}
