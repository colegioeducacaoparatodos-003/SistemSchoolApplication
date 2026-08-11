package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDateTime;

public interface GradeTableProjection {

    Long getPkGrade();

    Long getEvaluationPk();

    String getEvaluationDescription();

    Long getEnrolmentPk();

    String getEnrolmentNumber();

    Long getStudentPk();

    String getStudentFullName();

    Double getValue();

    String getStatus();

    String getObservation();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}