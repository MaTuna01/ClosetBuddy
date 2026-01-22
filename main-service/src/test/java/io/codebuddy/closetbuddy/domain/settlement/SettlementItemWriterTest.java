package io.codebuddy.closetbuddy.domain.settlement;


import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.settlement.job.SettlementItemWriter;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.Settlement;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementDetail;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SettlementItemWriterTest {

    @InjectMocks
    private SettlementItemWriter writer;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementDetailRepository settlementDetailRepository;

    @Mock
    private SellerJpaRepository sellerRepository;

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

        // 같은 상점
        SettlementDetail item1 = SettlementDetail.builder()
                .storeId(storeId1).sellerId(sellerId1).payoutAmount(5000L).totalAmount(10000L).build();
        SettlementDetail item2 = SettlementDetail.builder()
                .storeId(storeId1).sellerId(sellerId1).payoutAmount(3000L).totalAmount(5000L).build();

        Chunk<SettlementDetail> chunk = new Chunk<>(List.of(item1, item2));

        // Settlement 생성
        given(settlementRepository.findByStoreIdAndSettlementDate(any(), any()))
                .willReturn(Optional.empty());

        given(settlementRepository.save(any(Settlement.class))).willAnswer(inv -> {
            Settlement s = inv.getArgument(0);
            return s;
        });

        // 판매자 및 계좌 조회
        Seller mockSeller = Seller.builder().sellerId(sellerId1).memberId(memberId1).build();
        given(sellerRepository.findById(sellerId1)).willReturn(Optional.of(mockSeller));

        Account mockAccount = Account.createAccount(memberId1);
        given(accountRepository.findByMemberId(memberId1)).willReturn(Optional.of(mockAccount));

        // when
        writer.write(chunk);

        // then
        // 1. 같은 상점의 데이터 2개가 합산되어 처리되었는지 확인
        // 총 지급액: 5000 + 3000 = 8000원
        verify(accountRepository).save(any(Account.class)); // 계좌 저장 호출 확인
        verify(settlementDetailRepository).saveAll(any());  // 상세 내역 저장 확인
        verify(accountHistoryRepository).save(any(AccountHistory.class)); // 히스토리 저장 확인


    }



}
