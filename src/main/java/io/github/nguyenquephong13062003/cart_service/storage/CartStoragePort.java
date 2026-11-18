package io.github.nguyenquephong13062003.cart_service.storage;

import io.github.nguyenquephong13062003.cart_service.entity.Cart;
import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import io.github.nguyenquephong13062003.cart_service.identity.CartOwner;

import java.util.List;
import java.util.Optional;

/**
 * Cổng lưu trữ giỏ hàng. Tầng nghiệp vụ chỉ phụ thuộc interface này.
 * Hiện có {@code JpaCartStorageAdapter} (PostgreSQL); chuyển sang Redis chỉ cần viết thêm
 * một implementation mới và đổi {@code cart.storage.type}, không sửa CartService.
 */
public interface CartStoragePort {

    /** Lưu (tạo mới hoặc cập nhật) cả giỏ và các item; trả về đối tượng đã có ID. */
    Cart save(Cart cart);

    Optional<Cart> findById(Long cartId);

    /** Tìm giỏ chứa CartItem có ID tương ứng. */
    Optional<Cart> findByItemId(Long itemId);

    /** Các giỏ thuộc chủ sở hữu (theo userId và/hoặc guestSessionId), mới cập nhật nhất trước; status null = tất cả. */
    List<Cart> findByOwner(CartOwner owner, CartStatus status);

    /** Xóa cứng giỏ và toàn bộ CartItem. */
    void delete(Cart cart);
}
