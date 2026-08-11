package com.SistemSchool.modulo_Recursoa_Humano.model;

import java.beans.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.util.Objects;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkTeacher;
    private String teacherNumber;

    private String fristName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private QualificationLevel qualificationLivel;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private TeacherStatus status;
    private String photoPhath;

    private String biNumber;
    private LocalDate biExpiryDate;

    // Morada do Estudante
    private String addressStreet;
    private String addressProvice;

    /** Salário base bruto */
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "base_salary", precision = 12, scale = 2)
    private BigDecimal baseSalary;

    // ─────────────────────────────────────────────────────────────
    // CONTACTO
    // ─────────────────────────────────────────────────────────────
    private String email;
    private String phone;
    private String mobilePhone;
    
    // AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    @Transient
    public String getDisplayName() {
        return fristName + " " + lastName;
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────────────────────────


    public Teacher() {
    }

    public Teacher(Long pkTeacher, String teacherNumber, String fristName, String lastName, Gender gender, QualificationLevel qualificationLivel, ContractType contractType, TeacherStatus status, String photoPhath, String biNumber, LocalDate biExpiryDate, String addressStreet, String addressProvice, BigDecimal baseSalary, String email, String phone, String mobilePhone, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public Teacher pkTeacher(Long pkTeacher) {
        setPkTeacher(pkTeacher);
        return this;
    }

    public Teacher teacherNumber(String teacherNumber) {
        setTeacherNumber(teacherNumber);
        return this;
    }

    public Teacher fristName(String fristName) {
        setFristName(fristName);
        return this;
    }

    public Teacher lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public Teacher gender(Gender gender) {
        setGender(gender);
        return this;
    }

    public Teacher qualificationLivel(QualificationLevel qualificationLivel) {
        setQualificationLivel(qualificationLivel);
        return this;
    }

    public Teacher contractType(ContractType contractType) {
        setContractType(contractType);
        return this;
    }

    public Teacher status(TeacherStatus status) {
        setStatus(status);
        return this;
    }

    public Teacher photoPhath(String photoPhath) {
        setPhotoPhath(photoPhath);
        return this;
    }

    public Teacher biNumber(String biNumber) {
        setBiNumber(biNumber);
        return this;
    }

    public Teacher biExpiryDate(LocalDate biExpiryDate) {
        setBiExpiryDate(biExpiryDate);
        return this;
    }

    public Teacher addressStreet(String addressStreet) {
        setAddressStreet(addressStreet);
        return this;
    }

    public Teacher addressProvice(String addressProvice) {
        setAddressProvice(addressProvice);
        return this;
    }

    public Teacher baseSalary(BigDecimal baseSalary) {
        setBaseSalary(baseSalary);
        return this;
    }

    public Teacher email(String email) {
        setEmail(email);
        return this;
    }

    public Teacher phone(String phone) {
        setPhone(phone);
        return this;
    }

    public Teacher mobilePhone(String mobilePhone) {
        setMobilePhone(mobilePhone);
        return this;
    }

    public Teacher obs(String obs) {
        setObs(obs);
        return this;
    }

    public Teacher createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Teacher updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Teacher)) {
            return false;
        }
        Teacher teacher = (Teacher) o;
        return Objects.equals(pkTeacher, teacher.pkTeacher) && Objects.equals(teacherNumber, teacher.teacherNumber) && Objects.equals(fristName, teacher.fristName) && Objects.equals(lastName, teacher.lastName) && Objects.equals(gender, teacher.gender) && Objects.equals(qualificationLivel, teacher.qualificationLivel) && Objects.equals(contractType, teacher.contractType) && Objects.equals(status, teacher.status) && Objects.equals(photoPhath, teacher.photoPhath) && Objects.equals(biNumber, teacher.biNumber) && Objects.equals(biExpiryDate, teacher.biExpiryDate) && Objects.equals(addressStreet, teacher.addressStreet) && Objects.equals(addressProvice, teacher.addressProvice) && Objects.equals(baseSalary, teacher.baseSalary) && Objects.equals(email, teacher.email) && Objects.equals(phone, teacher.phone) && Objects.equals(mobilePhone, teacher.mobilePhone) && Objects.equals(obs, teacher.obs) && Objects.equals(createdAt, teacher.createdAt) && Objects.equals(updatedAt, teacher.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkTeacher, teacherNumber, fristName, lastName, gender, qualificationLivel, contractType, status, photoPhath, biNumber, biExpiryDate, addressStreet, addressProvice, baseSalary, email, phone, mobilePhone, obs, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " pkTeacher='" + getPkTeacher() + "'" +
            ", teacherNumber='" + getTeacherNumber() + "'" +
            ", fristName='" + getFristName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", gender='" + getGender() + "'" +
            ", qualificationLivel='" + getQualificationLivel() + "'" +
            ", contractType='" + getContractType() + "'" +
            ", status='" + getStatus() + "'" +
            ", photoPhath='" + getPhotoPhath() + "'" +
            ", biNumber='" + getBiNumber() + "'" +
            ", biExpiryDate='" + getBiExpiryDate() + "'" +
            ", addressStreet='" + getAddressStreet() + "'" +
            ", addressProvice='" + getAddressProvice() + "'" +
            ", baseSalary='" + getBaseSalary() + "'" +
            ", email='" + getEmail() + "'" +
            ", phone='" + getPhone() + "'" +
            ", mobilePhone='" + getMobilePhone() + "'" +
            ", obs='" + getObs() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }    
    
}
