package io.codebuddy.closetbuddy.domain.sellers.model.entity;

import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "seller",
    uniqueConstraints = @UniqueConstraint(name = "uk_seller_member", columnNames = "id"))
@NoArgsConstructor
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private Member member;
    @Column(name = "seller_name")
    private String sellerName;
    //연속 cascade 추후 구현
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Store> stores = new ArrayList<>();

    @Builder
    public Seller(Long sellerId, Member member, String sellerName) {
        this.sellerId = sellerId;
        this.member = member;
        this.sellerName = sellerName;
    }

    //update 메서드 로직
    public void update(String sellerName) {
        this.sellerName = sellerName;
    }
}
