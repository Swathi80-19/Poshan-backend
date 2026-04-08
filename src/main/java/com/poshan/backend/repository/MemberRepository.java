package com.poshan.backend.repository;

import com.poshan.backend.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailIgnoreCase(String email);

    Optional<Member> findByUsernameIgnoreCase(String username);
}
