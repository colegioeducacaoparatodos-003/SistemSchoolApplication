package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;

public class EvaluationDTO {

    private Long pkEvaluation;
    private Long disciplinePk;
    private String disciplineName;
    private String evaluationName;
    private EvaluationType evaluationType;
    private Trimester trimester;
    private LocalDate evaluationDate;
    private String anoLectivo;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EvaluationDTO() {}

    public EvaluationDTO(Long pkEvaluation, Long disciplinePk, String disciplineName, String evaluationName,
                         EvaluationType evaluationType, Trimester trimester, LocalDate evaluationDate,
                         String anoLectivo, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkEvaluation = pkEvaluation;
        this.disciplinePk = disciplinePk;
        this.disciplineName = disciplineName;
        this.evaluationName = evaluationName;
        this.evaluationType = evaluationType;
        this.trimester = trimester;
        this.evaluationDate = evaluationDate;
        this.anoLectivo = anoLectivo;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EvaluationDTO fromEntity(Evaluation e) {
        return new EvaluationDTO(
            e.getPkEvaluation(),
            e.getDiscipline() != null ? e.getDiscipline().getPkDiscipline() : null,
            e.getDiscipline() != null ? e.getDiscipline().getDisciplineName() : null,
            e.getEvaluationName(), e.getEvaluationType(), e.getTrimester(),
            e.getEvaluationDate(), e.getAnoLectivo(), e.getObs(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    // Getters e Setters
    public Long getPkEvaluation() { return pkEvaluation; }
    public void setPkEvaluation(Long pkEvaluation) { this.pkEvaluation = pkEvaluation; }

    public Long getDisciplinePk() { return disciplinePk; }
    public void setDisciplinePk(Long disciplinePk) { this.disciplinePk = disciplinePk; }

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

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
}