package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.SistemSchool.modulo_secrtaria.model.Enrolment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.beans.Transient;

@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkGrade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_evaluation"))
    private Evaluation evaluation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_enrolment"))
    private Enrolment enrolment;

    @Column(nullable = false)
    private Double score;

    private LocalDate launchDate;

    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.launchDate == null) {
            this.launchDate = LocalDate.now();
        }
    }

    @Transient
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Grade() {}

    public Grade(Long pkGrade, Evaluation evaluation, Enrolment enrolment, Double score, LocalDate launchDate,
                 String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkGrade = pkGrade;
        this.evaluation = evaluation;
        this.enrolment = enrolment;
        this.score = score;
        this.launchDate = launchDate;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkGrade() { return pkGrade; }
    public void setPkGrade(Long pkGrade) { this.pkGrade = pkGrade; }

    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }

    public Enrolment getEnrolment() { return enrolment; }
    public void setEnrolment(Enrolment enrolment) { this.enrolment = enrolment; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Grade pkGrade(Long pkGrade) { setPkGrade(pkGrade); return this; }
    public Grade evaluation(Evaluation evaluation) { setEvaluation(evaluation); return this; }
    public Grade enrolment(Enrolment enrolment) { setEnrolment(enrolment); return this; }
    public Grade score(Double score) { setScore(score); return this; }
    public Grade launchDate(LocalDate launchDate) { setLaunchDate(launchDate); return this; }
    public Grade obs(String obs) { setObs(obs); return this; }
    public Grade createdAt(LocalDateTime createdAt) { setCreatedAt(createdAt); return this; }
    public Grade updatedAt(LocalDateTime updatedAt) { setUpdatedAt(updatedAt); return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Grade)) return false;
        Grade grade = (Grade) o;
        return Objects.equals(pkGrade, grade.pkGrade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkGrade);
    }
}