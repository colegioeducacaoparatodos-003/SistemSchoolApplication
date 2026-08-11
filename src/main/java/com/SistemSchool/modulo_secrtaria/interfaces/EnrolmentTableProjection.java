package com.SistemSchool.modulo_secrtaria.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EnrolmentTableProjection {

    Long getPhEnrolment();

    String getEnrolmentNumer();

    String getShift();

    String getEnrolmentType();

    Long getStudentPk();

    String getStudentFullName();

    String getStudentNumber();

    // Dados "achatados" de SchoolClass (mesmo padrão usado para Student)
    Long getSchoolclassPk();

    String getSchoolclassnome();

    String getSchoolclasscode();

    LocalDate getEnrolmentData();

    String getObs();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}