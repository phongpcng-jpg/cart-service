package io.github.nguyenquephong13062003.cart_service.storage.jpa;

import io.github.nguyenquephong13062003.cart_service.entity.Cart;
import io.github.nguyenquephong13062003.cart_service.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    @Query("select c from Cart c join c.items i where i.id = :itemId")
    Optional<Cart> findByItemId(@Param("itemId") Long itemId);

    List<Cart> findByUserId(String userId);

    List<Cart> findByUserIdAndStatus(String userId, CartStatus status);

    List<Cart> findByGuestSessionId(String guestSessionId);

    List<Cart> findByGuestSessionIdAndStatus(String guestSessionId, CartStatus status);
}
