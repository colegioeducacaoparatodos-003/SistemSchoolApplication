package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.DocumentDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.DocumentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.model.Document;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.DocumentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository repository;
    private final StudentService studentService;

    @PersistenceContext
    private EntityManager entityManager;

    public DocumentService(DocumentRepository repository, StudentService studentService) {
        this.repository = repository;
        this.studentService = studentService;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Document save(Document document) {
        if (repository.existsByDocumentNumber(document.getDocumentNumber())) {
            throw new RuntimeException("Número de documento já existe: " + document.getDocumentNumber());
        }
        return repository.save(document);
    }

    public void update(DocumentDTO dto) {
        Document document = repository.findById(dto.getPhDocument())
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + dto.getPhDocument()));

        document.setDocumentNumber(dto.getDocumentNumber());
        document.setDocumentType(dto.getDocumentType());
        document.setIssueDate(dto.getIssueDate());
        document.setExpiryDate(dto.getExpiryDate());
        document.setObs(dto.getObs());

        if (dto.getStudentId() != null
                && (document.getStudent() == null || !dto.getStudentId().equals(document.getStudent().getPkStudent()))) {
            Student student = studentService.findById(dto.getStudentId());
            document.setStudent(student);
        }

        repository.save(document);
    }

    public void delete(Long id, String uploadBaseDir) {
        Document document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + id));

        deletePhysicalFile(document, uploadBaseDir);
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<DocumentDTO> getAllDocuments() {
        return repository.findAllDocumentsDTO();
    }

    public List<DocumentDTO> getByStudentId(Long studentId) {
        return repository.findAllByStudentId(studentId);
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA (COM FILTROS)
    // ─────────────────────────────────────────────────────────────

    public Page<DocumentDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder(
            "SELECT new com.SistemSchool.modulo_secrtaria.dto.DocumentDTO(" +
            "d.pkDocument, d.documentNumber, d.fileName, d.filePath, " +
            "d.student.pkStudent, d.student.fullName, d.documentType, " +
            "d.issueDate, d.expiryDate, d.obs, d.createdAt, d.updatedAt) " +
            "FROM Document d WHERE 1=1 "
        );
        StringBuilder countJpql = new StringBuilder(
            "SELECT COUNT(d) FROM Document d WHERE 1=1 "
        );

        Map<String, Object> params = new HashMap<>();
        LocalDate today = LocalDate.now();

        if (filters != null) {
            if (filters.get("documentType") != null) {
                jpql.append("AND d.documentType = :documentType ");
                countJpql.append("AND d.documentType = :documentType ");
                params.put("documentType", filters.get("documentType"));
            }
            if (filters.get("studentName") != null) {
                String name = "%" + filters.get("studentName").toString().toLowerCase() + "%";
                jpql.append("AND LOWER(d.student.fullName) LIKE :studentName ");
                countJpql.append("AND LOWER(d.student.fullName) LIKE :studentName ");
                params.put("studentName", name);
            }
            if (filters.get("issueStartDate") != null) {
                jpql.append("AND d.issueDate >= :issueStartDate ");
                countJpql.append("AND d.issueDate >= :issueStartDate ");
                params.put("issueStartDate", filters.get("issueStartDate"));
            }
            if (filters.get("issueEndDate") != null) {
                jpql.append("AND d.issueDate <= :issueEndDate ");
                countJpql.append("AND d.issueDate <= :issueEndDate ");
                params.put("issueEndDate", filters.get("issueEndDate"));
            }
            if (filters.get("expiryStatus") != null) {
                String status = filters.get("expiryStatus").toString();
                switch (status) {
                    case "ok":
                        jpql.append("AND d.expiryDate IS NOT NULL AND d.expiryDate > :expiryThreshold ");
                        countJpql.append("AND d.expiryDate IS NOT NULL AND d.expiryDate > :expiryThreshold ");
                        params.put("expiryThreshold", today.plusDays(30));
                        break;
                    case "soon":
                        jpql.append("AND d.expiryDate IS NOT NULL AND d.expiryDate BETWEEN :today AND :expiryThreshold ");
                        countJpql.append("AND d.expiryDate IS NOT NULL AND d.expiryDate BETWEEN :today AND :expiryThreshold ");
                        params.put("today", today);
                        params.put("expiryThreshold", today.plusDays(30));
                        break;
                    case "expired":
                        jpql.append("AND d.expiryDate IS NOT NULL AND d.expiryDate < :today ");
                        countJpql.append("AND d.expiryDate IS NOT NULL AND d.expiryDate < :today ");
                        params.put("today", today);
                        break;
                    case "none":
                        jpql.append("AND d.expiryDate IS NULL ");
                        countJpql.append("AND d.expiryDate IS NULL ");
                        break;
                }
            }
        }

        // ORDER BY
        if (sort.isSorted()) {
            jpql.append("ORDER BY ");
            List<String> orderClauses = new ArrayList<>();
            for (Sort.Order order : sort) {
                orderClauses.add("d." + order.getProperty() + " " + order.getDirection().name());
            }
            jpql.append(String.join(", ", orderClauses));
        } else {
            jpql.append("ORDER BY d.createdAt DESC");
        }

        TypedQuery<DocumentDTO> query = entityManager.createQuery(jpql.toString(), DocumentDTO.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        Long total = countQuery.getSingleResult();
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<DocumentDTO> content = query.getResultList();

        return new PageImpl<>(content, PageRequest.of(page, size, sort), total);
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public DocumentDTO getDocumentDTOById(Long id) {
        Document document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + id));
        return DocumentDTO.fromEntity(document);
    }

    public List<Document> getByDocumentType(DocumentType documentType) {
        return repository.findByDocumentType(documentType);
    }

    public Document getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + id));
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS (stat cards)
    // ─────────────────────────────────────────────────────────────

    public long getTotalDocumentCount() {
        return repository.count();
    }

    public long getExpiringSoonCount() {
        LocalDate today = LocalDate.now();
        return repository.countExpiringBetween(today, today.plusDays(30));
    }

    public long getExpiredCount() {
        return repository.countExpiredBefore(LocalDate.now());
    }

    public long getDistinctStudentsCount() {
        return repository.countDistinctStudents();
    }

    // ─────────────────────────────────────────────────────────────
    // UPLOAD DE DOCUMENTO
    // ─────────────────────────────────────────────────────────────

    public Document uploadDocument(Document document, InputStream fileContent, String originalFileName,
            String uploadBaseDir) throws IOException {

        Path uploadDirPath = Paths.get(uploadBaseDir);
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }

        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = uploadDirPath.resolve(storedFileName);

        try (InputStream in = fileContent) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        document.setFileName(originalFileName);
        document.setFilePath(storedFileName);

        return save(document);
    }

    public void replaceDocumentFile(Long documentId, InputStream fileContent, String originalFileName,
            String uploadBaseDir) throws IOException {

        Document document = getById(documentId);
        deletePhysicalFile(document, uploadBaseDir);

        Path uploadDirPath = Paths.get(uploadBaseDir);
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }

        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = uploadDirPath.resolve(storedFileName);

        try (InputStream in = fileContent) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        document.setFileName(originalFileName);
        document.setFilePath(storedFileName);

        repository.save(document);
    }

    // ─────────────────────────────────────────────────────────────
    // DOWNLOAD DE DOCUMENTO
    // ─────────────────────────────────────────────────────────────

    public byte[] downloadDocumentFile(Long documentId, String uploadBaseDir) throws IOException {
        Document document = getById(documentId);

        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            throw new RuntimeException("Este documento não tem ficheiro anexado.");
        }

        Path filePath = Paths.get(uploadBaseDir, document.getFilePath());

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Ficheiro não encontrado em disco: " + filePath);
        }

        return Files.readAllBytes(filePath);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVADOS
    // ─────────────────────────────────────────────────────────────

    private void deletePhysicalFile(Document document, String uploadBaseDir) {
        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            return;
        }
        try {
            Path filePath = Paths.get(uploadBaseDir, document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // não interrompe a operação principal
        }
    }
}