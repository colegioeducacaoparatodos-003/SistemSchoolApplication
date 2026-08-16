package com.SistemSchool.modulo_secrtaria.dto;

import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.model.Document;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class DocumentSpecifications {

    private DocumentSpecifications() {
    }

    public static Specification<Document> withDocumentType(DocumentType documentType) {
        return (root, query, cb) -> documentType == null
                ? cb.conjunction()
                : cb.equal(root.get("documentType"), documentType);
    }

    public static Specification<Document> withIssueDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return cb.conjunction();
            }
            if (start != null && end != null) {
                return cb.between(root.get("issueDate"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("issueDate"), start);
            }
            return cb.lessThanOrEqualTo(root.get("issueDate"), end);
        };
    }

    public static Specification<Document> withExpiryStatus(String status, int soonThresholdDays) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            LocalDate today = LocalDate.now();

            if ("none".equals(status)) {
                return cb.isNull(root.get("expiryDate"));
            }
            if ("expired".equals(status)) {
                return cb.and(
                        cb.isNotNull(root.get("expiryDate")),
                        cb.lessThan(root.get("expiryDate"), today));
            }
            if ("soon".equals(status)) {
                return cb.and(
                        cb.isNotNull(root.get("expiryDate")),
                        cb.greaterThanOrEqualTo(root.get("expiryDate"), today),
                        cb.lessThanOrEqualTo(root.get("expiryDate"), today.plusDays(soonThresholdDays)));
            }
            if ("ok".equals(status)) {
                return cb.and(
                        cb.isNotNull(root.get("expiryDate")),
                        cb.greaterThan(root.get("expiryDate"), today.plusDays(soonThresholdDays)));
            }
            return cb.conjunction();
        };
    }
}