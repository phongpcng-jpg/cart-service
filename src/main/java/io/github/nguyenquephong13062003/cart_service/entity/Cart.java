package io.github.nguyenquephong13062003.cart_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Giỏ hàng. Chỉ lưu ID tham chiếu (userId), không lưu thông tin User/Product/Order.
 * Quan hệ 2 chiều với {@link CartItem}: Cart quản lý logic (addItem/removeItem),
 * CartItem giữ khóa ngoại cart_id.
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_user_id", columnList = "user_id"),
        @Index(name = "idx_carts_guest_session_id", columnList = "guest_session_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    /** ID người dùng (từ header X-User-Id). Null nếu là giỏ của khách vãng lai. */
    @Column(name = "user_id", length = 64)
    private String userId;

    /** ID phiên khách chưa đăng nhập (từ header X-Guest-Session-Id). */
    @Column(name = "guest_session_id", length = 64)
    private String guestSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CartStatus status;

    /** Tổng tiền tạm tính = sum(unitPrice * quantity). */
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    /** Optimistic locking: chặn 2 request đồng thời ghi đè nhau. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    // ------------------------------------------------------------------ factory

    public static Cart create(String userId, String guestSessionId) {
        Cart cart = new Cart();
        cart.userId = userId;
        cart.guestSessionId = guestSessionId;
        cart.status = CartStatus.EMPTY;
        cart.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Instant now = Instant.now();
        cart.createdAt = now;
        cart.updatedAt = now;
        return cart;
    }

    // ------------------------------------------------------------------ relationship (2 chiều)

    /** Danh sách chỉ đọc; mọi thay đổi phải đi qua addItem/removeItem để giữ 2 chiều nhất quán. */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Tạo CartItem mới và gắn vào giỏ (cập nhật cả 2 phía của quan hệ). */
    public CartItem addItem(Long productId, String skuId, int quantity, BigDecimal unitPrice) {
        CartItem item = new CartItem(productId, skuId, quantity, unitPrice);
        item.attachTo(this);
        this.items.add(item);
        return item;
    }

    /** Gỡ CartItem khỏi giỏ; orphanRemoval sẽ xóa dòng tương ứng trong DB. */
    public void removeItem(CartItem item) {
        this.items.remove(item);
    }

    public Optional<CartItem> findItem(Long productId, String skuId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId) && i.getSkuId().equals(skuId))
                .findFirst();
    }

    public Optional<CartItem> findItemById(Long itemId) {
        return items.stream()
                .filter(i -> i.getId() != null && i.getId().equals(itemId))
                .findFirst();
    }

    // ------------------------------------------------------------------ behaviour

    /** Đồng bộ trạng thái theo số lượng item: không có item -> EMPTY, ngược lại -> ACTIVE. */
    public void syncStatusWithItems() {
        this.status = items.isEmpty() ? CartStatus.EMPTY : CartStatus.ACTIVE;
    }

    public void changeStatus(CartStatus newStatus) {
        this.status = newStatus;
        touch();
    }

    /** Tính lại tổng tiền tạm tính và cập nhật updated_at. */
    public void recalculate() {
        this.subtotal = items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
