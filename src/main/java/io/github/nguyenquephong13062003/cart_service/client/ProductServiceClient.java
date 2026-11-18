package io.github.nguyenquephong13062003.cart_service.client;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cổng giao tiếp với product-service.
 * Hiện dùng {@link MockProductServiceClientImpl}; sau này thay bằng RestClient/OpenFeign
 * (override {@link #getProducts(Collection)} để gọi theo lô, tránh N+1 call).
 */
public interface ProductServiceClient {

    Optional<ProductInfo> getProduct(Long productId);

    default Map<Long, ProductInfo> getProducts(Collection<Long> productIds) {
        Map<Long, ProductInfo> result = new LinkedHashMap<>();
        for (Long productId : productIds) {
            getProduct(productId).ifPresent(info -> result.put(productId, info));
        }
        return result;
    }
}
