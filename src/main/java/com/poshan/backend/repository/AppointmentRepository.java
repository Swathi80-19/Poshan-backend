package com.poshan.backend.repository;

import com.poshan.backend.entity.Appointment;
import com.poshan.backend.enums.AppointmentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByNutritionistIdOrderByScheduledAtAsc(Long nutritionistId);

    List<Appointment> findAllByNutritionistIdOrderByScheduledAtDesc(Long nutritionistId);

    List<Appointment> findAllByNutritionistIdAndStatusOrderByScheduledAtAsc(Long nutritionistId, AppointmentStatus status);

    List<Appointment> findAllByMemberIdOrderByScheduledAtAsc(Long memberId);

    List<Appointment> findAllByMemberIdOrderByScheduledAtDesc(Long memberId);

    boolean existsByMemberIdAndNutritionistId(Long memberId, Long nutritionistId);

    java.util.Optional<Appointment> findFirstByMemberIdAndNutritionistIdOrderByScheduledAtDesc(Long memberId, Long nutritionistId);

    void deleteAllByMemberId(Long memberId);
}
