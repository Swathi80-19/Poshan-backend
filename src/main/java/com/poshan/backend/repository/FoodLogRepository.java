package com.poshan.backend.repository;

import com.poshan.backend.entity.FoodLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findAllByMemberIdOrderByLoggedAtDesc(Long memberId);
}
