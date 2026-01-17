package io.codebuddy.closetbuddy.domain.sellers.controller;


import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.sellers.model.dto.SellerResponse;
import io.codebuddy.closetbuddy.domain.sellers.model.dto.SellerUpsertRequest;
import io.codebuddy.closetbuddy.domain.sellers.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog/sellers")
public class SellerApiController {

    private final SellerService sellerService;

    //내 정보 조회
    @Operation(
            summary = "판매자 정보 조회",
            description = "판매자 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "판매자 정보 조회 완료."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<SellerResponse> getMyInfo(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails
    ) {
        SellerResponse response = sellerService.getSellerInfo(memberPrincipalDetails.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //내 정보 수정
    @Operation(
            summary = "판매자 정보 수정",
            description = "판매자 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "판매자 정보 수정 완료"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @PutMapping("/me")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @RequestBody @Valid SellerUpsertRequest request
    ) {
        sellerService.updateSeller(memberPrincipalDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    //등록 해제
    @Operation(
            summary = "판매자 등록 해제",
            description = "판매자 등록을 해제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "판매자 등록 해제 완료"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> unregister(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails
    ) {
        sellerService.unregisterSeller(memberPrincipalDetails.getId());
        return ResponseEntity.noContent().build();
    }

}
