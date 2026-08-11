package com.SistemSchool.modulo_secrtaria.model;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.Objects;

import com.SistemSchool.modulo_secrtaria.io.Classe;
import com.SistemSchool.modulo_secrtaria.io.SchoolClaassStatus;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;

@Entity
@Table(name = "school_class")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkSchoolClass;

    private String classCode;
    private String className;

    @Enumerated(EnumType.STRING)
    private Classe classe;
    @Enumerated(EnumType.STRING)
    private ShiftType turno;

    // Formato sugerido: "2025/2026"
    private String anoLectivo;
    // Capacidade da Sala
    private Integer capacidade;
    // Sala (ex: Sala 7 )
    private String room;

    @Enumerated(EnumType.STRING)
    private SchoolClaassStatus status;

    // Auditoria
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = this.status;
        }

    }

    @Transient
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //==========================================
    // Construtors and GETTER - SETTER
    //==========================================

    public SchoolClass() {
    }


    public SchoolClass(Long pkSchoolClass, String classCode, String className, Classe classe, ShiftType turno, String anoLectivo, Integer capacidade, String room, SchoolClaassStatus status, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkSchoolClass = pkSchoolClass;
        this.classCode = classCode;
        this.className = className;
        this.classe = classe;
        this.turno = turno;
        this.anoLectivo = anoLectivo;
        this.capacidade = capacidade;
        this.room = room;
        this.status = status;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getPkSchoolClass() {
        return this.pkSchoolClass;
    }

    public void setPkSchoolClass(Long pkSchoolClass) {
        this.pkSchoolClass = pkSchoolClass;
    }

    public String getClassCode() {
        return this.classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Classe getClasse() {
        return this.classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public ShiftType getTurno() {
        return this.turno;
    }

    public void setTurno(ShiftType turno) {
        this.turno = turno;
    }

    public String getAnoLectivo() {
        return this.anoLectivo;
    }

    public void setAnoLectivo(String anoLectivo) {
        this.anoLectivo = anoLectivo;
    }

    public Integer getCapacidade() {
        return this.capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public String getRoom() {
        return this.room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public SchoolClaassStatus getStatus() {
        return this.status;
    }

    public void setStatus(SchoolClaassStatus status) {
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


}
