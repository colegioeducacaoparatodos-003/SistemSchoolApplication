package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.config.SessionBean;
import com.SistemSchool.modulo_secrtaria.dto.DocumentDTO;
import com.SistemSchool.modulo_secrtaria.dto.DocumentFilterCriteria;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.io.DocumentType;
import com.SistemSchool.modulo_secrtaria.lazy.DocumentLazyModel;
import com.SistemSchool.modulo_secrtaria.service.DocumentService;
import com.SistemSchool.modulo_secrtaria.service.DocumentService.DownloadedFile;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class DocumentController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DocumentController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS DOS FORMULÁRIOS (DTO-first: bate com o binding das views)
    // ─────────────────────────────────────────────────────────────

    private DocumentDTO novoDocumento = new DocumentDTO();
    private DocumentDTO editDto = new DocumentDTO();

    private Long selectedStudentId;
    private Long selectedId; // usado para eliminar (prepareDelete/delete)

    private UploadedFile uploadedFile;

    // ─────────────────────────────────────────────────────────────
    // FILTROS DA TOOLBAR (documents.xhtml)
    // ─────────────────────────────────────────────────────────────

    private DocumentType filterDocumentType;
    private String filterExpiryStatus;
    private LocalDate filterIssueStartDate;
    private LocalDate filterIssueEndDate;

    // ─────────────────────────────────────────────────────────────
    // LISTAS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    private List<StudentDTO> students = new java.util.ArrayList<>();

    private transient DocumentLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private DocumentService documentService;

    @Inject
    private StudentService studentService;

    @Inject
    private SessionBean sessionBean;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new DocumentLazyModel(this);
        loadStudents();
    }

    private void loadStudents() {
        try {
            students = studentService.getAllStudents();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos para o formulário de documentos", e);
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

    public DocumentService getDocumentService() {
        return documentService;
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    public DocumentFilterCriteria buildFilterCriteria() {
        DocumentFilterCriteria criteria = new DocumentFilterCriteria();
        criteria.setDocumentType(filterDocumentType);
        criteria.setExpiryStatus(filterExpiryStatus);
        criteria.setIssueStartDate(filterIssueStartDate);
        criteria.setIssueEndDate(filterIssueEndDate);
        return criteria;
    }

    public void applyFilters() {
        // O próprio update="dtDocuments" no botão/ajax força o p:dataTable a
        // chamar load() novamente, que por sua vez lê os filtros atuais
        // através de buildFilterCriteria(). Nada mais é necessário aqui.
    }

    public void clearFilters() {
        filterDocumentType = null;
        filterExpiryStatus = null;
        filterIssueStartDate = null;
        filterIssueEndDate = null;
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS (cards do topo)
    // ─────────────────────────────────────────────────────────────

    public long getTotalDocumentCount() {
        return documentService.countTotalDocuments();
    }

    public long getExpiringSoonCount() {
        return documentService.countExpiringSoon();
    }

    public long getExpiredCount() {
        return documentService.countExpired();
    }

    public long getDistinctStudentsCount() {
        return documentService.countDistinctStudentsWithDocuments();
    }

    /** Classe CSS do badge de validade (ok | soon | expired | none) usada na view. */
    public String expiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) {
            return "none";
        }
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return "expired";
        }
        if (!expiryDate.isAfter(today.plusDays(DocumentService.EXPIRY_SOON_THRESHOLD_DAYS))) {
            return "soon";
        }
        return "ok";
    }

    // ─────────────────────────────────────────────────────────────
    // UPLOAD DE FICHEIRO
    // ─────────────────────────────────────────────────────────────

    public void handleFileUpload(FileUploadEvent event) {
        this.uploadedFile = event.getFile();
        addMessage(FacesMessage.SEVERITY_INFO, "Ficheiro selecionado",
                event.getFile().getFileName() + " pronto para ser gravado");
    }

    // ─────────────────────────────────────────────────────────────
    // CRIAÇÃO
    // ─────────────────────────────────────────────────────────────

    public void openCreateDialog() {
        novoDocumento = new DocumentDTO();
        selectedStudentId = null;
        uploadedFile = null;
    }

    public void salvarDocumento() {
        try {
            if (selectedStudentId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Selecione um aluno antes de gravar.");
                return;
            }
            if (novoDocumento.getDocumentType() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Selecione o tipo de documento.");
                return;
            }

            novoDocumento.setStudentPk(selectedStudentId);

            documentService.save(novoDocumento, uploadedFile);

            novoDocumento = new DocumentDTO();
            selectedStudentId = null;
            uploadedFile = null;

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Documento", "Documento registado com sucesso");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar ficheiro do documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Erro ao gravar o ficheiro: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VISUALIZAÇÃO / EDIÇÃO
    // ─────────────────────────────────────────────────────────────

    public void openViewDialog(Long id) {
        loadIntoEditDto(id);
    }

    public void openEditDialog(Long id) {
        DocumentDTO dto = loadIntoEditDto(id);
        if (dto != null) {
            selectedStudentId = dto.getStudentPk();
            uploadedFile = null;
        }
    }

    private DocumentDTO loadIntoEditDto(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhum documento selecionado!", "");
            return null;
        }
        try {
            DocumentDTO dto = documentService.findDtoById(id);
            mapDtoFields(dto, editDto = new DocumentDTO());
            return dto;
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Documento não encontrado", "");
            return null;
        }
    }

    private void mapDtoFields(DocumentDTO source, DocumentDTO target) {
        target.setPhDocument(source.getPhDocument());
        target.setDocumentNumber(source.getDocumentNumber());
        target.setFileName(source.getFileName());
        target.setFilePath(source.getFilePath());
        target.setStudentPk(source.getStudentPk());
        target.setStudentName(source.getStudentName());
        target.setDocumentType(source.getDocumentType());
        target.setIssueDate(source.getIssueDate());
        target.setExpiryDate(source.getExpiryDate());
        target.setObs(source.getObs());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (selectedStudentId != null) {
                editDto.setStudentPk(selectedStudentId);
            }
            documentService.update(editDto, uploadedFile);
            editDto = new DocumentDTO();
            selectedStudentId = null;
            uploadedFile = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Documento", "Documento atualizado com sucesso");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar ficheiro do documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Erro ao atualizar o ficheiro: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ELIMINAÇÃO — visível e permitida apenas para ADMIN
    // ─────────────────────────────────────────────────────────────

    public void prepareDelete(Long id) {
        if (!sessionBean.isAdmin()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acesso negado",
                    "Apenas o utilizador ADMIN pode eliminar documentos.");
            return;
        }
        this.selectedId = id;
    }

    public void delete() {
        if (!sessionBean.isAdmin()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acesso negado",
                    "Apenas o utilizador ADMIN pode eliminar documentos.");
            return;
        }
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum documento selecionado!", "");
            return;
        }
        try {
            // ADMIN pode forçar a eliminação mesmo que o documento esteja
            // relacionado com outros registos (ver DocumentService#delete).
            documentService.delete(selectedId, true);
            selectedId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Documento", "Documento eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DOWNLOAD (acionado via commandButton ajax="false" + f:param documentId)
    // ─────────────────────────────────────────────────────────────

    public void downloadDocumento() {
        String idParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("documentId");

        if (idParam == null || idParam.isBlank()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Documento", "Nenhum documento indicado para download.");
            return;
        }

        try {
            Long id = Long.valueOf(idParam);
            DownloadedFile file = documentService.downloadDocument(id);

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            externalContext.responseReset();
            externalContext.setResponseContentType(file.getContentType());
            externalContext.setResponseContentLength(file.getContent().length);

            String encodedFileName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            externalContext.setResponseHeader("Content-Disposition",
                    "attachment; filename=\"" + file.getFileName() + "\"; filename*=UTF-8''" + encodedFileName);

            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            try (ServletOutputStream out = response.getOutputStream()) {
                out.write(file.getContent());
                out.flush();
            }

            facesContext.responseComplete();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao efetuar download do documento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento",
                    "Não foi possível efetuar o download: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UTIL
    // ─────────────────────────────────────────────────────────────

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS E SETTERS
    // ─────────────────────────────────────────────────────────────

    public DocumentDTO getNovoDocumento() {
        return novoDocumento;
    }

    public void setNovoDocumento(DocumentDTO novoDocumento) {
        this.novoDocumento = novoDocumento;
    }

    public DocumentDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(DocumentDTO editDto) {
        this.editDto = editDto;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public Long getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(Long selectedStudentId) {
        this.selectedStudentId = selectedStudentId;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void setLazyModel(DocumentLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public DocumentType getFilterDocumentType() {
        return filterDocumentType;
    }

    public void setFilterDocumentType(DocumentType filterDocumentType) {
        this.filterDocumentType = filterDocumentType;
    }

    public String getFilterExpiryStatus() {
        return filterExpiryStatus;
    }

    public void setFilterExpiryStatus(String filterExpiryStatus) {
        this.filterExpiryStatus = filterExpiryStatus;
    }

    public LocalDate getFilterIssueStartDate() {
        return filterIssueStartDate;
    }

    public void setFilterIssueStartDate(LocalDate filterIssueStartDate) {
        this.filterIssueStartDate = filterIssueStartDate;
    }

    public LocalDate getFilterIssueEndDate() {
        return filterIssueEndDate;
    }

    public void setFilterIssueEndDate(LocalDate filterIssueEndDate) {
        this.filterIssueEndDate = filterIssueEndDate;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS E LISTAS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public DocumentType[] getTiposDocumento() {
        return DocumentType.values();
    }

    public List<StudentDTO> getStudents() {
        return students;
    }

    public void refreshStudents() {
        loadStudents();
    }

    public List<DocumentDTO> getDocuments() {
        return documentService.getAllDocuments();
    }
}