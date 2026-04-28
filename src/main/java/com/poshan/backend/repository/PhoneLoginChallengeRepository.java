package com.poshan.backend.repository;

import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.entity.PhoneLoginChallenge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneLoginChallengeRepository extends JpaRepository<PhoneLoginChallenge, Long> {

    Optional<PhoneLoginChallenge> findByChallengeId(String challengeId);

    List<PhoneLoginChallenge> findAllByMemberAndUsedAtIsNull(Member member);

    List<PhoneLoginChallenge> findAllByNutritionistAndUsedAtIsNull(Nutritionist nutritionist);
}
