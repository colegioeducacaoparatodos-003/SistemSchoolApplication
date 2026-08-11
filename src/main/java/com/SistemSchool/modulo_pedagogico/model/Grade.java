package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.GradeStatus;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;

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
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkGrade;

    /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_evaluation"))
    private Evaluation evaluation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_enrolment"))
    private Enrolment enrolment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_student"))
    private Student student;

    private Double value;

    @Enumerated(EnumType.STRING)
    private GradeStatus status;

    private String observation;


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
            this.status = GradeStatus.PENDING;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Grade() {
    }

    public Grade(Long pkGrade, Evaluation evaluation, Enrolment enrolment, Student student, Double value, GradeStatus status, String observation, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkGrade = pkGrade;
        this.evaluation = evaluation;
        this.enrolment = enrolment;
        this.student = student;
        this.value = value;
        this.status = status;
        this.observation = observation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkGrade() {
        return this.pkGrade;
    }

    public void setPkGrade(Long pkGrade) {
        this.pkGrade = pkGrade;
    }

    public Evaluation getEvaluation() {
        return this.evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public Enrolment getEnrolment() {
        return this.enrolment;
    }

    public void setEnrolment(Enrolment enrolment) {
        this.enrolment = enrolment;
    }

    public Student getStudent() {
        return this.student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    public Grade pkGrade(Long pkGrade) {
        setPkGrade(pkGrade);
        return this;
    }

    public Grade evaluation(Evaluation evaluation) {
        setEvaluation(evaluation);
        return this;
    }

    public Grade enrolment(Enrolment enrolment) {
        setEnrolment(enrolment);
        return this;
    }

    public Grade student(Student student) {
        setStudent(student);
        return this;
    }

    public Grade value(Double value) {
        setValue(value);
        return this;
    }

    public Grade status(GradeStatus status) {
        setStatus(status);
        return this;
    }

    public Grade observation(String observation) {
        setObservation(observation);
        return this;
    }

    public Grade createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Grade updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Grade)) {
            return false;
        }
        Grade grade = (Grade) o;
        return Objects.equals(pkGrade, grade.pkGrade) && Objects.equals(evaluation, grade.evaluation) && Objects.equals(enrolment, grade.enrolment) && Objects.equals(student, grade.student) && Objects.equals(value, grade.value) && Objects.equals(status, grade.status) && Objects.equals(observation, grade.observation) && Objects.equals(createdAt, grade.createdAt) && Objects.equals(updatedAt, grade.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkGrade, evaluation, enrolment, student, value, status, observation, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " pkGrade='" + getPkGrade() + "'" +
            ", evaluation='" + getEvaluation() + "'" +
            ", enrolment='" + getEnrolment() + "'" +
            ", student='" + getStudent() + "'" +
            ", value='" + getValue() + "'" +
            ", status='" + getStatus() + "'" +
            ", observation='" + getObservation() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
    
}