package com.SistemSchool.modulo_secrtaria.model;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import jakarta.persistence.Table;
import java.util.Objects;

import com.SistemSchool.modulo_secrtaria.io.EnrolmentType;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;

@Entity
@Table(name = "enrolment")
public class Enrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long phEnrolment;
    private String enrolmentNumer;

    @Enumerated(EnumType.STRING)
    private ShiftType shift;
    @Enumerated(EnumType.STRING)
    private EnrolmentType enrolmentType;

    /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name =  "schoolClass_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_enrolment_schoolClass"))
    private SchoolClass schoolClass;

        /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name =  "student_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_enrolment_student"))
    private Student student;

    private LocalDate enrolmentData;
    
    //AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.enrolmentData == null) {
            this.enrolmentData = LocalDate.now();
        }
    }

    @Transient
    public void onUpdateString(){
         this.updatedAt = LocalDateTime.now();
    }


    public Enrolment() {
    }

    public Enrolment(Long phEnrolment, String enrolmentNumer, ShiftType shift, EnrolmentType enrolmentType, SchoolClass schoolClass, Student student, LocalDate enrolmentData, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.phEnrolment = phEnrolment;
        this.enrolmentNumer = enrolmentNumer;
        this.shift = shift;
        this.enrolmentType = enrolmentType;
        this.schoolClass = schoolClass;
        this.student = student;
        this.enrolmentData = enrolmentData;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public SchoolClass getSchoolClass() {
        return this.schoolClass;
    }

    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
    }

    public Student getStudent() {
        return this.student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    public Enrolment phEnrolment(Long phEnrolment) {
        setPhEnrolment(phEnrolment);
        return this;
    }

    public Enrolment enrolmentNumer(String enrolmentNumer) {
        setEnrolmentNumer(enrolmentNumer);
        return this;
    }

    public Enrolment shift(ShiftType shift) {
        setShift(shift);
        return this;
    }

    public Enrolment enrolmentType(EnrolmentType enrolmentType) {
        setEnrolmentType(enrolmentType);
        return this;
    }

    public Enrolment schoolClass(SchoolClass schoolClass) {
        setSchoolClass(schoolClass);
        return this;
    }

    public Enrolment student(Student student) {
        setStudent(student);
        return this;
    }

    public Enrolment enrolmentData(LocalDate enrolmentData) {
        setEnrolmentData(enrolmentData);
        return this;
    }

    public Enrolment obs(String obs) {
        setObs(obs);
        return this;
    }

    public Enrolment createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Enrolment updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

}
