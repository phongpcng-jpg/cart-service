package io.github.nguyenquephong13062003.cart_service.service;

import io.github.nguyenquephong13062003.cart_service.client.ProductInfo;
import io.github.nguyenquephong13062003.cart_service.client.ProductServiceClient;
import io.github.nguyenquephong13062003.cart_service.dto.request.CartItemRequest;
import io.github.nguyenquephong13062003.cart_service.dto.request.CreateCartRequest;
import io.github.nguyenquephong13062003.cart_service.dto.request.UpdateCartItemRequest;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartItemResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartResponse;
import io.github.nguyenquephong13062003.cart_service.dto.response.CartSummaryResponse;
import io.github.nguyenquephong13062003.cart_service.entity.Cart;
import io.github.nguyenquephong13062003.cart_service.entity.CartItem;
import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import io.github.nguyenquephong13062003.cart_service.exception.BusinessException;
import io.github.nguyenquephong13062003.cart_service.exception.ErrorCode;
import io.github.nguyenquephong13062003.cart_service.identity.CartOwner;
import io.github.nguyenquephong13062003.cart_service.mapper.CartMapper;
import io.github.nguyenquephong13062003.cart_service.storage.CartStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Toàn bộ nghiệp vụ giỏ hàng. Chỉ phụ thuộc {@link CartStoragePort} và {@link ProductServiceClient}
 * (interface), nên đổi PostgreSQL -> Redis hay mock -> product-service thật không cần sửa class này.
 *
 * Lưu ý: việc làm giàu response bằng ProductServiceClient hiện nằm trong transaction (mock nên không
 * ảnh hưởng). Khi thay bằng client HTTP thật, nên tách phần gọi client ra khỏi transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartStoragePort storage;
    private final ProductServiceClient productClient;
    private final CartMapper mapper;

    // ================================================================== queries

    @Override
    @Transactional(readOnly = true)
    public CartSummaryResponse getCartSummary(CartOwner owner, Long cartId) {
        return mapper.toSummary(loadOwnedCart(owner, cartId));
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartDetail(CartOwner owner, Long cartId) {
        return toResponse(loadOwnedCart(owner, cartId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartSummaryResponse> listCarts(CartOwner owner, CartStatus status) {
        return storage.findByOwner(owner, status).stream().map(mapper::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> listCartItems(CartOwner owner, Long cartId) {
        Cart cart = loadOwnedCart(owner, cartId);
        return mapper.toItemResponses(cart, fetchProducts(productIdsOf(List.of(cart))));
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItem(CartOwner owner, Long itemId) {
        Cart cart = loadOwnedCartByItem(owner, itemId);
        CartItem item = findItemOrThrow(cart, itemId);
        ProductInfo product = fetchProducts(Set.of(item.getProductId())).get(item.getProductId());
        return mapper.toItemResponse(item, product);
    }

    // ================================================================== create

    @Override
    public CartResponse createCart(CartOwner owner, CreateCartRequest request) {
        Cart cart = Cart.create(owner.userId(), owner.guestSessionId());
        if (request != null && request.items() != null) {
            rejectDuplicateItems(request.items());
            request.items().forEach(item -> cart.addItem(item.productId(), item.skuId(), item.quantity(),
                    item.unitPrice().setScale(2, RoundingMode.HALF_UP)));
        }
        cart.syncStatusWithItems();   // không có item -> EMPTY, ngược lại -> ACTIVE
        cart.recalculate();
        return toResponse(storage.save(cart));
    }

    // ================================================================== status

    @Override
    public CartResponse updateStatus(CartOwner owner, Long cartId, CartStatus targetStatus) {
        Cart cart = loadOwnedCart(owner, cartId);
        CartStatus current = cart.getStatus();
        if (!current.canTransitionTo(targetStatus)) {
            String hint = current == CartStatus.CANCELLED
                    ? " (use the reactivate endpoint to reopen a cancelled cart)" : "";
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cart %d cannot change status from %s to %s%s"
                            .formatted(cartId, current, targetStatus, hint));
        }
        cart.changeStatus(targetStatus);
        return toResponse(storage.save(cart));
    }

    @Override
    public CartResponse reactivateCart(CartOwner owner, Long cartId) {
        Cart cart = loadOwnedCart(owner, cartId);
        if (cart.getStatus() != CartStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only CANCELLED carts can be reactivated, cart %d is %s".formatted(cartId, cart.getStatus()));
        }
        cart.syncStatusWithItems();   // không có CartItem -> EMPTY, có -> ACTIVE
        cart.recalculate();
        return toResponse(storage.save(cart));
    }

    // ================================================================== items

    @Override
    public CartResponse addItem(CartOwner owner, Long cartId, CartItemRequest request) {
        Cart cart = loadOwnedCart(owner, cartId);
        if (!cart.getStatus().acceptsNewItems()) {
            throw new BusinessException(ErrorCode.CART_NOT_MODIFIABLE,
                    "Items can only be added to EMPTY or ACTIVE carts, cart %d is %s"
                            .formatted(cartId, cart.getStatus()));
        }
        upsertItem(cart, request);
        cart.syncStatusWithItems();   // giỏ EMPTY có item đầu tiên -> ACTIVE
        cart.recalculate();
        return toResponse(storage.save(cart));
    }

    @Override
    public CartResponse updateItem(CartOwner owner, Long itemId, UpdateCartItemRequest request) {
        Cart cart = loadOwnedCartByItem(owner, itemId);
        requireActive(cart, "updated");
        CartItem item = findItemOrThrow(cart, itemId);

        boolean skuChanged = !item.getSkuId().equals(request.skuId());
        boolean quantityChanged = item.getQuantity() != request.quantity();
        if (!skuChanged && !quantityChanged) {
            return toResponse(cart);  // không có thay đổi -> không đụng tới tổng tiền / updated_at
        }

        if (skuChanged) {
            Optional<CartItem> target = cart.findItem(item.getProductId(), request.skuId());
            if (target.isPresent()) {
                // sku mới đã có trong giỏ -> cộng dồn quantity vào dòng đó (giữ đơn giá của dòng đó) và xóa dòng hiện tại
                addQuantity(target.get(), request.quantity());
                cart.removeItem(item);
            } else {
                item.changeSku(request.skuId());
                item.changeQuantity(request.quantity());
            }
        } else {
            item.changeQuantity(request.quantity());
        }

        cart.recalculate();
        return toResponse(storage.save(cart));
    }

    @Override
    public CartResponse removeItem(CartOwner owner, Long itemId) {
        Cart cart = loadOwnedCartByItem(owner, itemId);
        requireActive(cart, "removed");
        CartItem item = findItemOrThrow(cart, itemId);

        cart.removeItem(item);
        cart.syncStatusWithItems();   // hết item -> EMPTY
        cart.recalculate();
        return toResponse(storage.save(cart));
    }

    // ================================================================== delete

    @Override
    public void deleteCart(CartOwner owner, Long cartId) {
        Cart cart = loadOwnedCart(owner, cartId);
        if (cart.getStatus() == CartStatus.PAID) {
            throw new BusinessException(ErrorCode.CART_NOT_MODIFIABLE,
                    "Cart %d is PAID and cannot be deleted".formatted(cartId));
        }
        storage.delete(cart);  // xóa cứng cart + toàn bộ CartItem
    }

    // ================================================================== helpers

    /** Danh sách item ban đầu khi tạo giỏ không được chứa cặp (productId, skuId) trùng nhau. */
    private void rejectDuplicateItems(List<CartItemRequest> items) {
        Set<List<Object>> seen = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            CartItemRequest item = items.get(i);
            if (!seen.add(List.of(item.productId(), item.skuId()))) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                        "items[%d]: duplicate productId %d with skuId '%s' in the initial item list"
                                .formatted(i, item.productId(), item.skuId()));
            }
        }
    }

    /**
     * Thêm item mới, hoặc nếu cặp (productId, skuId) đã có thì cộng dồn quantity
     * và cập nhật đơn giá theo giá mới nhất trong request.
     */
    private void upsertItem(Cart cart, CartItemRequest request) {
        BigDecimal price = request.unitPrice().setScale(2, RoundingMode.HALF_UP);
        Optional<CartItem> existing = cart.findItem(request.productId(), request.skuId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            addQuantity(item, request.quantity());
            item.changeUnitPrice(price);
        } else {
            cart.addItem(request.productId(), request.skuId(), request.quantity(), price);
        }
    }

    /** Cộng thêm quantity vào item, chặn vượt {@link CartItem#MAX_QUANTITY}. */
    private void addQuantity(CartItem item, int quantityToAdd) {
        long merged = (long) item.getQuantity() + quantityToAdd;
        if (merged > CartItem.MAX_QUANTITY) {
            throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED,
                    "Quantity of product %d / sku '%s' would be %d, maximum is %d"
                            .formatted(item.getProductId(), item.getSkuId(), merged, CartItem.MAX_QUANTITY));
        }
        item.changeQuantity((int) merged);
    }

    private Cart loadOwnedCart(CartOwner owner, Long cartId) {
        Cart cart = storage.findById(cartId).orElseThrow(() ->
                new BusinessException(ErrorCode.CART_NOT_FOUND, "Cart %d not found".formatted(cartId)));
        assertOwner(cart, owner);
        return cart;
    }

    private Cart loadOwnedCartByItem(CartOwner owner, Long itemId) {
        Cart cart = storage.findByItemId(itemId).orElseThrow(() ->
                new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item %d not found".formatted(itemId)));
        assertOwner(cart, owner);
        return cart;
    }

    private CartItem findItemOrThrow(Cart cart, Long itemId) {
        return cart.findItemById(itemId).orElseThrow(() ->
                new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item %d not found".formatted(itemId)));
    }

    private void requireActive(Cart cart, String action) {
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CART_NOT_MODIFIABLE,
                    "Items can only be %s while the cart is ACTIVE, cart %d is %s"
                            .formatted(action, cart.getId(), cart.getStatus()));
        }
    }

    /** Giỏ thuộc về người gọi nếu khớp userId hoặc guestSessionId. */
    private void assertOwner(Cart cart, CartOwner owner) {
        boolean byUser = owner.hasUserId() && owner.userId().equals(cart.getUserId());
        boolean byGuest = owner.hasGuestSessionId() && owner.guestSessionId().equals(cart.getGuestSessionId());
        if (!byUser && !byGuest) {
            throw new BusinessException(ErrorCode.CART_ACCESS_DENIED,
                    "You do not have access to cart %d".formatted(cart.getId()));
        }
    }

    private CartResponse toResponse(Cart cart) {
        return mapper.toResponse(cart, fetchProducts(productIdsOf(List.of(cart))));
    }

    private Set<Long> productIdsOf(List<Cart> carts) {
        return carts.stream()
                .flatMap(cart -> cart.getItems().stream())
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());
    }

    /** Gọi product-service 1 lần cho tất cả sản phẩm; nếu lỗi vẫn trả giỏ hàng (không kèm thông tin sản phẩm). */
    private Map<Long, ProductInfo> fetchProducts(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        try {
            return productClient.getProducts(productIds);
        } catch (RuntimeException ex) {
            log.warn("Could not load product info for {} products, returning cart without it: {}",
                    productIds.size(), ex.getMessage());
            return Map.of();
        }
    }
}
