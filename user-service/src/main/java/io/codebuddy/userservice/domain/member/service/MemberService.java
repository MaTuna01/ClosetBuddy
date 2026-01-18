package io.codebuddy.userservice.domain.member.service;

import io.codebuddy.userservice.domain.common.model.entity.Member;
import io.codebuddy.userservice.domain.common.repository.MemberRepository;
import io.codebuddy.userservice.domain.common.repository.RefreshTokenRepository;
import io.codebuddy.userservice.domain.member.model.dto.MemberResponse;
import io.codebuddy.userservice.domain.member.model.dto.MemberUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
        return new MemberResponse(m.getId(),
                m.getMemberId(),
                m.getUsername(),
                m.getEmail(),
                m.getAddress(),
                m.getPhone(),
                m.getRole().name());
    }

    public MemberResponse updateMe(Long memberId, MemberUpdateRequest req) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        if (req.name() != null) m.setUsername(req.name());
        if (req.email() != null) m.setEmail(req.email());
        if (req.phone() != null)   m.setPhone(req.phone());
        if (req.address() != null) m.setAddress(req.address());

        return new MemberResponse(m.getId(),
                m.getMemberId(),
                m.getUsername(),
                m.getEmail(),
                m.getAddress(),
                m.getPhone(),
                m.getRole().name());
    }

    public void deleteMe(Long memberId) {
        refreshTokenRepository.deleteAllByMember_Id(memberId); // 1) 자식 먼저 삭제
        memberRepository.deleteById(memberId); // 2) 부모 삭제
    }

//    /*
//    판매자 등록
//    return: 판매자로 등록에 성공했으니, 생성된 ID가 무엇인지 사용자에게 알려주겠다는 의미로 ID 값을 반환
//     */
//    public Long registerSeller(Long memberId, SellerUpsertRequest req) {
//
//        //1. 회원 조회
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
//
//        // 2. 이미 판매자인지 검증
//        if (sellerJpaRepository.existsByMemberId(memberId)) {
//            throw new IllegalStateException("이미 판매자입니다.");
//        }
//
//        // 3. Member의 권한 변경
//        member.setRole(Role.SELLER);
//
//        // 4. Seller 엔티티 생성 및 저장
//        Seller seller = Seller.builder()
//                .member(member)
//                .sellerName(req.sellerName())
//                .build();
//
//        return sellerJpaRepository.save(seller).getSellerId();
//
//    }

}
