package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.lazy.EnrolmentLazyModel;
import com.SistemSchool.modulo_secrtaria.io.EnrolmentType;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import com.SistemSchool.modulo_secrtaria.service.SchoolClassService;
import com.SistemSchool.modulo_secrtaria.service.StudentService;
import com.SistemSchool.report.PdfReportService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class EnrolmentController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EnrolmentController.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── MODELOS ──
    private Enrolment enrolment = new Enrolment();
    private EnrolmentDTO editDto = new EnrolmentDTO();
    private EnrolmentDTO selectedEnrolment = new EnrolmentDTO();
    private Long selectedId;
    private Long selectedStudentId;
    private Long selectedSchoolClassId;

    // ── FILTROS AVANÇADOS ──
    private Long filterSchoolClassId;
    private String filterShift;
    private String filterEnrolmentType;
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private String filterStudentName;

    // ── LISTAS ──
    private List<StudentDTO> students = new ArrayList<>();
    private List<SchoolClass> schoolClasses = new ArrayList<>();

    // ── ESTATÍSTICAS ──
    private long totalEnrolmentCount;
    private long newEnrolmentCount;
    private long distinctClassesCount;
    private long distinctStudentsCount;

    // ── SERVIÇOS ──
    @Inject
    private EnrolmentService enrolmentService;
    @Inject
    private StudentService studentService;
    @Inject
    private SchoolClassRepository schoolClassRepository;
    @Inject
    private SchoolClassService schoolClassService;
    private transient EnrolmentLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new EnrolmentLazyModel(enrolmentService);
        loadStudents();
        loadSchoolClasses();
        computeStatistics();
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matrículas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de matrículas", e);
        }
        return "/management/secretaria/enrolments.xhtml?faces-redirect=true";
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterSchoolClassId = null;
        filterShift = null;
        filterEnrolmentType = null;
        filterStartDate = null;
        filterEndDate = null;
        filterStudentName = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    /**
     * Retorna a lista filtrada em memória com base nos critérios da view.
     */
    public List<EnrolmentDTO> getFilteredEnrolments() {
        List<EnrolmentDTO> all = enrolmentService.getAllEnrolments();

        return all.stream()
                .filter(e -> filterSchoolClassId == null ||
                        filterSchoolClassId.toString().isEmpty() ||
                        (e.getSchoolClassPk() != null && e.getSchoolClassPk().equals(filterSchoolClassId)))
                .filter(e -> filterShift == null || filterShift.isBlank() ||
                        (e.getShift() != null && e.getShift().toString().equalsIgnoreCase(filterShift)))
                .filter(e -> filterEnrolmentType == null || filterEnrolmentType.isBlank() ||
                        (e.getEnrolmentType() != null
                                && e.getEnrolmentType().toString().equalsIgnoreCase(filterEnrolmentType)))
                .filter(e -> filterStartDate == null ||
                        (e.getEnrolmentData() != null && !e.getEnrolmentData().isBefore(filterStartDate)))
                .filter(e -> filterEndDate == null ||
                        (e.getEnrolmentData() != null && !e.getEnrolmentData().isAfter(filterEndDate)))
                .filter(e -> filterStudentName == null || filterStudentName.isBlank() ||
                        (e.getStudentFullName() != null &&
                                e.getStudentFullName().toLowerCase().contains(filterStudentName.toLowerCase())))
                .collect(Collectors.toList());
    }

    /**
     * Monta a descrição dos filtros ativos para exibir no cabeçalho do PDF.
     */
    public String buildFilterDescription() {
        StringBuilder sb = new StringBuilder();
        if (filterSchoolClassId != null) {
            SchoolClass sc = schoolClasses.stream()
                    .filter(c -> filterSchoolClassId.equals(c.getPkSchoolClass()))
                    .findFirst().orElse(null);
            if (sc != null)
                sb.append("Turma: ").append(sc.getClassCode()).append(" | ");
        }
        if (filterShift != null && !filterShift.isBlank())
            sb.append("Turno: ").append(filterShift).append(" | ");
        if (filterEnrolmentType != null && !filterEnrolmentType.isBlank())
            sb.append("Tipo: ").append(filterEnrolmentType).append(" | ");
        if (filterStartDate != null)
            sb.append("De: ").append(filterStartDate.format(DATE_FMT)).append(" | ");
        if (filterEndDate != null)
            sb.append("Até: ").append(filterEndDate.format(DATE_FMT)).append(" | ");
        if (filterStudentName != null && !filterStudentName.isBlank())
            sb.append("Aluno: ").append(filterStudentName).append(" | ");

        return sb.length() > 0 ? sb.toString() : null;
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPORTAÇÕES E IMPRESSÃO
    // ═══════════════════════════════════════════════════════════════

    /** A4 Landscape → Lista completa com filtros */
    public void exportEnrolmentListPdf() {
        try {
            List<EnrolmentDTO> list = getFilteredEnrolments();
            if (list.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                        "Nenhuma matrícula para exportar com os filtros actuais.");
                return;
            }
            String filterText = buildFilterDescription();
            byte[] pdf = PdfReportService.generateEnrolmentListReport(list, filterText);
            PdfReportService.streamToResponse(pdf,
                    "lista_matriculas_" + LocalDate.now() + ".pdf", true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar PDF", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    /** A4 Portrait → Relatório da turma seleccionada no filtro */
    public void exportEnrolmentsByClassPdf(Long schoolClassPk) {
        if (schoolClassPk == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                    "Selecione uma turma no filtro 'Turma' para gerar este relatório.");
            return;
        }
        try {
            SchoolClass sc = schoolClassService.getById(schoolClassPk);
            List<EnrolmentDTO> list = enrolmentService.getEnrolmentsBySchoolClassDTO(schoolClassPk);
            if (list.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Turma sem matrículas.");
                return;
            }
            byte[] pdf = PdfReportService.generateEnrolmentsByClassReport(sc, list);
            PdfReportService.streamToResponse(pdf,
                    "turma_" + sc.getClassCode() + "_" + LocalDate.now() + ".pdf", true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar PDF da turma", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    /** A5 → Ficha individual do aluno */
    public void printEnrolmentPdf(Long id) {
        if (id == null)
            return;
        try {
            EnrolmentDTO en = enrolmentService.getAllEnrolments().stream()
                    .filter(e -> id.equals(e.getPhEnrolment()))
                    .findFirst().orElse(null);
            if (en == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Matrícula não encontrada.");
                return;
            }
            StudentDTO st = studentService.getAllStudents().stream()
                    .filter(s -> s.getPkStudent().equals(en.getStudentPk()))
                    .findFirst().orElse(null);
            if (st == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Aluno não encontrado.");
                return;
            }
            byte[] pdf = PdfReportService.generateEnrolmentReport(en, st);
            PdfReportService.streamToResponse(pdf,
                    "matricula_" + en.getEnrolmentNumber() + ".pdf", true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao imprimir matrícula", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    /** A6 → Cartão de matrícula pocket */
    public void printEnrolmentCardA6(Long id) {
        if (id == null)
            return;
        try {
            EnrolmentDTO en = enrolmentService.getAllEnrolments().stream()
                    .filter(e -> id.equals(e.getPhEnrolment()))
                    .findFirst().orElse(null);
            if (en == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Matrícula não encontrada.");
                return;
            }
            StudentDTO st = studentService.getAllStudents().stream()
                    .filter(s -> s.getPkStudent().equals(en.getStudentPk()))
                    .findFirst().orElse(null);
            if (st == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Aluno não encontrado.");
                return;
            }
            byte[] pdf = PdfReportService.generateEnrolmentCardA6(en, st);
            PdfReportService.streamToResponse(pdf,
                    "cartao_" + en.getEnrolmentNumber() + ".pdf", true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar cartão A6", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    private void loadStudents() {
        try {
            students = studentService.getAllStudents();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos", e);
        }
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassRepository.findAll();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas", e);
        }
    }

    private void computeStatistics() {
        try {
            List<EnrolmentDTO> all = getFilteredEnrolments();
            totalEnrolmentCount = all.size();
            YearMonth currentMonth = YearMonth.from(LocalDate.now());
            newEnrolmentCount = all.stream()
                    .filter(e -> e.getEnrolmentData() != null)
                    .filter(e -> YearMonth.from(e.getEnrolmentData()).equals(currentMonth))
                    .count();
            Set<Object> classes = new HashSet<>();
            Set<Object> studentIds = new HashSet<>();
            for (EnrolmentDTO e : all) {
                if (e.getSchoolClassPk() != null)
                    classes.add(e.getSchoolClassPk());
                if (e.getStudentPk() != null)
                    studentIds.add(e.getStudentPk());
            }
            distinctClassesCount = classes.size();
            distinctStudentsCount = studentIds.size();
        } catch (Exception e) {
            totalEnrolmentCount = 0;
            newEnrolmentCount = 0;
            distinctClassesCount = 0;
            distinctStudentsCount = 0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    public void prepareNewEnrolment() {
        enrolment = new Enrolment();
        enrolment.setEnrolmentNumer(enrolmentService.generateNextEnrolmentNumber());
        selectedStudentId = null;
        selectedSchoolClassId = null;
        loadStudents();
        loadSchoolClasses();
    }

    public String saveEnrolment() {
        try {
            if (selectedStudentId == null || selectedSchoolClassId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Matrícula", "Preencha todos os campos obrigatórios.");
                return null;
            }
            // Usa findById que já existe no teu StudentService
            Student student = studentService.findById(selectedStudentId);
            enrolment.setStudent(student);

            SchoolClass schoolClass = schoolClasses.stream()
                    .filter(sc -> selectedSchoolClassId.equals(sc.getPkSchoolClass()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada."));
            enrolment.setSchoolClass(schoolClass);

            enrolmentService.save(enrolment);
            enrolment = new Enrolment();
            selectedStudentId = null;
            selectedSchoolClassId = null;
            init();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Matrícula registada com sucesso");
            return "/management/secretaria/enrolments.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar matrícula", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
            return null;
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma matrícula selecionada");
            return;
        }
        this.selectedId = id;
        EnrolmentDTO dto = enrolmentService.getAllEnrolments().stream()
                .filter(e -> id.equals(e.getPhEnrolment())).findFirst().orElse(null);
        if (dto != null) {
            mapDtoFields(dto, editDto = new EnrolmentDTO());
            mapDtoFields(dto, selectedEnrolment);
            selectedStudentId = dto.getStudentPk();
            selectedSchoolClassId = dto.getSchoolClassPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Matrícula não encontrada");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma matrícula selecionada");
            return;
        }
        this.selectedId = id;
    }

    public void deleteEnrolment() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma matrícula selecionada para eliminar");
            return;
        }
        try {
            enrolmentService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Matrícula eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar matrícula", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            if (selectedStudentId != null)
                editDto.setStudentPk(selectedStudentId);
            if (selectedSchoolClassId != null)
                editDto.setSchoolClassPk(selectedSchoolClassId);
            enrolmentService.update(editDto);
            init();
            editDto = new EnrolmentDTO();
            selectedId = null;
            selectedStudentId = null;
            selectedSchoolClassId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Matrícula atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void viewEnrolmentDetails(Long id) {
        if (id == null)
            return;
        EnrolmentDTO dto = enrolmentService.getAllEnrolments().stream()
                .filter(e -> id.equals(e.getPhEnrolment())).findFirst().orElse(null);
        if (dto != null) {
            this.selectedEnrolment = dto;
            this.selectedId = id;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

    private void mapDtoFields(EnrolmentDTO source, EnrolmentDTO target) {
        target.setPhEnrolment(source.getPhEnrolment());
        target.setEnrolmentNumber(source.getEnrolmentNumber());
        target.setShift(source.getShift());
        target.setEnrolmentType(source.getEnrolmentType());
        target.setStudentPk(source.getStudentPk());
        target.setStudentFullName(source.getStudentFullName());
        target.setStudentNumber(source.getStudentNumber());
        target.setSchoolClassPk(source.getSchoolClassPk());
        target.setSchoolClassCode(source.getSchoolClassCode());
        target.setSchoolClassName(source.getSchoolClassName());
        target.setEnrolmentData(source.getEnrolmentData());
        target.setObs(source.getObs());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public Enrolment getEnrolment() {
        return enrolment;
    }

    public void setEnrolment(Enrolment enrolment) {
        this.enrolment = enrolment;
    }

    public EnrolmentDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(EnrolmentDTO editDto) {
        this.editDto = editDto;
    }

    public EnrolmentDTO getSelectedEnrolment() {
        return selectedEnrolment;
    }

    public void setSelectedEnrolment(EnrolmentDTO selectedEnrolment) {
        this.selectedEnrolment = selectedEnrolment;
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

    public Long getSelectedSchoolClassId() {
        return selectedSchoolClassId;
    }

    public void setSelectedSchoolClassId(Long selectedSchoolClassId) {
        this.selectedSchoolClassId = selectedSchoolClassId;
    }

    public EnrolmentLazyModel getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(EnrolmentLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public Long getFilterSchoolClassId() {
        return filterSchoolClassId;
    }

    public void setFilterSchoolClassId(Long filterSchoolClassId) {
        this.filterSchoolClassId = filterSchoolClassId;
    }

    public String getFilterShift() {
        return filterShift;
    }

    public void setFilterShift(String filterShift) {
        this.filterShift = filterShift;
    }

    public String getFilterEnrolmentType() {
        return filterEnrolmentType;
    }

    public void setFilterEnrolmentType(String filterEnrolmentType) {
        this.filterEnrolmentType = filterEnrolmentType;
    }

    public LocalDate getFilterStartDate() {
        return filterStartDate;
    }

    public void setFilterStartDate(LocalDate filterStartDate) {
        this.filterStartDate = filterStartDate;
    }

    public LocalDate getFilterEndDate() {
        return filterEndDate;
    }

    public void setFilterEndDate(LocalDate filterEndDate) {
        this.filterEndDate = filterEndDate;
    }

    public String getFilterStudentName() {
        return filterStudentName;
    }

    public void setFilterStudentName(String filterStudentName) {
        this.filterStudentName = filterStudentName;
    }

    public long getTotalEnrolmentCount() {
        return totalEnrolmentCount;
    }

    public long getNewEnrolmentCount() {
        return newEnrolmentCount;
    }

    public long getDistinctClassesCount() {
        return distinctClassesCount;
    }

    public long getDistinctStudentsCount() {
        return distinctStudentsCount;
    }

    public List<StudentDTO> getStudents() {
        return students;
    }

    public List<SchoolClass> getSchoolClasses() {
        return schoolClasses;
    }

    public ShiftType[] getShifts() {
        return ShiftType.values();
    }

    public EnrolmentType[] getEnrolmentTypes() {
        return EnrolmentType.values();
    }

    public List<EnrolmentDTO> getEnrolments() {
        return enrolmentService.getAllEnrolments();
    }
}