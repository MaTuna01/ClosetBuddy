package io.codebuddy.closetbuddy.domain.catalog.sellers.controller;


import io.codebuddy.closetbuddy.domain.catalog.web.dto.CatalogResult;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.dto.SellerResponse;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.dto.SellerUpsertRequest;
import io.codebuddy.closetbuddy.domain.catalog.sellers.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog/sellers")
public class SellerApiController {

    private final SellerService sellerService;

    //판매자 등록
    @Operation(
            summary = "판매자 등록",
            description = "판매자를 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "판매자 등록 완료"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 등록된 판매자"
            )
    })
    @PostMapping
    public ResponseEntity<CatalogResult<Void>> register(
            @CurrentUser CurrentUserInfo currentUser,
            @RequestBody @Valid SellerUpsertRequest request
    ) {
        sellerService.registerSeller(Long.parseLong(currentUser.userId()), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CatalogResult.messageOnly("판매자 등록이 완료되었습니다."));
    }

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
            @CurrentUser CurrentUserInfo currentUser
    ) {
        SellerResponse response = sellerService.getSellerInfo(Long.parseLong(currentUser.userId()));
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
    public ResponseEntity<CatalogResult<Void>> update(
            @CurrentUser CurrentUserInfo currentUser,
            @RequestBody @Valid SellerUpsertRequest request
    ) {
        sellerService.updateSeller(Long.parseLong(currentUser.userId()), request);
        return ResponseEntity.ok(CatalogResult.messageOnly("판매자 정보가 수정되었습니다."));
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
    public ResponseEntity<CatalogResult<Void>> unregister(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        sellerService.unregisterSeller(Long.parseLong(currentUser.userId()));
        return ResponseEntity.ok(CatalogResult.messageOnly("판매자 등록이 해제되었습니다."));
    }

}
