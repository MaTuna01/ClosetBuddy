package io.codebuddy.payservice.domain.pay.accounts.repository;

import io.codebuddy.payservice.domain.pay.accounts.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByMemberId(Long memberId);
}