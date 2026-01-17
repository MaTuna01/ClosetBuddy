package io.codebuddy.closetbuddy.domain.member.model.dto;

public record MemberUpdateRequest(String name, String email,String phone, String address) {
}
