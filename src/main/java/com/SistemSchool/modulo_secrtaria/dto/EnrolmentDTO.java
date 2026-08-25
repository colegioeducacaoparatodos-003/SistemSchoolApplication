package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_secrtaria.io.EnrolmentType;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;

public class EnrolmentDTO {

    private Long phEnrolment;
    private String enrolmentNumber;        // ← corrigido
    private ShiftType shift;
    private EnrolmentType enrolmentType;

    private Long studentPk;
    private String studentFullName;
    private String studentNumber;

    private Long schoolClassPk;            // ← corrigido
    private String schoolClassName;        // ← corrigido
    private String schoolClassCode;        // ← corrigido

    private LocalDate enrolmentData;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EnrolmentDTO() {}

    public EnrolmentDTO(Long phEnrolment, String enrolmentNumber, ShiftType shift, 
        EnrolmentType enrolmentType, Long studentPk, String studentFullName, 
        String studentNumber, Long schoolClassPk, String schoolClassName, 
        String schoolClassCode, LocalDate enrolmentData, String obs, 
        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.phEnrolment = phEnrolment;
        this.enrolmentNumber = enrolmentNumber;
        this.shift = shift;
        this.enrolmentType = enrolmentType;
        this.studentPk = studentPk;
        this.studentFullName = studentFullName;
        this.studentNumber = studentNumber;
        this.schoolClassPk = schoolClassPk;
        this.schoolClassName = schoolClassName;
        this.schoolClassCode = schoolClassCode;
        this.enrolmentData = enrolmentData;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EnrolmentDTO fromEntity(Enrolment enrolment) {
        return new EnrolmentDTO(
                enrolment.getPhEnrolment(),
                enrolment.getEnrolmentNumer(),
                enrolment.getShift(),
                enrolment.getEnrolmentType(),
                enrolment.getStudent() != null ? enrolment.getStudent().getPkStudent() : null,
                enrolment.getStudent() != null ? enrolment.getStudent().getFullName() : null,
               enrolment.getStudent() != null ? enrolment.getStudent().getSudentNumber() : null,
                enrolment.getSchoolClass() != null ? enrolment.getSchoolClass().getPkSchoolClass() : null,
                enrolment.getSchoolClass() != null ? enrolment.getSchoolClass().getClassName() : null,
                enrolment.getSchoolClass() != null ? enrolment.getSchoolClass().getClassCode() : null,
                enrolment.getEnrolmentData(),
                enrolment.getObs(),
                enrolment.getCreatedAt(),
                enrolment.getUpdatedAt());
    }

    // Getters e Setters corrigidos
    public Long getPhEnrolment() { return phEnrolment; }
    public void setPhEnrolment(Long phEnrolment) { this.phEnrolment = phEnrolment; }

    public String getEnrolmentNumber() { return enrolmentNumber; }        // ← corrigido
    public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

    public ShiftType getShift() { return shift; }
    public void setShift(ShiftType shift) { this.shift = shift; }

    public EnrolmentType getEnrolmentType() { return enrolmentType; }
    public void setEnrolmentType(EnrolmentType enrolmentType) { this.enrolmentType = enrolmentType; }

    public Long getStudentPk() { return studentPk; }
    public void setStudentPk(Long studentPk) { this.studentPk = studentPk; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public Long getSchoolClassPk() { return schoolClassPk; }              // ← corrigido
    public void setSchoolClassPk(Long schoolClassPk) { this.schoolClassPk = schoolClassPk; }

    public String getSchoolClassName() { return schoolClassName; }        // ← corrigido
    public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

    public String getSchoolClassCode() { return schoolClassCode; }        // ← corrigido
    public void setSchoolClassCode(String schoolClassCode) { this.schoolClassCode = schoolClassCode; }

    public LocalDate getEnrolmentData() { return enrolmentData; }
    public void setEnrolmentData(LocalDate enrolmentData) { this.enrolmentData = enrolmentData; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}