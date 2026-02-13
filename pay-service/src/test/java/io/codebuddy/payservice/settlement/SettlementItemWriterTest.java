package io.codebuddy.payservice.settlement;


import io.codebuddy.payservice.domain.pay.accounts.model.entity.Account;
import io.codebuddy.payservice.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.payservice.domain.settlement.job.SettlementItemWriter;
import io.codebuddy.payservice.domain.settlement.model.entity.Settlement;
import io.codebuddy.payservice.domain.settlement.model.entity.SettlementDetail;
import io.codebuddy.payservice.domain.settlement.model.vo.RawDataStatus;
import io.codebuddy.payservice.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRawDataRepository;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementItemWriterTest {

    @InjectMocks
    private SettlementItemWriter writer;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementDetailRepository settlementDetailRepository;

    @Mock
    private SettlementRawDataRepository settlementRawDataRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountHistoryRepository accountHistoryRepository;

    @Test
    @DisplayName("정산 Writer 성공: 상점별로 그룹핑되어 정산서가 생성되고 예치금이 지급된다")
    void settlementItemWriter_Success() {

        // given
        Long storeId1 = 100L;
        Long sellerId1 = 50L;
        Long memberId1 = 1L;

        // 정산 원천 데이터 id 설정
        Long rawDataId1 = 10L;
        Long rawDataId2 = 20L;

        // 같은 상점
        SettlementDetail item1 = SettlementDetail.builder()
                .settlementRawDataId(rawDataId1)
                .storeId(storeId1)
                .sellerId(sellerId1)
                .memberId(memberId1)
                .payoutAmount(5000L)
                .totalAmount(10000L)
                .build();

        SettlementDetail item2 = SettlementDetail.builder()
                .settlementRawDataId(rawDataId2)
                .storeId(storeId1)
                .sellerId(sellerId1)
                .memberId(memberId1)
                .payoutAmount(3000L)
                .totalAmount(5000L)
                .build();

        Chunk<SettlementDetail> chunk = new Chunk<>(List.of(item1, item2));

        // Settlement 생성
        given(settlementRepository.findByStoreIdAndSettlementDate(any(), any()))
                .willReturn(Optional.empty());

        given(settlementRepository.save(any(Settlement.class))).willAnswer(inv -> {
            Settlement s = inv.getArgument(0);
            if (s.getSettleId() == null) {
                ReflectionTestUtils.setField(s, "settleId", 999L);
            }
            return s;
        });

        Account mockAccount = Account.createAccount(memberId1);
        given(accountRepository.findByMemberId(memberId1)).willReturn(Optional.of(mockAccount));

        // when
        writer.write(chunk);

        // then
        // 같은 상점의 데이터 2개가 합산되어 처리되었는지 확인
        // 총 지급액: 5000 + 3000 = 8000원
        // 디테일이 저장되었는지 검증
        verify(settlementDetailRepository).saveAll(any());

        // 정산 데이터 저장이 정상적으로 호출되었는지 검증
        // (최초 생성 1회 + 누적 금액 업데이트 1회 + 상태 SETTLED 업데이트 1회 = 총 3회 호출)
        verify(settlementRepository, times(3)).save(any(Settlement.class));

        // 계좌(Account) 충전 및 저장 검증
        verify(accountRepository).save(any(Account.class));
        assertThat(mockAccount.getBalance()).isEqualTo(8000L); // 잔액이 8000원 충전되었는지 확인

        // 예치금 내역(AccountHistory) 생성 검증
        verify(accountHistoryRepository).save(any(AccountHistory.class));

        // 정산 원천 데이터 변화 검증 (updateStatusByIds가 호출되었는지 확인)
        List<Long> expectedRawDataIds = List.of(rawDataId1, rawDataId2);
        verify(settlementRawDataRepository).updateStatusByIds(expectedRawDataIds, RawDataStatus.SETTLED);
    }



}
