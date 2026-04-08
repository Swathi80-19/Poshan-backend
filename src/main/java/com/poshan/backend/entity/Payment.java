package com.poshan.backend.entity;

import com.poshan.backend.enums.PaymentStatus;
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
public class Payment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String planId;
    private String planLabel;
    private Integer amount;
    private Integer total;
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;
}
