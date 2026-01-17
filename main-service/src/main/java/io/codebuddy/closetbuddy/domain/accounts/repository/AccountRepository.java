package io.codebuddy.closetbuddy.domain.accounts.repository;

import io.codebuddy.closetbuddy.domain.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByMember(Member member);

    Optional<Account> findByMemberId(Long memberId);
}
