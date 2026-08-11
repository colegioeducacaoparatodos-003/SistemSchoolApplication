package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.EvaluationStatus;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "evaluation")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkEvaluation;

    /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_evaluation_discipline"))
    private Discipline discipline;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_evaluation_schedule"))
    private Schedule schedule;

    private String title;

    @Enumerated(EnumType.STRING)
    private EvaluationType type;

    private Double weight;

    private LocalDate evaluationDate;

    @Enumerated(EnumType.STRING)
    private EvaluationStatus status;

    private Integer trimester;

    
    // =======================================
    // Auditoria
    // =======================================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = EvaluationStatus.OPEN;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Evaluation() {
    }

    public Evaluation(Long pkEvaluation, Discipline discipline, 
                      Schedule schedule, String title, EvaluationType type, 
                      Double weight, LocalDate evaluationDate, EvaluationStatus status, 
                      Integer trimester, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.pkEvaluation = pkEvaluation;
        this.discipline = discipline;
        this.schedule = schedule;
        this.title = title;
        this.type = type;
        this.weight = weight;
        this.evaluationDate = evaluationDate;
        this.status = status;
        this.trimester = trimester;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
    }

    public Long getPkEvaluation() {
        return this.pkEvaluation;
    }

    public void setPkEvaluation(Long phEvaluation) {
        this.pkEvaluation = phEvaluation;
    }

    public Discipline getDiscipline() {
        return this.discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EvaluationType getType() {
        return this.type;
    }

    public void setType(EvaluationType type) {
        this.type = type;
    }

    public Double getWeight() {
        return this.weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDate getEvaluationDate() {
        return this.evaluationDate;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public EvaluationStatus getStatus() {
        return this.status;
    }

    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }

    public Integer getTrimester() {
        return this.trimester;
    }

    public void setTrimester(Integer trimester) {
        this.trimester = trimester;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Evaluation pkEvaluation(Long phEvaluation) {
        setPkEvaluation(phEvaluation);
        return this;
    }

    public Evaluation discipline(Discipline discipline) {
        setDiscipline(discipline);
        return this;
    }

    public Evaluation schedule(Schedule schedule) {
        setSchedule(schedule);
        return this;
    }

    public Evaluation title(String title) {
        setTitle(title);
        return this;
    }

    public Evaluation type(EvaluationType type) {
        setType(type);
        return this;
    }

    public Evaluation weight(Double weight) {
        setWeight(weight);
        return this;
    }

    public Evaluation evaluationDate(LocalDate evaluationDate) {
        setEvaluationDate(evaluationDate);
        return this;
    }

    public Evaluation status(EvaluationStatus status) {
        setStatus(status);
        return this;
    }

    public Evaluation trimester(Integer trimester) {
        setTrimester(trimester);
        return this;
    }

    public Evaluation createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Evaluation updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Evaluation)) {
            return false;
        }
        Evaluation evaluation = (Evaluation) o;
        return Objects.equals(pkEvaluation, evaluation.pkEvaluation) && Objects.equals(discipline, evaluation.discipline) && Objects.equals(schedule, evaluation.schedule) && Objects.equals(title, evaluation.title) && Objects.equals(type, evaluation.type) && Objects.equals(weight, evaluation.weight) && Objects.equals(evaluationDate, evaluation.evaluationDate) && Objects.equals(status, evaluation.status) && Objects.equals(trimester, evaluation.trimester) && Objects.equals(createdAt, evaluation.createdAt) && Objects.equals(updatedAt, evaluation.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkEvaluation, discipline, schedule, title, type, weight, evaluationDate, status, trimester, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " phEvaluation='" + getPkEvaluation() + "'" +
            ", discipline='" + getDiscipline() + "'" +
            ", schedule='" + getSchedule() + "'" +
            ", title='" + getTitle() + "'" +
            ", type='" + getType() + "'" +
            ", weight='" + getWeight() + "'" +
            ", evaluationDate='" + getEvaluationDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", trimester='" + getTrimester() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }

}
