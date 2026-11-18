package io.github.nguyenquephong13062003.cart_service.entity;

/** Trạng thái giỏ hàng: trống / đang hoạt động / đã hủy / đã thanh toán. */
public enum CartStatus {
    EMPTY,
    ACTIVE,
    CANCELLED,
    PAID;

    /**
     * Bảng chuyển trạng thái thủ công (PATCH status):
     *  - EMPTY  -> CANCELLED
     *  - ACTIVE -> CANCELLED | PAID
     * Việc CANCELLED -> EMPTY/ACTIVE chỉ đi qua nghiệp vụ "tái kích hoạt" (reactivate).
     */
    public boolean canTransitionTo(CartStatus target) {
        return switch (this) {
            case EMPTY -> target == CANCELLED;
            case ACTIVE -> target == CANCELLED || target == PAID;
            case CANCELLED, PAID -> false;
        };
    }

    /** Chỉ giỏ trống hoặc đang hoạt động mới được thêm CartItem. */
    public boolean acceptsNewItems() {
        return this == EMPTY || this == ACTIVE;
    }
}
