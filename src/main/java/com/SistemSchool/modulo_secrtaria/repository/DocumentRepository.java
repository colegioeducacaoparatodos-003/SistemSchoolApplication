package com.SistemSchool.modulo_secrtaria.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.SistemSchool.modulo_secrtaria.interfaces.DocumentTableProjection;
import com.SistemSchool.modulo_secrtaria.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

       @Query(value = """
                     SELECT d.pk_document AS pkDocument,
                            d.document_type AS documentType,
                            d.file_name AS fileName,
                            d.content_type AS contentType,
                            d.file_size AS fileSize,
                            d.upload_date AS uploadDate
                     FROM document d

                     """, countQuery = "SELECT COUNT(*) FROM document", nativeQuery = true)
       Page<DocumentTableProjection> findAllForTable(Pageable pageable);

       // Busca por documentType (LIKE para permitir parte do texto)
       @Query(value = """
                     SELECT d.pk_document AS pkDocument,
                            d.document_type AS documentType,
                            d.file_name AS fileName,
                            d.content_type AS contentType,
                            d.file_size AS fileSize,
                            d.upload_date AS uploadDate
                     FROM document d
                     WHERE d.document_type = :documentType
                     """, countQuery = """
                     SELECT COUNT(*)
                     FROM document d
                     WHERE d.document_type = :documentType
                     """, nativeQuery = true)
       List<DocumentTableProjection> findAllForTableByType(@Param("documentType") String documentType);
}
