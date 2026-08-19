package com.SistemSchool.modulo_pedagogico.dto;

import java.util.List;

public class PautaDTO {

    private String schoolClassName;
    private String disciplineName;
    private Integer trimester;
    private String academicYear;
    private String teacherName;
    private List<AlunoNotaDTO> studentGrades;

    public PautaDTO() {
    }

    public String getSchoolClassName() {
        return this.schoolClassName;
    }

    public void setSchoolClassName(String schoolClassName) {
        this.schoolClassName = schoolClassName;
    }

    public String getDisciplineName() {
        return this.disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public Integer getTrimester() {
        return this.trimester;
    }

    public void setTrimester(Integer trimester) {
        this.trimester = trimester;
    }

    public String getAcademicYear() {
        return this.academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getTeacherName() {
        return this.teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<AlunoNotaDTO> getStudentGrades() {
        return this.studentGrades;
    }

    public void setStudentGrades(List<AlunoNotaDTO> studentGrades) {
        this.studentGrades = studentGrades;
    }

    public PautaDTO schoolClassName(String schoolClassName) {
        setSchoolClassName(schoolClassName);
        return this;
    }

    public PautaDTO disciplineName(String disciplineName) {
        setDisciplineName(disciplineName);
        return this;
    }

    public PautaDTO trimester(Integer trimester) {
        setTrimester(trimester);
        return this;
    }

    public PautaDTO academicYear(String academicYear) {
        setAcademicYear(academicYear);
        return this;
    }

    public PautaDTO teacherName(String teacherName) {
        setTeacherName(teacherName);
        return this;
    }

    public PautaDTO studentGrades(List<AlunoNotaDTO> studentGrades) {
        setStudentGrades(studentGrades);
        return this;
    }
}