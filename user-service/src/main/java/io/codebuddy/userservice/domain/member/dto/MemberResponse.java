package io.codebuddy.userservice.domain.member.dto;

public record MemberResponse(Long id, String memberId, String name, String email, String address, String phone, String role) {
}
