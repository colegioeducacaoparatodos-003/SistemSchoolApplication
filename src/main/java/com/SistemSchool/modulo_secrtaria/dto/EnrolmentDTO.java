package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_secrtaria.io.EnrolmentType;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;

public class EnrolmentDTO {

    private Long phEnrolment;
    private String enrolmentNumer;
    private ShiftType shift;
    private EnrolmentType enrolmentType;

    // Dados "achatados" do Student, para evitar carregar a entidade completa
    // na tabela lazy (mesma lógica usada no StudentTableProjection).
    private Long studentPk;
    private String studentFullName;
    private String studentNumber;

    private Long schoolclassPk;
    private String schoolclassnome;
    private String schoolclasscode;

    private LocalDate enrolmentData;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EnrolmentDTO() {
    }


    public EnrolmentDTO(Long phEnrolment, String enrolmentNumer, ShiftType shift, 
        EnrolmentType enrolmentType, Long studentPk, String studentFullName, 
        String studentNumber, Long schoolclassPk, String schoolclassnome, 
        String schoolclasscode, LocalDate enrolmentData, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.phEnrolment = phEnrolment;
        this.enrolmentNumer = enrolmentNumer;
        this.shift = shift;
        this.enrolmentType = enrolmentType;
        this.studentPk = studentPk;
        this.studentFullName = studentFullName;
        this.studentNumber = studentNumber;
        this.schoolclassPk = schoolclassPk;
        this.schoolclassnome = schoolclassnome;
        this.schoolclasscode = schoolclasscode;
        this.enrolmentData = enrolmentData;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    

    // Construtor sem studentNumber, útil para queries JPQL mais simples
    public EnrolmentDTO(Long phEnrolment, String enrolmentNumer, ShiftType shift, 
        EnrolmentType enrolmentType, Long studentPk, String studentFullName, 
         Long schoolclassPk, String schoolclasscode, LocalDate enrolmentData, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(phEnrolment, enrolmentNumer, shift, enrolmentType, studentPk, studentFullName,
                null, schoolclassPk, null, schoolclasscode,  enrolmentData, obs, createdAt, updatedAt);
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (enrolment → DTO)
    // ─────────────────────────────────────────────────────────────

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


    public Long getPhEnrolment() {
        return this.phEnrolment;
    }

    public void setPhEnrolment(Long phEnrolment) {
        this.phEnrolment = phEnrolment;
    }

    public String getEnrolmentNumer() {
        return this.enrolmentNumer;
    }

    public void setEnrolmentNumer(String enrolmentNumer) {
        this.enrolmentNumer = enrolmentNumer;
    }

    public ShiftType getShift() {
        return this.shift;
    }

    public void setShift(ShiftType shift) {
        this.shift = shift;
    }

    public EnrolmentType getEnrolmentType() {
        return this.enrolmentType;
    }

    public void setEnrolmentType(EnrolmentType enrolmentType) {
        this.enrolmentType = enrolmentType;
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

    public String getStudentNumber() {
        return this.studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public Long getSchoolclassPk() {
        return this.schoolclassPk;
    }

    public void setSchoolclassPk(Long schoolclassPk) {
        this.schoolclassPk = schoolclassPk;
    }

    public String getSchoolclassnome() {
        return this.schoolclassnome;
    }

    public void setSchoolclassnome(String schoolclassnome) {
        this.schoolclassnome = schoolclassnome;
    }

    public String getSchoolclasscode() {
        return this.schoolclasscode;
    }

    public void setSchoolclasscode(String schoolclasscode) {
        this.schoolclasscode = schoolclasscode;
    }

    public LocalDate getEnrolmentData() {
        return this.enrolmentData;
    }

    public void setEnrolmentData(LocalDate enrolmentData) {
        this.enrolmentData = enrolmentData;
    }

    public String getObs() {
        return this.obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
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

}