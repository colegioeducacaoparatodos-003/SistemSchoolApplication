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

    LocalDate getBiExpiryData();

    String getAddressStreet();

    String getAddressProvice();

    String getNameFather();

    String getNameMather();

    String getEmail();

    String getPhone_1();

    String getPhone_2();

    String getUploadPhoto();

    String getStatus();

    String getObs();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}