package io.codebuddy.closetbuddy.domain.accounts.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor // 역직렬화를 위해 기본 생성자 필수
@JsonIgnoreProperties(ignoreUnknown = true) // 정의하지 않은 필드는 무시
public class TossPaymentResponse {
    private String paymentKey;
    private String status;
    private String method;
    private Long totalAmount;
    private String approvedAt;

}