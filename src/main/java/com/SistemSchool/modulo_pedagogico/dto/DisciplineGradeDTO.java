package com.SistemSchool.modulo_pedagogico.dto;

public class DisciplineGradeDTO {

    private String disciplineName;
    private String disciplineCode;
    private Double finalGrade;

    public DisciplineGradeDTO() {
    }

    public DisciplineGradeDTO(String disciplineName, String disciplineCode, Double finalGrade) {
        this.disciplineName = disciplineName;
        this.disciplineCode = disciplineCode;
        this.finalGrade = finalGrade;
    }

    public String getDisciplineName() {
        return this.disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public String getDisciplineCode() {
        return this.disciplineCode;
    }

    public void setDisciplineCode(String disciplineCode) {
        this.disciplineCode = disciplineCode;
    }

    public Double getFinalGrade() {
        return this.finalGrade;
    }

    public void setFinalGrade(Double finalGrade) {
        this.finalGrade = finalGrade;
    }
}