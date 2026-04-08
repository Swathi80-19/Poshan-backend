package com.poshan.backend.repository;

import com.poshan.backend.entity.AuthToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenAndRevokedFalse(String token);

    void deleteAllByMemberId(Long memberId);
}
