package com.SistemSchool.modulo_secrtaria.model;

import java.time.LocalDate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.primefaces.model.file.UploadedFile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pkDocument;
    private String documentType;
    // DECLARATION | VISAS | LIABILITY AGREEMENT | LAWS | OTHER
    private String fileName;
    private String filePath;
    private String contentType;
    private long fileSize;
    private LocalDate uploadDate;
    private int fkUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_pk", nullable = false)
    private Student student;

    @Transient
    private UploadedFile uploadedFile;

    public Document() {
    }

    public Document(int pkDocument, String documentType, String fileName, String filePath, String contentType,
            long fileSize, LocalDate uploadDate, int fkUser, UploadedFile uploadedFile) {
        this.pkDocument = pkDocument;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.fkUser = fkUser;
        this.uploadedFile = uploadedFile;
    }

    public int getPkDocument() {
        return this.pkDocument;
    }

    public void setPkDocument(int pkDocument) {
        this.pkDocument = pkDocument;
    }

    public String getDocumentType() {
        return this.documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDate getUploadDate() {
        return this.uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getFkUser() {
        return this.fkUser;
    }

    public void setFkUser(int fkUser) {
        this.fkUser = fkUser;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public UploadedFile getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public Document pkDocument(int pkDocument) {
        setPkDocument(pkDocument);
        return this;
    }

    public Document documentType(String documentType) {
        setDocumentType(documentType);
        return this;
    }

    public Document fileName(String fileName) {
        setFileName(fileName);
        return this;
    }

    public Document filePath(String filePath) {
        setFilePath(filePath);
        return this;
    }

    public Document contentType(String contentType) {
        setContentType(contentType);
        return this;
    }

    public Document fileSize(long fileSize) {
        setFileSize(fileSize);
        return this;
    }

    public Document uploadDate(LocalDate uploadDate) {
        setUploadDate(uploadDate);
        return this;
    }

    public Document fkUser(int fkUser) {
        setFkUser(fkUser);
        return this;
    }

    public Document uploadedFile(UploadedFile uploadedFile) {
        setUploadedFile(uploadedFile);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString() {
        return "{" +
                " pkDocument='" + getPkDocument() + "'" +
                ", documentType='" + getDocumentType() + "'" +
                ", fileName='" + getFileName() + "'" +
                ", filePath='" + getFilePath() + "'" +
                ", contentType='" + getContentType() + "'" +
                ", fileSize='" + getFileSize() + "'" +
                ", uploadDate='" + getUploadDate() + "'" +
                ", fkUser='" + getFkUser() + "'" +
                ", uploadedFile='" + getUploadedFile() + "'" +
                "}";
    }


}
