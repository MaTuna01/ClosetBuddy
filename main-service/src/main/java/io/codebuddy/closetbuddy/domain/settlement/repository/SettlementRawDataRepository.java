package io.codebuddy.closetbuddy.domain.settlement.repository;

import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementRawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRawDataRepository extends JpaRepository<SettlementRawData, Long> {
}
