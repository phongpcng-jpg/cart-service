package io.github.nguyenquephong13062003.cart_service.identity;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Cho phép controller khai báo trực tiếp tham số {@link CartOwner}.
 * Sau này nếu Gateway chuyển sang JWT/claims chỉ cần sửa class này.
 */
public class CartOwnerArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CartOwner.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return new CartOwner(
                webRequest.getHeader(CartOwner.USER_ID_HEADER),
                webRequest.getHeader(CartOwner.GUEST_SESSION_ID_HEADER));
    }
}
