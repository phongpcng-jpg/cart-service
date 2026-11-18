package io.github.nguyenquephong13062003.cart_service.identity;

import io.github.nguyenquephong13062003.cart_service.exception.BusinessException;
import io.github.nguyenquephong13062003.cart_service.exception.ErrorCode;

/**
 * Chủ sở hữu giỏ hàng, lấy từ header do API Gateway gắn vào:
 *  - X-User-Id          : người dùng đã đăng nhập
 *  - X-Guest-Session-Id : khách chưa đăng nhập
 * Phải có ít nhất một trong hai.
 */
public record CartOwner(String userId, String guestSessionId) {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String GUEST_SESSION_ID_HEADER = "X-Guest-Session-Id";
    private static final int MAX_LENGTH = 64;

    public CartOwner {
        userId = blankToNull(userId);
        guestSessionId = blankToNull(guestSessionId);
        if (userId == null && guestSessionId == null) {
            throw new BusinessException(ErrorCode.MISSING_IDENTITY,
                    "Header '%s' or '%s' is required".formatted(USER_ID_HEADER, GUEST_SESSION_ID_HEADER));
        }
        if ((userId != null && userId.length() > MAX_LENGTH)
                || (guestSessionId != null && guestSessionId.length() > MAX_LENGTH)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Identity headers must not exceed %d characters".formatted(MAX_LENGTH));
        }
    }

    public boolean hasUserId() {
        return userId != null;
    }

    public boolean hasGuestSessionId() {
        return guestSessionId != null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
