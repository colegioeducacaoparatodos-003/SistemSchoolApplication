package com.SistemSchool.modulo_Recursoa_Humano.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TeacherTableProjection {

    Long getPkTeacher();

    String getTeacherNumber();

    String getFristName();

    String getLastName();

    String getGender();

    String getQualificationLivel();

    String getContractType();

    String getStatus();

    String getPhotoPhath();

    String getBiNumber();

    LocalDate getBiExpiryDate();

    String getAddressStreet();

    String getAddressProvice();

    BigDecimal getBaseSalary();

    String getEmail();

    String getPhone();

    String getMobilePhone();

    String getObs();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}