package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.modulo_secrtaria.dto.DocumentDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.lazy.DocumentLazyModel;
import com.SistemSchool.modulo_secrtaria.model.Document;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.service.DocumentService;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class DocumentController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DocumentController.class.getName());

    private static final String UPLOAD_RELATIVE_PATH = "/documents_files";

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Document novoDocumento = new Document();
    private DocumentDTO editDto = new DocumentDTO();
    private Long selectedId;

    private Long selectedStudentId;
    private List<StudentDTO> students = new ArrayList<>();

    private byte[] uploadedFileBytes;
    private String uploadedFileName;

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    private DocumentType filterDocumentType;
    private String filterExpiryStatus;
    private LocalDate filterIssueStartDate;
    private LocalDate filterIssueEndDate;
    private String filterStudentName;

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalDocumentCount;
    private long expiringSoonCount;
    private long expiredCount;
    private long distinctStudentsCount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private DocumentService documentService;

    @Inject
    private StudentService studentService;

    private transient DocumentLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new DocumentLazyModel(documentService);
        loadStudents();
        computeStatistics();
    }

    private void loadStudents() {
        try {
            students = studentService.getAllStudents();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos para o formulário de documento", e);
        }
    }

    private void computeStatistics() {
        try {
            totalDocumentCount = documentService.getTotalDocumentCount();
            expiringSoonCount = documentService.getExpiringSoonCount();
            expiredCount = documentService.getExpiredCount();
            distinctStudentsCount = documentService.getDistinctStudentsCount();
        } catch (Exception e) {
            totalDocumentCount = 0;
            expiringSoonCount = 0;
            expiredCount = 0;
            distinctStudentsCount = 0;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de documentos", e);
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar documentos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de documentos", e);
        }
        return "/management/secretaria/document.xhtml?faces-redirect=true";
    }

    public DocumentLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    public void applyFilters() {
        Map<String, Object> filters = new HashMap<>();
        if (filterDocumentType != null) {
            filters.put("documentType", filterDocumentType.name());
        }
        if (filterExpiryStatus != null && !filterExpiryStatus.isBlank()) {
            filters.put("expiryStatus", filterExpiryStatus);
        }
        if (filterIssueStartDate != null) {
            filters.put("issueStartDate", filterIssueStartDate);
        }
        if (filterIssueEndDate != null) {
            filters.put("issueEndDate", filterIssueEndDate);
        }
        if (filterStudentName != null && !filterStudentName.isBlank()) {
            filters.put("studentName", filterStudentName.trim().toLowerCase());
        }
        lazyModel.setFilters(filters);
    }

    public void clearFilters() {
        filterDocumentType = null;
        filterExpiryStatus = null;
        filterIssueStartDate = null;
        filterIssueEndDate = null;
        filterStudentName = null;
        lazyModel.setFilters(new HashMap<>());
    }

    // ─────────────────────────────────────────────────────────────
    // UPLOAD
    // ─────────────────────────────────────────────────────────────

    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            this.uploadedFileBytes = file.getContent();
            this.uploadedFileName = file.getFileName();
            addMessage(FacesMessage.SEVERITY_INFO, "Ficheiro carregado",
                    "\"" + file.getFileName() + "\" pronto para ser guardado.");
        } catch (Exception e) {
            this.uploadedFileBytes = null;
            this.uploadedFileName = null;
            LOGGER.log(Level.SEVERE, "Erro ao ler ficheiro enviado", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Ficheiro", "Não foi possível ler o ficheiro enviado.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public void openCreateDialog() {
        this.novoDocumento = new Document();
        this.uploadedFileBytes = null;
        this.uploadedFileName = null;
        this.selectedStudentId = null;
    }

    public void salvarDocumento() {
        try {
            if (uploadedFileBytes == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Documento", "Selecione um ficheiro antes de guardar.");
                return;
            }
            if (selectedStudentId == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Documento", "Selecione o aluno associado ao documento.");
                return;
            }

            Student student = studentService.findById(selectedStudentId);
            novoDocumento.setStudent(student);

            String uploadBaseDir = resolveUploadDir();

            documentService.uploadDocument(
                    novoDocumento,
                    new ByteArrayInputStream(uploadedFileBytes),
                    uploadedFileName,
                    uploadBaseDir);

            resetCreateForm();
            init();

            addMessage(FacesMessage.SEVERITY_INFO, "Documento", "Documento carregado com sucesso");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao guardar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VIEW / EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openViewDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum documento selecionado!", "");
            return;
        }
        this.selectedId = id;
        try {
            this.editDto = documentService.getDocumentDTOById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar documento para visualização", e);
            addMessage(FacesMessage.SEVERITY_WARN, "Documento não encontrado", "");
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum documento selecionado!", "");
            return;
        }
        this.selectedId = id;
        try {
            this.editDto = documentService.getDocumentDTOById(id);
            this.selectedStudentId = editDto.getStudentId();
            this.uploadedFileBytes = null;
            this.uploadedFileName = null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar documento para edição", e);
            addMessage(FacesMessage.SEVERITY_WARN, "Documento não encontrado", "");
        }
    }

    public void saveUpdate() {
        try {
            if (uploadedFileBytes != null) {
                String uploadBaseDir = resolveUploadDir();
                documentService.replaceDocumentFile(
                        editDto.getPhDocument(),
                        new ByteArrayInputStream(uploadedFileBytes),
                        uploadedFileName,
                        uploadBaseDir);
            }

            if (selectedStudentId != null) {
                editDto.setStudentId(selectedStudentId);
            }

            documentService.update(editDto);

            uploadedFileBytes = null;
            uploadedFileName = null;
            editDto = new DocumentDTO();
            selectedId = null;
            selectedStudentId = null;
            init();

            addMessage(FacesMessage.SEVERITY_INFO, "Documento", "Documento atualizado com sucesso");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", e.getMessage());
        }
    }

    public void prepareDelete(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum documento selecionado!", "");
            return;
        }
        this.selectedId = id;
        try {
            Document document = documentService.getById(id);
            this.editDto = DocumentDTO.fromEntity(document);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar documento para eliminação", e);
            addMessage(FacesMessage.SEVERITY_WARN, "Documento não encontrado", "");
        }
    }

    public void delete() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum documento selecionado!", "");
            return;
        }
        try {
            documentService.delete(selectedId, resolveUploadDir());
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Documento", "Documento eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DOWNLOAD
    // ─────────────────────────────────────────────────────────────

    public void downloadDocumento() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        try {
            String idParam = externalContext.getRequestParameterMap().get("documentId");
            if (idParam == null || idParam.isBlank()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Documento não especificado.");
                return;
            }
            Long documentId = Long.valueOf(idParam);

            Document document = documentService.getById(documentId);
            String uploadBaseDir = resolveUploadDir();

            byte[] fileBytes = documentService.downloadDocumentFile(documentId, uploadBaseDir);

            String encodedFileName = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            externalContext.responseReset();
            externalContext.setResponseContentType(guessContentType(document.getFileName()));
            externalContext.setResponseContentLength(fileBytes.length);
            externalContext.setResponseHeader(
                    "Content-Disposition",
                    "attachment; filename=\"" + document.getFileName() + "\"; filename*=UTF-8''" + encodedFileName);

            try (OutputStream output = externalContext.getResponseOutputStream()) {
                output.write(fileBytes);
                output.flush();
            }

            facesContext.responseComplete();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao descarregar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Erro ao descarregar: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UTIL
    // ─────────────────────────────────────────────────────────────

    private String resolveUploadDir() {
        String realPath = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRealPath(UPLOAD_RELATIVE_PATH);

        if (realPath == null) {
            realPath = System.getProperty("java.io.tmpdir") + UPLOAD_RELATIVE_PATH;
        }
        return realPath;
    }

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }

    private void resetCreateForm() {
        this.novoDocumento = new Document();
        this.uploadedFileBytes = null;
        this.uploadedFileName = null;
        this.selectedStudentId = null;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS E SETTERS
    // ─────────────────────────────────────────────────────────────

    public Document getNovoDocumento() { return novoDocumento; }
    public void setNovoDocumento(Document novoDocumento) { this.novoDocumento = novoDocumento; }

    public DocumentDTO getEditDto() { return editDto; }
    public void setEditDto(DocumentDTO editDto) { this.editDto = editDto; }

    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }

    public Long getSelectedStudentId() { return selectedStudentId; }
    public void setSelectedStudentId(Long selectedStudentId) { this.selectedStudentId = selectedStudentId; }

    public void setLazyModel(DocumentLazyModel lazyModel) { this.lazyModel = lazyModel; }

    public long getTotalDocumentCount() { return totalDocumentCount; }
    public long getExpiringSoonCount() { return expiringSoonCount; }
    public long getExpiredCount() { return expiredCount; }
    public long getDistinctStudentsCount() { return distinctStudentsCount; }

    public String expiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return "none";
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) return "expired";
        if (!expiryDate.isAfter(today.plusDays(30))) return "soon";
        return "ok";
    }

    public DocumentType[] getTiposDocumento() { return DocumentType.values(); }
    public List<StudentDTO> getStudents() { return students; }

    public void refreshStudents() { loadStudents(); }

    public List<DocumentDTO> getDocuments() { return documentService.getAllDocuments(); }

    // ─── FILTROS GETTERS / SETTERS ───

    public DocumentType getFilterDocumentType() { return filterDocumentType; }
    public void setFilterDocumentType(DocumentType filterDocumentType) { this.filterDocumentType = filterDocumentType; }

    public String getFilterExpiryStatus() { return filterExpiryStatus; }
    public void setFilterExpiryStatus(String filterExpiryStatus) { this.filterExpiryStatus = filterExpiryStatus; }

    public LocalDate getFilterIssueStartDate() { return filterIssueStartDate; }
    public void setFilterIssueStartDate(LocalDate filterIssueStartDate) { this.filterIssueStartDate = filterIssueStartDate; }

    public LocalDate getFilterIssueEndDate() { return filterIssueEndDate; }
    public void setFilterIssueEndDate(LocalDate filterIssueEndDate) { this.filterIssueEndDate = filterIssueEndDate; }

    public String getFilterStudentName() { return filterStudentName; }
    public void setFilterStudentName(String filterStudentName) { this.filterStudentName = filterStudentName; }
}