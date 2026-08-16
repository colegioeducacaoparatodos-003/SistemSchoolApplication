package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.DocumentDTO;
import com.SistemSchool.modulo_secrtaria.dto.DocumentFilterCriteria;
import com.SistemSchool.modulo_secrtaria.dto.DocumentSpecifications;
import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.model.Document;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.DocumentRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.primefaces.model.file.UploadedFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {

    // ─────────────────────────────────────────────────────────────
    // CONFIGURAÇÃO
    // ─────────────────────────────────────────────────────────────

    private static final String DOCUMENTS_FOLDER = "documents_files";
    private static final String DOCUMENT_NUMBER_PREFIX = "DOC";
    private static final int SEQUENCE_LENGTH = 6;

    /** Nº de dias antes do vencimento a partir do qual um documento é considerado "a expirar em breve". */
    public static final int EXPIRY_SOON_THRESHOLD_DAYS = 30;

    private final DocumentRepository repository;
    private final StudentRepository studentRepository;

    public DocumentService(DocumentRepository documentRepository, StudentRepository studentRepository) {
        this.repository = documentRepository;
        this.studentRepository = studentRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL (DTO-first, para casar com os formulários da view)
    // ─────────────────────────────────────────────────────────────

    public DocumentDTO save(DocumentDTO dto, UploadedFile uploadedFile) throws IOException {
        if (dto.getStudentPk() == null) {
            throw new RuntimeException("É necessário indicar o aluno do documento.");
        }
        if (dto.getDocumentType() == null) {
            throw new RuntimeException("É necessário indicar o tipo de documento.");
        }

        Student student = studentRepository.findById(dto.getStudentPk())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));

        Document document = new Document();
        document.setStudent(student);
        document.setDocumentType(dto.getDocumentType());
        document.setIssueDate(dto.getIssueDate());
        document.setExpiryDate(dto.getExpiryDate());
        document.setObs(dto.getObs());

        // O documentNumber é SEMPRE gerado pelo servidor — o campo na view é
        // meramente informativo/desabilitado, nunca confiar no valor recebido do cliente.
        document.setDocumentNumber(generateDocumentNumber());

        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            storeUploadedFile(document, uploadedFile);
        }

        Document saved = repository.save(document);
        return DocumentDTO.fromEntity(saved);
    }

    public void update(DocumentDTO dto, UploadedFile uploadedFile) throws IOException {
        Document document = repository.findById(dto.getPhDocument())
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + dto.getPhDocument()));

        if (dto.getStudentPk() != null
                && !dto.getStudentPk().equals(document.getStudent().getPkStudent())) {
            Student student = studentRepository.findById(dto.getStudentPk())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));
            document.setStudent(student);
        }

        document.setDocumentType(dto.getDocumentType());
        document.setIssueDate(dto.getIssueDate());
        document.setExpiryDate(dto.getExpiryDate());
        document.setObs(dto.getObs());
        // documentNumber não é editável — gerado uma única vez na criação.

        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            deletePhysicalFile(document);
            storeUploadedFile(document, uploadedFile);
        }

        repository.save(document);
    }

    /**
     * Elimina o documento.
     * <p>
     * Quando {@code forceDelete} é {@code true} (utilizador ADMIN), a aplicação
     * não impõe nenhum bloqueio adicional a nível de Java — a única coisa capaz
     * de impedir a eliminação nesse caso é uma restrição de integridade
     * referencial (FK) a nível da própria base de dados. Se isso acontecer,
     * configure ON DELETE CASCADE (ou remova/realoque manualmente os registos
     * dependentes) na chave estrangeira correspondente para permitir que o
     * ADMIN elimine mesmo com relações existentes.
     */
    public void delete(Long id, boolean forceDelete) {
        Document document = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));
        try {
            repository.delete(document);
            repository.flush();
            deletePhysicalFile(document);
        } catch (DataIntegrityViolationException e) {
            if (!forceDelete) {
                throw new IllegalStateException(
                        "Não é possível eliminar este documento porque está associado a outros registos.");
            }
            throw new IllegalStateException(
                    "Este documento está associado a outros registos e a base de dados impede a eliminação. "
                            + "Configure eliminação em cascata (ON DELETE CASCADE) na chave estrangeira "
                            + "correspondente para permitir a eliminação forçada por ADMIN.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO AUTOMÁTICA DO documentNumber (DOC-ano-sequencia)
    // ─────────────────────────────────────────────────────────────

    public synchronized String generateDocumentNumber() {
        int year = LocalDate.now().getYear();
        String prefix = DOCUMENT_NUMBER_PREFIX + "-" + year + "-";

        List<String> lastNumbers = repository.findLastDocumentNumbersByPrefix(prefix, PageRequest.of(0, 1));

        int nextSequence = 1;
        if (!lastNumbers.isEmpty()) {
            String lastNumber = lastNumbers.get(0);
            String sequencePart = lastNumber.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                nextSequence = 1;
            }
        }

        String candidate = prefix + String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);

        while (repository.existsByDocumentNumber(candidate)) {
            nextSequence++;
            candidate = prefix + String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);
        }

        return candidate;
    }

    /** Pré-visualização do próximo número (usado apenas para exibição no diálogo de criação). */
    public String previewNextDocumentNumber() {
        return generateDocumentNumber();
    }

    // ─────────────────────────────────────────────────────────────
    // ARMAZENAMENTO DE FICHEIROS (upload / download / delete físico)
    // ─────────────────────────────────────────────────────────────

    private void storeUploadedFile(Document document, UploadedFile uploadedFile) throws IOException {
        Path directory = resolveDocumentsDirectory();
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String extension = extractExtension(uploadedFile.getFileName());
        String storedFileName = (document.getDocumentNumber() != null
                ? document.getDocumentNumber()
                : UUID.randomUUID().toString()) + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        Path destination = directory.resolve(storedFileName);

        try (OutputStream outputStream = Files.newOutputStream(destination)) {
            outputStream.write(uploadedFile.getContent());
        }

        document.setFileName(uploadedFile.getFileName());
        document.setFilePath(storedFileName);
    }

    private void deletePhysicalFile(Document document) {
        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            return;
        }
        try {
            Path filePath = resolveDocumentsDirectory().resolve(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("Aviso: não foi possível eliminar o ficheiro físico: " + e.getMessage());
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    private Path resolveDocumentsDirectory() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String realPath = facesContext.getExternalContext().getRealPath("/");
        return new File(realPath, DOCUMENTS_FOLDER).toPath();
    }

    public DownloadedFile downloadDocument(Long id) throws IOException {
        Document document = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + id));

        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            throw new RuntimeException("Este documento não possui ficheiro associado para download.");
        }

        Path filePath = resolveDocumentsDirectory().resolve(document.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("O ficheiro físico do documento não foi encontrado no servidor.");
        }

        byte[] content = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String downloadName = document.getFileName() != null
                ? document.getFileName()
                : document.getDocumentNumber() + extractExtension(document.getFilePath());

        return new DownloadedFile(downloadName, contentType, content);
    }

    public static class DownloadedFile {
        private final String fileName;
        private final String contentType;
        private final byte[] content;

        public DownloadedFile(String fileName, String contentType, byte[] content) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getContent() {
            return content;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<DocumentDTO> getAllDocuments() {
        return repository.findAll().stream()
                .map(DocumentDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA (com filtros dinâmicos via Specification)
    // ─────────────────────────────────────────────────────────────

    public Page<DocumentDTO> findLazy(int page, int size, Sort sort, DocumentFilterCriteria filter) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Document> spec = Specification.where(null);

        if (filter != null) {
            if (filter.getDocumentType() != null) {
                spec = spec.and(DocumentSpecifications.withDocumentType(filter.getDocumentType()));
            }
            if (filter.getIssueStartDate() != null || filter.getIssueEndDate() != null) {
                spec = spec.and(DocumentSpecifications.withIssueDateBetween(
                        filter.getIssueStartDate(), filter.getIssueEndDate()));
            }
            if (filter.getExpiryStatus() != null && !filter.getExpiryStatus().isBlank()) {
                spec = spec.and(DocumentSpecifications.withExpiryStatus(
                        filter.getExpiryStatus(), EXPIRY_SOON_THRESHOLD_DAYS));
            }
        }

        return repository.findAll(spec, pageable).map(DocumentDTO::fromEntity);
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS (cards do topo da listagem)
    // ─────────────────────────────────────────────────────────────

    public long countTotalDocuments() {
        return repository.count();
    }

    public long countExpiringSoon() {
        LocalDate today = LocalDate.now();
        return repository.countByExpiryDateBetween(today, today.plusDays(EXPIRY_SOON_THRESHOLD_DAYS));
    }

    public long countExpired() {
        return repository.countByExpiryDateBefore(LocalDate.now());
    }

    public long countDistinctStudentsWithDocuments() {
        return repository.countDistinctStudents();
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Document> getByStudent(Long studentPk) {
        return repository.findByStudent_PkStudent(studentPk);
    }

    public List<Document> getByType(DocumentType documentType) {
        return repository.findByDocumentType(documentType);
    }

    public Document getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + id));
    }

    public Document findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com id: " + id));
    }

    public DocumentDTO findDtoById(Long id) {
        return DocumentDTO.fromEntity(findById(id));
    }
}