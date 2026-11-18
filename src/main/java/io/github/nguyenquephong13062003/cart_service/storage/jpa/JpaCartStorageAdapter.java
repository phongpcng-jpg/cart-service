package io.github.nguyenquephong13062003.cart_service.storage.jpa;

import io.github.nguyenquephong13062003.cart_service.entity.Cart;
import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import io.github.nguyenquephong13062003.cart_service.identity.CartOwner;
import io.github.nguyenquephong13062003.cart_service.storage.CartStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementation lưu trữ bằng PostgreSQL qua Spring Data JPA. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cart.storage", name = "type", havingValue = "jpa", matchIfMissing = true)
public class JpaCartStorageAdapter implements CartStoragePort {

    private final CartJpaRepository repository;

    @Override
    public Cart save(Cart cart) {
        // flush ngay để ID của Cart/CartItem mới có sẵn khi map response và để lộ lỗi optimistic lock sớm
        return repository.saveAndFlush(cart);
    }

    @Override
    public Optional<Cart> findById(Long cartId) {
        return repository.findById(cartId);
    }

    @Override
    public Optional<Cart> findByItemId(Long itemId) {
        return repository.findByItemId(itemId);
    }

    @Override
    public List<Cart> findByOwner(CartOwner owner, CartStatus status) {
        Map<Long, Cart> merged = new LinkedHashMap<>();
        List<Cart> found = new ArrayList<>();
        if (owner.hasUserId()) {
            found.addAll(status == null
                    ? repository.findByUserId(owner.userId())
                    : repository.findByUserIdAndStatus(owner.userId(), status));
        }
        if (owner.hasGuestSessionId()) {
            found.addAll(status == null
                    ? repository.findByGuestSessionId(owner.guestSessionId())
                    : repository.findByGuestSessionIdAndStatus(owner.guestSessionId(), status));
        }
        found.forEach(cart -> merged.putIfAbsent(cart.getId(), cart));
        return merged.values().stream()
                .sorted(Comparator.comparing(Cart::getUpdatedAt).reversed())
                .toList();
    }

    @Override
    public void delete(Cart cart) {
        repository.delete(cart);
    }
}
