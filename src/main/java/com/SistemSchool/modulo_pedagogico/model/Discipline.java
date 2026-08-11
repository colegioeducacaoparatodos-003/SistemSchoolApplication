package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDateTime;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "discipline")
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkDiscipline;

    // =======================
    // Código da disciplina
    // Ex.: MAT001
    // ========================
    private String disciplineCode;

    // =====================
    // Nome da disciplina
    // ======================
    private String disciplineName;

    // ==============================
    // Carga horária anual
    // ==============================
    private Integer workload;

    @Enumerated(EnumType.STRING)
    private DisciplineStatus status;

    // =======================================
    // Auditoria
    // =======================================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = DisciplineStatus.ACTIVE;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Discipline() {
    }

    public Discipline(Long pkDiscipline, String disciplineCode, String disciplineName, Integer workload,
            DisciplineStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkDiscipline = pkDiscipline;
        this.disciplineCode = disciplineCode;
        this.disciplineName = disciplineName;
        this.workload = workload;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DEPOIS (correto)
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

}
