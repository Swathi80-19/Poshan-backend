package com.poshan.backend.repository;

import com.poshan.backend.entity.Nutritionist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionistRepository extends JpaRepository<Nutritionist, Long> {

    java.util.List<Nutritionist> findAllByOrderByCreatedAtDesc();

    Optional<Nutritionist> findByEmailIgnoreCase(String email);

    Optional<Nutritionist> findByUsernameIgnoreCase(String username);
}
