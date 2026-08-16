package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.model.Document;

public class DocumentDTO {

    private Long phDocument;
    private String documentNumber;

    private String fileName;
    private String filePath;

    // Dados "achatados" do Student
    private Long studentPk;
    private String studentName;

    private DocumentType documentType;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    private String obs;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentDTO() {
    }

    public DocumentDTO(Long phDocument, String documentNumber, String fileName, String filePath,
            Long studentPk, String studentName, DocumentType documentType,
            LocalDate issueDate, LocalDate expiryDate, String obs,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.phDocument = phDocument;
        this.documentNumber = documentNumber;
        this.fileName = fileName;
        this.filePath = filePath;
        this.studentPk = studentPk;
        this.studentName = studentName;
        this.documentType = documentType;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (document → DTO)
    // ─────────────────────────────────────────────────────────────

    public static DocumentDTO fromEntity(Document document) {
        return new DocumentDTO(
                document.getPhDocument(),
                document.getDocumentNumber(),
                document.getFileName(),
                document.getFilePath(),
                document.getStudent() != null ? document.getStudent().getPkStudent() : null,
                document.getStudent() != null ? document.getStudent().getFullName() : null,
                document.getDocumentType(),
                document.getIssueDate(),
                document.getExpiryDate(),
                document.getObs(),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

    public Long getPhDocument() {
        return this.phDocument;
    }

    public void setPhDocument(Long phDocument) {
        this.phDocument = phDocument;
    }

    public String getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
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

    public Long getStudentPk() {
        return this.studentPk;
    }

    public void setStudentPk(Long studentPk) {
        this.studentPk = studentPk;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public DocumentType getDocumentType() {
        return this.documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public LocalDate getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getObs() {
        return this.obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DocumentDTO phDocument(Long phDocument) {
        setPhDocument(phDocument);
        return this;
    }

    public DocumentDTO documentNumber(String documentNumber) {
        setDocumentNumber(documentNumber);
        return this;
    }

    public DocumentDTO fileName(String fileName) {
        setFileName(fileName);
        return this;
    }

    public DocumentDTO filePath(String filePath) {
        setFilePath(filePath);
        return this;
    }

    public DocumentDTO studentPk(Long studentPk) {
        setStudentPk(studentPk);
        return this;
    }

    public DocumentDTO studentName(String studentName) {
        setStudentName(studentName);
        return this;
    }

    public DocumentDTO documentType(DocumentType documentType) {
        setDocumentType(documentType);
        return this;
    }

    public DocumentDTO issueDate(LocalDate issueDate) {
        setIssueDate(issueDate);
        return this;
    }

    public DocumentDTO expiryDate(LocalDate expiryDate) {
        setExpiryDate(expiryDate);
        return this;
    }

    public DocumentDTO obs(String obs) {
        setObs(obs);
        return this;
    }

    public DocumentDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public DocumentDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}