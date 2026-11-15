package com.SistemSchool.modulo_secrtaria.service;

import jakarta.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.SistemSchool.modulo_secrtaria.dto.DocumentTableDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.DocumentTableProjection;
import com.SistemSchool.modulo_secrtaria.model.Document;
import com.SistemSchool.modulo_secrtaria.repository.DocumentRepository;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository repository;

    /**
     * Pasta onde os ficheiros são gravados. Pode ser sobreposta em
     * application.properties com:
    *   app.documents.storage-path=/caminho/absoluto/documents_files
    * O padrão aponta para src/main/webapp/documents_files no projeto.
     */
    @Value("${app.documents.storage-path:${user.dir}/src/main/webapp/documents_files}")
    private String storageDir;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public void upload(Document document) {
        try {
            save(document);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível guardar o ficheiro do documento.", e);
        }
    }

    public Document findById(int id) {
        return repository.findById(id).orElseThrow();
    }

    public Document save(Document document) throws IOException {

        if (document.getStudent() == null || document.getStudent().getPkStudent() == null) {
            throw new IllegalArgumentException("O documento deve estar associado a um aluno.");
        }

        UploadedFile uploadedFile = document.getUploadedFile();

        if (uploadedFile == null) {
            throw new IllegalArgumentException("Nenhum ficheiro foi selecionado para upload.");
        }

        String nomeGravado = salvarFicheiro(uploadedFile);
        String nomeOriginal = uploadedFile.getFileName();
        String extensaoOriginal = obterExtensao(nomeOriginal);

        if (document.getFileName() == null || document.getFileName().isBlank()) {
            document.setFileName(nomeOriginal);
        } else if (!extensaoOriginal.isEmpty() && obterExtensao(document.getFileName()).isEmpty()) {
            document.setFileName(document.getFileName() + extensaoOriginal);
        }
        document.setContentType(uploadedFile.getContentType());
        document.setFilePath(nomeGravado);
        document.setFileSize(uploadedFile.getSize());
        document.setUploadDate(LocalDate.now());

        return repository.save(document);
    }

    /**
     * Grava o InputStream do UploadedFile em disco, dentro da pasta configurada,
     * com um nome único (UUID + extensão original) para nunca sobrescrever
     * ficheiros já existentes com o mesmo nome.
     *
     * @return o nome do ficheiro efetivamente gravado (usado depois em filePath)
     */
    private String salvarFicheiro(UploadedFile uploadedFile) throws IOException {

        Path pastaDestino = resolverPastaDestino();

        if (!Files.exists(pastaDestino)) {
            Files.createDirectories(pastaDestino);
        }

        String nomeOriginal = uploadedFile.getFileName();
        String extensao = obterExtensao(nomeOriginal);

        String nomeFinal = UUID.randomUUID() + extensao;
        Path destino = pastaDestino.resolve(nomeFinal);

        try (InputStream in = uploadedFile.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        return nomeFinal;
    }

    private String obterExtensao(String nomeFicheiro) {
        if (nomeFicheiro == null) {
            return "";
        }

        int dotIndex = nomeFicheiro.lastIndexOf('.');
        return dotIndex > 0 ? nomeFicheiro.substring(dotIndex) : "";
    }

    private Path resolverPastaDestino() {
        Path path = Paths.get(storageDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(storageDir);
        }
        return path.normalize();
    }

    /**
     * Resolve o File físico correspondente a um Document já gravado
     * (usado no download).
     */
    public File resolveFile(Document document) {
        return resolverPastaDestino().resolve(document.getFilePath()).toFile();
    }

    public void delete(int id) {
            com.SistemSchool.config.AdminDeleteGuard.requireAdmin();
        repository.deleteById(id);
    }

    public List<DocumentTableProjection> getDocumentsType(String documentType) {
        try {
            return repository.findAllForTableByType(documentType);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar documentos por tipo: " + documentType, e);
        }
    }

    public Page<DocumentTableDTO> findLazy(int page, int size, Sort sort) {

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new DocumentTableDTO(
                p.getPkDocument(),
                p.getDocumentType(),
                p.getFileName(),
                p.getContentType(),
                p.getFileSize(),
                p.getUploadDate()));
    }
}