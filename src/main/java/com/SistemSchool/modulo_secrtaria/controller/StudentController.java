package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.lazy.StudentLazyModel;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class StudentController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());

    private final StudentService studentService;
    private StudentLazyModel lazyModel;

    // ── Diálogo de criação (usa a entidade diretamente) ──
    private Student novoAluno;
    private UploadedFile ficheiroFoto;
    // ── Filtros avançados ──
    private StudentStatus filterStatus;
    private Gender filterGender;
    private LocalDate filterBirthDateFrom;
    private LocalDate filterBirthDateTo;
    private String filterStudentName;

    // ── Diálogo de edição (usa o DTO diretamente) ──
    private StudentDTO editDto;
    private UploadedFile uploadedPhoto;

    private StudentDTO alunoSelecionado;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        this.lazyModel = new StudentLazyModel(studentService);
        prepararNovo();
    }

    /**
     * Usado como {@code action} num link/menu (ex:
     * {@code action="#{studentController.load}"})
     * para carregar o modelo antes de navegar para a listagem de alunos.
     */
    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de alunos", e);
        }
        return "/management/secretaria/students.xhtml?faces-redirect=true";
    }

    // ─────────────────────────────────────────────────────────────
    // CRIAÇÃO
    // ─────────────────────────────────────────────────────────────

    public void prepararNovo() {
        this.novoAluno = new Student();
        this.novoAluno.setStatus(StudentStatus.ACTIVE);
        this.ficheiroFoto = null;
    }

    public void salvar() {
        try {
            studentService.save(novoAluno, ficheiroFoto);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Aluno registado com sucesso.");
            PrimeFaces.current().executeScript("PF('createStudentDialog').hide()");
            PrimeFaces.current().ajax().update("formAlunos:tblAlunos", "formAlunos:growl");
            prepararNovo();
        } catch (RuntimeException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao guardar", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIÇÃO (formulário com ajax="false" por causa do p:fileUpload simple)
    // ─────────────────────────────────────────────────────────────

    public void prepararEdicao(StudentDTO dto) {
        this.alunoSelecionado = dto;
        Student entidade = studentService.findById(dto.getPkStudent());
        this.editDto = StudentDTO.fromEntity(entidade);
        this.uploadedPhoto = null;
    }

    /**
     * Ação do botão "Guardar" do diálogo de edição.
     * O botão usa ajax="false" (postback normal) porque o p:fileUpload em
     * mode="simple" só funciona com submit tradicional do formulário.
     * Por isso usamos Flash + faces-redirect para preservar as mensagens
     * e evitar reenvio do formulário ao dar refresh (padrão PRG).
     */
    public String saveUpdate() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        try {
            studentService.update(editDto, uploadedPhoto);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dados do aluno atualizados.");
        } catch (RuntimeException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao guardar", e.getMessage());
            LOGGER.log(Level.WARNING, "Erro ao atualizar aluno", e);
        }
        return "/management/secretaria/students.xhtml?faces-redirect=true";
    }

    // ─────────────────────────────────────────────────────────────
    // ELIMINAÇÃO
    // ─────────────────────────────────────────────────────────────

    public void eliminar(StudentDTO dto) {
        try {
            studentService.delete(dto.getPkStudent());
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Aluno eliminado.");
            PrimeFaces.current().ajax().update("formAlunos:tblAlunos", "formAlunos:growl");
        } catch (RuntimeException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao eliminar", e.getMessage());
        }
    }

    public void applyFilters() {
        lazyModel.setFilterStatus(filterStatus);
        lazyModel.setFilterGender(filterGender);
        lazyModel.setFilterBirthDateFrom(filterBirthDateFrom);
        lazyModel.setFilterBirthDateTo(filterBirthDateTo);
        lazyModel.setFilterStudentName(filterStudentName);
        PrimeFaces.current().ajax().update("formAlunos:dtAlunos");
    }

    public void clearFilters() {
        this.filterStatus = null;
        this.filterGender = null;
        this.filterBirthDateFrom = null;
        this.filterBirthDateTo = null;
        this.filterStudentName = null;
        lazyModel.clearFilters();
        PrimeFaces.current().ajax().update("formAlunos:dtAlunos");
    }

    // ─────────────────────────────────────────────────────────────
    // UPLOAD DE FOTO (diálogo de criação — p:fileUpload mode="advanced")
    // ─────────────────────────────────────────────────────────────

    public void handleFileUpload(FileUploadEvent event) {
        this.ficheiroFoto = event.getFile();
        addMessage(FacesMessage.SEVERITY_INFO, "Ficheiro recebido",
                event.getFile().getFileName() + " será guardado ao confirmar.");
    }

    // ─────────────────────────────────────────────────────────────
    // PREENCHIMENTO AUTOMÁTICO DO NÚMERO DE ALUNO
    // ─────────────────────────────────────────────────────────────

    public void gerarNumeroAluno() {
        if (novoAluno == null) {
            return;
        }
        novoAluno.setSudentNumber(studentService.gerarNumeroAluno());
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDAÇÃO DO BI EM TEMPO REAL (p:ajax event="blur")
    // ─────────────────────────────────────────────────────────────

    public void validarBI() {
        String bi = novoAluno != null ? novoAluno.getBiNumber()
                : (editDto != null ? editDto.getBiNumber() : null);
        if (bi == null || bi.isBlank()) {
            return;
        }
        try {
            studentService.validarBI(bi);
            addMessage(FacesMessage.SEVERITY_INFO, "BI válido", "O número de BI está no formato correto.");
        } catch (RuntimeException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "BI inválido", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS (cartões da página de listagem)
    // ─────────────────────────────────────────────────────────────

    public long getTotalStudentsCount() {
        return studentService.getTotalStudentsCount();
    }

    public long getActiveStudentsCount() {
        return studentService.getActiveStudentsCount();
    }

    public long getGraduatedStudentsCount() {
        return studentService.getGraduatedStudentsCount();
    }

    public long getNewStudentsThisMonthCount() {
        return studentService.getNewStudentsThisMonthCount();
    }

    // ═══════════════════════════════════════════════════════════
    // EXPORTAÇÕES / IMPRESSÃO (implementa a lógica PDF conforme necessário)
    // ═══════════════════════════════════════════════════════════

    public void exportStudentsPdf() {
        // TODO: gerar PDF com todos os alunos (respeitando filtros atuais se desejado)
        addMessage(FacesMessage.SEVERITY_INFO, "Exportar", "PDF completo em desenvolvimento.");
    }

    public void exportActiveStudentsPdf() {
        // TODO: gerar PDF apenas com alunos ativos
        addMessage(FacesMessage.SEVERITY_INFO, "Exportar", "PDF de alunos ativos em desenvolvimento.");
    }

    public void printStudentCard(Long studentId) {
        // TODO: gerar cartão do aluno (A6)
        addMessage(FacesMessage.SEVERITY_INFO, "Imprimir", "Cartão do aluno em desenvolvimento.");
    }

    public void printStudentDetailsPdf(Long studentId) {
        // TODO: gerar ficha detalhada do aluno
        addMessage(FacesMessage.SEVERITY_INFO, "Imprimir", "Ficha detalhada em desenvolvimento.");
    }
    // ─────────────────────────────────────────────────────────────
    // LISTAS AUXILIARES PARA COMBOS (p:selectOneMenu)
    // ─────────────────────────────────────────────────────────────

    /** Usado no diálogo de criação (student-create.xhtml). */
    public Gender[] getGeneros() {
        return Gender.values();
    }

    /** Usado no diálogo de criação (student-create.xhtml). */
    public StudentStatus[] getEstados() {
        return StudentStatus.values();
    }

    /** Usado no diálogo de edição (student-edit.xhtml). */
    public Gender[] getGenders() {
        return Gender.values();
    }

    /** Usado no diálogo de edição (student-edit.xhtml). */
    public StudentStatus[] getStudentStatuses() {
        return StudentStatus.values();
    }

    // ─────────────────────────────────────────────────────────────
    // AUXILIAR
    // ─────────────────────────────────────────────────────────────

    private void addMessage(FacesMessage.Severity severidade, String sumario, String detalhe) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severidade, sumario, detalhe));
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

    public StudentStatus[] getStatusList() {
        return StudentStatus.values();
    }

    public Gender[] getGenderList() {
        return Gender.values();
    }

    public LazyDataModel<StudentDTO> getLazyModel() {
        return lazyModel;
    }

    public Student getNovoAluno() {
        return novoAluno;
    }

    public void setNovoAluno(Student novoAluno) {
        this.novoAluno = novoAluno;
    }

    public StudentDTO getAlunoSelecionado() {
        return alunoSelecionado;
    }

    public void setAlunoSelecionado(StudentDTO alunoSelecionado) {
        this.alunoSelecionado = alunoSelecionado;
    }

    public StudentDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(StudentDTO editDto) {
        this.editDto = editDto;
    }

    public UploadedFile getUploadedPhoto() {
        return uploadedPhoto;
    }

    public void setUploadedPhoto(UploadedFile uploadedPhoto) {
        this.uploadedPhoto = uploadedPhoto;
    }
}