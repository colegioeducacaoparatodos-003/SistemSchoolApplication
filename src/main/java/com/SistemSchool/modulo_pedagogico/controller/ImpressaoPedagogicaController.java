package com.SistemSchool.modulo_pedagogico.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;

import com.SistemSchool.modulo_pedagogico.dto.BoletimDTO;
import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.dto.PautaDTO;
import com.SistemSchool.modulo_pedagogico.report.BoletimPdfGenerator;
import com.SistemSchool.modulo_pedagogico.report.PautaPdfGenerator;
import com.SistemSchool.modulo_pedagogico.service.BoletimService;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.PautaService;
import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.dto.SchoolClassDTO;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import com.SistemSchool.modulo_secrtaria.service.SchoolClassService;

@Named
@ViewScoped
public class ImpressaoPedagogicaController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ImpressaoPedagogicaController.class.getName());

    // Ajusta ao nome real da tua escola (ou lê de uma tabela de configuração)
    private static final String NOME_ESCOLA = "Escola Primária Nº 46 ABD Aprendizagem";

    // ─────────────────────────────────────────────────────────────
    // SELEÇÕES DO FORMULÁRIO
    // ─────────────────────────────────────────────────────────────

    private Long selectedEnrolmentId;      // para o Boletim
    private Long selectedSchoolClassId;    // para a Pauta
    private Long selectedDisciplineId;     // para a Pauta
    private Integer selectedTrimester;

    // Lançamento manual do Boletim (comportamento / observação / período)
    private String period;
    private String behavior;
    private String observation;

    // Listas para dropdowns
    private List<EnrolmentDTO> enrolments = new java.util.ArrayList<>();
    private List<SchoolClassDTO> schoolClasses = new java.util.ArrayList<>();
    private List<DisciplineDTO> disciplines = new java.util.ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private BoletimService boletimService;

    @Inject
    private PautaService pautaService;

    @Inject
    private BoletimPdfGenerator boletimPdfGenerator;

    @Inject
    private PautaPdfGenerator pautaPdfGenerator;

    @Inject
    private EnrolmentService enrolmentService;

    @Inject
    private SchoolClassService schoolClassService;

    @Inject
    private DisciplineService disciplineService;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        loadEnrolments();
        loadSchoolClasses();
        loadDisciplines();
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar impressão", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar dados de impressão pedagógica", e);
        }
        return "/management/pedagogico/impressao.xhtml?faces-redirect=true";
    }

    private void loadEnrolments() {
        try {
            enrolments = enrolmentService.getAllEnrolments();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matrículas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar matrículas para impressão do Boletim", e);
        }
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassService.getAllSchoolClasses();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas para impressão da Pauta", e);
        }
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllDisciplines();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas para impressão da Pauta", e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // AÇÕES DE IMPRESSÃO
    // ─────────────────────────────────────────────────────────────

    public void imprimirBoletim() {
        if (selectedEnrolmentId == null || selectedTrimester == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Boletim", "Selecione a matrícula e o trimestre.");
            return;
        }
        try {
            BoletimDTO dto = boletimService.gerarBoletim(selectedEnrolmentId, selectedTrimester);
            byte[] pdf = boletimPdfGenerator.gerar(dto, NOME_ESCOLA);
            streamPdf(pdf, "boletim-" + selectedEnrolmentId + "-t" + selectedTrimester + ".pdf");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar boletim", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Boletim", e.getMessage());
        }
    }

    public void imprimirPauta() {
        if (selectedSchoolClassId == null || selectedDisciplineId == null || selectedTrimester == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Pauta", "Selecione a turma, a disciplina e o trimestre.");
            return;
        }
        try {
            PautaDTO dto = pautaService.gerarPauta(selectedSchoolClassId, selectedDisciplineId, selectedTrimester);
            byte[] pdf = pautaPdfGenerator.gerar(dto, NOME_ESCOLA);
            streamPdf(pdf, "pauta-" + selectedSchoolClassId + "-" + selectedDisciplineId + "-t" + selectedTrimester + ".pdf");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pauta", e.getMessage());
        }
    }

    private void streamPdf(byte[] pdf, String nomeArquivo) {

        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + nomeArquivo + "\"");
        response.setContentLength(pdf.length);

        try {
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
            fc.responseComplete();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao enviar o PDF para o browser", e);
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

    public Long getSelectedEnrolmentId() {
        return selectedEnrolmentId;
    }

    public void setSelectedEnrolmentId(Long selectedEnrolmentId) {
        this.selectedEnrolmentId = selectedEnrolmentId;
    }

    public Long getSelectedSchoolClassId() {
        return selectedSchoolClassId;
    }

    public void setSelectedSchoolClassId(Long selectedSchoolClassId) {
        this.selectedSchoolClassId = selectedSchoolClassId;
    }

    public Long getSelectedDisciplineId() {
        return selectedDisciplineId;
    }

    public void setSelectedDisciplineId(Long selectedDisciplineId) {
        this.selectedDisciplineId = selectedDisciplineId;
    }

    public Integer getSelectedTrimester() {
        return selectedTrimester;
    }

    public void setSelectedTrimester(Integer selectedTrimester) {
        this.selectedTrimester = selectedTrimester;
    }

    public List<EnrolmentDTO> getEnrolments() {
        return enrolments;
    }

    public List<SchoolClassDTO> getSchoolClasses() {
        return schoolClasses;
    }

    public List<DisciplineDTO> getDisciplines() {
        return disciplines;
    }

    public Integer[] getTrimesters() {
        return new Integer[] { 1, 2, 3 };
    }
}