package io.codebuddy.payservice.domain.settlement.job;

import io.codebuddy.payservice.domain.pay.accounts.model.entity.Account;
import io.codebuddy.payservice.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.payservice.domain.pay.accounts.model.vo.TransactionType;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.payservice.domain.settlement.model.entity.Settlement;
import io.codebuddy.payservice.domain.settlement.model.entity.SettlementDetail;
import io.codebuddy.payservice.domain.settlement.model.vo.RawDataStatus;
import io.codebuddy.payservice.domain.settlement.model.vo.SettlementStatus;
import io.codebuddy.payservice.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRawDataRepository;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRepository;
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
    private final SettlementRawDataRepository settlementRawDataRepository;

    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateStr;

    @Override
    public void write(Chunk<? extends SettlementDetail> chunk) {
        // Chunk 단위의 데이터를 StoreId 기준으로 그룹핑 (상점 별 정산서)
        Map<Long, List<SettlementDetail>> groupedByStore = new HashMap<>();
        // rawData의 정산 상태 변경을 위해 미리 id 저장
        List<Long> rawDataIds= new ArrayList<>();

        for (SettlementDetail item : chunk.getItems()) {
            Long storeId = item.getStoreId();

            // Map에 해당 storeId가 없으면 새 리스트 생성
            if (!groupedByStore.containsKey(storeId)) {
                groupedByStore.put(storeId, new ArrayList<>());
            }
            // storeId 기준으로 그룹핑 하기
            groupedByStore.get(storeId).add(item);

            rawDataIds.add(item.getSettlementRawDataId());

        }

        // 상점 별로 그룹핑된 데이터 별로 꺼내 정산 내역에 저장
        groupedByStore.forEach((storeId, details) -> {
            Settlement settlement = null; // try-catch 범위 밖에서 초기화
            try {
                Long sellerId = details.get(0).getSellerId();
                Long memberId = details.get(0).getMemberId();
                LocalDate settlementDate = (targetDateStr != null) ? LocalDate.parse(targetDateStr) : LocalDate.now();

                // Settlement 조회/생성
                // 이미 처리된 chunk가 있다면 이미 정산서 존재
                settlement = settlementRepository.findByStoreIdAndSettlementDate(storeId, settlementDate)
                        .orElseGet(() -> settlementRepository.save(
                                Settlement.builder()
                                        .memberId(memberId)
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
                settlementRepository.save(settlement); // 변경된 총액 업데이트 (상태는 아직 SCHEDULED)

                // 예치금 지급 및 이력 저장
                // 1. 지급할 금액이 있는지 확인 (0원이면 지급 스킵)
                long payoutAmount = settlement.getPayoutAmount();

                if (payoutAmount > 0) {

                    //  Account 조회
                    Account account = accountRepository.findByMemberId(memberId)
                            .orElseThrow(() -> new RuntimeException("정산 계좌를 찾을 수 없습니다. MemberID: " + memberId));

                    //  잔액 충전
                    account.charge(payoutAmount);
                    accountRepository.save(account);

                    //  히스토리 기록
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

                // 성공 시 상태 업데이트
                // rawData 업데이트
                if (!rawDataIds.isEmpty()) {
                    settlementRawDataRepository.updateStatusByIds(rawDataIds, RawDataStatus.SETTLED);
                }
                // Settlement 업데이트
                settlement.setSettleStatus(SettlementStatus.SETTLED);
                settlementRepository.save(settlement);

            } catch (Exception e) {
                // [예외 처리] 해당 상점의 정산 실패 처리 (로그 남기기 + DB 상태 변경)
                log.error("정산 실패 - StoreId: {}, 사유: {}", storeId, e.getMessage(), e);

                if (settlement != null) {
                    // 정산 데이터는 생성되었으나 지급 과정에서 실패한 경우 -> 상태를 FAILED로 변경
                    settlement.setSettleStatus(SettlementStatus.FAILED);
                    settlementRepository.save(settlement);
                }
                // 여기서 예외를 다시 던지지 않아야 다른 상점의 정산은 계속 진행됨
            }
        });
    }
}
