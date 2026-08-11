package com.SistemSchool.modulo_secrtaria.repository;

import com.SistemSchool.modulo_secrtaria.dto.DocumentDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.DocumentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.model.Document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

        // -------------------------------
        // Lazy Loading para tabela (nativeQuery)
        // -------------------------------

        @Query(value = """
                        SELECT d.pk_document     AS pkDocument,
                               d.document_number AS documentNumber,
                               d.file_name        AS fileName,
                               d.file_path        AS filePath,
                               s.pk_student        AS studentId,
                               s.full_name          AS studentName,
                               d.document_type     AS documentType,
                               d.issue_date         AS issueDate,
                               d.expiry_date        AS expiryDate,
                               d.obs                AS obs,
                               d.created_at         AS createdAt,
                               d.updated_at         AS updatedAt
                        FROM document d
                        INNER JOIN student s ON s.pk_student = d.student_pk
                        """, countQuery = "SELECT COUNT(*) FROM document", nativeQuery = true)
        Page<DocumentTableProjection> findAllForTable(Pageable pageable);

        // -------------------------------
        // Lista completa com DTO (JPQL)
        // -------------------------------

        @Query("""
                        SELECT new com.SistemSchool.modulo_secrtaria.dto.DocumentDTO(
                               d.pkDocument,
                               d.documentNumber,
                               d.fileName,
                               d.filePath,
                               d.student.pkStudent,
                               d.student.fullName,
                               d.documentType,
                               d.issueDate,
                               d.expiryDate,
                               d.obs,
                               d.createdAt,
                               d.updatedAt
                        )
                        FROM Document d
                        """)
        List<DocumentDTO> findAllDocumentsDTO();

        @Query("""
                        SELECT new com.SistemSchool.modulo_secrtaria.dto.DocumentDTO(
                               d.pkDocument,
                               d.documentNumber,
                               d.fileName,
                               d.filePath,
                               d.student.pkStudent,
                               d.student.fullName,
                               d.documentType,
                               d.issueDate,
                               d.expiryDate,
                               d.obs,
                               d.createdAt,
                               d.updatedAt
                        )
                        FROM Document d
                        WHERE d.student.pkStudent = :studentId
                        """)
        List<DocumentDTO> findAllByStudentId(@Param("studentId") Long studentId);

        // -------------------------------
        // Queries utilitárias
        // -------------------------------

        boolean existsByDocumentNumber(String documentNumber);

        List<Document> findByDocumentType(DocumentType documentType);

        List<Document> findByStudent_PkStudent(Long studentId);

        // -------------------------------
        // Estatísticas (stat cards)
        // -------------------------------

        @Query("SELECT COUNT(d) FROM Document d WHERE d.expiryDate IS NOT NULL AND d.expiryDate BETWEEN :start AND :end")
        long countExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

        @Query("SELECT COUNT(d) FROM Document d WHERE d.expiryDate IS NOT NULL AND d.expiryDate < :date")
        long countExpiredBefore(@Param("date") LocalDate date);

        @Query("SELECT COUNT(DISTINCT d.student.pkStudent) FROM Document d")
        long countDistinctStudents();
}