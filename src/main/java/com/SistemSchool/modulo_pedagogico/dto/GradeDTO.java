package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.GradeStatus;
import com.SistemSchool.modulo_pedagogico.model.Grade;

public class GradeDTO {

    private Long pkGrade;

    // Dados "achatados" da Evaluation (+ Discipline via Evaluation)
    private Long evaluationPk;
    private String evaluationDescription; // "Título - Disciplina"

    // Dados "achatados" do Enrolment
    private Long enrolmentPk;
    private String enrolmentNumber;

    // Dados "achatados" do Student
    private Long studentPk;
    private String studentFullName;

    private Double value;
    private GradeStatus status;
    private String observation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GradeDTO() {
    }

    public GradeDTO(Long pkGrade, Long evaluationPk, String evaluationDescription,
            Long enrolmentPk, String enrolmentNumber, Long studentPk, String studentFullName,
            Double value, GradeStatus status, String observation,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkGrade = pkGrade;
        this.evaluationPk = evaluationPk;
        this.evaluationDescription = evaluationDescription;
        this.enrolmentPk = enrolmentPk;
        this.enrolmentNumber = enrolmentNumber;
        this.studentPk = studentPk;
        this.studentFullName = studentFullName;
        this.value = value;
        this.status = status;
        this.observation = observation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (grade → DTO)
    // ─────────────────────────────────────────────────────────────

    public static GradeDTO fromEntity(Grade grade) {
        String evalDesc = null;
        if (grade.getEvaluation() != null) {
            String title = grade.getEvaluation().getTitle();
            String discipline = grade.getEvaluation().getDiscipline() != null
                    ? grade.getEvaluation().getDiscipline().getDisciplineName()
                    : null;
            evalDesc = discipline != null ? title + " - " + discipline : title;
        }

        return new GradeDTO(
                grade.getPkGrade(),
                grade.getEvaluation() != null ? grade.getEvaluation().getPkEvaluation() : null,
                evalDesc,
                grade.getEnrolment() != null ? grade.getEnrolment().getPhEnrolment() : null,
                grade.getEnrolment() != null ? grade.getEnrolment().getEnrolmentNumer() : null,
                grade.getStudent() != null ? grade.getStudent().getPkStudent() : null,
                grade.getStudent() != null ? grade.getStudent().getFullName() : null,
                grade.getValue(),
                grade.getStatus(),
                grade.getObservation(),
                grade.getCreatedAt(),
                grade.getUpdatedAt());
    }

    public Long getPkGrade() {
        return this.pkGrade;
    }

    public void setPkGrade(Long pkGrade) {
        this.pkGrade = pkGrade;
    }

    public Long getEvaluationPk() {
        return this.evaluationPk;
    }

    public void setEvaluationPk(Long evaluationPk) {
        this.evaluationPk = evaluationPk;
    }

    public String getEvaluationDescription() {
        return this.evaluationDescription;
    }

    public void setEvaluationDescription(String evaluationDescription) {
        this.evaluationDescription = evaluationDescription;
    }

    public Long getEnrolmentPk() {
        return this.enrolmentPk;
    }

    public void setEnrolmentPk(Long enrolmentPk) {
        this.enrolmentPk = enrolmentPk;
    }

    public String getEnrolmentNumber() {
        return this.enrolmentNumber;
    }

    public void setEnrolmentNumber(String enrolmentNumber) {
        this.enrolmentNumber = enrolmentNumber;
    }

    public Long getStudentPk() {
        return this.studentPk;
    }

    public void setStudentPk(Long studentPk) {
        this.studentPk = studentPk;
    }

    public String getStudentFullName() {
        return this.studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public GradeStatus getStatus() {
        return this.status;
    }

    public void setStatus(GradeStatus status) {
        this.status = status;
    }

    public String getObservation() {
        return this.observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
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

    public GradeDTO pkGrade(Long pkGrade) {
        setPkGrade(pkGrade);
        return this;
    }

    public GradeDTO evaluationPk(Long evaluationPk) {
        setEvaluationPk(evaluationPk);
        return this;
    }

    public GradeDTO evaluationDescription(String evaluationDescription) {
        setEvaluationDescription(evaluationDescription);
        return this;
    }

    public GradeDTO enrolmentPk(Long enrolmentPk) {
        setEnrolmentPk(enrolmentPk);
        return this;
    }

    public GradeDTO enrolmentNumber(String enrolmentNumber) {
        setEnrolmentNumber(enrolmentNumber);
        return this;
    }

    public GradeDTO studentPk(Long studentPk) {
        setStudentPk(studentPk);
        return this;
    }

    public GradeDTO studentFullName(String studentFullName) {
        setStudentFullName(studentFullName);
        return this;
    }

    public GradeDTO value(Double value) {
        setValue(value);
        return this;
    }

    public GradeDTO status(GradeStatus status) {
        setStatus(status);
        return this;
    }

    public GradeDTO observation(String observation) {
        setObservation(observation);
        return this;
    }

    public GradeDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public GradeDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}