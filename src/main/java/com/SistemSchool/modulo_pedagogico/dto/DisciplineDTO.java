package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;

public class DisciplineDTO {

    private Long pkDiscipline;
    private String disciplineCode;
    private String disciplineName;
    private String description;
    private DisciplineStatus status;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DisciplineDTO() {}

    public DisciplineDTO(Long pkDiscipline, String disciplineCode, String disciplineName, String description,
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

    public static DisciplineDTO fromEntity(Discipline d) {
        return new DisciplineDTO(
            d.getPkDiscipline(), d.getDisciplineCode(), d.getDisciplineName(),
            d.getDescription(), d.getStatus(), d.getObs(), d.getCreatedAt(), d.getUpdatedAt()
        );
    }

    // Getters e Setters
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
}