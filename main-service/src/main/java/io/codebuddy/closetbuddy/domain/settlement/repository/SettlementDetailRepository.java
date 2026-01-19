package io.codebuddy.closetbuddy.domain.settlement.repository;

import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {
}
