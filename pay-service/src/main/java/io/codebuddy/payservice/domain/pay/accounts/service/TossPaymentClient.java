package io.codebuddy.payservice.domain.pay.accounts.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.payservice.domain.pay.accounts.model.dto.AccountCommand;
import io.codebuddy.payservice.domain.pay.accounts.model.dto.PaymentSuccessDto;
import io.codebuddy.payservice.domain.pay.accounts.model.dto.TossPaymentResponse;
import io.codebuddy.payservice.domain.pay.accounts.model.vo.TossCancelRequest;
import io.codebuddy.payservice.domain.pay.accounts.model.vo.TossPaymentConfirm;
import io.codebuddy.payservice.domain.pay.exception.PayErrorCode;
import io.codebuddy.payservice.domain.pay.exception.PayException;
import io.codebuddy.payservice.domain.pay.exception.TossErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
@RequiredArgsConstructor
public class TossPaymentClient implements PaymentClient {

    @Value("${custom.payments.toss.secrets}")
    private String tossPaymentSecrets;

    @Value("${custom.payments.toss.url}")
    private String tossPaymentUrl;

    private final ObjectMapper om;

    @Override
    public PaymentSuccessDto confirm(AccountCommand command) {
        try {
            // 1. 시크릿 키 인코딩
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentSecrets + ":").getBytes(StandardCharsets.UTF_8));

            // 2. 요청 객체를 JSON 문자열로 변환
            String requestBody = om.writeValueAsString(
                    new TossPaymentConfirm(
                            command.getPaymentKey(),
                            command.getOrderId(),
                            command.getAmount()));

            // 3. HttpClient 생성
            HttpClient client = HttpClient.newHttpClient();

            // 4. 요청 생성
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tossPaymentUrl + "/confirm"))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 5. 요청 전송 및 응답 수신
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 6. 결과 처리
            if (response.statusCode() == 200) {
                // 성공 시: json 문자열 -> dto로 매핑
                TossPaymentResponse tossResponse = om.readValue(response.body(), TossPaymentResponse.class);
                return PaymentSuccessDto.builder()
                        .paymentKey(tossResponse.getPaymentKey())
                        .status(tossResponse.getStatus())
                        .totalAmount(tossResponse.getTotalAmount())
                        .approvedAt(tossResponse.getApprovedAt())
                        .build();
            } else {
                // 실패 시: 에러 로그 출력 및 예외 발생
                log.error("토스 결제 승인 실패. 응답코드: {}, 내용: {}", response.statusCode(), response.body());

                TossErrorResponse errorResponse = om.readValue(response.body(), TossErrorResponse.class);

                PayErrorCode payErrorCode;
                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    payErrorCode = PayErrorCode.TOSS_PAYMENT_CLIENT_ERROR;
                } else {
                    payErrorCode = PayErrorCode.TOSS_PAYMENT_SERVER_ERROR;
                }

                throw new PayException(
                        payErrorCode,
                        errorResponse.getCode(),
                        errorResponse.getMessage()
                );
            }

        } catch (PayException e) {
            throw e;
        } catch (Exception e) {
            log.error("토스 결제 통신 중 에러 발생", e);
            throw new RuntimeException("결제 시스템 연동 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void cancel(String paymentKey, String reason) {
        try {
            // 인증 헤더 (시크릿 키)
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentSecrets + ":").getBytes(StandardCharsets.UTF_8));

            // 요청 Body 생성 (Record -> JSON)
            String requestBody = om.writeValueAsString(new TossCancelRequest(reason));

            // HttpClient 준비
            HttpClient client = HttpClient.newHttpClient();

            // 요청 만들기 (POST)
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tossPaymentUrl + "/" + paymentKey + "/cancel"))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 전송
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 토스 결제 취소 실패
            if (response.statusCode() != 200) {
                TossErrorResponse errorResponse = om.readValue(response.body(), TossErrorResponse.class);

                log.error("토스 환불 실패. 상태코드: {}, 내용: {}", response.statusCode(), response.body());

                PayErrorCode payErrorCode;
                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    payErrorCode = PayErrorCode.TOSS_PAYMENT_CLIENT_ERROR;
                } else {
                    payErrorCode = PayErrorCode.TOSS_PAYMENT_SERVER_ERROR;
                }

                throw new PayException(
                        payErrorCode,
                        errorResponse.getCode(),
                        errorResponse.getMessage()
                );
            }

            log.info("토스 결제 취소 성공. paymentKey: {}", paymentKey);

        } catch (PayException e) {
            // PayException은 Exception이 잡지 않도록 함
            throw e;
        } catch (Exception e) { // 여기서 예외를 던져야 deleteHistory의 @Transactional이 작동해 DB도 롤백
            log.error("토스 결제 취소 중 통신 오류 발생", e);
            // 그 외(JsonProcessingException, IOException 등)만 RuntimeException으로 감쌉니다.
            throw new RuntimeException("결제 취소 연동 중 오류가 발생했습니다.", e);
        }


    }
}
