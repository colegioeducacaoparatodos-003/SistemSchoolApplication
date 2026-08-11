package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_secrtaria.io.Classe;
import com.SistemSchool.modulo_secrtaria.io.SchoolClaassStatus;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;

public class SchoolClassDTO {

    private Long pkSchoolClass;
    private String classCode;
    private String className;
    private Classe classe;
    private ShiftType turno;
    private String anoLectivo;
    private Integer capacidade;
    private String room;
    private SchoolClaassStatus status;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SchoolClassDTO() {
    }

    public SchoolClassDTO(Long pkSchoolClass, String classCode, String className, Classe classe, ShiftType turno,
            String anoLectivo, Integer capacidade, String room, SchoolClaassStatus status, String obs,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (entity → DTO)
    // ─────────────────────────────────────────────────────────────

    public static SchoolClassDTO fromEntity(SchoolClass entity) {
        return new SchoolClassDTO(
                entity.getPkSchoolClass(),
                entity.getClassCode(),
                entity.getClassName(),
                entity.getClasse(),
                entity.getTurno(),
                entity.getAnoLectivo(),
                entity.getCapacidade(),
                entity.getRoom(),
                entity.getStatus(),
                entity.getObs(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // FLUENT SETTERS (opcional, mesmo padrão do EnrolmentDTO)
    // ─────────────────────────────────────────────────────────────

    public SchoolClassDTO pkSchoolClass(Long pkSchoolClass) {
        setPkSchoolClass(pkSchoolClass);
        return this;
    }

    public SchoolClassDTO classCode(String classCode) {
        setClassCode(classCode);
        return this;
    }

    public SchoolClassDTO className(String className) {
        setClassName(className);
        return this;
    }

    public SchoolClassDTO classe(Classe classe) {
        setClasse(classe);
        return this;
    }

    public SchoolClassDTO turno(ShiftType turno) {
        setTurno(turno);
        return this;
    }

    public SchoolClassDTO anoLectivo(String anoLectivo) {
        setAnoLectivo(anoLectivo);
        return this;
    }

    public SchoolClassDTO capacidade(Integer capacidade) {
        setCapacidade(capacidade);
        return this;
    }

    public SchoolClassDTO room(String room) {
        setRoom(room);
        return this;
    }

    public SchoolClassDTO status(SchoolClaassStatus status) {
        setStatus(status);
        return this;
    }

    public SchoolClassDTO obs(String obs) {
        setObs(obs);
        return this;
    }

    public SchoolClassDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public SchoolClassDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}