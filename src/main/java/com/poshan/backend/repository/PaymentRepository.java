package com.poshan.backend.repository;

import com.poshan.backend.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByMemberIdOrderByPaidAtDesc(Long memberId);

    List<Payment> findAllByNutritionistIdOrderByPaidAtDesc(Long nutritionistId);

    void deleteAllByMemberId(Long memberId);
}
