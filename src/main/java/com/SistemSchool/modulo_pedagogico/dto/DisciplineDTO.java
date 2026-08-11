package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;

public class DisciplineDTO {

    private Long pkDiscipline;
    private String disciplineCode;
    private String disciplineName;
    private Integer workload;
    private DisciplineStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DisciplineDTO() {
    }

    public DisciplineDTO(Long pkDiscipline, String disciplineCode, String disciplineName, Integer workload,
            DisciplineStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkDiscipline = pkDiscipline;
        this.disciplineCode = disciplineCode;
        this.disciplineName = disciplineName;
        this.workload = workload;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (entity → DTO)
    // ─────────────────────────────────────────────────────────────

    public static DisciplineDTO fromEntity(Discipline entity) {
        return new DisciplineDTO(
                entity.getPkDiscipline(),
                entity.getDisciplineCode(),
                entity.getDisciplineName(),
                entity.getWorkload(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

    public Long getPkDiscipline() {
        return this.pkDiscipline;
    }

    public void setPkDiscipline(Long pkDiscipline) {
        this.pkDiscipline = pkDiscipline;
    }

    public String getDisciplineCode() {
        return this.disciplineCode;
    }

    public void setDisciplineCode(String disciplineCode) {
        this.disciplineCode = disciplineCode;
    }

    public String getDisciplineName() {
        return this.disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public Integer getWorkload() {
        return this.workload;
    }

    public void setWorkload(Integer workload) {
        this.workload = workload;
    }

    public DisciplineStatus getStatus() {
        return this.status;
    }

    public void setStatus(DisciplineStatus status) {
        this.status = status;
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
    // FLUENT SETTERS (mesmo padrão do SchoolClassDTO)
    // ─────────────────────────────────────────────────────────────

    public DisciplineDTO pkDiscipline(Long pkDiscipline) {
        setPkDiscipline(pkDiscipline);
        return this;
    }

    public DisciplineDTO disciplineCode(String disciplineCode) {
        setDisciplineCode(disciplineCode);
        return this;
    }

    public DisciplineDTO disciplineName(String disciplineName) {
        setDisciplineName(disciplineName);
        return this;
    }

    public DisciplineDTO workload(Integer workload) {
        setWorkload(workload);
        return this;
    }

    public DisciplineDTO status(DisciplineStatus status) {
        setStatus(status);
        return this;
    }

    public DisciplineDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public DisciplineDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}