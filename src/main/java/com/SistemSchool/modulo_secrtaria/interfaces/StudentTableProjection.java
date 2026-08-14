package com.SistemSchool.modulo_secrtaria.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface StudentTableProjection {

    Long getPkStudent();

    String getSudentNumber();

    String getFristName();

    String getLastName();

    String getFullName();

    String getGender();

    String getBiNumber();

    LocalDate getNascDate();

    String getEmail();

    String getPhone_1();

    String getUploadPhoto();

    String getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}