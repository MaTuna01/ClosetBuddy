package io.codebuddy.closetbuddy.domain.pay.accounts.repository;

import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.DepositCharge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositChargeRepository extends JpaRepository<DepositCharge,Long> {
}
