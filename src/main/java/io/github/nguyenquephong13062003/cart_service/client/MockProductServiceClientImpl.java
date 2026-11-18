package io.github.nguyenquephong13062003.cart_service.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/** Dữ liệu giả lập, xác định theo productId (cùng ID luôn ra cùng kết quả). */
@Component
@ConditionalOnProperty(prefix = "cart.product-client", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockProductServiceClientImpl implements ProductServiceClient {

    @Override
    public Optional<ProductInfo> getProduct(Long productId) {
        BigDecimal price = BigDecimal.valueOf(50_000L + Math.floorMod(productId, 10L) * 25_000L).setScale(2);
        return Optional.of(new ProductInfo(
                productId,
                "Mock Product #" + productId,
                "https://picsum.photos/seed/product-" + productId + "/400/400",
                price));
    }
}
