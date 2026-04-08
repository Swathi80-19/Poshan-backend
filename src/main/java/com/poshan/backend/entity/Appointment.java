package com.poshan.backend.entity;

import com.poshan.backend.enums.AppointmentMode;
import com.poshan.backend.enums.AppointmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Appointment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    private LocalDateTime scheduledAt;
    private String dateLabel;
    private String timeLabel;

    @Enumerated(EnumType.STRING)
    private AppointmentMode mode;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String notes;
}
