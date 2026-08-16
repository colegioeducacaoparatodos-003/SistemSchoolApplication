package com.SistemSchool.modulo_secrtaria.dto;

import java.time.LocalDate;

import com.SistemSchool.modulo_secrtaria.io.DocumentType;

/**
 * Critérios opcionais usados para filtrar a listagem lazy de documentos
 * a partir da toolbar de filtros da view (documents.xhtml).
 */
public class DocumentFilterCriteria {

    private DocumentType documentType;
    private String expiryStatus; // "ok" | "soon" | "expired" | "none"
    private LocalDate issueStartDate;
    private LocalDate issueEndDate;

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getExpiryStatus() {
        return expiryStatus;
    }

    public void setExpiryStatus(String expiryStatus) {
        this.expiryStatus = expiryStatus;
    }

    public LocalDate getIssueStartDate() {
        return issueStartDate;
    }

    public void setIssueStartDate(LocalDate issueStartDate) {
        this.issueStartDate = issueStartDate;
    }

    public LocalDate getIssueEndDate() {
        return issueEndDate;
    }

    public void setIssueEndDate(LocalDate issueEndDate) {
        this.issueEndDate = issueEndDate;
    }

    public boolean isEmpty() {
        return documentType == null
                && (expiryStatus == null || expiryStatus.isBlank())
                && issueStartDate == null
                && issueEndDate == null;
    }
}