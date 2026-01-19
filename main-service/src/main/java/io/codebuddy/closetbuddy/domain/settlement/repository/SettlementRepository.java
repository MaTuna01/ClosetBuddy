package io.codebuddy.closetbuddy.domain.settlement.repository;

import io.codebuddy.closetbuddy.domain.settlement.model.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement,Long> {
    Optional<Settlement> findByStoreIdAndSettlementDate(Long storeId, LocalDate settlementDate);
}
