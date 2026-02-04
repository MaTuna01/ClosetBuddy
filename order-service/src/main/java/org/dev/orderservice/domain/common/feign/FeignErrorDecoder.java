package org.dev.orderservice.domain.common.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
//        log.error("feign 호출 에러 - method: {}, status: {}, reason: {}",
//                methodKey, response.status(), response.reason());
//
//        // 판매자 역할 관련 API 호출 실패 시 적절한 SellerErrorCode로 매핑
//        return switch (response.status()) {
//            case 400 -> // Bad Request - 잘못된 요청
//                new SellerException(SellerErrorCode.ROLE_GRANT_FAILED);
//            case 401, 403 -> // Unauthorized, Forbidden - 인증/인가 실패(권한 없음)
//                new SellerException(SellerErrorCode.UNAUTHORIZED_ACCESS);
//            case 404 -> //Not Found - 회원을 찾을 수 없음
//                new SellerException(SellerErrorCode.SELLER_NOT_FOUND);
//            case 409 -> //Conflict - 이미 판매자로 등록됨
//                new SellerException(SellerErrorCode.ALREADY_REGISTERED);
//            case 500, 502, 503, 504 -> // ServerError- 서비스 불가
//                new SellerException(SellerErrorCode.ROLE_GRANT_FAILED);
//            default ->  defaultErrorDecoder.decode(methodKey, response);
//        };
        return  defaultErrorDecoder.decode(methodKey, response);
    }
}
