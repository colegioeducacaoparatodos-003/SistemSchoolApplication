package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;

import jakarta.persistence.Column;
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

@Entity
@Table(name = "evaluation")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkEvaluation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_evaluation_discipline"))
    private Discipline discipline;

    @Column(nullable = false)
    private String evaluationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationType evaluationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimester trimester;

    private LocalDate evaluationDate;
    private String anoLectivo;

    // AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.evaluationDate == null) {
            this.evaluationDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void onUpdate() {   // <-- ALTERADO: protected → public
        this.updatedAt = LocalDateTime.now();
    }

    public Evaluation() {}

    public Evaluation(Long pkEvaluation, Discipline discipline, String evaluationName, EvaluationType evaluationType,
                      Trimester trimester, LocalDate evaluationDate, String anoLectivo,
                      String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkEvaluation = pkEvaluation;
        this.discipline = discipline;
        this.evaluationName = evaluationName;
        this.evaluationType = evaluationType;
        this.trimester = trimester;
        this.evaluationDate = evaluationDate;
        this.anoLectivo = anoLectivo;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkEvaluation() { return pkEvaluation; }
    public void setPkEvaluation(Long pkEvaluation) { this.pkEvaluation = pkEvaluation; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public String getEvaluationName() { return evaluationName; }
    public void setEvaluationName(String evaluationName) { this.evaluationName = evaluationName; }

    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }

    public Trimester getTrimester() { return trimester; }
    public void setTrimester(Trimester trimester) { this.trimester = trimester; }

    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Evaluation pkEvaluation(Long pkEvaluation) { setPkEvaluation(pkEvaluation); return this; }
    public Evaluation discipline(Discipline discipline) { setDiscipline(discipline); return this; }
    public Evaluation evaluationName(String evaluationName) { setEvaluationName(evaluationName); return this; }
    public Evaluation evaluationType(EvaluationType evaluationType) { setEvaluationType(evaluationType); return this; }
    public Evaluation trimester(Trimester trimester) { setTrimester(trimester); return this; }
    public Evaluation evaluationDate(LocalDate evaluationDate) { setEvaluationDate(evaluationDate); return this; }
    public Evaluation anoLectivo(String anoLectivo) { setAnoLectivo(anoLectivo); return this; }
    public Evaluation obs(String obs) { setObs(obs); return this; }
    public Evaluation createdAt(LocalDateTime createdAt) { setCreatedAt(createdAt); return this; }
    public Evaluation updatedAt(LocalDateTime updatedAt) { setUpdatedAt(updatedAt); return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evaluation)) return false;
        Evaluation that = (Evaluation) o;
        return Objects.equals(pkEvaluation, that.pkEvaluation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkEvaluation);
    }
}