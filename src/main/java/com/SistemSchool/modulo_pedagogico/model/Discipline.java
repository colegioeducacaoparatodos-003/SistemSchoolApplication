package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.beans.Transient;

@Entity
@Table(name = "discipline")
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkDiscipline;

    @Column(unique = true)
    //EX: DISC-2026-00001
    private String disciplineCode;
    private String disciplineName;
    private String description;

    @Enumerated(EnumType.STRING)
    private DisciplineStatus status;

    // AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DisciplineStatus.ATIVO;
        }
    }

    @Transient
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Discipline() {}

    public Discipline(Long pkDiscipline, String disciplineCode, String disciplineName, String description,
                      DisciplineStatus status, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkDiscipline = pkDiscipline;
        this.disciplineCode = disciplineCode;
        this.disciplineName = disciplineName;
        this.description = description;
        this.status = status;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkDiscipline() { return pkDiscipline; }
    public void setPkDiscipline(Long pkDiscipline) { this.pkDiscipline = pkDiscipline; }

    public String getDisciplineCode() { return disciplineCode; }
    public void setDisciplineCode(String disciplineCode) { this.disciplineCode = disciplineCode; }

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DisciplineStatus getStatus() { return status; }
    public void setStatus(DisciplineStatus status) { this.status = status; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Discipline pkDiscipline(Long pkDiscipline) { setPkDiscipline(pkDiscipline); return this; }
    public Discipline disciplineCode(String disciplineCode) { setDisciplineCode(disciplineCode); return this; }
    public Discipline disciplineName(String disciplineName) { setDisciplineName(disciplineName); return this; }
    public Discipline description(String description) { setDescription(description); return this; }
    public Discipline status(DisciplineStatus status) { setStatus(status); return this; }
    public Discipline obs(String obs) { setObs(obs); return this; }
    public Discipline createdAt(LocalDateTime createdAt) { setCreatedAt(createdAt); return this; }
    public Discipline updatedAt(LocalDateTime updatedAt) { setUpdatedAt(updatedAt); return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Discipline)) return false;
        Discipline that = (Discipline) o;
        return Objects.equals(pkDiscipline, that.pkDiscipline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkDiscipline);
    }
}