package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.lazy.StudentLazyModel;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.SistemSchool.report.PdfReportService;
import com.SistemSchool.service.BIValidationService;
import com.itextpdf.text.DocumentException;

@Named
@ViewScoped
public class StudentController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());

    private static final String STUDENT_IMG_FOLDER = "student_img";
    private static final String STUDENT_IMG_WEB = "/" + STUDENT_IMG_FOLDER + "/";

    // ════════════════════════════════════════════════════════════
    // MODELOS
    // ════════════════════════════════════════════════════════════

    private Student student = new Student();

    private StudentDTO editDto = new StudentDTO();
    private StudentDTO selectedStudent = new StudentDTO();
    private Long selectedId;

    private UploadedFile uploadedPhoto;

    private long totalStudentCount;
    private long activeStudentCount;
    private long inactiveStudentCount;
    private long newStudentCount;

    // ════════════════════════════════════════════════════════════
    // FILTROS
    // ════════════════════════════════════════════════════════════

    private List<StudentDTO> allStudents = new ArrayList<>();

    private String filterStudentNumber;
    private String filterFullName;
    private Gender filterGender;
    private StudentStatus filterStatus;
    private String filterProvince;
    private String filterBiNumber;

    // ════════════════════════════════════════════════════════════
    // SERVIÇOS
    // ════════════════════════════════════════════════════════════

    @Inject
    private StudentService studentService;

    @Inject
    private BIValidationService biValidationService;

    private transient StudentLazyModel lazyModel;

    // ════════════════════════════════════════════════════════════
    // INICIALIZAÇÃO
    // ════════════════════════════════════════════════════════════

    @PostConstruct
    public void init() {
        lazyModel = new StudentLazyModel(studentService);
        loadAllStudents();
        computeStatistics();
    }

    private void loadAllStudents() {
        allStudents = studentService.getAllStudents();
        if (allStudents == null) {
            allStudents = new ArrayList<>();
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
        }
        return "/management/secretaria/students.xhtml?faces-redirect=true";
    }

    public StudentLazyModel getLazyModel() {
        return lazyModel;
    }

    // ════════════════════════════════════════════════════════════
    // FILTROS E ESTATÍSTICAS (padrão EnrolmentController)
    // ════════════════════════════════════════════════════════════

    public List<StudentDTO> getFilteredStudents() {
        if (allStudents == null) {
            return new ArrayList<>();
        }

        return allStudents.stream()
                .filter(s -> isBlank(filterStudentNumber)
                        || (s.getSudentNumber() != null && s.getSudentNumber().toLowerCase().contains(filterStudentNumber.toLowerCase())))
                .filter(s -> isBlank(filterFullName)
                        || (s.getFullName() != null && s.getFullName().toLowerCase().contains(filterFullName.toLowerCase())))
                .filter(s -> filterGender == null
                        || (s.getGender() != null && s.getGender().equals(filterGender)))
                .filter(s -> filterStatus == null
                        || (s.getStatus() != null && s.getStatus().equals(filterStatus)))
                .filter(s -> isBlank(filterProvince)
                        || (s.getAddressProvice() != null && s.getAddressProvice().toLowerCase().contains(filterProvince.toLowerCase())))
                .filter(s -> isBlank(filterBiNumber)
                        || (s.getBiNumber() != null && s.getBiNumber().toLowerCase().contains(filterBiNumber.toLowerCase())))
                .collect(Collectors.toList());
    }

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterStudentNumber = null;
        filterFullName = null;
        filterGender = null;
        filterStatus = null;
        filterProvince = null;
        filterBiNumber = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    private void computeStatistics() {
        try {
            List<StudentDTO> filtered = getFilteredStudents();
            totalStudentCount = filtered.size();
            activeStudentCount = filtered.stream()
                    .filter(s -> s.getStatus() == StudentStatus.ACTIVE)
                    .count();
            inactiveStudentCount = filtered.stream()
                    .filter(s -> s.getStatus() == StudentStatus.INACTIVE)
                    .count();

            YearMonth currentMonth = YearMonth.from(LocalDate.now());
            newStudentCount = filtered.stream()
                    .filter(s -> s.getCreatedAt() != null)
                    .filter(s -> YearMonth.from(s.getCreatedAt()).equals(currentMonth))
                    .count();
        } catch (Exception e) {
            totalStudentCount = 0;
            activeStudentCount = 0;
            inactiveStudentCount = 0;
            newStudentCount = 0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    public String buildFilterDescription() {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(filterStudentNumber))
            sb.append("Nº: ").append(filterStudentNumber).append(" | ");
        if (!isBlank(filterFullName))
            sb.append("Nome: ").append(filterFullName).append(" | ");
        if (filterGender != null)
            sb.append("Género: ").append(filterGender).append(" | ");
        if (filterStatus != null)
            sb.append("Status: ").append(filterStatus).append(" | ");
        if (!isBlank(filterProvince))
            sb.append("Província: ").append(filterProvince).append(" | ");
        if (!isBlank(filterBiNumber))
            sb.append("BI: ").append(filterBiNumber).append(" | ");
        return sb.length() > 0 ? sb.toString() : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        StudentDTO student = (StudentDTO) value;
        return (student.getSudentNumber() != null && student.getSudentNumber().toLowerCase().contains(filterText))
                || (student.getFullName() != null && student.getFullName().toLowerCase().contains(filterText))
                || (student.getEmail() != null && student.getEmail().toLowerCase().contains(filterText))
                || (student.getPhone_1() != null && student.getPhone_1().toLowerCase().contains(filterText))
                || (student.getBiNumber() != null && student.getBiNumber().toLowerCase().contains(filterText))
                || (student.getAddressProvice() != null && student.getAddressProvice().toLowerCase().contains(filterText));
    }

    // ════════════════════════════════════════════════════════════
    // CRUD
    // ════════════════════════════════════════════════════════════

    public void prepareNewStudent() {
        student = new Student();
        student.setSudentNumber(studentService.generateNextStudentNumber());
        uploadedPhoto = null;
    }

    public String saveStudent() {
        try {
            if (student.getBiNumber() != null && !student.getBiNumber().isEmpty()
                    && !biValidationService.validar(student.getBiNumber())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        "Informe um número de BI válido (formato: 9 dígitos + 2 letras + 3 dígitos, ex: 123456789LA042).");
                return null;
            }

            processPhotoUpload();
            studentService.save(student);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Aluno registado com sucesso");

            return "/management/secretaria/students.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar aluno", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
            return null;
        }
    }

    public void validarBI() {
        if (student.getBiNumber() != null && !student.getBiNumber().isEmpty()
                && !biValidationService.validar(student.getBiNumber())) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "BI inválido",
                            "Formato esperado: 9 dígitos + 2 letras + 3 dígitos."));
        }
    }

    private void processPhotoUpload() throws IOException {
        if (uploadedPhoto == null || uploadedPhoto.getContent() == null
                || uploadedPhoto.getContent().length == 0) {
            return;
        }

        if (uploadedPhoto.getSize() > 2097152) {
            throw new IOException("O arquivo excede o tamanho máximo de 2MB.");
        }

        String originalName = uploadedPhoto.getFileName();
        if (originalName == null || !originalName.matches("(?i).+\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Apenas ficheiros JPG, PNG ou WEBP são permitidos.");
        }

        String realPath = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRealPath(STUDENT_IMG_WEB);

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

        student.setUploadPhoto(STUDENT_IMG_WEB + uniqueName);
    }

    private String processPhotoUploadInternal() throws IOException {
        if (uploadedPhoto == null || uploadedPhoto.getContent() == null
                || uploadedPhoto.getContent().length == 0) {
            return null;
        }

        if (uploadedPhoto.getSize() > 2097152) {
            throw new IOException("O arquivo excede o tamanho máximo de 2MB.");
        }

        String originalName = uploadedPhoto.getFileName();
        if (originalName == null || !originalName.matches("(?i).+\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Apenas ficheiros JPG, PNG ou WEBP são permitidos.");
        }

        String realPath = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRealPath(STUDENT_IMG_WEB);

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

        return STUDENT_IMG_WEB + uniqueName;
    }

    // ════════════════════════════════════════════════════════════
    // EDIT / UPDATE / DELETE
    // ════════════════════════════════════════════════════════════

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno selecionado");
            return;
        }
        this.selectedId = id;

        StudentDTO dto = allStudents.stream()
                .filter(s -> s.getPkStudent().equals(id))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, editDto = new StudentDTO());
            mapDtoFields(dto, selectedStudent);
            uploadedPhoto = null;
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Aluno não encontrado");
        }
    }

    public void openViewDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno selecionado");
            return;
        }
        this.selectedId = id;

        StudentDTO dto = allStudents.stream()
                .filter(s -> s.getPkStudent().equals(id))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, selectedStudent);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Aluno não encontrado");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno selecionado");
            return;
        }
        this.selectedId = id;
    }

    public void deleteStudent() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno selecionado para eliminar");
            return;
        }
        try {
            studentService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Aluno eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar aluno", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void delete(Long id) {
        if (id == null) return;
        try {
            studentService.delete(id);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Aluno eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar aluno", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void loadSelectedStudent() {
        if (selectedId == null || selectedId == 0) {
            return;
        }

        StudentDTO dto = allStudents.stream()
                .filter(s -> s.getPkStudent().equals(selectedId))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, selectedStudent);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Aluno não encontrado");
        }
    }

    private void mapDtoFields(StudentDTO source, StudentDTO target) {
        target.setPkStudent(source.getPkStudent());
        target.setSudentNumber(source.getSudentNumber());
        target.setFristName(source.getFristName());
        target.setLastName(source.getLastName());
        target.setFullName(source.getFullName());
        target.setGender(source.getGender());
        target.setBiNumber(source.getBiNumber());
        target.setNascDate(source.getNascDate());
        target.setBiExpiryData(source.getBiExpiryData());
        target.setAddressStreet(source.getAddressStreet());
        target.setAddressProvice(source.getAddressProvice());
        target.setNameFather(source.getNameFather());
        target.setNameMather(source.getNameMather());
        target.setEmail(source.getEmail());
        target.setPhone_1(source.getPhone_1());
        target.setPhone_2(source.getPhone_2());
        target.setUploadPhoto(source.getUploadPhoto());
        target.setStatus(source.getStatus());
        target.setObs(source.getObs());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (editDto.getBiNumber() != null && !editDto.getBiNumber().isEmpty()
                    && !biValidationService.validar(editDto.getBiNumber())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Informe um número de BI válido.");
                return;
            }

            if (uploadedPhoto != null && uploadedPhoto.getContent() != null
                    && uploadedPhoto.getContent().length > 0) {
                String photoPath = processPhotoUploadInternal();
                if (photoPath != null) {
                    editDto.setUploadPhoto(photoPath);
                }
            }

            studentService.update(editDto);
            init();
            editDto = new StudentDTO();
            selectedId = null;
            uploadedPhoto = null;

            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Aluno atualizado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar aluno", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // PDF
    // ════════════════════════════════════════════════════════════

    public void printStudentPdf(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno selecionado");
            return;
        }
        try {
            StudentDTO dto = allStudents.stream()
                    .filter(s -> s.getPkStudent().equals(id))
                    .findFirst()
                    .orElse(null);

            if (dto == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Aluno não encontrado");
                return;
            }

            byte[] pdf = PdfReportService.generateStudentReport(dto);
            String fileName = "aluno_" + dto.getSudentNumber() + ".pdf";
            PdfReportService.streamToResponse(pdf, fileName);

        } catch (DocumentException | IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void printStudentPdf() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno selecionado");
            return;
        }
        printStudentPdf(selectedId);
    }

    // ════════════════════════════════════════════════════════════
    // UTIL
    // ════════════════════════════════════════════════════════════

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ════════════════════════════════════════════════════════════
    // GETTERS E SETTERS
    // ════════════════════════════════════════════════════════════

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public UploadedFile getUploadedPhoto() { return uploadedPhoto; }
    public void setUploadedPhoto(UploadedFile uploadedPhoto) { this.uploadedPhoto = uploadedPhoto; }

    public StudentDTO getEditDto() { return editDto; }
    public void setEditDto(StudentDTO editDto) { this.editDto = editDto; }

    public StudentDTO getSelectedStudent() { return selectedStudent; }
    public void setSelectedStudent(StudentDTO selectedStudent) { this.selectedStudent = selectedStudent; }

    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }

    public void setLazyModel(StudentLazyModel lazyModel) { this.lazyModel = lazyModel; }

    public StudentService getStudentService() { return studentService; }
    public void setStudentService(StudentService studentService) { this.studentService = studentService; }

    // ── Filtros ──
    public String getFilterStudentNumber() { return filterStudentNumber; }
    public void setFilterStudentNumber(String filterStudentNumber) { this.filterStudentNumber = filterStudentNumber; }

    public String getFilterFullName() { return filterFullName; }
    public void setFilterFullName(String filterFullName) { this.filterFullName = filterFullName; }

    public Gender getFilterGender() { return filterGender; }
    public void setFilterGender(Gender filterGender) { this.filterGender = filterGender; }

    public StudentStatus getFilterStatus() { return filterStatus; }
    public void setFilterStatus(StudentStatus filterStatus) { this.filterStatus = filterStatus; }

    public String getFilterProvince() { return filterProvince; }
    public void setFilterProvince(String filterProvince) { this.filterProvince = filterProvince; }

    public String getFilterBiNumber() { return filterBiNumber; }
    public void setFilterBiNumber(String filterBiNumber) { this.filterBiNumber = filterBiNumber; }

    // ════════════════════════════════════════════════════════════
    // ENUMS PARA DROPDOWNS
    // ════════════════════════════════════════════════════════════

    public Gender[] getGenders() {
        return Gender.values();
    }

    public StudentStatus[] getStudentStatuses() {
        return StudentStatus.values();
    }

    public List<StudentDTO> getStudents() {
        return allStudents;
    }

    // Métodos para estatísticas

    public long getTotalStudentCount() { return totalStudentCount; }
    public long getActiveStudentCount() { return activeStudentCount; }
    public long getInactiveStudentCount() { return inactiveStudentCount; }
    public long getNewStudentCount() { return newStudentCount; }
}