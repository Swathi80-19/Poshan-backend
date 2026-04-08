package com.poshan.backend.repository;

import com.poshan.backend.entity.MemberProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

    Optional<MemberProfile> findByMemberId(Long memberId);
}
