package com.SistemSchool.modulo_pedagogico.dto;

public class AlunoNotaDTO {

    private Integer number;
    private String studentName;
    private Double finalGrade;
    private String situation; // "Aprovado", "Reprovado", "-"

    public AlunoNotaDTO() {
    }

    public AlunoNotaDTO(Integer number, String studentName, Double finalGrade, String situation) {
        this.number = number;
        this.studentName = studentName;
        this.finalGrade = finalGrade;
        this.situation = situation;
    }

    public Integer getNumber() {
        return this.number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Double getFinalGrade() {
        return this.finalGrade;
    }

    public void setFinalGrade(Double finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getSituation() {
        return this.situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }
}