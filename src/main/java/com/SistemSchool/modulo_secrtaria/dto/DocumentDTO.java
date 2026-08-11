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
    private Long studentId;
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
            Long studentId, String studentName, DocumentType documentType, LocalDate issueDate,
            LocalDate expiryDate, String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.phDocument = phDocument;
        this.documentNumber = documentNumber;
        this.fileName = fileName;
        this.filePath = filePath;
        this.studentId = studentId;
        this.studentName = studentName;
        this.documentType = documentType;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (entity → DTO)
    // ─────────────────────────────────────────────────────────────

    public static DocumentDTO fromEntity(Document entity) {
        return new DocumentDTO(
                entity.getPhDocument(),
                entity.getDocumentNumber(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getStudent() != null ? entity.getStudent().getPkStudent() : null,
                entity.getStudent() != null ? entity.getStudent().getFullName() : null,
                entity.getDocumentType(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getObs(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
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

    public Long getStudentId() {
        return this.studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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
}