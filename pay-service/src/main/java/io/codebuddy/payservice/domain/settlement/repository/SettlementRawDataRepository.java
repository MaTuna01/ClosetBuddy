package io.codebuddy.payservice.domain.settlement.repository;

import io.codebuddy.payservice.domain.settlement.model.entity.SettlementRawData;
import io.codebuddy.payservice.domain.settlement.model.vo.RawDataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRawDataRepository extends JpaRepository<SettlementRawData, Long> {

    // 정산 완료 시 rawData의 상태를 bulk update 하기 위한 update query
    // Modifying을 사용하여 bulk 연산 수행 시 영속성 컨텍스트 미갱신 문제 발생 가능하므로 트랜잭션 수행 이후 초기화 수행 명시
    @Modifying(clearAutomatically = true) // 영속성 컨텍스트 초기화
    @Query("UPDATE SettlementRawData s SET s.status = :status WHERE s.settlementRawDataId IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") RawDataStatus status);


    // 결제 취소 시 rawData의 상태를 bulk update 하기 위한 update query
    // paymentId에 맞는 모든 rawData를 한번에 update
    @Modifying
    @Query("UPDATE SettlementRawData s SET s.status = :status WHERE s.paymentId = :paymentId")
    void updateStatusByPaymentId(@Param("paymentId") Long paymentId, @Param("status") RawDataStatus status);

}