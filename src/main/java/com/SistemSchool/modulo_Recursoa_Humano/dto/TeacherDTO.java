package com.SistemSchool.modulo_Recursoa_Humano.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;

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

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (Teacher → DTO)
    // ─────────────────────────────────────────────────────────────

    public static TeacherDTO fromEntity(Teacher teacher) {
        return new TeacherDTO(
                teacher.getPkTeacher(),
                teacher.getTeacherNumber(),
                teacher.getFristName(),
                teacher.getLastName(),
                teacher.getGender(),
                teacher.getQualificationLivel(),
                teacher.getContractType(),
                teacher.getStatus(),
                teacher.getPhotoPhath(),
                teacher.getBiNumber(),
                teacher.getBiExpiryDate(),
                teacher.getAddressStreet(),
                teacher.getAddressProvice(),
                teacher.getBaseSalary(),
                teacher.getEmail(),
                teacher.getPhone(),
                teacher.getMobilePhone(),
                teacher.getObs(),
                teacher.getCreatedAt(),
                teacher.getUpdatedAt());
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

    // ─────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────

    public String getDisplayName() {
        return this.fristName + " " + this.lastName;
    }

    public TeacherDTO pkTeacher(Long pkTeacher) {
        setPkTeacher(pkTeacher);
        return this;
    }

    public TeacherDTO teacherNumber(String teacherNumber) {
        setTeacherNumber(teacherNumber);
        return this;
    }

    public TeacherDTO fristName(String fristName) {
        setFristName(fristName);
        return this;
    }

    public TeacherDTO lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public TeacherDTO gender(Gender gender) {
        setGender(gender);
        return this;
    }

    public TeacherDTO qualificationLivel(QualificationLevel qualificationLivel) {
        setQualificationLivel(qualificationLivel);
        return this;
    }

    public TeacherDTO contractType(ContractType contractType) {
        setContractType(contractType);
        return this;
    }

    public TeacherDTO status(TeacherStatus status) {
        setStatus(status);
        return this;
    }

    public TeacherDTO photoPhath(String photoPhath) {
        setPhotoPhath(photoPhath);
        return this;
    }

    public TeacherDTO biNumber(String biNumber) {
        setBiNumber(biNumber);
        return this;
    }

    public TeacherDTO biExpiryDate(LocalDate biExpiryDate) {
        setBiExpiryDate(biExpiryDate);
        return this;
    }

    public TeacherDTO addressStreet(String addressStreet) {
        setAddressStreet(addressStreet);
        return this;
    }

    public TeacherDTO addressProvice(String addressProvice) {
        setAddressProvice(addressProvice);
        return this;
    }

    public TeacherDTO baseSalary(BigDecimal baseSalary) {
        setBaseSalary(baseSalary);
        return this;
    }

    public TeacherDTO email(String email) {
        setEmail(email);
        return this;
    }

    public TeacherDTO phone(String phone) {
        setPhone(phone);
        return this;
    }

    public TeacherDTO mobilePhone(String mobilePhone) {
        setMobilePhone(mobilePhone);
        return this;
    }

    public TeacherDTO obs(String obs) {
        setObs(obs);
        return this;
    }

    public TeacherDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public TeacherDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}