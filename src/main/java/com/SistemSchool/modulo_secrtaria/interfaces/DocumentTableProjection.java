package com.SistemSchool.modulo_secrtaria.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DocumentTableProjection {

    Long getPkDocument();

    String getDocumentNumber();

    String getFileName();

    String getFilePath();

    Long getStudentPk();

    String getStudentFullName();

    String getDocumentType();

    LocalDate getIssueDate();

    LocalDate getExpiryDate();

    String getObs();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}