package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Grade;

public class GradeDTO {

    private Long pkGrade;
    private Long evaluationPk;
    private String evaluationName;
    private EvaluationType evaluationType;
    private Trimester trimester;
    private String disciplineName;

    private Long enrolmentPk;
    private String studentFullName;
    private String studentNumber;
    private String schoolClassName;

    private Double score;
    private LocalDate launchDate;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GradeDTO() {}

    public GradeDTO(Long pkGrade, Long evaluationPk, String evaluationName, EvaluationType evaluationType,
                    Trimester trimester, String disciplineName, Long enrolmentPk, String studentFullName,
                    String studentNumber, String schoolClassName, Double score, LocalDate launchDate,
                    String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkGrade = pkGrade;
        this.evaluationPk = evaluationPk;
        this.evaluationName = evaluationName;
        this.evaluationType = evaluationType;
        this.trimester = trimester;
        this.disciplineName = disciplineName;
        this.enrolmentPk = enrolmentPk;
        this.studentFullName = studentFullName;
        this.studentNumber = studentNumber;
        this.schoolClassName = schoolClassName;
        this.score = score;
        this.launchDate = launchDate;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GradeDTO fromEntity(Grade g) {
        return new GradeDTO(
            g.getPkGrade(),
            g.getEvaluation() != null ? g.getEvaluation().getPkEvaluation() : null,
            g.getEvaluation() != null ? g.getEvaluation().getEvaluationName() : null,
            g.getEvaluation() != null ? g.getEvaluation().getEvaluationType() : null,
            g.getEvaluation() != null ? g.getEvaluation().getTrimester() : null,
            g.getEvaluation() != null && g.getEvaluation().getDiscipline() != null
                ? g.getEvaluation().getDiscipline().getDisciplineName() : null,
            g.getEnrolment() != null ? g.getEnrolment().getPhEnrolment() : null,
            g.getEnrolment() != null && g.getEnrolment().getStudent() != null
                ? g.getEnrolment().getStudent().getFullName() : null,
            g.getEnrolment() != null && g.getEnrolment().getStudent() != null
                ? g.getEnrolment().getStudent().getSudentNumber() : null,
            g.getEnrolment() != null && g.getEnrolment().getSchoolClass() != null
                ? g.getEnrolment().getSchoolClass().getClassName() : null,
            g.getScore(), g.getLaunchDate(), g.getObs(), g.getCreatedAt(), g.getUpdatedAt()
        );
    }

    // Getters e Setters
    public Long getPkGrade() { return pkGrade; }
    public void setPkGrade(Long pkGrade) { this.pkGrade = pkGrade; }

    public Long getEvaluationPk() { return evaluationPk; }
    public void setEvaluationPk(Long evaluationPk) { this.evaluationPk = evaluationPk; }

    public String getEvaluationName() { return evaluationName; }
    public void setEvaluationName(String evaluationName) { this.evaluationName = evaluationName; }

    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }

    public Trimester getTrimester() { return trimester; }
    public void setTrimester(Trimester trimester) { this.trimester = trimester; }

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

    public Long getEnrolmentPk() { return enrolmentPk; }
    public void setEnrolmentPk(Long enrolmentPk) { this.enrolmentPk = enrolmentPk; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getSchoolClassName() { return schoolClassName; }
    public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

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
}