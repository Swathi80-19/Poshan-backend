package com.poshan.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Report extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    private String goal;
    private Integer sessionsCompleted;
    private Integer completion;
    private String bmiChange;
    private LocalDate sessionDate;
    private String clinicalNote;
    private String recommendations;

    @ElementCollection
    @CollectionTable(name = "report_goals_met", joinColumns = @JoinColumn(name = "report_id"))
    private List<String> goalsMet = new ArrayList<>();
}
