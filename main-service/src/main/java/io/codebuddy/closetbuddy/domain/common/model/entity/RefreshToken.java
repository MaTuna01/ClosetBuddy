package io.codebuddy.closetbuddy.domain.common.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Entity
@Table(name = "Refresh_Table")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @Column(name = "refresh_token_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String refreshToken;
    @Setter
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private final LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public RefreshToken(String refreshToken, Member member) {
        this.refreshToken = refreshToken;
        this.member = member;
    }

    public void rotate(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }
}
