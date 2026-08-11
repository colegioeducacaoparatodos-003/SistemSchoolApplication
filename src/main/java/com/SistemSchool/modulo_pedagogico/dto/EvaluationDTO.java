package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.EvaluationStatus;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;

public class EvaluationDTO {

    private Long pkEvaluation;

    // Dados "achatados" da Discipline, para evitar carregar a entidade completa
    // na tabela lazy (mesma lógica usada no EnrolmentDTO).
    private Long disciplinePk;
    private String disciplineName;

    // Dados "achatados" do Schedule
    private Long schedulePk;
    private WeekDay scheduleWeekDay;

    private String title;
    private EvaluationType type;
    private Double weight;
    private LocalDate evaluationDate;
    private EvaluationStatus status;
    private Integer trimester;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EvaluationDTO() {
    }

    public EvaluationDTO(Long pkEvaluation, Long disciplinePk, String disciplineName,
            Long schedulePk, WeekDay scheduleWeekDay, String title, EvaluationType type,
            Double weight, LocalDate evaluationDate, EvaluationStatus status,
            Integer trimester, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkEvaluation = pkEvaluation;
        this.disciplinePk = disciplinePk;
        this.disciplineName = disciplineName;
        this.schedulePk = schedulePk;
        this.scheduleWeekDay = scheduleWeekDay;
        this.title = title;
        this.type = type;
        this.weight = weight;
        this.evaluationDate = evaluationDate;
        this.status = status;
        this.trimester = trimester;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (evaluation → DTO)
    // ─────────────────────────────────────────────────────────────

    public static EvaluationDTO fromEntity(Evaluation evaluation) {
        return new EvaluationDTO(
                evaluation.getPkEvaluation(),
                evaluation.getDiscipline() != null ? evaluation.getDiscipline().getPkDiscipline() : null,
                evaluation.getDiscipline() != null ? evaluation.getDiscipline().getDisciplineName() : null,
                evaluation.getSchedule() != null ? evaluation.getSchedule().getPkSchedule() : null,
                evaluation.getSchedule() != null ? evaluation.getSchedule().getWeekDay() : null,
                evaluation.getTitle(),
                evaluation.getType(),
                evaluation.getWeight(),
                evaluation.getEvaluationDate(),
                evaluation.getStatus(),
                evaluation.getTrimester(),
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt());
    }

    public Long getPkEvaluation() {
        return this.pkEvaluation;
    }

    public void setPkEvaluation(Long pkEvaluation) {
        this.pkEvaluation = pkEvaluation;
    }

    public Long getDisciplinePk() {
        return this.disciplinePk;
    }

    public void setDisciplinePk(Long disciplinePk) {
        this.disciplinePk = disciplinePk;
    }

    public String getDisciplineName() {
        return this.disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public Long getSchedulePk() {
        return this.schedulePk;
    }

    public void setSchedulePk(Long schedulePk) {
        this.schedulePk = schedulePk;
    }

    public WeekDay getScheduleWeekDay() {
        return this.scheduleWeekDay;
    }

    public void setScheduleWeekDay(WeekDay scheduleWeekDay) {
        this.scheduleWeekDay = scheduleWeekDay;
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

    public EvaluationDTO pkEvaluation(Long pkEvaluation) {
        setPkEvaluation(pkEvaluation);
        return this;
    }

    public EvaluationDTO disciplinePk(Long disciplinePk) {
        setDisciplinePk(disciplinePk);
        return this;
    }

    public EvaluationDTO disciplineName(String disciplineName) {
        setDisciplineName(disciplineName);
        return this;
    }

    public EvaluationDTO schedulePk(Long schedulePk) {
        setSchedulePk(schedulePk);
        return this;
    }

    public EvaluationDTO scheduleWeekDay(WeekDay scheduleWeekDay) {
        setScheduleWeekDay(scheduleWeekDay);
        return this;
    }

    public EvaluationDTO title(String title) {
        setTitle(title);
        return this;
    }

    public EvaluationDTO type(EvaluationType type) {
        setType(type);
        return this;
    }

    public EvaluationDTO weight(Double weight) {
        setWeight(weight);
        return this;
    }

    public EvaluationDTO evaluationDate(LocalDate evaluationDate) {
        setEvaluationDate(evaluationDate);
        return this;
    }

    public EvaluationDTO status(EvaluationStatus status) {
        setStatus(status);
        return this;
    }

    public EvaluationDTO trimester(Integer trimester) {
        setTrimester(trimester);
        return this;
    }

    public EvaluationDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public EvaluationDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}