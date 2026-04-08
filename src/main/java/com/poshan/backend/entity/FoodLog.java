package com.poshan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FoodLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String dayLabel;
    private String mealType;
    private String foodName;
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fats;
    private Integer fiber;
    private LocalDateTime loggedAt;
}
