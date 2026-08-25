package com.SistemSchool.modulo_pedagogico.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BoletimDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Cabeçalho do aluno
    private String studentFullName;
    private String enrolmentNumber;
    private String schoolClassName;
    private String academicYear;
    private Integer trimester;
    private String period; // Manhã / Tarde

    // Disciplinas
    private List<BoletimDisciplineRowDTO> disciplines = new ArrayList<>();
    private Double generalAverage;
    private String finalResult; // Aprovado / Reprovado

    public BoletimDTO() {}

    // Getters / Setters
    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getEnrolmentNumber() { return enrolmentNumber; }
    public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

    public String getSchoolClassName() { return schoolClassName; }
    public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getTrimester() { return trimester; }
    public void setTrimester(Integer trimester) { this.trimester = trimester; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<BoletimDisciplineRowDTO> getDisciplines() { return disciplines; }
    public void setDisciplines(List<BoletimDisciplineRowDTO> disciplines) { this.disciplines = disciplines; }

    public Double getGeneralAverage() { return generalAverage; }
    public void setGeneralAverage(Double generalAverage) { this.generalAverage = generalAverage; }

    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }

    public String getGeneralAverageFormatted() {
        return generalAverage != null ? String.format("%.1f", generalAverage) : "-";
    }
}
