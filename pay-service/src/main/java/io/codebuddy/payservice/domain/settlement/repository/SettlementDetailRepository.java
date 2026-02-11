package io.codebuddy.payservice.domain.settlement.repository;

import io.codebuddy.payservice.domain.settlement.model.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {
}
