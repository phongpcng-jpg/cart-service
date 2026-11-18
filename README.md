# cart-service

Spring Boot 4.1.2 · Java 21 · PostgreSQL · Gradle

## Chạy nhanh
```bash
docker compose -f docker/docker-compose.yml up -d     # PostgreSQL (cart_db / cart_user / cart_pass)
./gradlew bootRun                                     # http://localhost:8083
```
> `CartServiceApplicationTests.contextLoads` cần PostgreSQL đang chạy (hoặc bỏ test đó khi build offline).

## Định danh (do API Gateway gắn)
| Header | Ý nghĩa |
|---|---|
| `X-User-Id` | Người dùng đã đăng nhập |
| `X-Guest-Session-Id` | Khách chưa đăng nhập (cần ít nhất 1 trong 2 header) |

## Ví dụ
```bash
# Tạo giỏ kèm item (có item -> ACTIVE; bỏ body / items rỗng -> EMPTY; cặp productId+skuId trùng trong danh sách -> 400)
curl -X POST localhost:8083/api/v1/carts -H 'X-User-Id: u1' -H 'Content-Type: application/json' \
  -d '{"items":[{"productId":1,"skuId":"RED-M","quantity":2,"unitPrice":150000}]}'

# Thêm item (cùng productId+skuId thì cộng dồn quantity và cập nhật unitPrice theo giá mới nhất)
curl -X POST localhost:8083/api/v1/carts/1/items -H 'X-User-Id: u1' -H 'Content-Type: application/json' \
  -d '{"productId":1,"skuId":"RED-M","quantity":1,"unitPrice":150000}'

# Đổi sku/quantity của item (nếu sku mới đã có trong giỏ thì cộng dồn vào dòng đó, dòng hiện tại bị xóa)
curl -X PUT localhost:8083/api/v1/cart-items/1 -H 'X-User-Id: u1' -H 'Content-Type: application/json' \
  -d '{"skuId":"RED-L","quantity":5}'

# Truy vấn
curl localhost:8083/api/v1/carts                 -H 'X-User-Id: u1'   # danh sách giỏ (không kèm items), ?status=ACTIVE
curl localhost:8083/api/v1/carts/1               -H 'X-User-Id: u1'   # thông tin giỏ (không kèm items)
curl localhost:8083/api/v1/carts/1/detail        -H 'X-User-Id: u1'   # thông tin giỏ đầy đủ + items
curl localhost:8083/api/v1/carts/1/items         -H 'X-User-Id: u1'   # danh sách items của giỏ
curl localhost:8083/api/v1/cart-items/1          -H 'X-User-Id: u1'   # thông tin 1 item

# Xóa item / đổi trạng thái / tái kích hoạt / xóa cứng
curl -X DELETE localhost:8083/api/v1/cart-items/1 -H 'X-User-Id: u1'
curl -X PATCH  localhost:8083/api/v1/carts/1/status -H 'X-User-Id: u1' -H 'Content-Type: application/json' -d '{"status":"PAID"}'
curl -X POST   localhost:8083/api/v1/carts/1/reactivate -H 'X-User-Id: u1'
curl -X DELETE localhost:8083/api/v1/carts/1 -H 'X-User-Id: u1'
```

## Nâng cấp sau này
- **Redis:** viết `RedisCartStorageAdapter implements CartStoragePort`, gắn
  `@ConditionalOnProperty(prefix="cart.storage", name="type", havingValue="redis")`, đổi `cart.storage.type=redis`.
- **product-service thật:** viết `RestProductServiceClientImpl implements ProductServiceClient`
  (override `getProducts` để gọi theo lô), đổi `cart.product-client.mode`.
- **Schema production:** dùng Flyway/Liquibase với `db/schema.sql`, đặt `JPA_DDL_AUTO=validate`.
