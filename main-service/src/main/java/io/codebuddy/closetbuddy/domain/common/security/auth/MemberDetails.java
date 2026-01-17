package io.codebuddy.closetbuddy.domain.common.security.auth;

import io.codebuddy.closetbuddy.domain.common.model.dto.Role;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class MemberDetails implements OAuth2User, UserDetails {

    private final Member member;
    private final Map<String, Object> attributes;

    // form 로그인용
    public MemberDetails(Member member) {
        this.member = member;
        this.attributes = Map.of();
    }

    // oauth 로그인용
    public MemberDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    // 편의 메서드
    public Long getId() {
        return member.getId();
    }

    public Member getMember() {
        return member;
    }

    public Role getRole() {
        return member.getRole();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    //UserDetails 에서 사용자 식별하기 위한 메서드
    @Override
    public String getUsername() {
        return member.getMemberId();
    }

    // Oauth2 로그인 부분에서 Google에서 가져온 정보들
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(member.getRole().name()));
    }

    //OAuth2User 계열에서 현재 인증 주체의 이름(식별자)를 돌려주기 위해 쓰이는 메서드
    @Override
    public String getName() {
        return member.getEmail();
    }
}
