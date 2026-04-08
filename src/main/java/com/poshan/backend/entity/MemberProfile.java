package com.poshan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MemberProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    private Integer age;
    private String gender;
    private Double heightCm;
    private Double currentWeightKg;
    private Double targetWeightKg;
    private String activityLevel;
    private String goalFocus;
    private Integer calorieGoal;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatsGoal;
    private Integer fiberGoal;
    private Integer waterGoal;
    private Integer stepGoal;
    private Integer activeMinutesGoal;
    private Double sleepGoal;
}
