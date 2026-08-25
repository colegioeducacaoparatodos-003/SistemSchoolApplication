package com.SistemSchool.modulo_Recursoa_Humano.controller;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.lazy.TeacherLazyModel;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.service.TeacherService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import com.SistemSchool.service.BIValidationService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.validator.ValidatorException;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.SistemSchool.report.PdfReportService;
import com.itextpdf.text.DocumentException;

@Named
@ViewScoped
public class TeacherController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String TEACHER_IMG_FOLDER = "teacher_img";
    private static final String TEACHER_IMG_WEB = "/" + TEACHER_IMG_FOLDER + "/";

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Teacher teacher = new Teacher();

    private TeacherDTO editDto = new TeacherDTO();
    private TeacherDTO selectedTeacher = new TeacherDTO();
    private Long selectedId;

    private UploadedFile uploadedPhoto;

    private long totalTeacherCount;
    private long activeTeacherCount;
    private long onLeaveTeacherCount;
    private long newTeacherCount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private TeacherService teacherService;

    @Inject
    private BIValidationService biValidationService;

    private transient TeacherLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new TeacherLazyModel(teacherService);
        loadStatistics();
    }

    private void loadStatistics() {
        totalTeacherCount = teacherService.countAll();
        activeTeacherCount = teacherService.countByStatus(TeacherStatus.ACTIVE);
        onLeaveTeacherCount = teacherService.countByStatus(TeacherStatus.ON_LEAVE);

        List<TeacherDTO> allTeachers = teacherService.getAllTeachers();
        if (allTeachers == null) {
            newTeacherCount = 0;
            return;
        }

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        newTeacherCount = 0;
        if (allTeachers != null) {
            for (TeacherDTO teacherDto : allTeachers) {
                if (teacherDto.getCreatedAt() != null && teacherDto.getCreatedAt().isAfter(thirtyDaysAgo)) {
                    newTeacherCount++;
                }
            }
        }
    }

    public String loadTeachers() {
        try {
            lazyModel = new TeacherLazyModel(teacherService); // Recarga na navegação
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar professores", e.getMessage());
            e.printStackTrace();
        }
        return "/management/recursohumano/teachers.xhtml?faces-redirect=true";
    }

    public TeacherLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────
    // PREPARAÇÃO DO NOVO PROFESSOR (abertura do diálogo de criação)
    // ─────────────────────────────────────────────────────────────

    public void prepareNewTeacher() {
        teacher = new Teacher();
        uploadedPhoto = null;
        teacher.setTeacherNumber(teacherService.generateTeacherNumber());
    }

    public String saveTeacher() {
        try {
            // 1. Upload da foto
            processPhotoUpload();

            // 2. Persistir o professor
            teacherService.save(teacher);

            // 3. Repor estado
            teacher = new Teacher();
            uploadedPhoto = null;
            init(); // Recarrega o lazy model e as estatísticas

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Professor", "Professor registado com sucesso");

            return "/management/recursohumano/teachers.xhtml?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", e.getMessage());
            return null;
        }
    }

    /**
     * Validador customizado para o campo BI, usado via
     * validator="#{teacherController.validateBI}"
     * no p:inputText. O campo é opcional: só valida o formato se algo for
     * preenchido.
     */
    public void validateBI(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return;
        }

        String bi = value.toString().trim();
        if (bi.isEmpty()) {
            return;
        }

        if (!biValidationService.validar(bi)) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "BI inválido",
                    "Formato inválido. Ex: 003456789LA034 (9 dígitos + 2 letras + 3 dígitos)");
            throw new ValidatorException(message);
        }
    }

    /**
     * Lógica de upload centralizada, chamada dentro do saveTeacher.
     */
    private void processPhotoUpload() throws IOException {
        if (uploadedPhoto == null || uploadedPhoto.getContent() == null
                || uploadedPhoto.getContent().length == 0) {
            return;
        }

        if (uploadedPhoto.getSize() > 2097152) {
            throw new IOException("O arquivo excede o tamanho máximo de 2MB.");
        }

        if (uploadedPhoto.getContent().length > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("A foto não pode exceder 2 MB.");
        }

        String originalName = uploadedPhoto.getFileName();
        if (originalName == null || !originalName.matches("(?i).+\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Apenas ficheiros JPG, PNG ou WEBP são permitidos.");
        }

        String realPath = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRealPath(TEACHER_IMG_WEB);

        Path uploadDir = Paths.get(realPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : ".jpg";
        String uniqueName = UUID.randomUUID().toString() + extension;

        Path destination = uploadDir.resolve(uniqueName);
        try (InputStream is = uploadedPhoto.getInputStream()) {
            Files.copy(is, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        teacher.setPhotoPhath(TEACHER_IMG_WEB + uniqueName);
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog() {
        if (selectedId == null || selectedId == 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhum professor selecionado!", "");
            return;
        }

        TeacherDTO dto = null;
        for (TeacherDTO teacherDto : teacherService.getAllTeachers()) {
            if (teacherDto.getPkTeacher() != null && teacherDto.getPkTeacher().equals(selectedId)) {
                dto = teacherDto;
                break;
            }
        }

        if (dto != null) {
            mapDtoFields(dto, editDto = new TeacherDTO());
            mapDtoFields(dto, selectedTeacher);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Professor não encontrado", "");
        }
    }

    public void loadSelectedTeacher() {
        if (selectedId == null || selectedId == 0) {
            return;
        }

        TeacherDTO dto = null;
        for (TeacherDTO teacherDto : teacherService.getAllTeachers()) {
            if (teacherDto.getPkTeacher() != null && teacherDto.getPkTeacher().equals(selectedId)) {
                dto = teacherDto;
                break;
            }
        }

        if (dto != null) {
            mapDtoFields(dto, selectedTeacher);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Professor não encontrado", "");
        }
    }

    private void mapDtoFields(TeacherDTO source, TeacherDTO target) {
        target.setPkTeacher(source.getPkTeacher());
        target.setTeacherNumber(source.getTeacherNumber());
        target.setFristName(source.getFristName());
        target.setLastName(source.getLastName());
        target.setGender(source.getGender());
        target.setQualificationLivel(source.getQualificationLivel());
        target.setContractType(source.getContractType());
        target.setStatus(source.getStatus());
        target.setPhotoPhath(source.getPhotoPhath());
        target.setBiNumber(source.getBiNumber());
        target.setBiExpiryDate(source.getBiExpiryDate());
        target.setAddressStreet(source.getAddressStreet());
        target.setAddressProvice(source.getAddressProvice());
        target.setBaseSalary(source.getBaseSalary());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
        target.setMobilePhone(source.getMobilePhone());
        target.setObs(source.getObs());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            teacherService.update(editDto);
            init(); // Recarrega o lazy model e as estatísticas
            editDto = new TeacherDTO();
            selectedId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Professor", "Professor atualizado com sucesso");
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", e.getMessage());
        }
    }

    public void delete() {
        try {
            teacherService.delete(selectedId);
            selectedId = null;
            init(); // Recarrega o lazy model e as estatísticas
            addMessage(FacesMessage.SEVERITY_INFO, "Professor", "Professor eliminado com sucesso");
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PDF
    // ─────────────────────────────────────────────────────────────
    /**
     * public void printStudentPdf() {
     * if (selectedId == null) {
     * addMessage(FacesMessage.SEVERITY_WARN, "Nenhum aluno selecionado!", "");
     * return;
     * }
     * try {
     * TeacherDTO dto = teacherService.getAllTeachers().stream()
     * .filter(s -> s.getPkTeacher().equals(selectedId))
     * .findFirst()
     * .orElse(null);
     * 
     * if (dto == null) {
     * addMessage(FacesMessage.SEVERITY_WARN, "Aluno não encontrado", "");
     * return;
     * }
     * 
     * byte[] pdf = PdfReportService.generateStudentReport(dto);
     * String fileName = "aluno_" + dto.getTeacherNumber() + ".pdf";
     * PdfReportService.streamToResponse(pdf, fileName);
     * 
     * } catch (DocumentException | IOException e) {
     * e.printStackTrace();
     * addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar PDF", e.getMessage());
     * }
     * }
     */
    // ─────────────────────────────────────────────────────────────
    // UTIL
    // ─────────────────────────────────────────────────────────────

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS E SETTERS
    // ─────────────────────────────────────────────────────────────

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public UploadedFile getUploadedPhoto() {
        return uploadedPhoto;
    }

    public void setUploadedPhoto(UploadedFile uploadedPhoto) {
        this.uploadedPhoto = uploadedPhoto;
    }

    public TeacherDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(TeacherDTO editDto) {
        this.editDto = editDto;
    }

    public TeacherDTO getSelectedTeacher() {
        return selectedTeacher;
    }

    public void setSelectedTeacher(TeacherDTO selectedTeacher) {
        this.selectedTeacher = selectedTeacher;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public void setLazyModel(TeacherLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public TeacherService getTeacherService() {
        return teacherService;
    }

    public void setTeacherService(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public Gender[] getGenders() {
        return Gender.values();
    }

    public QualificationLevel[] getQualificationLevels() {
        return QualificationLevel.values();
    }

    public ContractType[] getContractTypes() {
        return ContractType.values();
    }

    public TeacherStatus[] getTeacherStatuses() {
        return TeacherStatus.values();
    }

    // ─────────────────────────────────────────────────────────────
    // LABELS LEGÍVEIS PARA OS ENUMS (f:selectItems var/itemLabel)
    // ─────────────────────────────────────────────────────────────

    public String genderLabel(Gender g) {
        if (g == null)
            return "";
        switch (g) {
            case MALE:
                return "Masculino";
            case FEMALE:
                return "Feminino";
            case OTHER:
                return "Outro";
            case PREFER_NOT_TO_SAY:
                return "Prefiro não dizer";
            default:
                return g.name();
        }
    }

    public String qualificationLabel(QualificationLevel q) {
        if (q == null)
            return "";
        switch (q) {
            case SECONDARY:
                return "Ensino Médio";
            case BACHELOR:
                return "Licenciatura";
            case POST_GRADUATION:
                return "Pós-Graduação";
            case MASTER:
                return "Mestrado";
            case DOCTORATE:
                return "Doutoramento";
            case POST_DOCTORATE:
                return "Pós-Doutoramento";
            default:
                return q.name();
        }
    }

    public String contractTypeLabel(ContractType c) {
        if (c == null)
            return "";
        return c.name(); // Ajustar conforme os valores reais de ContractType
    }

    public String statusLabel(TeacherStatus s) {
        if (s == null)
            return "";
        switch (s) {
            case ACTIVE:
                return "Ativo";
            case ON_LEAVE:
                return "Em Licença";
            case SUSPENDED:
                return "Suspenso";
            case RESIGNED:
                return "Demitido";
            case RETIRED:
                return "Reformado";
            case TERMINATED:
                return "Rescindido";
            case DECEASED:
                return "Falecido";
            default:
                return s.name();
        }
    }

    public java.util.List<TeacherDTO> getTeachers() {
        return teacherService.getAllTeachers();
    }

    // Métodos para estatísticas

    public long getTotalTeacherCount() {
        return totalTeacherCount;
    }

    public long getActiveTeacherCount() {
        return activeTeacherCount;
    }

    public long getOnLeaveTeacherCount() {
        return onLeaveTeacherCount;
    }

    public long getNewTeacherCount() {
        return newTeacherCount;
    }

    public void setTotalTeacherCount(long totalTeacherCount) {
        this.totalTeacherCount = totalTeacherCount;
    }

    public void setActiveTeacherCount(long activeTeacherCount) {
        this.activeTeacherCount = activeTeacherCount;
    }

    public void setOnLeaveTeacherCount(long onLeaveTeacherCount) {
        this.onLeaveTeacherCount = onLeaveTeacherCount;
    }

    public void setNewTeacherCount(long newTeacherCount) {
        this.newTeacherCount = newTeacherCount;
    }

    public BIValidationService getBiValidationService() {
        return this.biValidationService;
    }

    public void setBiValidationService(BIValidationService biValidationService) {
        this.biValidationService = biValidationService;
    }

}
