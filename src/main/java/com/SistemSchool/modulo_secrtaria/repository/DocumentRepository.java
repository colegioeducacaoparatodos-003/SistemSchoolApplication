package com.SistemSchool.modulo_secrtaria.repository;

import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.model.Document;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       List<Document> findByStudent_PkStudent(Long studentPk);

       List<Document> findByDocumentType(DocumentType documentType);

       boolean existsByDocumentNumber(String documentNumber);

       Optional<Document> findByDocumentNumber(String documentNumber);

       // -------------------------------
       // Geração de sequência do documentNumber (DOC-ano-sequencia)
       // -------------------------------

       @Query("""
                     SELECT d.documentNumber
                     FROM Document d
                     WHERE d.documentNumber LIKE CONCAT(:prefix, '%')
                     ORDER BY d.documentNumber DESC
                     """)
       List<String> findLastDocumentNumbersByPrefix(@Param("prefix") String prefix, Pageable pageable);

       // -------------------------------
       // Estatísticas (cards do topo da listagem)
       // -------------------------------

       long countByExpiryDateBefore(LocalDate date);

       long countByExpiryDateBetween(LocalDate start, LocalDate end);

       @Query("SELECT COUNT(DISTINCT d.student.pkStudent) FROM Document d")
       long countDistinctStudents();
}