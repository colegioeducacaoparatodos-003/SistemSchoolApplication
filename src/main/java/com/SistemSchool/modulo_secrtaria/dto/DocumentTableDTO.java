package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDate;

public class DocumentTableDTO {

    private int pkDocument;
    private String documentType;
    private String fileName;
    private String contentType;
    private long fileSize;
    private LocalDate uploadDate;
    private String userName;

    public DocumentTableDTO(
            int pkDocument,
            String documentType,
            String fileName,
            String contentType,
            long fileSize,
            LocalDate uploadDate) {

        this.pkDocument = pkDocument;
        this.documentType = documentType;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;

    }

    public int getPkDocument() {
        return pkDocument;
    }

    public void setPkDocument(int pkDocument) {
        this.pkDocument = pkDocument;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // getters
}