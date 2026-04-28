package com.poshan.backend.entity;

import com.poshan.backend.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Nutritionist extends BaseEntity {

    private String name;

    private String username;

    private String email;

    private String phone;

    private Integer experience;

    private Integer age;

    private String passwordHash;

    private String specialization;

    private String bio;

    private Boolean emailVerified = false;

    private LocalDateTime emailVerifiedAt;

    @Enumerated(EnumType.STRING)
    private Role role = Role.NUTRITIONIST;

    private Integer loginCount = 0;

    private LocalDateTime lastLoginAt;
}
