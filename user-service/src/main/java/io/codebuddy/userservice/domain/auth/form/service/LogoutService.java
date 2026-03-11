package io.codebuddy.userservice.domain.auth.form.service;

import io.codebuddy.userservice.domain.auth.token.domain.RefreshToken;
import io.codebuddy.userservice.domain.auth.token.repository.TokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LogoutService {

    // private final MemberRepository memberRepository;

//    private final RefreshTokenRepository refreshTokenRepository;
//    private final RefreshTokenBlackListRepository blackListRepository;

    private final TokenRepository tokenRepository;
    private final EntityManager entityManager;



    @Transactional
    public boolean signOut(Long targetCode) {

        RefreshToken findRefreshToken = tokenRepository.findValidRefToken(targetCode)
                .orElseThrow(NoSuchElementException::new);

        tokenRepository.addBlackList(findRefreshToken);

        entityManager.flush();

        return true;
    }

    @Transactional
    public void deleteRefreshToken(Long targetCode) {

        RefreshToken findRefreshToken = tokenRepository.findRefreshTokenById(targetCode)
                .orElseThrow(NoSuchElementException::new);

        tokenRepository.delete(findRefreshToken);

        entityManager.flush();

    }


}
