package io.github.nguyenquephong13062003.cart_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dòng hàng trong giỏ. Chỉ lưu ID tham chiếu (productId, skuId) và đơn giá tại thời điểm thêm vào giỏ.
 */
@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cart_items_cart_product_sku",
                columnNames = {"cart_id", "product_id", "sku_id"}),
        indexes = @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    /** Giới hạn số lượng tối đa của 1 dòng hàng (chặn tràn số khi cộng dồn). */
    public static final int MAX_QUANTITY = 9_999;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false, updatable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Mã phân loại hàng (size, màu...). */
    @Column(name = "sku_id", nullable = false, length = 100)
    private String skuId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** Đơn giá tại thời điểm thêm vào giỏ (snapshot). */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    CartItem(Long productId, String skuId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** Chỉ {@link Cart} được phép gắn item vào giỏ. */
    void attachTo(Cart cart) {
        this.cart = cart;
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void changeSku(String skuId) {
        this.skuId = skuId;
    }

    public void changeUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
