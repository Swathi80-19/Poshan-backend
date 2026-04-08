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
public class ActivityLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String dayLabel;
    private Integer steps;
    private Integer activeMinutes;
    private Integer water;
    private Double sleepHours;
    private Integer sleepQuality;
    private Double weight;
    private String mood;
    private String notes;
    private LocalDateTime loggedAt;
}
