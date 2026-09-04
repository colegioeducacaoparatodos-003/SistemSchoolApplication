package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDateTime;

public interface TrimesterResultTableProjection {
    Long getPkTrimesterResult();
    Long getEnrolmentPk();
    String getStudentFullName();
    String getStudentNumber();
    String getSchoolclassName();
    Long getDisciplinePk();
    String getDisciplineName();
    String getTrimester();
    Double getMac();
    Double getNpt();
    Double getMt();
    String getSituation();
    String getObs();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}