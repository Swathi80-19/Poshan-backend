package com.poshan.backend.repository;

import com.poshan.backend.entity.EmailVerificationToken;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    List<EmailVerificationToken> findAllByMemberAndUsedAtIsNull(Member member);

    List<EmailVerificationToken> findAllByNutritionistAndUsedAtIsNull(Nutritionist nutritionist);
}
