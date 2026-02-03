package io.codebuddy.userservice.domain.common.exception;

import io.codebuddy.userservice.domain.auth.form.controller.LoginController;
import io.codebuddy.userservice.domain.member.controller.MemberController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/*
정확한 에러 메시지를 사용자에게 출력하기 위한 메서드
 */
@RestControllerAdvice(assignableTypes = {LoginController.class, MemberController.class})
public class GlobalExceptionHandler {
    /**
     * @Valid 검증 실패 시 호출됨
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        // 에러 상세 정보 추출
        List<ErrorResponse.FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldErrorDetail.builder()
                        .field(error.getField())
                        .value(String.valueOf(error.getRejectedValue()))
                        .reason(error.getDefaultMessage()) // DTO에 적은 message가 여기 들어감
                        .build())
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .code("INVALID_INPUT_VALUE")
                .message("입력값이 유효하지 않습니다.")
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //회원가입 했을 때 중복된 값을 예외처리해주는 메소드
    @ExceptionHandler(DuplicateMemberFieldException.class)
    protected ResponseEntity<ErrorResponse> handleDuplicateMemberField(DuplicateMemberFieldException e) {

        ErrorResponse.FieldErrorDetail detail = ErrorResponse.FieldErrorDetail.builder()
                .field(e.getField())
                .value(e.getValue())
                .reason(e.getMessage())
                .build();

        ErrorResponse response = ErrorResponse.builder()
                .code("DUPLICATE_VALUE")
                .message("중복된 값이 존재합니다.")
                .errors(List.of(detail))
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
