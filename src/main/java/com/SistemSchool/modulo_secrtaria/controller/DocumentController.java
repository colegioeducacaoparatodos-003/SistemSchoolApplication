package com.SistemSchool.modulo_secrtaria.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.PrimeFaces;

import com.SistemSchool.modulo_secrtaria.dto.DocumentTableDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.DocumentTableProjection;
import com.SistemSchool.modulo_secrtaria.service.DocumentService;
import com.SistemSchool.modulo_secrtaria.model.Document;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;
import com.SistemSchool.modulo_secrtaria.lazy.*;
import com.SistemSchool.controller.*;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;

@Named
@ViewScoped
public class DocumentController implements Serializable {

    private static final long serialVersionUID = 1L;

    private Document document = new Document();

    private LazyDataModel<DocumentTableDTO> lazyModel;

    private StreamedContent fileToDownload;

    @Inject
    private DocumentService service;

    @Inject
    private UserController loginController;

    @Inject
    private StudentRepository studentRepository;

    private Integer selectedId;
    private String documentType;
    private List<DocumentTableProjection> documents;
    private List<Student> students;
    private Long selectedStudentId;

    @PostConstruct
    public void init() {
        students = studentRepository.findAll();
        lazyModel = new DocumentLazyModel(service);
    }

    // Método loadDocument para Renderização
    public String load() {
        try {
            lazyModel = new DocumentLazyModel(service);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao processar",
                            e.getMessage()));
            e.printStackTrace();
        }
        return "/management/secretaria/documents.xhtml?faces-redirect=true";
    }

    // Método loadDocument para Renderização com a lista de documentos
    public String loadDocumentPage() {
        try {
            lazyModel = new DocumentLazyModel(service);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao processar",
                            e.getMessage()));
            e.printStackTrace();
        }
        return "/documents.xhtml?faces-redirect=true";
    }

    public void add() {

        try {

            if (selectedStudentId == null) {
                throw new IllegalArgumentException("Selecione o aluno do documento.");
            }
            document.setStudent(studentRepository.getReferenceById(selectedStudentId));

            service.save(document);

            lazyModel = new DocumentLazyModel(service);

            document = new Document();
            selectedStudentId = null;

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Document",
                    "Document uploaded successfully");
                PrimeFaces.current().ajax().addCallbackParam("saved", true);

        } catch (Exception e) {
            e.printStackTrace();

            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Document",
                    e.getMessage() == null ? "Falha ao guardar o documento." : e.getMessage());
                PrimeFaces.current().ajax().addCallbackParam("saved", false);
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ================== UPLOAD ==================
    public void upload() {
        try {
            UploadedFile uploaded = document.getUploadedFile();

            if (uploaded == null) {
                FacesContext.getCurrentInstance()
                        .addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione um arquivo!"));
                return;
            }

            // Salva no banco
            service.upload(document);

            // Reset
            document = new Document();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Arquivo enviado!"));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                            "Falha no upload: " + e.getMessage()));
        }
    }

    // ================== List for Type ==================
    public void loadDocumentsType() {
        try {
            documents = service.getDocumentsType(documentType);

            if (documents == null || documents.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Nenhum documento encontrado!", "");
            }

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao buscar documentos!", e.getMessage());
        }
    }

    // ================== DOWNLOAD ==================
    public void downloadDocument() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext ec = facesContext.getExternalContext();

        try {
            Document document = service.findById(selectedId);
            File file = service.resolveFile(document);

            if (!file.exists()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Ficheiro não encontrado em disco.");
                return;
            }

            try (InputStream fileStream = new FileInputStream(file);
                    OutputStream out = ec.getResponseOutputStream()) {

                ec.responseReset();
                ec.setResponseContentType(document.getContentType());
                ec.setResponseContentLength((int) file.length());
                ec.setResponseHeader(
                        "Content-Disposition",
                        "attachment; filename=\"" + document.getFileName() + "\"");

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                out.flush();
                facesContext.responseComplete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Documento", "Falha no download: " + e.getMessage());
        }
    }

    public StreamedContent getFileToDownload() {
        return fileToDownload;
    }

    // ================== GETTERS ==================
    public LazyDataModel<DocumentTableDTO> getLazyModel() {
        return lazyModel;
    }

    public Document getDocument() {
        return document;
    }

    public List<Student> getStudents() {
        return students;
    }

    public Long getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(Long selectedStudentId) {
        this.selectedStudentId = selectedStudentId;
    }

    // ================== SETTERS ==================
    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Integer selectedId) {
        this.selectedId = selectedId;
    }

    public void setLazyModel(LazyDataModel<DocumentTableDTO> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public void setFileToDownload(StreamedContent fileToDownload) {
        this.fileToDownload = fileToDownload;
    }

    public DocumentService getService() {
        return this.service;
    }

    public void setService(DocumentService service) {
        this.service = service;
    }

    public UserController getLoginController() {
        return this.loginController;
    }

    public void setLoginController(UserController loginController) {
        this.loginController = loginController;
    }

    public String getSearchDocumentType() {
        return this.documentType;
    }

    public void setSearchDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public List<DocumentTableProjection> getDocuments() {
        return this.documents;
    }

    public void setDocuments(List<DocumentTableProjection> documents) {
        this.documents = documents;
    }

}