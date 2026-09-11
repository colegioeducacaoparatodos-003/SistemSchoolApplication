package com.SistemSchool.modulo_secrtaria.interfaces;

import java.time.LocalDate;

public interface DocumentTableProjection {

    int getPkDocument();

    String getDocumentType();

    String getFileName();

    String getContentType();

    long getFileSize();

    LocalDate getUploadDate();

}