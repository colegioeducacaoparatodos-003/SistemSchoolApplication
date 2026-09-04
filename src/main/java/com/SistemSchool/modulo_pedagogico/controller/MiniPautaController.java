package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class MiniPautaController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MiniPautaController.class.getName());

    // ── FILTROS ──
    private Long selectedSchoolClassId;
    private String selectedEducationLevel;
    private List<Long> selectedDisciplineIds = new ArrayList<>();
    private List<Trimester> selectedTrimesters = new ArrayList<>();

    // ── LISTAS ──
    private List<SchoolClass> schoolClasses = new ArrayList<>();
    private List<Discipline> disciplines = new ArrayList<>();

    // ── RESULTADO ──
    private boolean rendered = false;
    private List<MiniPautaBlock> reportBlocks = new ArrayList<>();

    // ── SERVIÇOS ──
    @Inject
    private TrimesterResultService trimesterResultService;
    @Inject
    private SchoolClassRepository schoolClassRepository;
    @Inject
    private DisciplineService disciplineService;

    @PostConstruct
    public void init() {
        loadSchoolClasses();
        loadDisciplines();

        // ═══════════════════════════════════════════════════════════════
        // Pré-preenchimento via flash (vem do botão "Mini-Pauta desta turma")
        //
        // IMPORTANTE: este bloco NUNCA pode deixar escapar uma exceção não
        // tratada. Se o @PostConstruct lançar, o CDI/Spring falha ao criar
        // o bean e todas as expressões EL "#{miniPautaController...}" na
        // página passam a resolver para null (erro
        // "PropertyNotFoundException: ... identifier [miniPautaController]
        // resolved to null" durante PROCESS_VALIDATIONS). Já aconteceu
        // antes noutros controllers deste módulo pela mesma razão:
        // FacesContext pode ainda não estar disponível no momento em que
        // o bean é instanciado.
        // ═══════════════════════════════════════════════════════════════
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null) {
                return;
            }
            Map<String, Object> flash = ctx.getExternalContext().getFlash();
            if (flash == null || !Boolean.TRUE.equals(flash.get("mpAutoSearch"))) {
                return;
            }

            Object schoolClassIdObj = flash.get("mpSchoolClassId");
            if (schoolClassIdObj instanceof Number) {
                selectedSchoolClassId = ((Number) schoolClassIdObj).longValue();
            } else if (schoolClassIdObj != null) {
                LOGGER.log(Level.WARNING,
                        "mpSchoolClassId no flash scope com tipo inesperado: {0}",
                        schoolClassIdObj.getClass());
            }

            Object trimObj = flash.get("mpTrimester");
            if (trimObj != null) {
                String trimStr = trimObj.toString();
                if (!trimStr.isBlank()) {
                    try {
                        selectedTrimesters = new ArrayList<>(List.of(Trimester.valueOf(trimStr)));
                    } catch (IllegalArgumentException ignored) {
                        // valor de trimestre desconhecido — ignora e mantém filtro vazio
                    }
                }
            }

            flash.remove("mpAutoSearch");
            flash.remove("mpSchoolClassId");
            flash.remove("mpTrimester");

        } catch (Exception e) {
            // Nunca deixar o @PostConstruct falhar por causa do pré-preenchimento.
            // Pior caso: os filtros ficam vazios e o utilizador seleciona manualmente.
            LOGGER.log(Level.SEVERE, "Erro ao pré-preencher filtros da Mini-Pauta via flash scope", e);
        }
    }

    public void search() {
        if (selectedSchoolClassId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione uma turma.");
            rendered = false;
            return;
        }

        rendered = true;
        reportBlocks = new ArrayList<>();

        try {
            SchoolClass selectedClass = schoolClasses.stream()
                    .filter(sc -> sc.getPkSchoolClass().equals(selectedSchoolClassId))
                    .findFirst().orElse(null);

            if (selectedClass == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Turma não encontrada.");
                rendered = false;
                return;
            }

            String educationLevel = resolveEducationLevel(selectedClass);
            String scaleLabel = "PRIMARIO".equals(educationLevel) ? "0–10" : "0–20";

            List<TrimesterResultDTO> all = trimesterResultService.getAllResults();
            if (all == null) all = new ArrayList<>();

            final String className = selectedClass.getClassName();

            List<TrimesterResultDTO> filtered = all.stream()
                    .filter(r -> r.getSchoolClassName() != null && className.equalsIgnoreCase(r.getSchoolClassName()))
                    .filter(r -> selectedDisciplineIds == null || selectedDisciplineIds.isEmpty() ||
                            (r.getDisciplinePk() != null && selectedDisciplineIds.contains(r.getDisciplinePk())))
                    .filter(r -> selectedTrimesters == null || selectedTrimesters.isEmpty() ||
                            (r.getTrimester() != null && selectedTrimesters.contains(r.getTrimester())))
                    .sorted(Comparator
                            .comparing(TrimesterResultDTO::getDisciplineName, Comparator.nullsFirst(String::compareTo))
                            .thenComparing(TrimesterResultDTO::getTrimester, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(TrimesterResultDTO::getStudentFullName, Comparator.nullsFirst(String::compareTo)))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_INFO, "Mini-Pauta",
                        "Nenhum resultado encontrado para os filtros selecionados.");
                return;
            }

            Map<String, Map<Trimester, List<TrimesterResultDTO>>> grouped = new LinkedHashMap<>();
            for (TrimesterResultDTO r : filtered) {
                String discName = r.getDisciplineName() != null ? r.getDisciplineName() : "Sem Disciplina";
                Trimester trim = r.getTrimester();
                grouped.computeIfAbsent(discName, k -> new LinkedHashMap<>())
                       .computeIfAbsent(trim, k -> new ArrayList<>())
                       .add(r);
            }

            for (Map.Entry<String, Map<Trimester, List<TrimesterResultDTO>>> discEntry : grouped.entrySet()) {
                for (Map.Entry<Trimester, List<TrimesterResultDTO>> trimEntry : discEntry.getValue().entrySet()) {
                    MiniPautaBlock block = new MiniPautaBlock();
                    block.setDisciplineName(discEntry.getKey());
                    block.setTrimester(trimEntry.getKey() != null ? trimEntry.getKey().toString() : "");
                    block.setScaleLabel(scaleLabel);
                    block.setEducationLevel(educationLevel);

                    int rowNum = 1;
                    for (TrimesterResultDTO r : trimEntry.getValue()) {
                        MiniPautaRow row = new MiniPautaRow();
                        row.setRowNumber(rowNum++);
                        row.setStudentName(r.getStudentFullName() != null ? r.getStudentFullName() : "—");
                        row.setMac(r.getMac());
                        row.setNpt(r.getNpt());
                        row.setMt(r.getMt());
                        row.setSituation(r.getSituation() != null ? r.getSituation().toString() : "—");
                        block.getRows().add(row);
                    }
                    reportBlocks.add(block);
                }
            }

            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                    "Mini-Pauta gerada com " + reportBlocks.size() + " bloco(s).");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar mini-pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível gerar a mini-pauta: " + e.getMessage());
            rendered = false;
        }
    }

    public void clear() {
        selectedSchoolClassId = null;
        selectedEducationLevel = null;
        selectedDisciplineIds = new ArrayList<>();
        selectedTrimesters = new ArrayList<>();
        rendered = false;
        reportBlocks = new ArrayList<>();
    }

    private String resolveEducationLevel(SchoolClass schoolClass) {
        if (selectedEducationLevel != null && !selectedEducationLevel.isBlank()) {
            return selectedEducationLevel;
        }
        String name = schoolClass.getClassName();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.matches(".*\\b(1º|2º|3º|4º|1o|2o|3o|4o|primária|primaria|inicial|pré|pre)\\b.*")) {
                return "PRIMARIO";
            }
        }
        return "I_CICLO";
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassRepository.findAll();
            if (schoolClasses == null) schoolClasses = new ArrayList<>();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao carregar turmas: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas", e);
        }
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllActive();
            if (disciplines == null) disciplines = new ArrayList<>();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao carregar disciplinas: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // VIEW MODELS INTERNOS
    // ═══════════════════════════════════════════════════════════════

    public static class MiniPautaBlock implements Serializable {
        private static final long serialVersionUID = 1L;
        private String disciplineName;
        private String trimester;
        private String scaleLabel;
        private String educationLevel;
        private List<MiniPautaRow> rows = new ArrayList<>();

        public boolean isEmpty() { return rows == null || rows.isEmpty(); }

        public String getDisciplineName() { return disciplineName; }
        public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }
        public String getTrimester() { return trimester; }
        public void setTrimester(String trimester) { this.trimester = trimester; }
        public String getScaleLabel() { return scaleLabel; }
        public void setScaleLabel(String scaleLabel) { this.scaleLabel = scaleLabel; }
        public String getEducationLevel() { return educationLevel; }
        public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
        public List<MiniPautaRow> getRows() { return rows; }
        public void setRows(List<MiniPautaRow> rows) { this.rows = rows; }
    }

    public static class MiniPautaRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private int rowNumber;
        private String studentName;
        private Double mac;
        private Double npt;
        private Double mt;
        private String situation;

        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Double getMac() { return mac; }
        public void setMac(Double mac) { this.mac = mac; }
        public String getMacFormatted() { return formatScore(mac); }
        public Double getNpt() { return npt; }
        public void setNpt(Double npt) { this.npt = npt; }
        public String getNptFormatted() { return formatScore(npt); }
        public Double getMt() { return mt; }
        public void setMt(Double mt) { this.mt = mt; }
        public String getMtFormatted() { return formatScore(mt); }
        public String getSituation() { return situation; }
        public void setSituation(String situation) { this.situation = situation; }

        private static String formatScore(Double v) {
            if (v == null) return "—";
            return String.format(java.util.Locale.forLanguageTag("pt"), "%.1f", v).replace('.', ',');
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public Long getSelectedSchoolClassId() { return selectedSchoolClassId; }
    public void setSelectedSchoolClassId(Long selectedSchoolClassId) { this.selectedSchoolClassId = selectedSchoolClassId; }
    public String getSelectedEducationLevel() { return selectedEducationLevel; }
    public void setSelectedEducationLevel(String selectedEducationLevel) { this.selectedEducationLevel = selectedEducationLevel; }
    public List<Long> getSelectedDisciplineIds() { return selectedDisciplineIds; }
    public void setSelectedDisciplineIds(List<Long> selectedDisciplineIds) { this.selectedDisciplineIds = selectedDisciplineIds; }
    public List<Trimester> getSelectedTrimesters() { return selectedTrimesters; }
    public void setSelectedTrimesters(List<Trimester> selectedTrimesters) { this.selectedTrimesters = selectedTrimesters; }
    public List<SchoolClass> getSchoolClasses() { return schoolClasses; }
    public List<Discipline> getDisciplines() { return disciplines; }
    public List<String> getEducationLevels() { return List.of("PRIMARIO", "I_CICLO"); }
    public Trimester[] getTrimesters() { return Trimester.values(); }
    public boolean isRendered() { return rendered; }
    public void setRendered(boolean rendered) { this.rendered = rendered; }
    public List<MiniPautaBlock> getReportBlocks() { return reportBlocks; }
    public void setReportBlocks(List<MiniPautaBlock> reportBlocks) { this.reportBlocks = reportBlocks; }
}