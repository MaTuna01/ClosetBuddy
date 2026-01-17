package io.codebuddy.closetbuddy.domain.form.login.security.auth;

import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// UserDetailsService 를 구현한 클래스
@Service
public class MemberPrincipalDetailService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        System.out.println("username : " + username);
        System.out.println("id : " + member);

        return new MemberDetails(member);
    }
    /*
    // memberRepository.findByUserid(username): Member 테이블에서 userid 컬럼 값이 username 변수 값과 같은 행을 찾아서 그 행을 Member 객체로 매핑해서 반환
     */
}
