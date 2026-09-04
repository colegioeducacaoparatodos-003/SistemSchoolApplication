package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface GradeTableProjection {
    Long getPkGrade();
    Long getEvaluationPk();
    String getEvaluationName();
    String getEvaluationType();
    String getTrimester();
    String getDisciplineName();
    Long getEnrolmentPk();
    String getStudentFullName();
    String getStudentNumber();
    String getSchoolclassName();
    Double getScore();
    LocalDate getLaunchDate();
    String getObs();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}