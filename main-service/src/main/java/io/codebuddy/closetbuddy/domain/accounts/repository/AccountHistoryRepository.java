package io.codebuddy.closetbuddy.domain.accounts.repository;

import io.codebuddy.closetbuddy.domain.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.accounts.model.entity.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory,Long> {

    List<AccountHistory> findByAccountOrderByCreatedAtDesc(Account account);

    Optional<AccountHistory> findByAccountAndAccountHistoryId(Account account, Long accountHistoryId);

    /*
     특정 회원(memberId)의 소유인 계좌 내역 전체를 생성일시 내림차순(최신순)으로 조회
      동작 원리: AccountHistory -> Account -> MemberId를 타고 들어가서 조회
      @param memberId 조회하려는 회원의 식별자 (로그인된 사용자 본인)
      @return 해당 회원의 모든 계좌 내역 리스트
     */

    List<AccountHistory> findByAccount_MemberIdOrderByCreatedAtDesc(Long memberId);

    /*
    특정 내역(historyId)을 조회하되, 반드시 해당 회원의 계좌(memberId)에 속한 내역인지 함께 검증합니다.
    설명: 내역 ID가 존재하더라도, 요청한 회원(memberId)의 소유가 아니면 조회되지 않습니다
    @param memberId 요청한 회원의 식별자 (본인 확인용)
    @param accountHistoryId 조회하려는 내역의 ID
    @return 두 조건(본인 소유 + 내역 ID)이 모두 일치하는 내역
     */
    Optional<AccountHistory> findByAccount_MemberIdAndAccountHistoryId(Long memberId, Long accountHistoryId);

}
