package io.github.nguyenquephong13062003.cart_service.exception;

/** Lỗi nghiệp vụ có chủ đích; được {@link GlobalExceptionHandler} chuyển thành response chuẩn. */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
