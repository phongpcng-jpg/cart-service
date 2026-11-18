package io.github.nguyenquephong13062003.cart_service.exception;

import org.springframework.http.HttpStatus;

/** Mã lỗi nghiệp vụ + HTTP status tương ứng. */
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
    MISSING_IDENTITY(HttpStatus.UNAUTHORIZED),
    CART_ACCESS_DENIED(HttpStatus.FORBIDDEN),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT),
    CART_NOT_MODIFIABLE(HttpStatus.CONFLICT),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT),
    QUANTITY_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
