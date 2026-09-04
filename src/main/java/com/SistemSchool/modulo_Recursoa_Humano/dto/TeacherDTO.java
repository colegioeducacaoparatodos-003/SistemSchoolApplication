package com.SistemSchool.modulo_Recursoa_Humano.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;

public class TeacherDTO {

    private Long pkTeacher;
    private String teacherNumber;

    private String fristName;
    private String lastName;

    private Gender gender;
    private QualificationLevel qualificationLivel;
    private ContractType contractType;
    private TeacherStatus status;

    private String photoPhath;

    private String biNumber;
    private LocalDate biExpiryDate;

    private String addressStreet;
    private String addressProvice;

    private BigDecimal baseSalary;

    private String email;
    private String phone;
    private String mobilePhone;

    private String obs;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TeacherDTO() {
    }

    public TeacherDTO(Long pkTeacher, String teacherNumber, String fristName, String lastName, Gender gender,
            QualificationLevel qualificationLivel, ContractType contractType, TeacherStatus status,
            String photoPhath, String biNumber, LocalDate biExpiryDate, String addressStreet,
            String addressProvice, BigDecimal baseSalary, String email, String phone, String mobilePhone,
            String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkTeacher = pkTeacher;
        this.teacherNumber = teacherNumber;
        this.fristName = fristName;
        this.lastName = lastName;
        this.gender = gender;
        this.qualificationLivel = qualificationLivel;
        this.contractType = contractType;
        this.status = status;
        this.photoPhath = photoPhath;
        this.biNumber = biNumber;
        this.biExpiryDate = biExpiryDate;
        this.addressStreet = addressStreet;
        this.addressProvice = addressProvice;
        this.baseSalary = baseSalary;
        this.email = email;
        this.phone = phone;
        this.mobilePhone = mobilePhone;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Construtor reduzido usado na listagem lazy (tabela de professores) e na exportação. */
    public TeacherDTO(Long pkTeacher, String teacherNumber, String fristName, String lastName,
            QualificationLevel qualificationLivel, ContractType contractType, TeacherStatus status,
            String photoPhath, String email, String phone, LocalDateTime createdAt) {
        this.pkTeacher = pkTeacher;
        this.teacherNumber = teacherNumber;
        this.fristName = fristName;
        this.lastName = lastName;
        this.qualificationLivel = qualificationLivel;
        this.contractType = contractType;
        this.status = status;
        this.photoPhath = photoPhath;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public Long getPkTeacher() {
        return this.pkTeacher;
    }

    public void setPkTeacher(Long pkTeacher) {
        this.pkTeacher = pkTeacher;
    }

    public String getTeacherNumber() {
        return this.teacherNumber;
    }

    public void setTeacherNumber(String teacherNumber) {
        this.teacherNumber = teacherNumber;
    }

    public String getFristName() {
        return this.fristName;
    }

    public void setFristName(String fristName) {
        this.fristName = fristName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public QualificationLevel getQualificationLivel() {
        return this.qualificationLivel;
    }

    public void setQualificationLivel(QualificationLevel qualificationLivel) {
        this.qualificationLivel = qualificationLivel;
    }

    public ContractType getContractType() {
        return this.contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public TeacherStatus getStatus() {
        return this.status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }

    public String getPhotoPhath() {
        return this.photoPhath;
    }

    public void setPhotoPhath(String photoPhath) {
        this.photoPhath = photoPhath;
    }

    public String getBiNumber() {
        return this.biNumber;
    }

    public void setBiNumber(String biNumber) {
        this.biNumber = biNumber;
    }

    public LocalDate getBiExpiryDate() {
        return this.biExpiryDate;
    }

    public void setBiExpiryDate(LocalDate biExpiryDate) {
        this.biExpiryDate = biExpiryDate;
    }

    public String getAddressStreet() {
        return this.addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressProvice() {
        return this.addressProvice;
    }

    public void setAddressProvice(String addressProvice) {
        this.addressProvice = addressProvice;
    }

    public BigDecimal getBaseSalary() {
        return this.baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
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