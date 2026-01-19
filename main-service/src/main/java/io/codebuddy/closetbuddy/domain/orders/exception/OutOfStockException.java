package io.codebuddy.closetbuddy.domain.orders.exception;

/**
 * 재고 부족 예외 처리용
 * RuntimeException으로 처리하여 실행 중에 발생하며
 * 시스템 환경이나 Input값이 잘못된 경우,
 * 프로그래머가 잡아내기 위한 조건 등에 부합할때 사용하기에 알맞은 예외처리
 */
public class OutOfStockException extends RuntimeException{

    // 재고 부족하면 메시지를 전송한다.
    public OutOfStockException(String message) {
        super(message);
    }

}
