package com.poshan.backend.repository;

import com.poshan.backend.entity.ActivityLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByMemberIdOrderByLoggedAtDesc(Long memberId);
}
