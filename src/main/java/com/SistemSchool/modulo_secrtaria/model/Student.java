package com.SistemSchool.modulo_secrtaria.model;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkStudent;

    /** Número interno do Aluno(ex: ALU-2026-00456) */
    private String sudentNumber;
    private String fristName;
    private String lastName;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String biNumber;
    private LocalDate nascDate;
    private LocalDate biExpiryData;

    // Morada do Estudante
    private String addressStreet;
    private String addressProvice;

    /** Encarregado de Educação */
    private String nameFather;
    private String nameMather;
    // Contacto dos encarregados
    private String email;
    private String phone_1;
    private String phone_2;

    //FOTO E STATUS
    private String uploadPhoto;    
    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    //AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.fullName == null || this.fullName.isBlank()) {
            this.fullName = this.fristName + " " + this.lastName;
        }
    }

    @Transient
    public String getDisplayName(){
        return fristName + " " + lastName;
    }


    public Student() {
    }

    public Student(Long pkStudent, String sudentNumber, String fristName, String lastName, String fullName, Gender gender, String biNumber, LocalDate nascDate, LocalDate biExpiryData, String addressStreet, String addressProvice, String nameFather, String nameMather, String email, String phone_1, String phone_2, String uploadPhoto, StudentStatus status, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkStudent = pkStudent;
        this.sudentNumber = sudentNumber;
        this.fristName = fristName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.gender = gender;
        this.biNumber = biNumber;
        this.nascDate = nascDate;
        this.biExpiryData = biExpiryData;
        this.addressStreet = addressStreet;
        this.addressProvice = addressProvice;
        this.nameFather = nameFather;
        this.nameMather = nameMather;
        this.email = email;
        this.phone_1 = phone_1;
        this.phone_2 = phone_2;
        this.uploadPhoto = uploadPhoto;
        this.status = status;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkStudent() {
        return this.pkStudent;
    }

    public void setPkStudent(Long pkStudent) {
        this.pkStudent = pkStudent;
    }

    public String getSudentNumber() {
        return this.sudentNumber;
    }

    public void setSudentNumber(String sudentNumber) {
        this.sudentNumber = sudentNumber;
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

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBiNumber() {
        return this.biNumber;
    }

    public void setBiNumber(String biNumber) {
        this.biNumber = biNumber;
    }

    public LocalDate getNascDate() {
        return this.nascDate;
    }

    public void setNascDate(LocalDate nascDate) {
        this.nascDate = nascDate;
    }

    public LocalDate getBiExpiryData() {
        return this.biExpiryData;
    }

    public void setBiExpiryData(LocalDate biExpiryData) {
        this.biExpiryData = biExpiryData;
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

    public String getNameFather() {
        return this.nameFather;
    }

    public void setNameFather(String nameFather) {
        this.nameFather = nameFather;
    }

    public String getNameMather() {
        return this.nameMather;
    }

    public void setNameMather(String nameMather) {
        this.nameMather = nameMather;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_1() {
        return this.phone_1;
    }

    public void setPhone_1(String phone_1) {
        this.phone_1 = phone_1;
    }

    public String getPhone_2() {
        return this.phone_2;
    }

    public void setPhone_2(String phone_2) {
        this.phone_2 = phone_2;
    }

    public String getUploadPhoto() {
        return this.uploadPhoto;
    }

    public void setUploadPhoto(String uploadPhoto) {
        this.uploadPhoto = uploadPhoto;
    }

    public StudentStatus getStatus() {
        return this.status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
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

    public Student pkStudent(Long pkStudent) {
        setPkStudent(pkStudent);
        return this;
    }

    public Student sudentNumber(String sudentNumber) {
        setSudentNumber(sudentNumber);
        return this;
    }

    public Student fristName(String fristName) {
        setFristName(fristName);
        return this;
    }

    public Student lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public Student fullName(String fullName) {
        setFullName(fullName);
        return this;
    }

    public Student gender(Gender gender) {
        setGender(gender);
        return this;
    }

    public Student biNumber(String biNumber) {
        setBiNumber(biNumber);
        return this;
    }

    public Student nascDate(LocalDate nascDate) {
        setNascDate(nascDate);
        return this;
    }

    public Student biExpiryData(LocalDate biExpiryData) {
        setBiExpiryData(biExpiryData);
        return this;
    }

    public Student addressStreet(String addressStreet) {
        setAddressStreet(addressStreet);
        return this;
    }

    public Student addressProvice(String addressProvice) {
        setAddressProvice(addressProvice);
        return this;
    }

    public Student nameFather(String nameFather) {
        setNameFather(nameFather);
        return this;
    }

    public Student nameMather(String nameMather) {
        setNameMather(nameMather);
        return this;
    }

    public Student email(String email) {
        setEmail(email);
        return this;
    }

    public Student phone_1(String phone_1) {
        setPhone_1(phone_1);
        return this;
    }

    public Student phone_2(String phone_2) {
        setPhone_2(phone_2);
        return this;
    }

    public Student uploadPhoto(String uploadPhoto) {
        setUploadPhoto(uploadPhoto);
        return this;
    }

    public Student status(StudentStatus status) {
        setStatus(status);
        return this;
    }

    public Student obs(String obs) {
        setObs(obs);
        return this;
    }

    public Student createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Student updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Student)) {
            return false;
        }
        Student student = (Student) o;
        return Objects.equals(pkStudent, student.pkStudent) && Objects.equals(sudentNumber, student.sudentNumber) && Objects.equals(fristName, student.fristName) && Objects.equals(lastName, student.lastName) && Objects.equals(fullName, student.fullName) && Objects.equals(gender, student.gender) && Objects.equals(biNumber, student.biNumber) && Objects.equals(nascDate, student.nascDate) && Objects.equals(biExpiryData, student.biExpiryData) && Objects.equals(addressStreet, student.addressStreet) && Objects.equals(addressProvice, student.addressProvice) && Objects.equals(nameFather, student.nameFather) && Objects.equals(nameMather, student.nameMather) && Objects.equals(email, student.email) && Objects.equals(phone_1, student.phone_1) && Objects.equals(phone_2, student.phone_2) && Objects.equals(uploadPhoto, student.uploadPhoto) && Objects.equals(status, student.status) && Objects.equals(obs, student.obs) && Objects.equals(createdAt, student.createdAt) && Objects.equals(updatedAt, student.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkStudent, sudentNumber, fristName, lastName, fullName, gender, biNumber, nascDate, biExpiryData, addressStreet, addressProvice, nameFather, nameMather, email, phone_1, phone_2, uploadPhoto, status, obs, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " pkStudent='" + getPkStudent() + "'" +
            ", sudentNumber='" + getSudentNumber() + "'" +
            ", fristName='" + getFristName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", fullName='" + getFullName() + "'" +
            ", gender='" + getGender() + "'" +
            ", biNumber='" + getBiNumber() + "'" +
            ", nascDate='" + getNascDate() + "'" +
            ", biExpiryData='" + getBiExpiryData() + "'" +
            ", addressStreet='" + getAddressStreet() + "'" +
            ", addressProvice='" + getAddressProvice() + "'" +
            ", nameFather='" + getNameFather() + "'" +
            ", nameMather='" + getNameMather() + "'" +
            ", email='" + getEmail() + "'" +
            ", phone_1='" + getPhone_1() + "'" +
            ", phone_2='" + getPhone_2() + "'" +
            ", uploadPhoto='" + getUploadPhoto() + "'" +
            ", status='" + getStatus() + "'" +
            ", obs='" + getObs() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
    


}
