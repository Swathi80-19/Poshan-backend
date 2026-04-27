package com.poshan.backend.repository;

import com.poshan.backend.entity.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByMemberIdAndNutritionistIdOrderByCreatedAtAsc(Long memberId, Long nutritionistId);

    Optional<ChatMessage> findFirstByMemberIdAndNutritionistIdOrderByCreatedAtDesc(Long memberId, Long nutritionistId);
}
