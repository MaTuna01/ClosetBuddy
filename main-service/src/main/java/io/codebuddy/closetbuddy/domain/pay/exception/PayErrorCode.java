package io.codebuddy.closetbuddy.domain.pay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PayErrorCode {

    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Account
    NOT_ENOUGH_BALANCE(HttpStatus.BAD_REQUEST, "예치금 잔액이 부족합니다."),
    ACCOUNT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "내역이 존재하지 않거나 접근 권한이 없습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계좌 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST,"요청 금액과 실제 결제 금액이 일치하지 않습니다."),
    CANNOT_CANCEL_TYPE(HttpStatus.BAD_REQUEST,"충전 내역만 취소 가능합니다."),
    DEPOSIT_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "PG 결제 내역을 찾을 수 없습니다."),
    ALREADY_CANCELED_TRANSACTION(HttpStatus.BAD_REQUEST,"이미 취소된 내역입니다."),
    INSUFFICIENT_BALANCE_FOR_REFUND(HttpStatus.BAD_REQUEST,"잔액이 부족하여 취소할 수 없습니다."),

    // TOSS
    TOSS_PAYMENT_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "결제 요청이 잘못되었습니다."), // 4xx 에러
    TOSS_PAYMENT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "토스 결제 시스템에 오류가 발생했습니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없거나 접근 권한이 없습니다."),
    DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "이미 결제된 주문입니다.");



    private final HttpStatus status;
    private final String message;
}
