package com.poshan.backend.repository;

import com.poshan.backend.entity.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByNutritionistIdOrderBySessionDateDesc(Long nutritionistId);

    List<Report> findAllByMemberIdOrderBySessionDateDesc(Long memberId);

    java.util.Optional<Report> findByIdAndNutritionistId(Long id, Long nutritionistId);

    void deleteAllByMemberId(Long memberId);
}
