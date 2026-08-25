package com.SistemSchool.modulo_pedagogico.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MiniPautaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Cabeçalho
    private String schoolName;
    private String disciplineName;
    private String schoolClassName;
    private String schoolClassCode;
    private String teacherName;
    private Integer trimester;
    private String academicYear;

    // Estatísticas da turma
    private Double classAverage;
    private long approvedCount;
    private long failedCount;

    private List<MiniPautaStudentRowDTO> students = new ArrayList<>();

    public MiniPautaDTO() {}

    // Getters / Setters
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

    public String getSchoolClassName() { return schoolClassName; }
    public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

    public String getSchoolClassCode() { return schoolClassCode; }
    public void setSchoolClassCode(String schoolClassCode) { this.schoolClassCode = schoolClassCode; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getTrimester() { return trimester; }
    public void setTrimester(Integer trimester) { this.trimester = trimester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Double getClassAverage() { return classAverage; }
    public void setClassAverage(Double classAverage) { this.classAverage = classAverage; }

    public long getApprovedCount() { return approvedCount; }
    public void setApprovedCount(long approvedCount) { this.approvedCount = approvedCount; }

    public long getFailedCount() { return failedCount; }
    public void setFailedCount(long failedCount) { this.failedCount = failedCount; }

    public List<MiniPautaStudentRowDTO> getStudents() { return students; }
    public void setStudents(List<MiniPautaStudentRowDTO> students) { this.students = students; }

    public long getTotalStudents() {
        return students != null ? students.size() : 0;
    }
}
