package io.github.nguyenquephong13062003.cart_service.exception;

import java.time.Instant;
import java.util.List;

/** Định dạng lỗi chuẩn trả về cho mọi API. */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldViolation> details) {

    /** Chi tiết lỗi theo từng trường (dùng cho lỗi validation). */
    public record FieldViolation(String field, String message) {
    }
}
