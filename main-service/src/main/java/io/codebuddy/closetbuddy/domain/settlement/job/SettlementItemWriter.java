package io.codebuddy.closetbuddy.domain.settlement.job;

import io.codebuddy.closetbuddy.domain.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.accounts.model.entity.AccountHistory;
import io.codebuddy.closetbuddy.domain.accounts.model.vo.TransactionType;
import io.codebuddy.closetbuddy.domain.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.Settlement;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementDetail;
import io.codebuddy.closetbuddy.domain.settlement.model.vo.SettlementStatus;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class SettlementItemWriter implements ItemWriter<SettlementDetail> {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;

    private final SellerJpaRepository sellerRepository;
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateStr;

    @Override
    public void write(Chunk<? extends SettlementDetail> chunk) {
        // Chunk 단위의 데이터를 StoreId 기준으로 그룹핑 (상점 별 정산서)
        Map<Long, List<SettlementDetail>> groupedByStore = new HashMap<>();

        for (SettlementDetail item : chunk.getItems()) {
            Long storeId = item.getStoreId();

            // Map에 해당 storeId가 없으면 새 리스트 생성
            if (!groupedByStore.containsKey(storeId)) {
                groupedByStore.put(storeId, new ArrayList<>());
            }
            // storeId 기준으로 그룹핑 하기
            groupedByStore.get(storeId).add(item);
        }

        // 상점 별로 그룹핑된 데이터 별로 꺼내 정산 내역에 저장
        groupedByStore.forEach((storeId, details) -> {
            Long sellerId = details.get(0).getSellerId(); // 같은 그룹이면 SellerId도 같음
            // 매달 10일 정산
            LocalDate settlementDate = (targetDateStr != null)
                    ? LocalDate.parse(targetDateStr)
                    : LocalDate.now();

            // Settlement 조회/생성
            // 이미 처리된 chunk가 있다면 이미 정산서 존재
            Settlement settlement = settlementRepository.findByStoreIdAndSettlementDate(storeId, settlementDate)
                    .orElseGet(() -> settlementRepository.save(
                            Settlement.builder()
                                    .storeId(storeId)
                                    .sellerId(sellerId)
                                    .settlementDate(settlementDate)
                                    .totalSalesAmount(0L)
                                    .payoutAmount(0L)
                                    .createdAt(LocalDateTime.now())
                                    .settleStatus(SettlementStatus.SCHEDULED)
                                    .build()
                    ));

            // SettlementDetail에 부모 ID 주입 및 금액 누적
            for (SettlementDetail detail : details) {
                detail.setSettleId(settlement.getSettleId()); // FK 설정
                settlement.addAmounts(detail.getTotalAmount(), detail.getPayoutAmount()); // 금액 누적 -> 최종 영수증 금액 갱신
            }

            // 저장 (Detail 저장 및 Settlement 업데이트)
            settlementDetailRepository.saveAll(details);
            settlement.setSettleStatus(SettlementStatus.SETTLED);
            settlementRepository.save(settlement); // 변경된 총액과 상태 업데이트


            // 예치금 지급 및 이력 저장
            // 1. 지급할 금액이 있는지 확인 (0원이면 지급 스킵)
            long payoutAmount = settlement.getPayoutAmount();

            if (payoutAmount > 0) {
                // 2. Seller ID로 Member ID 찾기
                Seller seller = sellerRepository.findById(sellerId)
                        .orElseThrow(() -> new RuntimeException("판매자 정보를 찾을 수 없습니다. ID: " + sellerId));

                // 2. Member ID 추출
                Long memberId = seller.getMemberId();

                // 3. Account 조회
                Account account = accountRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new RuntimeException("정산 계좌를 찾을 수 없습니다. MemberID: " + memberId));

                // 4. 잔액 충전
                account.charge(payoutAmount);
                accountRepository.save(account);

                // 5. 히스토리 기록
                AccountHistory history = AccountHistory.builder()
                        .account(account)
                        .type(TransactionType.SETTLEMENT)
                        .amount(payoutAmount)
                        .balanceSnapshot(account.getBalance()) // 충전 후 잔액 스냅샷
                        .refId(settlement.getSettleId())       // 참조 ID = 정산 ID
                        .createdAt(LocalDateTime.now())
                        .build();

                accountHistoryRepository.save(history);

                log.info("정산 지급 완료 - SellerId: {}, Amount: {}, SettleId: {}",
                        sellerId, payoutAmount, settlement.getSettleId());
            }



        });
    }
}