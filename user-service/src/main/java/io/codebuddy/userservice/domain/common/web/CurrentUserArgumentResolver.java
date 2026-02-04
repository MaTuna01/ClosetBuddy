package io.codebuddy.userservice.domain.common.web;

import io.codebuddy.userservice.domain.common.exception.AuthHeaderMissingException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String USER_ID_HEADER =  "X-USER-ID";
    private static final String ROLE_HEADER = "X-USER-ROLE";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(CurrentUserInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory){
        String userId = webRequest.getHeader(USER_ID_HEADER);
        String role = webRequest.getHeader(ROLE_HEADER);

        if (!StringUtils.hasText(userId) || !StringUtils.hasText(role)){
            throw new AuthHeaderMissingException("X-USER-ID or X-USER-ROLE header missing");
        }

        return new CurrentUserInfo(userId, role);
    }
}
