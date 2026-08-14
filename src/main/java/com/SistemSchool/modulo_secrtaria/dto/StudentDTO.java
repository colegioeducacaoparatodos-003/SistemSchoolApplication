package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;

public class StudentDTO {

    private Long pkStudent;

    private String sudentNumber;
    private String fristName;
    private String lastName;
    private String fullName;

    private Gender gender;

    private String biNumber;
    private LocalDate nascDate;
    private LocalDate biExpiryData;

    private String addressStreet;
    private String addressProvice;

    private String nameFather;
    private String nameMather;

    private String email;
    private String phone_1;
    private String phone_2;

    private String uploadPhoto;
    private StudentStatus status;

    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudentDTO() {
    }

    public StudentDTO(Long pkStudent, String sudentNumber, String fristName, String lastName, String fullName,
            Gender gender, String biNumber, LocalDate nascDate, LocalDate biExpiryData,
            String addressStreet, String addressProvice, String nameFather, String nameMather,
            String email, String phone_1, String phone_2, String uploadPhoto, StudentStatus status,
            String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (Student → DTO)
    // ─────────────────────────────────────────────────────────────

    public static StudentDTO fromEntity(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentDTO(
                student.getPkStudent(),
                student.getSudentNumber(),
                student.getFristName(),
                student.getLastName(),
                student.getFullName(),
                student.getGender(),
                student.getBiNumber(),
                student.getNascDate(),
                student.getBiExpiryData(),
                student.getAddressStreet(),
                student.getAddressProvice(),
                student.getNameFather(),
                student.getNameMather(),
                student.getEmail(),
                student.getPhone_1(),
                student.getPhone_2(),
                student.getUploadPhoto(),
                student.getStatus(),
                student.getObs(),
                student.getCreatedAt(),
                student.getUpdatedAt());
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // FLUENT SETTERS
    // ─────────────────────────────────────────────────────────────

    public StudentDTO pkStudent(Long pkStudent) {
        setPkStudent(pkStudent);
        return this;
    }

    public StudentDTO sudentNumber(String sudentNumber) {
        setSudentNumber(sudentNumber);
        return this;
    }

    public StudentDTO fristName(String fristName) {
        setFristName(fristName);
        return this;
    }

    public StudentDTO lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public StudentDTO fullName(String fullName) {
        setFullName(fullName);
        return this;
    }

    public StudentDTO gender(Gender gender) {
        setGender(gender);
        return this;
    }

    public StudentDTO biNumber(String biNumber) {
        setBiNumber(biNumber);
        return this;
    }

    public StudentDTO nascDate(LocalDate nascDate) {
        setNascDate(nascDate);
        return this;
    }

    public StudentDTO biExpiryData(LocalDate biExpiryData) {
        setBiExpiryData(biExpiryData);
        return this;
    }

    public StudentDTO addressStreet(String addressStreet) {
        setAddressStreet(addressStreet);
        return this;
    }

    public StudentDTO addressProvice(String addressProvice) {
        setAddressProvice(addressProvice);
        return this;
    }

    public StudentDTO nameFather(String nameFather) {
        setNameFather(nameFather);
        return this;
    }

    public StudentDTO nameMather(String nameMather) {
        setNameMather(nameMather);
        return this;
    }

    public StudentDTO email(String email) {
        setEmail(email);
        return this;
    }

    public StudentDTO phone_1(String phone_1) {
        setPhone_1(phone_1);
        return this;
    }

    public StudentDTO phone_2(String phone_2) {
        setPhone_2(phone_2);
        return this;
    }

    public StudentDTO uploadPhoto(String uploadPhoto) {
        setUploadPhoto(uploadPhoto);
        return this;
    }

    public StudentDTO status(StudentStatus status) {
        setStatus(status);
        return this;
    }

    public StudentDTO obs(String obs) {
        setObs(obs);
        return this;
    }

    public StudentDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public StudentDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}