package com.SistemSchool.modulo_secrtaria.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import java.util.Objects;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkDocument;
    private String documentNumber;

    // ==============================================
    // Nome do arquivo (caso seja digitalizado) /////
    // ==============================================
    private String fileName;

    // ======================================
    // Caminho onde o arquivo foi salvo /////
    // ======================================
    private String filePath;

    //==============================
    // Relacionamentos *////////////
    //==============================
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_document_student"))
    private Student student;
    
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    // =====================
    // Data de emissão /////
    // =====================
    private LocalDate issueDate;

    // ====================
    // Data de validade ///
    // ====================
    private LocalDate expiryDate;

    // ====================
    // Auditoria //////////
    // ====================
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Document() {

    }

    // Getters, Setters, Equals, HashCode, toString

    public Document(Long phDocument, String documentNumber, 
                    String fileName, String filePath, Student student, 
                    DocumentType documentType, LocalDate issueDate, 
                    LocalDate expiryDate, String obs, LocalDateTime createdAt, 
                    LocalDateTime updatedAt) {

        this.pkDocument = phDocument;
        this.documentNumber = documentNumber;
        this.fileName = fileName;
        this.filePath = filePath;
        this.student = student;
        this.documentType = documentType;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public Long getPhDocument() {
        return this.pkDocument;
    }

    public void setPhDocument(Long phDocument) {
        this.pkDocument = phDocument;
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

    public Student getStudent() {
        return this.student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    public Document phDocument(Long phDocument) {
        setPhDocument(phDocument);
        return this;
    }

    public Document documentNumber(String documentNumber) {
        setDocumentNumber(documentNumber);
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

    public Document student(Student student) {
        setStudent(student);
        return this;
    }

    public Document documentType(DocumentType documentType) {
        setDocumentType(documentType);
        return this;
    }

    public Document issueDate(LocalDate issueDate) {
        setIssueDate(issueDate);
        return this;
    }

    public Document expiryDate(LocalDate expiryDate) {
        setExpiryDate(expiryDate);
        return this;
    }

    public Document obs(String obs) {
        setObs(obs);
        return this;
    }

    public Document createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Document updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Document)) {
            return false;
        }
        Document document = (Document) o;
        return Objects.equals(pkDocument, document.pkDocument) && Objects.equals(documentNumber, document.documentNumber) && Objects.equals(fileName, document.fileName) && Objects.equals(filePath, document.filePath) && Objects.equals(student, document.student) && Objects.equals(documentType, document.documentType) && Objects.equals(issueDate, document.issueDate) && Objects.equals(expiryDate, document.expiryDate) && Objects.equals(obs, document.obs) && Objects.equals(createdAt, document.createdAt) && Objects.equals(updatedAt, document.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkDocument, documentNumber, fileName, filePath, student, documentType, issueDate, expiryDate, obs, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " phDocument='" + getPhDocument() + "'" +
            ", documentNumber='" + getDocumentNumber() + "'" +
            ", fileName='" + getFileName() + "'" +
            ", filePath='" + getFilePath() + "'" +
            ", student='" + getStudent() + "'" +
            ", documentType='" + getDocumentType() + "'" +
            ", issueDate='" + getIssueDate() + "'" +
            ", expiryDate='" + getExpiryDate() + "'" +
            ", obs='" + getObs() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
    
}