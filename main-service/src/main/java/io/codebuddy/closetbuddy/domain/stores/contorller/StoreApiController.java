package io.codebuddy.closetbuddy.domain.stores.contorller;


import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.stores.model.dto.StoreResponse;
import io.codebuddy.closetbuddy.domain.stores.model.dto.UpdateStoreRequest;
import io.codebuddy.closetbuddy.domain.stores.model.dto.UpsertStoreRequest;
import io.codebuddy.closetbuddy.domain.stores.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog")
public class StoreApiController {

    private final StoreService storeService;

    //상점 등록
    @Operation(
            summary = "가게 등록",
            description = "가게를 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "가게 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 가게 데이터"
            )
    })
    @PostMapping("/stores")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @RequestBody @Valid UpsertStoreRequest request
            ) {
        Long storeId = storeService.createStore(memberPrincipalDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(storeId);
    }

    //내 가게 조회(단건)
    @Operation(
            summary = "가게 조회",
            description = "가게를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "가게 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "가게 데이터 없음"
            )
    })
    @GetMapping("/stores/{store_id}")
    public ResponseEntity<StoreResponse> getOneStore(
            @PathVariable Long store_id
    ) {
        StoreResponse response = storeService.getStore(store_id);
        return ResponseEntity.ok(response);
    }

    // 내 상점 목록 조회 (로그인 필수)
    // /stores/me
    @GetMapping("/me")
    public ResponseEntity<List<StoreResponse>> getMyStores(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails
    ) {
        List<StoreResponse> response = storeService.getMyStores(memberPrincipalDetails.getId());
        return ResponseEntity.ok(response);
    }

    // 전체 상점 목록 조회 (로그인 불필요)
    // /stores
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        List<StoreResponse> response = storeService.getAllStores();
        return ResponseEntity.ok(response);
    }

    //내 가게 정보 수정
    @Operation(
            summary = "가게 수정",
            description = "가게 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "가게 수정 성공(반환 데이터 없음)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "가게 데이터 없음"
            )

    })
    @PutMapping("stores/{store_id}")
    public ResponseEntity<StoreResponse> updateStore(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @PathVariable Long store_id,
            @RequestBody @Valid UpdateStoreRequest request
            ) {
        storeService.updateStore(memberPrincipalDetails.getId(), store_id, request);
        return ResponseEntity.ok().build();
    }

    //내 가게 삭제(폐점)
    @Operation(
            summary = "가게 삭제",
            description = "가게를 삭제(폐점)합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "가게 삭제 성공(반환 값 없음)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "가게 데이터 없음"
            )
    })
    @DeleteMapping("stores/{store_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteStore(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @PathVariable Long store_id
    ) {
        storeService.deleteStore(memberPrincipalDetails.getId(), store_id);
        return ResponseEntity.noContent().build();
    }
}
