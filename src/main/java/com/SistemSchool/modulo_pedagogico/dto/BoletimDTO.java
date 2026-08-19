package com.SistemSchool.modulo_pedagogico.dto;

import java.util.List;

public class BoletimDTO {

    private String studentName;
    private String studentNumber;       // enrolmentNumer da matrícula
    private String schoolClassName;
    private String academicYear;        // vem de SchoolClass.anoLectivo (ex.: "2025/2026")
    private Integer trimester;
    private String period;
    private List<DisciplineGradeDTO> disciplineGrades;
    private String behavior;
    private String observation;

    public BoletimDTO() {
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNumber() {
        return this.studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getSchoolClassName() {
        return this.schoolClassName;
    }

    public void setSchoolClassName(String schoolClassName) {
        this.schoolClassName = schoolClassName;
    }

    public String getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getTrimester() {
        return this.trimester;
    }

    public void setTrimester(Integer trimester) {
        this.trimester = trimester;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<DisciplineGradeDTO> getDisciplineGrades() {
        return this.disciplineGrades;
    }

    public void setDisciplineGrades(List<DisciplineGradeDTO> disciplineGrades) {
        this.disciplineGrades = disciplineGrades;
    }

    public String getBehavior() {
        return this.behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public String getObservation() {
        return this.observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public BoletimDTO studentName(String studentName) {
        setStudentName(studentName);
        return this;
    }

    public BoletimDTO studentNumber(String studentNumber) {
        setStudentNumber(studentNumber);
        return this;
    }

    public BoletimDTO schoolClassName(String schoolClassName) {
        setSchoolClassName(schoolClassName);
        return this;
    }

    public BoletimDTO academicYear(String academicYear) {
        setAcademicYear(academicYear);
        return this;
    }

    public BoletimDTO trimester(Integer trimester) {
        setTrimester(trimester);
        return this;
    }

    public BoletimDTO period(String period) {
        setPeriod(period);
        return this;
    }

    public BoletimDTO disciplineGrades(List<DisciplineGradeDTO> disciplineGrades) {
        setDisciplineGrades(disciplineGrades);
        return this;
    }

    public BoletimDTO behavior(String behavior) {
        setBehavior(behavior);
        return this;
    }

    public BoletimDTO observation(String observation) {
        setObservation(observation);
        return this;
    }
}