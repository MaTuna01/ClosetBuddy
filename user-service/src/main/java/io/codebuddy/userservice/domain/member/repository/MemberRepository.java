package io.codebuddy.userservice.domain.member.repository;


import io.codebuddy.userservice.domain.member.domain.Member;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMemberId(String username);

    boolean existsByMemberId(String memberId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

}
