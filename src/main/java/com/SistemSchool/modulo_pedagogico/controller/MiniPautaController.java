package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.io.Classe;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import com.SistemSchool.modulo_secrtaria.service.StudentService;
import com.SistemSchool.report.OpenPdfAuditPageEvent;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class MiniPautaController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MiniPautaController.class.getName());

    // Paleta reaproveitada do CSS da view (--enr-black, --yellow-lt, --enr-muted)
    private static final Color PDF_HEADER_BG = new Color(17, 24, 39);
    private static final Color PDF_HEADER_TEXT = new Color(255, 210, 63);
    private static final Color PDF_TITLE_TEXT = new Color(17, 24, 39);
    private static final Color PDF_MUTED_TEXT = new Color(92, 104, 117);
    private static final Color PDF_PANEL_BG = new Color(245, 248, 250);
    private static final Color PDF_ACCENT_BG = new Color(17, 94, 124);
    private static final Color PDF_ACCENT_BG_DEEP = new Color(10, 67, 88);
    private static final Color PDF_META_LABEL_BG = new Color(236, 240, 244);
    private static final Color PDF_STRIPE_BG = new Color(250, 251, 252);
    private static final String DEFAULT_HEADER_PLACEHOLDER = "__________";
    private static final Color PDF_ACCENT_TEXT = new Color(255, 255, 255);
    private static final Color PDF_BORDER = new Color(206, 214, 222);
    private static final byte[] XLS_HEADER_BG = new byte[]{20, 20, 20};
    private static final byte[] XLS_GROUP_BG = new byte[]{38, 38, 38};
    private static final byte[] XLS_HEADER_TEXT = new byte[]{(byte) 255, (byte) 210, 63};
    private static final String[] SUB_HEADERS = {"MAC", "NPT", "MT"};
    private static final String[] MINI_PAUTA_SUB_HEADERS = {"MAC", "NPT", "MT", "SITUAÇÃO"};

    // Caminho do logotipo institucional dentro do webapp (webapp/resource/imgs/logo.png)
    private static final String LOGO_RESOURCE_PATH = "/resources/imgs/logo.jpg";
    private static final float LOGO_MAX_WIDTH = 60f;
    private static final float LOGO_MAX_HEIGHT = 60f;

    // ── CABEÇALHO INSTITUCIONAL (preencher / ligar a um bean de configuração) ──
    private String schoolName = "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS";
    private String municipality = "__________";
    private String educationDirectorate = "__________";
    private String schoolYear = "2026/2027";

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

    // Indica se a pauta atualmente renderizada é a "pauta em branco" (gerada a
    // partir dos alunos matriculados, sem nenhum resultado lançado) — usado
    // para ajustar textos/observações no PDF e no nome do ficheiro exportado.
    private boolean blankMode = false;
    private String finalSextaClasseMode = "PREENCHIDA";

    // ── SERVIÇOS ──
    @Inject
    private TrimesterResultService trimesterResultService;
    @Inject
    private GradeRepository gradeRepository;
    @Inject
    private ScheduleRepository scheduleRepository;
    @Inject
    private SchoolClassRepository schoolClassRepository;
    @Inject
    private DisciplineService disciplineService;
    @Inject
    private EnrolmentService enrolmentService;
    @Inject
    private StudentService studentService;

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
        // página passam a resolver para null.
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
            LOGGER.log(Level.SEVERE, "Erro ao pré-preencher filtros da Mini-Pauta via flash scope", e);
        }
    }

    public void search() {
        search(false);
    }

    private void search(boolean includeAllTrimesters) {
        if (selectedSchoolClassId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione uma turma.");
            rendered = false;
            return;
        }

        rendered = true;
        blankMode = false;
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
                    .filter(r -> includeAllTrimesters || selectedTrimesters == null || selectedTrimesters.isEmpty() ||
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

                Set<Long> enrolmentIds = filtered.stream()
                    .map(TrimesterResultDTO::getEnrolmentPk)
                    .filter(enrolmentId -> enrolmentId != null)
                    .collect(Collectors.toSet());
                Map<ObservationKey, LinkedHashSet<String>> observations = loadObservations(filtered, enrolmentIds);

            // ═══════════════════════════════════════════════════════════
            // NOVO FORMATO (oficial): UM bloco por DISCIPLINA, com os três
            // trimestres lado a lado — MAC | NPT | MT em cada trimestre.
            // Os alunos são a união de todos os trimestres, ordenada por nome.
            // ═══════════════════════════════════════════════════════════
            Map<String, Map<Trimester, List<TrimesterResultDTO>>> grouped = new LinkedHashMap<>();
            for (TrimesterResultDTO r : filtered) {
                String discName = r.getDisciplineName() != null ? r.getDisciplineName() : "Sem Disciplina";
                Trimester trim = r.getTrimester();
                grouped.computeIfAbsent(discName, k -> new LinkedHashMap<>())
                       .computeIfAbsent(trim, k -> new ArrayList<>())
                       .add(r);
            }

            for (Map.Entry<String, Map<Trimester, List<TrimesterResultDTO>>> discEntry : grouped.entrySet()) {
                Map<Trimester, List<TrimesterResultDTO>> trimMap = discEntry.getValue();

                // União de alunos de todos os trimestres, ordenada alfabeticamente
                List<String> studentNames = trimMap.values().stream()
                        .flatMap(List::stream)
                        .map(r -> r.getStudentFullName() != null ? r.getStudentFullName() : "—")
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.toList());

                MiniPautaBlock block = new MiniPautaBlock();
                block.setDisciplineName(discEntry.getKey());
                block.setScaleLabel(scaleLabel);
                block.setEducationLevel(educationLevel);

                int rowNum = 1;
                for (String studentName : studentNames) {
                    MiniPautaRow row = new MiniPautaRow();
                    row.setRowNumber(rowNum++);
                    row.setStudentName(studentName);

                    for (Trimester t : Trimester.values()) {
                        TrimesterScore score = new TrimesterScore();
                        TrimesterResultDTO r = trimMap.getOrDefault(t, List.of()).stream()
                                .filter(x -> studentName.equals(
                                        x.getStudentFullName() != null ? x.getStudentFullName() : "—"))
                                .findFirst()
                                .orElse(null);
                        if (r != null) {
                            score.setMac(r.getMac());
                            score.setNpt(r.getNpt());
                            score.setMt(r.getMt());
                            score.setSituation(r.getSituation() != null ? r.getSituation().getDescricao() : null);
                                score.setObservation(resolveObservation(observations,
                                    r.getEnrolmentPk(), r.getDisciplinePk(), t, r.getObs()));
                        }
                        row.getScores().put(String.valueOf(t.ordinal() + 1), score);
                    }
                    block.getRows().add(row);
                }
                reportBlocks.add(block);
            }

            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                    "Mini-Pauta gerada com " + reportBlocks.size() + " disciplina(s).");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar mini-pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível gerar a mini-pauta: " + e.getMessage());
            rendered = false;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PAUTA EM BRANCO — lista de alunos matriculados na turma, sem
    // depender de nenhum TrimesterResult já lançado. As colunas MAC/NPT/MT
    // ficam vazias (célula em branco, não "—") para o professor preencher
    // à mão em papel antes de os dados serem lançados no sistema.
    // ═══════════════════════════════════════════════════════════════

    public void gerarPautaEmBranco() {
        if (selectedSchoolClassId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione uma turma.");
            rendered = false;
            return;
        }

        rendered = true;
        blankMode = true;
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

            List<EnrolmentDTO> enrolments = enrolmentService.getEnrolmentsBySchoolClassDTO(selectedSchoolClassId);
            if (enrolments == null) {
                enrolments = new ArrayList<>();
            }

            if (enrolments.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_INFO, "Mini-Pauta", "Nenhum aluno matriculado nesta turma.");
                rendered = false;
                return;
            }

            // A query do EnrolmentRepository já devolve ordenado por
            // e.student.fullName — não é preciso ordenar de novo aqui.

            List<Schedule> classSchedules = scheduleRepository
                    .findBySchoolClassPkWithRelations(selectedSchoolClassId);
            Map<Long, Discipline> activeDisciplines = disciplines.stream()
                    .filter(discipline -> discipline.getPkDiscipline() != null)
                    .collect(Collectors.toMap(Discipline::getPkDiscipline, discipline -> discipline,
                        (first, duplicate) -> first, LinkedHashMap::new));
            Map<Long, Discipline> classDisciplines = new LinkedHashMap<>();
            if (classSchedules != null) {
                for (Schedule schedule : classSchedules) {
                    Discipline discipline = schedule.getDiscipline();
                    if (discipline != null && discipline.getPkDiscipline() != null) {
                        classDisciplines.putIfAbsent(discipline.getPkDiscipline(), discipline);
                    }
                }
            }

                Set<Long> classEnrolmentIds = enrolments.stream()
                    .map(EnrolmentDTO::getPhEnrolment)
                    .filter(enrolmentId -> enrolmentId != null)
                    .collect(Collectors.toSet());
                List<TrimesterResultDTO> classResults = trimesterResultService.getAllResults();
                Map<Long, Map<Long, Map<Trimester, TrimesterResultDTO>>> resultsByEnrolment = new HashMap<>();
                Map<ObservationKey, LinkedHashSet<String>> observations =
                    loadObservations(classResults, classEnrolmentIds);
                if (classResults != null) {
                    for (TrimesterResultDTO result : classResults) {
                        if (result.getEnrolmentPk() == null
                                || !classEnrolmentIds.contains(result.getEnrolmentPk())
                                || result.getDisciplinePk() == null) {
                            continue;
                        }
                        classDisciplines.computeIfAbsent(result.getDisciplinePk(), disciplineId -> {
                            Discipline discipline = activeDisciplines.get(disciplineId);
                            if (discipline == null) {
                                discipline = new Discipline();
                                discipline.setPkDiscipline(disciplineId);
                                discipline.setDisciplineName(result.getDisciplineName() != null
                                        ? result.getDisciplineName() : "Disciplina " + disciplineId);
                            }
                            return discipline;
                        });
                        if (result.getTrimester() != null) {
                            resultsByEnrolment
                                    .computeIfAbsent(result.getEnrolmentPk(), key -> new HashMap<>())
                                    .computeIfAbsent(result.getDisciplinePk(), key -> new HashMap<>())
                                    .put(result.getTrimester(), result);
                        }
                    }
                }

            if (selectedDisciplineIds != null) {
                for (Long disciplineId : selectedDisciplineIds) {
                    Discipline discipline = activeDisciplines.get(disciplineId);
                    if (discipline != null) {
                        classDisciplines.putIfAbsent(disciplineId, discipline);
                    }
                }
            }

            List<Discipline> targetDisciplines = classDisciplines.values().stream()
                    .filter(discipline -> selectedDisciplineIds == null || selectedDisciplineIds.isEmpty()
                            || selectedDisciplineIds.contains(discipline.getPkDiscipline()))
                    .sorted(Comparator.comparing(Discipline::getDisciplineName,
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .collect(Collectors.toList());

            if (targetDisciplines.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                    "Nenhuma disciplina foi encontrada para esta turma. "
                        + "Verifique os horários, os resultados lançados ou selecione uma disciplina.");
                rendered = false;
                return;
            }

            for (Discipline discipline : targetDisciplines) {
                MiniPautaBlock block = new MiniPautaBlock();
                block.setDisciplineName(discipline.getDisciplineName());
                block.setScaleLabel(scaleLabel);
                block.setEducationLevel(educationLevel);

                int rowNum = 1;
                for (EnrolmentDTO enr : enrolments) {
                    MiniPautaRow row = new MiniPautaRow();
                    row.setRowNumber(rowNum++);
                    row.setStudentName(enr.getStudentFullName() != null ? enr.getStudentFullName() : "—");

                    for (Trimester t : Trimester.values()) {
                        TrimesterScore score = new TrimesterScore();
                        Map<Long, Map<Trimester, TrimesterResultDTO>> enrolmentResults =
                            resultsByEnrolment.get(enr.getPhEnrolment());
                        Map<Trimester, TrimesterResultDTO> disciplineResults = enrolmentResults == null
                            ? null : enrolmentResults.get(discipline.getPkDiscipline());
                        TrimesterResultDTO result = disciplineResults == null ? null : disciplineResults.get(t);
                        if (result != null) {
                            score.setMac(result.getMac());
                            score.setNpt(result.getNpt());
                            score.setMt(result.getMt());
                            score.setSituation(result.getSituation() != null
                                ? result.getSituation().getDescricao() : null);
                        }
                        score.setObservation(resolveObservation(observations, enr.getPhEnrolment(),
                            discipline.getPkDiscipline(), t, result != null ? result.getObs() : null));
                        row.getScores().put(String.valueOf(t.ordinal() + 1), score);
                    }
                    block.getRows().add(row);
                }
                reportBlocks.add(block);
            }

                addMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                    "Mini-pauta gerada para " + enrolments.size() + " aluno(s), com notas existentes e "
                        + "campos pendentes em branco, em "
                            + reportBlocks.size() + " disciplina(s).");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar pauta em branco", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível gerar a pauta em branco: " + e.getMessage());
            rendered = false;
        }
    }

    public void exportarMiniPautaEmBranco() {
        gerarPautaEmBranco();
        if (rendered && reportBlocks != null && !reportBlocks.isEmpty()) {
            exportPdf();
        }
    }

    public void exportarMiniPautaEmBrancoExcel() {
        gerarPautaEmBranco();
        if (rendered && reportBlocks != null && !reportBlocks.isEmpty()) {
            exportExcel();
        }
    }

    public void exportarMiniPautaEmBrancoCsv() {
        gerarPautaEmBranco();
        if (rendered && reportBlocks != null && !reportBlocks.isEmpty()) {
            exportCsv();
        }
    }

    public void exportarPautaEmBranco() {
        if (selectedTrimesters == null || selectedTrimesters.size() != 1
                || selectedTrimesters.get(0) == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                    "Selecione apenas um trimestre para gerar a pauta.");
            return;
        }

        gerarPautaEmBranco();
        if (rendered && reportBlocks != null && !reportBlocks.isEmpty()) {
            exportPdfPautaEmBranco(selectedTrimesters.get(0));
        }
    }

    public void exportarMiniPautaPorClasseEmBranco() {
        gerarPautaEmBranco();
        if (rendered && reportBlocks != null && !reportBlocks.isEmpty()) {
            exportPdfByClass();
        }
    }

    public void exportarPautaFinalEmBranco() {
        finalSextaClasseMode = "BRANCO";
        exportPdfFinalSextaClasse();
    }

    public void clear() {
        selectedSchoolClassId = null;
        selectedEducationLevel = null;
        selectedDisciplineIds = new ArrayList<>();
        selectedTrimesters = new ArrayList<>();
        rendered = false;
        blankMode = false;
        reportBlocks = new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPORTAÇÃO — PDF / EXCEL / CSV
    //
    // Os três métodos seguem o mesmo contrato: escrevem diretamente na
    // resposta HTTP corrente via ExternalContext e chamam
    // facesContext.responseComplete() para impedir o JSF de tentar
    // renderizar a view por cima do ficheiro. Por isso os botões na
    // view TÊM de usar ajax="false" (postback normal), caso contrário
    // o browser não interpreta o Content-Disposition como download.
    //
    // Funcionam sem alterações tanto para a pauta normal (com resultados
    // já lançados) como para a pauta em branco (blankMode=true) — a única
    // diferença é que, em modo branco, os MiniPautaRow chegam com
    // TrimesterScore vazios, que formatScore() já traduz em células em
    // branco nas três exportações.
    // ═══════════════════════════════════════════════════════════════

    public void exportPdf() {
        if (!canExport()) {
            return;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            startDownload(buildFileNameBase() + ".pdf", "application/pdf");

            // Retrato (A4). Margem superior alargada para abrir espaço ao
            // logotipo institucional, desenhado no canto superior esquerdo
            // de cada página via ProfessionalDocumentPageEvent.
            Document document = new Document(PageSize.A4, 36, 36, 58, 30);
            OutputStream out = facesContext.getExternalContext().getResponseOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new ProfessionalDocumentPageEvent(loadLogoBytes()));
            document.open();

            for (MiniPautaBlock block : reportBlocks) {

                 
                addPdfInstitutionHeader(document, block);

                if (block.isEmpty()) {
                    document.add(new Paragraph("Nenhum aluno encontrado.",
                            FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_MUTED_TEXT)));
                } else {
                    document.add(buildPdfTable(block));
                    addPdfSignature(document);
                }

                // Nova página por disciplina (igual ao modelo oficial)
                if (reportBlocks.indexOf(block) < reportBlocks.size() - 1) {
                    document.newPage();
                }
            }

            document.close();
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar PDF da mini-pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível gerar o PDF: " + e.getMessage());
        }
    }

    private void exportPdfPautaEmBranco(Trimester trimester) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            String trimesterSlug = (trimester.ordinal() + 1) + "o-trimestre";
            startDownload(buildFileNameBase() + "-pauta-" + trimesterSlug + ".pdf", "application/pdf");

            int studentCount = reportBlocks.stream()
                    .filter(block -> block.getRows() != null)
                    .mapToInt(block -> block.getRows().size())
                    .max()
                    .orElse(0);
            boolean landscape = reportBlocks.size() > 3;
            Rectangle pageSize = landscape ? PageSize.A4.rotate() : PageSize.A4;
            float pageWidth = landscape
                    ? Math.max(pageSize.getWidth(), 300f + reportBlocks.size() * 72f)
                    : pageSize.getWidth();
            float minimumPageHeight = landscape ? 210f : 300f;
            float pageHeight = Math.max(pageSize.getHeight(), minimumPageHeight + studentCount * 17f);
            Document document = new Document(new Rectangle(0, 0, pageWidth, pageHeight), 24, 24, 24, 28);
            OutputStream out = facesContext.getExternalContext().getResponseOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new ProfessionalDocumentPageEvent(loadLogoBytes(), "Pauta"));
            document.open();

            addTeacherBlankClassHeader(document, trimester);
            document.add(buildTeacherBlankPautaTable());
            addPdfSignature(document);

            document.close();
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar pauta em branco", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível gerar a pauta: " + e.getMessage());
        }
    }

    private void addTeacherBlankClassHeader(Document document, Trimester trimester)
            throws com.lowagie.text.DocumentException {
        Paragraph institution = new Paragraph(
                "REPÚBLICA DE ANGOLA\nADMINISTRAÇÃO MUNICIPAL DE "
                        + safeHeaderValue(municipality, DEFAULT_HEADER_PLACEHOLDER)
                        + "\nDIRECÇÃO MUNICIPAL DA EDUCAÇÃO DE "
                        + safeHeaderValue(educationDirectorate, DEFAULT_HEADER_PLACEHOLDER),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PDF_TITLE_TEXT));
        institution.setAlignment(Element.ALIGN_CENTER);
        institution.setSpacingAfter(3f);
        document.add(institution);

        Paragraph title = new Paragraph("PAUTA — " + safeHeaderValue(schoolName,
                "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS").toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PDF_TITLE_TEXT));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5f);
        document.add(title);

        Paragraph meta = new Paragraph(
                "CLASSE / TURMA: " + resolveSelectedClassName()
                        + "     ANO LECTIVO: " + schoolYear
                        + "     " + trimester.getDescricao().toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PDF_TITLE_TEXT));
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingAfter(6f);
        document.add(meta);
    }

    /**
     * Exporta a pauta consolidada da turma com MT1, MT2, MT3 e MF por disciplina.
     */
    public void exportPdfByClass() {
        if (selectedSchoolClassId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione uma turma.");
            return;
        }

        if (!blankMode) {
            search(true);
        }
        if (!canExport()) {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            startDownload(buildFileNameBase() + "-por-classe.pdf", "application/pdf");

            Document document = new Document(PageSize.A4.rotate(), 18, 18, 18, 20);
            OutputStream out = facesContext.getExternalContext().getResponseOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new ProfessionalDocumentPageEvent(loadLogoBytes()));
            document.open();

            addMiniPautaClassPdfHeader(document);
            document.add(buildMiniPautaClassPdfTable());
            addMiniPautaClassPdfSignatures(document);

            document.close();
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar mini-pauta por classe", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível gerar o PDF por classe: " + e.getMessage());
        }
    }

    private void exportPdfFinalByClass() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            Document document = new Document(PageSize.A4.rotate(), 14, 14, 14, 16);
            ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, pdfBytes);
            writer.setPageEvent(new ProfessionalDocumentPageEvent(loadLogoBytes()));
            document.open();

            addClassPdfHeader(document);
            document.add(buildClassPdfTable());
            addClassPdfSignatures(document);
            document.close();

            startDownload(buildFileNameBase() + "-pauta-final.pdf", "application/pdf");
            try (OutputStream out = facesContext.getExternalContext().getResponseOutputStream()) {
                pdfBytes.writeTo(out);
            }
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar PDF da pauta final", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "Não foi possível gerar a pauta final: " + e.getMessage());
        }
    }

    public void exportPdfFinalSextaClasse() {
        SchoolClass selectedClass = resolveSelectedClass();
        if (selectedClass == null || selectedClass.getClasse() != Classe.SEXTA) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                    "Selecione uma turma da 6.ª classe para gerar a pauta final.");
            return;
        }

        if ("BRANCO".equals(finalSextaClasseMode)) {
            gerarPautaEmBranco();
            if (!rendered || reportBlocks == null || reportBlocks.isEmpty()) {
                return;
            }
        } else if (!canExport() || blankMode) {
            if (blankMode) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                        "Gere primeiro a pauta preenchida antes de imprimir.");
            }
            return;
        }

        exportPdfFinalByClass();
    }

    private void addMiniPautaClassPdfHeader(Document document)
            throws com.lowagie.text.DocumentException {
        PdfPTable institution = new PdfPTable(new float[]{2.8f, 5.8f, 2.8f});
        institution.setWidthPercentage(100f);
        institution.setWidths(new float[]{2.8f, 5.8f, 2.8f});
        institution.setSpacingAfter(2f);

        PdfPCell approvalCell = new PdfPCell();
        approvalCell.setBorder(PdfPCell.NO_BORDER);
        approvalCell.setPadding(0f);
        approvalCell.addElement(new Paragraph("Visto da Sub-Directora Pedagógica da Escola de Tutela",
            FontFactory.getFont(FontFactory.HELVETICA, 6, PDF_TITLE_TEXT)));
        approvalCell.addElement(new Paragraph("________________________________",
            FontFactory.getFont(FontFactory.HELVETICA, 7, PDF_MUTED_TEXT)));
        institution.addCell(approvalCell);

        PdfPCell republicCell = new PdfPCell();
        republicCell.setBorder(PdfPCell.NO_BORDER);
        republicCell.setPadding(0f);
        republicCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        republicCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            byte[] logoBytes = loadLogoBytes();
            if (logoBytes != null) {
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(28f, 28f);
            logo.setAlignment(Element.ALIGN_CENTER);
            republicCell.addElement(logo);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Logotipo não disponível no cabeçalho da mini-pauta", e);
        }
        Paragraph republic = new Paragraph("REPÚBLICA DE ANGOLA\nMINISTÉRIO DA EDUCAÇÃO\n"
            + "GOVERNO DA PROVÍNCIA DE LUANDA\nDIRECÇÃO MUNICIPAL DE EDUCAÇÃO DE "
            + safeHeaderValue(educationDirectorate, DEFAULT_HEADER_PLACEHOLDER)
            + "\nCOLÉGIO PÚBLICO Nº 919",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, PDF_TITLE_TEXT));
        republic.setAlignment(Element.ALIGN_CENTER);
        republic.setLeading(0f, 1.05f);
        republicCell.addElement(republic);
        institution.addCell(republicCell);

        PdfPCell schoolCell = new PdfPCell();
        schoolCell.setBorder(PdfPCell.NO_BORDER);
        schoolCell.setPadding(0f);
        Paragraph school = new Paragraph(safeHeaderValue(schoolName,
            "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS"),
            FontFactory.getFont(FontFactory.HELVETICA, 7, PDF_TITLE_TEXT));
        school.setAlignment(Element.ALIGN_RIGHT);
        schoolCell.addElement(school);
        institution.addCell(schoolCell);
        document.add(institution);

        SchoolClass selectedClass = resolveSelectedClass();
        String classYear = selectedClass != null && selectedClass.getAnoLectivo() != null
            ? selectedClass.getAnoLectivo() : schoolYear;
        Paragraph title = new Paragraph("PAUTA Nº 007/" + classYear,
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_TITLE_TEXT));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(1f);
        document.add(title);

        PdfPTable meta = new PdfPTable(4);
        meta.setWidthPercentage(100f);
        meta.setWidths(new float[]{2.2f, 2.2f, 1.6f, 1.1f});
        addMiniPautaMetaCell(meta, "ANO LECTIVO", classYear);
        addMiniPautaMetaCell(meta, "PERÍODO", formatClasse(selectedClass) + " CLASSE");
        addMiniPautaMetaCell(meta, "TURMA", selectedClass != null ? selectedClass.getClassCode() : "__________");
        addMiniPautaMetaCell(meta, "SALA", selectedClass != null ? selectedClass.getRoom() : "__________");
        document.add(meta);

        Paragraph disciplines = new Paragraph("D I S C I P L I N A S",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, PDF_TITLE_TEXT));
        disciplines.setAlignment(Element.ALIGN_CENTER);
        disciplines.setSpacingBefore(1f);
        disciplines.setSpacingAfter(2f);
        document.add(disciplines);
    }

    private void addMiniPautaMetaCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(1f);
        cell.addElement(new Paragraph(label,
            FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_MUTED_TEXT)));
        cell.addElement(new Paragraph(value != null ? value : "",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, PDF_TITLE_TEXT)));
        table.addCell(cell);
    }

    private PdfPTable buildMiniPautaClassPdfTable()
            throws com.lowagie.text.DocumentException {
        int disciplineCount = reportBlocks.size();
        int columnCount = 2 + disciplineCount * 4 + 1;
        PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100f);
        float[] widths = new float[columnCount];
        widths[0] = 0.48f;
        widths[1] = 3.9f;
        for (int i = 2; i < widths.length - 1; i++) {
            widths[i] = 0.62f;
        }
        widths[widths.length - 1] = 1.25f;
        table.setWidths(widths);
        table.setHeaderRows(2);
        table.setSplitLate(false);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5.5f, PDF_TITLE_TEXT);
        table.addCell(miniPautaClassHeaderCell("Nº", headerFont, 1, 2));
        table.addCell(miniPautaClassHeaderCell("NOME DO ALUNO", headerFont, 1, 2));
        for (MiniPautaBlock block : reportBlocks) {
            table.addCell(miniPautaClassHeaderCell(block.getDisciplineName(), headerFont, 4, 1));
        }
        table.addCell(miniPautaClassHeaderCell("OBSERVAÇÃO", headerFont, 1, 2));
        String[] subHeaders = {"MT1", "MT2", "MT3", "MF"};
        for (int i = 0; i < disciplineCount; i++) {
            for (String subHeader : subHeaders) {
                table.addCell(miniPautaClassHeaderCell(subHeader, headerFont, 1, 1));
            }
        }

        List<String> studentNames = reportBlocks.stream()
            .filter(block -> block.getRows() != null)
            .flatMap(block -> block.getRows().stream())
            .map(MiniPautaRow::getStudentName)
            .filter(name -> name != null && !name.isBlank())
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.toList());
        int rowCount = Math.max(26, studentNames.size());
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_TITLE_TEXT);

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            String studentName = rowIndex < studentNames.size() ? studentNames.get(rowIndex) : "";
            Color background = rowIndex % 2 == 0 ? Color.WHITE : new Color(248, 248, 248);
            table.addCell(miniPautaClassDataCell(String.valueOf(rowIndex + 1), cellFont,
                Element.ALIGN_CENTER, background));
            table.addCell(miniPautaClassDataCell(studentName, cellFont, Element.ALIGN_LEFT, background));

            for (MiniPautaBlock block : reportBlocks) {
                MiniPautaRow row = studentName.isBlank() || block.getRows() == null ? null
                    : block.getRows().stream()
                        .filter(candidate -> studentName.equals(candidate.getStudentName()))
                        .findFirst().orElse(null);
                for (int trimesterNumber = 1; trimesterNumber <= 3; trimesterNumber++) {
                    TrimesterScore score = row == null || row.getScores() == null
                        ? null : row.getScores().get(String.valueOf(trimesterNumber));
                    table.addCell(miniPautaClassDataCell(score != null ? score.getMtFormatted() : "",
                        cellFont, Element.ALIGN_CENTER, background));
                }
                table.addCell(miniPautaClassDataCell(resolveFinalMfd(row),
                    cellFont, Element.ALIGN_CENTER, background));
            }
            table.addCell(miniPautaClassDataCell(resolveClassSituation(studentName),
                cellFont, Element.ALIGN_LEFT, background));
        }
        return table;
    }

    private PdfPCell miniPautaClassHeaderCell(String text, Font font, int colspan, int rowspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        return cell;
    }

    private PdfPCell miniPautaClassDataCell(String text, Font font, int alignment, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        cell.setMinimumHeight(11.5f);
        cell.setBackgroundColor(background);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.8f);
        return cell;
    }

        private void addMiniPautaClassPdfSignatures(Document document)
            throws com.lowagie.text.DocumentException {
        PdfPTable signatures = new PdfPTable(3);
        signatures.setWidthPercentage(100f);
        signatures.setWidths(new float[]{1f, 1f, 1f});
        signatures.setSpacingBefore(8f);

        PdfPCell councilCell = new PdfPCell();
        councilCell.setBorder(PdfPCell.NO_BORDER);
        councilCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        councilCell.setPadding(0f);
        for (int lineNumber = 1; lineNumber <= 3; lineNumber++) {
            Paragraph line = new Paragraph(lineNumber + ". ____________________________",
                FontFactory.getFont(FontFactory.HELVETICA, 6, PDF_TITLE_TEXT));
            line.setAlignment(Element.ALIGN_CENTER);
            councilCell.addElement(line);
        }
        addSignatureCaption(councilCell, "O CONSELHO DE NOTAS");
        signatures.addCell(councilCell);

        PdfPCell deputyCell = new PdfPCell();
        deputyCell.setBorder(PdfPCell.NO_BORDER);
        deputyCell.setPadding(0f);
        Paragraph date = new Paragraph("______________, ____/____/______",
            FontFactory.getFont(FontFactory.HELVETICA, 6, PDF_TITLE_TEXT));
        date.setAlignment(Element.ALIGN_CENTER);
        deputyCell.addElement(date);
        Paragraph deputyLine = new Paragraph("____________________________",
            FontFactory.getFont(FontFactory.HELVETICA, 6, PDF_MUTED_TEXT));
        deputyLine.setAlignment(Element.ALIGN_CENTER);
        deputyLine.setSpacingBefore(3f);
        deputyCell.addElement(deputyLine);
        addSignatureCaption(deputyCell, "O SUB-DIRECTOR PEDAGÓGICO");
        signatures.addCell(deputyCell);

        PdfPCell directorCell = new PdfPCell();
        directorCell.setBorder(PdfPCell.NO_BORDER);
        directorCell.setPadding(0f);
        Paragraph directorLine = new Paragraph("____________________________",
            FontFactory.getFont(FontFactory.HELVETICA, 6, PDF_MUTED_TEXT));
        directorLine.setAlignment(Element.ALIGN_CENTER);
        directorLine.setSpacingBefore(13f);
        directorCell.addElement(directorLine);
        addSignatureCaption(directorCell, "O DIRECTOR GERAL");
        signatures.addCell(directorCell);

        document.add(signatures);
        }

    private void addClassPdfHeader(Document document) throws com.lowagie.text.DocumentException {
        PdfPTable institution = new PdfPTable(new float[]{2.8f, 5.8f, 2.8f});
        institution.setWidthPercentage(100f);
        institution.setWidths(new float[]{2.8f, 5.8f, 2.8f});
        institution.setSpacingAfter(1f);

        PdfPCell approvalCell = new PdfPCell();
        approvalCell.setBorder(PdfPCell.NO_BORDER);
        approvalCell.setPadding(0f);
        approvalCell.addElement(new Paragraph("Visto da Sub-Directora Pedagógica da Escola de Tutela",
            FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_TITLE_TEXT)));
        approvalCell.addElement(new Paragraph("____________________________",
            FontFactory.getFont(FontFactory.HELVETICA, 6, PDF_MUTED_TEXT)));
        institution.addCell(approvalCell);

        PdfPCell republicCell = new PdfPCell();
        republicCell.setBorder(PdfPCell.NO_BORDER);
        republicCell.setPadding(0f);
        republicCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        republicCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            byte[] logoBytes = loadLogoBytes();
            if (logoBytes != null) {
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(22f, 22f);
            logo.setAlignment(Element.ALIGN_CENTER);
            republicCell.addElement(logo);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Logotipo não disponível no cabeçalho da pauta final", e);
        }
        Paragraph republic = new Paragraph("REPÚBLICA DE ANGOLA\nMINISTÉRIO DA EDUCAÇÃO\n"
            + "GOVERNO DA PROVÍNCIA DE LUANDA\nDIRECÇÃO MUNICIPAL DE EDUCAÇÃO DE "
            + safeHeaderValue(educationDirectorate, DEFAULT_HEADER_PLACEHOLDER),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5.5f, PDF_TITLE_TEXT));
        republic.setAlignment(Element.ALIGN_CENTER);
        republic.setLeading(0f, 1f);
        republicCell.addElement(republic);
        institution.addCell(republicCell);

        PdfPCell schoolCell = new PdfPCell();
        schoolCell.setBorder(PdfPCell.NO_BORDER);
        schoolCell.setPadding(0f);
        schoolCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph school = new Paragraph(safeHeaderValue(schoolName,
            "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS").toUpperCase(),
            FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_TITLE_TEXT));
        school.setAlignment(Element.ALIGN_RIGHT);
        schoolCell.addElement(school);
        institution.addCell(schoolCell);
        document.add(institution);

        SchoolClass selectedClass = resolveSelectedClass();
        String classYear = selectedClass != null && selectedClass.getAnoLectivo() != null
            ? selectedClass.getAnoLectivo() : schoolYear;
        Paragraph title = new Paragraph("PAUTA Nº 007/" + classYear,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PDF_TITLE_TEXT));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(1f);
        document.add(title);

        PdfPTable meta = new PdfPTable(5);
        meta.setWidthPercentage(100f);
        meta.setWidths(new float[]{2.2f, 1.8f, 1.4f, 1.4f, 1.1f});
        addClassMetaCell(meta, "ANO LECTIVO", classYear);
        addClassMetaCell(meta, "PERÍODO", selectedClass != null && selectedClass.getTurno() != null
            ? selectedClass.getTurno().toString() : "__________");
        addClassMetaCell(meta, "CLASSE", formatClasse(selectedClass) + " CLASSE");
        addClassMetaCell(meta, "TURMA", selectedClass != null ? selectedClass.getClassCode() : "__________");
        addClassMetaCell(meta, "SALA", selectedClass != null && selectedClass.getRoom() != null
            ? selectedClass.getRoom() : "__________");
        document.add(meta);

        Paragraph disciplines = new Paragraph("D I S C I P L I N A S",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, PDF_TITLE_TEXT));
        disciplines.setAlignment(Element.ALIGN_CENTER);
        disciplines.setSpacingBefore(1f);
        disciplines.setSpacingAfter(1f);
        document.add(disciplines);
    }

    private void addClassMetaCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(1f);
        cell.addElement(new Paragraph(label,
                FontFactory.getFont(FontFactory.HELVETICA, 5, PDF_TITLE_TEXT)));
        cell.addElement(new Paragraph(value != null ? value : "",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5.5f, PDF_TITLE_TEXT)));
        table.addCell(cell);
    }

    private PdfPTable buildClassPdfTable() throws com.lowagie.text.DocumentException {
        int disciplineCount = reportBlocks.size();
        int columnCount = 3 + disciplineCount * 3 + 1;
        PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100f);
        float[] widths = new float[columnCount];
        widths[0] = 0.42f;
        widths[1] = 2.8f;
        widths[2] = 0.85f;
        for (int i = 3; i < widths.length - 1; i++) {
            widths[i] = 0.58f;
        }
        widths[widths.length - 1] = 1.1f;
        table.setWidths(widths);
        table.setHeaderRows(2);
        table.setSplitLate(false);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 4.8f, PDF_TITLE_TEXT);
        table.addCell(finalPautaHeaderCell("Nº", headerFont, 1, 2));
        table.addCell(finalPautaHeaderCell("NOME DO ALUNO", headerFont, 1, 2));
        table.addCell(finalPautaHeaderCell("DATA DE\nNASCIMENTO", headerFont, 1, 2));
        for (MiniPautaBlock block : reportBlocks) {
            table.addCell(finalPautaHeaderCell(block.getDisciplineName(), headerFont, 3, 1));
        }
        table.addCell(finalPautaHeaderCell("OBSERVAÇÃO", headerFont, 1, 2));
        for (int i = 0; i < disciplineCount; i++) {
            for (String subHeader : new String[]{"MFD", "NE", "NER"}) {
                table.addCell(finalPautaHeaderCell(subHeader, headerFont, 1, 1));
            }
        }

        List<String> studentNames = reportBlocks.stream()
                .filter(block -> block.getRows() != null)
                .flatMap(block -> block.getRows().stream())
                .map(MiniPautaRow::getStudentName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 5.2f, PDF_TITLE_TEXT);
        Map<String, LocalDate> birthDates = resolveBirthDates();

        for (int rowIndex = 0; rowIndex < studentNames.size(); rowIndex++) {
            String studentName = studentNames.get(rowIndex);
            table.addCell(finalPautaDataCell(String.valueOf(rowIndex + 1), cellFont, Element.ALIGN_CENTER));
            table.addCell(finalPautaDataCell(studentName, cellFont, Element.ALIGN_LEFT));
            table.addCell(finalPautaDataCell(formatDate(birthDates.get(studentName)), cellFont,
                Element.ALIGN_CENTER));
            for (MiniPautaBlock block : reportBlocks) {
                MiniPautaRow row = block.getRows().stream()
                        .filter(candidate -> studentName.equals(candidate.getStudentName()))
                        .findFirst().orElse(null);
                table.addCell(finalPautaDataCell(resolveFinalMfd(row), cellFont, Element.ALIGN_CENTER));
                table.addCell(finalPautaDataCell("", cellFont, Element.ALIGN_CENTER));
                table.addCell(finalPautaDataCell("", cellFont, Element.ALIGN_CENTER));
            }
            table.addCell(finalPautaDataCell("", cellFont, Element.ALIGN_LEFT));
        }
        return table;
    }

    private PdfPCell finalPautaHeaderCell(String text, Font font, int colspan, int rowspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.7f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(1.2f);
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        return cell;
    }

    private PdfPCell finalPautaDataCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(1.2f);
        cell.setMinimumHeight(10f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.7f);
        return cell;
    }

    private String resolveFinalMfd(MiniPautaRow row) {
        Double average = resolveFinalMfdValue(row);
        return average == null ? "" : TrimesterScore.formatScore(average);
    }

    private Double resolveFinalMfdValue(MiniPautaRow row) {
        if (row == null || row.getScores() == null) {
            return null;
        }

        double total = 0d;
        int count = 0;
        for (int trimesterNumber = 1; trimesterNumber <= 3; trimesterNumber++) {
            TrimesterScore score = row.getScores().get(String.valueOf(trimesterNumber));
            if (score != null && score.getMt() != null) {
                total += score.getMt();
                count++;
            }
        }
        return count == 0 ? null : total / count;
    }

    private String resolveClassSituation(String studentName) {
        if (studentName == null || studentName.isBlank()) {
            return "";
        }

        boolean failed = false;
        boolean pending = false;
        for (MiniPautaBlock block : reportBlocks) {
            MiniPautaRow row = block.getRows() == null ? null : block.getRows().stream()
                .filter(candidate -> studentName.equals(candidate.getStudentName()))
                .findFirst().orElse(null);
            Double finalAverage = resolveFinalMfdValue(row);
            if (finalAverage == null) {
                pending = true;
                continue;
            }

            double approvalThreshold = "PRIMARIO".equals(block.getEducationLevel()) ? 5d : 10d;
            if (finalAverage < approvalThreshold) {
                failed = true;
            }
        }

        if (failed) {
            return "Reprovado";
        }
        return pending ? "Em Curso" : "Aprovado";
    }

    private Map<String, LocalDate> resolveBirthDates() {
        Map<String, LocalDate> birthDates = new LinkedHashMap<>();
        try {
            List<StudentDTO> students = studentService.getAllStudents();
            if (students != null) {
                for (StudentDTO student : students) {
                    if (student.getFullName() != null) {
                        birthDates.put(student.getFullName(), student.getNascDate());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Não foi possível carregar as datas de nascimento para a pauta final", e);
        }
        return birthDates;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private SchoolClass resolveSelectedClass() {
        if (selectedSchoolClassId == null) {
            return null;
        }
        return schoolClasses.stream()
                .filter(schoolClass -> selectedSchoolClassId.equals(schoolClass.getPkSchoolClass()))
                .findFirst().orElse(null);
    }

    private Map<ObservationKey, LinkedHashSet<String>> loadObservations(
            List<TrimesterResultDTO> results, Set<Long> enrolmentIds) {
        Map<ObservationKey, LinkedHashSet<String>> observations = new HashMap<>();
        if (results != null) {
            for (TrimesterResultDTO result : results) {
                if (result.getEnrolmentPk() == null || result.getDisciplinePk() == null
                        || result.getTrimester() == null
                        || (enrolmentIds != null && !enrolmentIds.contains(result.getEnrolmentPk()))) {
                    continue;
                }
                addObservation(observations, result.getEnrolmentPk(), result.getDisciplinePk(),
                        result.getTrimester(), result.getObs());
            }
        }

        if (enrolmentIds != null && !enrolmentIds.isEmpty()) {
            for (Grade grade : gradeRepository.findWithObservationsByEnrolmentPks(
                    new ArrayList<>(enrolmentIds))) {
                if (grade.getEnrolment() == null || grade.getEvaluation() == null
                        || grade.getEvaluation().getDiscipline() == null) {
                    continue;
                }
                addObservation(observations, grade.getEnrolment().getPhEnrolment(),
                        grade.getEvaluation().getDiscipline().getPkDiscipline(),
                        grade.getEvaluation().getTrimester(), grade.getObs());
            }
        }
        return observations;
    }

    private void addObservation(Map<ObservationKey, LinkedHashSet<String>> observations,
            Long enrolmentId, Long disciplineId, Trimester trimester, String observation) {
        if (enrolmentId == null || disciplineId == null || trimester == null
                || observation == null || observation.isBlank()) {
            return;
        }
        observations.computeIfAbsent(new ObservationKey(enrolmentId, disciplineId, trimester),
                key -> new LinkedHashSet<>()).add(observation.trim());
    }

    private String resolveObservation(Map<ObservationKey, LinkedHashSet<String>> observations,
            Long enrolmentId, Long disciplineId, Trimester trimester, String resultObservation) {
        LinkedHashSet<String> values = new LinkedHashSet<>(observations.getOrDefault(
                new ObservationKey(enrolmentId, disciplineId, trimester), new LinkedHashSet<>()));
        if (resultObservation != null && !resultObservation.isBlank()) {
            values.add(resultObservation.trim());
        }
        return String.join("; ", values);
    }

    private String formatClasse(SchoolClass schoolClass) {
        if (schoolClass == null || schoolClass.getClasse() == null) {
            return "__________";
        }
        switch (schoolClass.getClasse()) {
            case PRIMEIRA: return "1ª";
            case SEGUNDA: return "2ª";
            case TERCEIRA: return "3ª";
            case QUARTA: return "4ª";
            case QUINTA: return "5ª";
            case SEXTA: return "6ª";
            case SETIMA: return "7ª";
            case OITAVA: return "8ª";
            case NONA: return "9ª";
            default: return schoolClass.getClasse().name();
        }
    }

    private void addClassPdfSignatures(Document document) throws com.lowagie.text.DocumentException {
        PdfPTable signatures = new PdfPTable(3);
        signatures.setWidthPercentage(100f);
        signatures.setWidths(new float[]{1f, 1f, 1f});
        signatures.setSpacingBefore(8f);

        PdfPCell councilCell = new PdfPCell();
        councilCell.setBorder(PdfPCell.NO_BORDER);
        councilCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        councilCell.setPadding(0f);
        for (int lineNumber = 1; lineNumber <= 3; lineNumber++) {
            Paragraph line = new Paragraph(lineNumber + ". __________________________",
                    FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_TITLE_TEXT));
                line.setAlignment(Element.ALIGN_CENTER);
            councilCell.addElement(line);
        }
        addSignatureCaption(councilCell, "O CORPO DE JÚRI");
        signatures.addCell(councilCell);

        PdfPCell deputyCell = new PdfPCell();
        deputyCell.setBorder(PdfPCell.NO_BORDER);
        deputyCell.setPadding(0f);
        Paragraph placeDate = new Paragraph("LUANDA, ______/______/________",
            FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_TITLE_TEXT));
        placeDate.setAlignment(Element.ALIGN_CENTER);
        deputyCell.addElement(placeDate);
        Paragraph deputyLine = new Paragraph("____________________________",
            FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_MUTED_TEXT));
        deputyLine.setAlignment(Element.ALIGN_CENTER);
        deputyLine.setSpacingBefore(4f);
        deputyCell.addElement(deputyLine);
        addSignatureCaption(deputyCell, "O SUBDIRECTOR PEDAGÓGICO");
        signatures.addCell(deputyCell);

        PdfPCell directorCell = new PdfPCell();
        directorCell.setBorder(PdfPCell.NO_BORDER);
        directorCell.setPadding(0f);
        Paragraph directorLine = new Paragraph("____________________________",
            FontFactory.getFont(FontFactory.HELVETICA, 5.5f, PDF_MUTED_TEXT));
        directorLine.setAlignment(Element.ALIGN_CENTER);
        directorLine.setSpacingBefore(12f);
        directorCell.addElement(directorLine);
        addSignatureCaption(directorCell, "O DIRECTOR DA ESCOLA");
        signatures.addCell(directorCell);
        document.add(signatures);
    }

    private PdfPCell buildSignatureCell(String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(0f);
        Paragraph line = new Paragraph("____________________________",
                FontFactory.getFont(FontFactory.HELVETICA, 8, PDF_MUTED_TEXT));
        line.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(line);
        addSignatureCaption(cell, label);
        return cell;
    }

    private void addSignatureCaption(PdfPCell cell, String label) {
        Paragraph caption = new Paragraph(label,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, PDF_TITLE_TEXT));
        caption.setAlignment(Element.ALIGN_CENTER);
        caption.setSpacingBefore(2f);
        cell.addElement(caption);
    }

    private record ObservationKey(Long enrolmentId, Long disciplineId, Trimester trimester) {}

    public void exportExcel() {
        if (!canExport()) {
            return;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFCellStyle instStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font instFont = workbook.createFont();
            instFont.setBold(true);
            instFont.setFontHeightInPoints((short) 11);
            instStyle.setFont(instFont);
            instStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle groupStyle = workbook.createCellStyle();
            groupStyle.setFillForegroundColor(new XSSFColor(XLS_GROUP_BG, null));
            groupStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            groupStyle.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont groupFont = workbook.createFont();
            groupFont.setBold(true);
            groupFont.setColor(new XSSFColor(XLS_HEADER_TEXT, null));
            groupStyle.setFont(groupFont);

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(XLS_HEADER_BG, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(XLS_HEADER_TEXT, null));
            headerStyle.setFont(headerFont);

            for (MiniPautaBlock block : reportBlocks) {
                String sheetName = WorkbookUtil.createSafeSheetName(block.getDisciplineName());
                Sheet sheet = workbook.createSheet(sheetName);

                // Cabeçalho institucional
                String[] instLines = institutionLines(block);
                for (int i = 0; i < instLines.length; i++) {
                    Row r = sheet.createRow(i);
                    Cell c = r.createCell(0);
                    c.setCellValue(instLines[i]);
                    c.setCellStyle(instStyle);
                }

                // Linha de grupo: DISCIPLINA / CLASSE / ANO LECTIVO
                Row metaRow = sheet.createRow(instLines.length + 1);
                Cell d = metaRow.createCell(0);
                d.setCellValue("DISCIPLINA: " + block.getDisciplineName());
                Cell t = metaRow.createCell(4);
                t.setCellValue("TURMA: " + resolveSelectedClassName());
                Cell y = metaRow.createCell(8);
                y.setCellValue("ANO LECTIVO: " + schoolYear);

                int headerRowIdx = instLines.length + 3;

                // Linha 1 do cabeçalho: Nº | Nome | 1º TRIMESTRE (span 3) | ...
                Row h1 = sheet.createRow(headerRowIdx);
                headerCell(workbook, h1, 0, "Nº", headerStyle);
                headerCell(workbook, h1, 1, "NOME COMPLETO", headerStyle);
                for (int tIdx = 0; tIdx < 3; tIdx++) {
                    int startColumn = 2 + tIdx * 4;
                    headerCell(workbook, h1, startColumn, (tIdx + 1) + "º TRIMESTRE", groupStyle);
                    sheet.addMergedRegion(new CellRangeAddress(headerRowIdx, headerRowIdx,
                            startColumn, startColumn + 3));
                }
                headerCell(workbook, h1, 14, "OBSERVAÇÃO", headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(headerRowIdx, headerRowIdx + 1, 14, 14));

                // Linha 2 do cabeçalho: MAC | NPT | MT ×3
                Row h2 = sheet.createRow(headerRowIdx + 1);
                for (int tIdx = 0; tIdx < 3; tIdx++) {
                    for (int s = 0; s < MINI_PAUTA_SUB_HEADERS.length; s++) {
                        headerCell(workbook, h2, 2 + tIdx * 4 + s, MINI_PAUTA_SUB_HEADERS[s], headerStyle);
                    }
                }
                sheet.addMergedRegion(new CellRangeAddress(headerRowIdx, headerRowIdx + 1, 0, 0));
                sheet.addMergedRegion(new CellRangeAddress(headerRowIdx, headerRowIdx + 1, 1, 1));

                int rowIdx = headerRowIdx + 2;
                for (MiniPautaRow row : block.getRows()) {
                    Row excelRow = sheet.createRow(rowIdx++);
                    excelRow.createCell(0).setCellValue(row.getRowNumber());
                    excelRow.createCell(1).setCellValue(row.getStudentName());
                    for (int tIdx = 1; tIdx <= 3; tIdx++) {
                        TrimesterScore s = row.getScores().get(String.valueOf(tIdx));
                        int startColumn = 2 + (tIdx - 1) * 4;
                        excelRow.createCell(startColumn).setCellValue(s.getMacFormatted());
                        excelRow.createCell(startColumn + 1).setCellValue(s.getNptFormatted());
                        excelRow.createCell(startColumn + 2).setCellValue(s.getMtFormatted());
                        excelRow.createCell(startColumn + 3).setCellValue(s.getSituation());
                    }
                    excelRow.createCell(14).setCellValue("");
                }

                // Assinatura do professor
                Row sigRow = sheet.createRow(rowIdx + 1);
                Cell sig = sigRow.createCell(4);
                sig.setCellValue("O Professor");

                for (int i = 0; i < 15; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            startDownload(buildFileNameBase() + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            try (OutputStream out = facesContext.getExternalContext().getResponseOutputStream()) {
                workbook.write(out);
            }
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar Excel da mini-pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível gerar o Excel: " + e.getMessage());
        }
    }

    public void exportCsv() {
        if (!canExport()) {
            return;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append('\uFEFF'); // BOM — garante acentuação correta ao abrir no Excel

            for (MiniPautaBlock block : reportBlocks) {
                for (String line : institutionLines(block)) {
                    sb.append(csvEscape(line)).append('\n');
                }
                sb.append(csvEscape("DISCIPLINA: " + block.getDisciplineName())).append(';')
                        .append(csvEscape("TURMA: " + resolveSelectedClassName())).append(';')
                        .append(csvEscape("ANO LECTIVO: " + schoolYear)).append('\n');
                sb.append('\n');

                sb.append("Nº;NOME COMPLETO;");
                for (int t = 1; t <= 3; t++) {
                    sb.append(t).append("ºT MAC;").append(t).append("ºT NPT;").append(t).append("ºT MT;")
                            .append(t).append("ºT SITUAÇÃO;");
                }
                sb.append("OBSERVAÇÃO\n");

                for (MiniPautaRow row : block.getRows()) {
                    sb.append(row.getRowNumber()).append(';')
                            .append(csvEscape(row.getStudentName())).append(';');
                    for (int t = 1; t <= 3; t++) {
                        TrimesterScore s = row.getScores().get(String.valueOf(t));
                        sb.append(csvEscape(s.getMacFormatted())).append(';')
                                .append(csvEscape(s.getNptFormatted())).append(';')
                            .append(csvEscape(s.getMtFormatted())).append(';')
                            .append(csvEscape(s.getSituation())).append(';');
                    }
                    sb.append('\n');
                }
                sb.append('\n');
            }

            startDownload(buildFileNameBase() + ".csv", "text/csv; charset=UTF-8");
            try (OutputStream out = facesContext.getExternalContext().getResponseOutputStream()) {
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar CSV da mini-pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível gerar o CSV: " + e.getMessage());
        }
    }

    // ── Auxiliares de exportação ──

    private boolean canExport() {
        if (!rendered || reportBlocks == null || reportBlocks.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Gere a mini-pauta antes de exportar.");
            return false;
        }
        return true;
    }

    private void startDownload(String fileName, String contentType) throws java.io.IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.responseReset();
        externalContext.setResponseContentType(contentType);
        externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    /**
     * Lê o logotipo institucional a partir de webapp{@value #LOGO_RESOURCE_PATH}.
     * Devolve null (sem lançar exceção) se o ficheiro não existir, para que a
     * exportação do PDF nunca falhe por causa do logotipo.
     */
    private byte[] loadLogoBytes() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            return null;
        }
        try (InputStream is = ctx.getExternalContext().getResourceAsStream(LOGO_RESOURCE_PATH)) {
            if (is == null) {
                LOGGER.log(Level.WARNING, "Logotipo não encontrado em webapp{0}", LOGO_RESOURCE_PATH);
                return null;
            }
            return is.readAllBytes();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao carregar o logotipo para o PDF", e);
            return null;
        }
    }

    /**
     * Desenha o logotipo institucional no canto superior esquerdo de cada
     * página do PDF, dentro da margem superior (não interfere com o fluxo
     * normal de conteúdo do documento).
     */
    private static class ProfessionalDocumentPageEvent extends OpenPdfAuditPageEvent {
        private final byte[] logoBytes;
        private final String documentTitle;

        ProfessionalDocumentPageEvent(byte[] logoBytes) {
            this(logoBytes, "Mini-Pauta");
        }

        ProfessionalDocumentPageEvent(byte[] logoBytes, String documentTitle) {
            this.logoBytes = logoBytes;
            this.documentTitle = documentTitle;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            // O logotipo institucional do cabeçalho permanece dentro do bloco do
            // documento; o desenho extra no corpo da página foi removido para não
            // duplicar a marca no PDF.
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            super.onEndPage(writer, document);
            PdfContentByte cb = writer.getDirectContent();
            String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String footerText = documentTitle + " • Gerado em " + generatedAt + " • Página "
                    + writer.getPageNumber();

            cb.saveState();
            cb.setColorStroke(PDF_BORDER);
            cb.setLineWidth(0.5f);
            cb.moveTo(document.left(), document.bottom() - 10f);
            cb.lineTo(document.right(), document.bottom() - 10f);
            cb.stroke();

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7, PDF_MUTED_TEXT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(footerText, footerFont),
                    (document.left() + document.right()) / 2f,
                    document.bottom() - 18f, 0);
            cb.restoreState();
        }
    }

    private String safeHeaderValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    /** Linhas do cabeçalho institucional (oficial). */
    private String[] institutionLines(MiniPautaBlock block) {
        return new String[]{
                "REPÚBLICA DE ANGOLA",
                "ADMINISTRAÇÃO MUNICIPAL DE " + safeHeaderValue(municipality, DEFAULT_HEADER_PLACEHOLDER),
                "DIRECÇÃO MUNICIPAL DA EDUCAÇÃO DE " + safeHeaderValue(educationDirectorate, DEFAULT_HEADER_PLACEHOLDER),
                "MINI-PAUTA",
                safeHeaderValue(schoolName, "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS")
        };
    }

    private void addPdfInstitutionHeader(Document document, MiniPautaBlock block)
            throws com.lowagie.text.DocumentException {
        addPdfInstitutionHeader(document, block, null);
    }

    private void addPdfInstitutionHeader(Document document, MiniPautaBlock block, Trimester trimester)
            throws com.lowagie.text.DocumentException {
        Paragraph rep = new Paragraph("REPÚBLICA DE ANGOLA",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PDF_TITLE_TEXT));
        rep.setAlignment(Element.ALIGN_CENTER);
        rep.setSpacingAfter(1f);
        document.add(rep);

        Paragraph municipal = new Paragraph(
                "ADMINISTRAÇÃO MUNICIPAL DE " + safeHeaderValue(municipality, DEFAULT_HEADER_PLACEHOLDER),
                FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_TITLE_TEXT));
        municipal.setAlignment(Element.ALIGN_CENTER);
        municipal.setSpacingAfter(1f);
        document.add(municipal);

        Paragraph education = new Paragraph(
                "DIRECÇÃO MUNICIPAL DA EDUCAÇÃO DE " + safeHeaderValue(educationDirectorate, DEFAULT_HEADER_PLACEHOLDER),
                FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_TITLE_TEXT));
        education.setAlignment(Element.ALIGN_CENTER);
        education.setSpacingAfter(6f);
        document.add(education);

        PdfPTable brandTable = new PdfPTable(new float[]{1.4f, 5.6f, 3.6f});
        brandTable.setWidthPercentage(100f);
        brandTable.setSpacingAfter(6f);
        brandTable.setSplitLate(false);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setPadding(4f);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setFixedHeight(76f);
        logoCell.setBackgroundColor(PDF_PANEL_BG);

        try {
            byte[] logoBytes = loadLogoBytes();
            if (logoBytes != null) {
                Image logo = Image.getInstance(logoBytes);
                float scale = Math.min(60f / logo.getWidth(), 60f / logo.getHeight()) * 100f;
                logo.scalePercent(scale);
                logoCell.addElement(logo);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao carregar o logotipo na tabela do cabeçalho da mini-pauta", e);
        }

        PdfPCell schoolCell = new PdfPCell();
        schoolCell.setBorder(PdfPCell.NO_BORDER);
        schoolCell.setPadding(10f);
        schoolCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        schoolCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        schoolCell.setFixedHeight(76f);
        schoolCell.setBackgroundColor(PDF_PANEL_BG);

        Paragraph schoolNameParagraph = new Paragraph(
                safeHeaderValue(schoolName, "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS").toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PDF_TITLE_TEXT));
        schoolNameParagraph.setAlignment(Element.ALIGN_LEFT);
        schoolNameParagraph.setLeading(0f, 1.15f);
        schoolCell.addElement(schoolNameParagraph);

        Paragraph schoolMeta = new Paragraph(
                (trimester == null ? "Mini-pauta • " : "Pauta • ")
                        + resolveSelectedClassName() + " • " + schoolYear
                        + (trimester == null ? "" : " • " + trimester.getDescricao()),
                FontFactory.getFont(FontFactory.HELVETICA, 8, PDF_MUTED_TEXT));
        schoolMeta.setAlignment(Element.ALIGN_LEFT);
        schoolMeta.setSpacingBefore(5f);
        schoolCell.addElement(schoolMeta);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(PdfPCell.NO_BORDER);
        titleCell.setBackgroundColor(PDF_ACCENT_BG);
        titleCell.setPadding(10f);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setFixedHeight(76f);

        Paragraph title = new Paragraph(trimester == null ? "MINI-PAUTA" : "PAUTA",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PDF_ACCENT_TEXT));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(2f);
        Paragraph subtitle = new Paragraph(
                "Relatório de avaliação escolar",
                FontFactory.getFont(FontFactory.HELVETICA, 8, PDF_ACCENT_TEXT));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        titleCell.addElement(title);
        titleCell.addElement(subtitle);

        brandTable.addCell(logoCell);
        brandTable.addCell(schoolCell);
        brandTable.addCell(titleCell);
        document.add(brandTable);

        PdfPTable metaTable = new PdfPTable(new float[]{2.7f, 2.7f, 2.7f, 2.0f});
        metaTable.setWidthPercentage(100f);
        metaTable.setSpacingBefore(1f);
        metaTable.setSpacingAfter(blankMode ? 6f : 8f);
        metaTable.setSplitLate(false);

        addMetaCell(metaTable, "DISCIPLINA", block.getDisciplineName(), true);
        addMetaCell(metaTable, "CLASSE / TURMA", resolveSelectedClassName(), false);
        addMetaCell(metaTable, "ANO LECTIVO", schoolYear, false);
        addMetaCell(metaTable, "ESCALA", block.getScaleLabel(), true);
        document.add(metaTable);

        if (blankMode) {
            Paragraph note = new Paragraph(
                    trimester == null
                            ? "Documento para preenchimento manual do professor — MAC, NPT e MT"
                            : "Documento para preenchimento manual do professor — MAC e NPT",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, PDF_MUTED_TEXT));
            note.setAlignment(Element.ALIGN_CENTER);
            note.setSpacingAfter(8f);
            document.add(note);
        }
    }

    private void addMetaCell(PdfPTable table, String label, String value, boolean accent) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, accent ? PDF_ACCENT_BG_DEEP : PDF_TITLE_TEXT)));
        labelCell.setBackgroundColor(accent ? PDF_META_LABEL_BG : new Color(248, 240, 177));
        labelCell.setBorderColor(PDF_BORDER);
        labelCell.setPadding(6f);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "—",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PDF_TITLE_TEXT)));
        valueCell.setBackgroundColor(Color.WHITE);
        valueCell.setBorderColor(PDF_BORDER);
        valueCell.setPadding(7f);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addPdfSignature(Document document) throws com.lowagie.text.DocumentException {
        Paragraph sig = new Paragraph("O Professor",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_TITLE_TEXT));
        sig.setAlignment(Element.ALIGN_CENTER);
        sig.setSpacingBefore(24f);
        document.add(sig);

        Paragraph line = new Paragraph("_____________________________",
                FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_MUTED_TEXT));
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingBefore(2f);
        document.add(line);
    }

    private PdfPTable buildPdfTable(MiniPautaBlock block) {
        PdfPTable table = new PdfPTable(new float[]{1.1f, 4.8f,
            1.3f, 1.3f, 1.3f, 2.0f, 1.3f, 1.3f, 1.3f, 2.0f,
            1.3f, 1.3f, 1.3f, 2.0f, 2.2f});
        table.setWidthPercentage(100f);
        table.setHeaderRows(2);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(10f);
        table.setSplitLate(false);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, PDF_HEADER_TEXT);

        table.addCell(pdfHeaderCell("Nº", headerFont, 1, 2));
        table.addCell(pdfHeaderCell("NOME COMPLETO", headerFont, 1, 2));
        for (int t = 1; t <= 3; t++) {
            table.addCell(pdfHeaderCell(t + "º TRIMESTRE", headerFont, 4, 1));
        }
        table.addCell(pdfHeaderCell("OBSERVAÇÃO", headerFont, 1, 2));

        for (int t = 1; t <= 3; t++) {
            for (String h : MINI_PAUTA_SUB_HEADERS) {
                table.addCell(pdfHeaderCell(h, headerFont, 1, 1));
            }
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, PDF_TITLE_TEXT);
        int rowIdx = 0;
        for (MiniPautaRow row : block.getRows()) {
            Color rowBg = rowIdx % 2 == 0 ? PDF_STRIPE_BG : Color.WHITE;
            table.addCell(pdfDataCell(String.valueOf(row.getRowNumber()), cellFont, Element.ALIGN_CENTER, rowBg));
            table.addCell(pdfDataCell(row.getStudentName(), cellFont, Element.ALIGN_LEFT, rowBg));
            for (int t = 1; t <= 3; t++) {
                TrimesterScore s = row.getScores().get(String.valueOf(t));
                table.addCell(pdfDataCell(s.getMacFormatted(), cellFont, Element.ALIGN_CENTER, rowBg));
                table.addCell(pdfDataCell(s.getNptFormatted(), cellFont, Element.ALIGN_CENTER, rowBg));
                table.addCell(pdfDataCell(s.getMtFormatted(), cellFont, Element.ALIGN_CENTER, rowBg));
                table.addCell(pdfDataCell(s.getSituation(), cellFont, Element.ALIGN_CENTER, rowBg));
            }
            table.addCell(pdfDataCell("", cellFont, Element.ALIGN_LEFT, rowBg));
            rowIdx++;
        }
        return table;
    }

    private PdfPTable buildTeacherBlankPautaTable() throws com.lowagie.text.DocumentException {
        int disciplineCount = reportBlocks.size();
        int columnCount = 2 + disciplineCount * 2;
        PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100f);
        table.setHeaderRows(2);
        table.setSpacingBefore(2f);
        table.setSplitRows(false);
        table.setSplitLate(false);

        float[] widths = new float[columnCount];
        widths[0] = 0.45f;
        widths[1] = 3.2f;
        for (int column = 2; column < columnCount; column++) {
            widths[column] = 0.8f;
        }
        table.setWidths(widths);

        float disciplineFontSize = disciplineCount > 10 ? 5.5f : 7f;
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, disciplineFontSize, PDF_HEADER_TEXT);
        addTeacherBlankPautaHeader(table, "Nº", headerFont, 1, 2);
        addTeacherBlankPautaHeader(table, "NOME COMPLETO", headerFont, 1, 2);
        for (MiniPautaBlock block : reportBlocks) {
            addTeacherBlankPautaHeader(table, block.getDisciplineName(), headerFont, 2, 1);
        }
        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6f, PDF_HEADER_TEXT);
        for (int index = 0; index < disciplineCount; index++) {
            addTeacherBlankPautaHeader(table, "MAC", subHeaderFont, 1, 1);
            addTeacherBlankPautaHeader(table, "NPT", subHeaderFont, 1, 1);
        }

        List<MiniPautaRow> students = reportBlocks.isEmpty() || reportBlocks.get(0).getRows() == null
                ? List.of() : reportBlocks.get(0).getRows();
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7, PDF_TITLE_TEXT);
        for (int index = 0; index < students.size(); index++) {
            MiniPautaRow row = students.get(index);
            Color background = index % 2 == 0 ? Color.WHITE : PDF_STRIPE_BG;
            addTeacherBlankPautaCell(table, String.valueOf(row.getRowNumber()), cellFont,
                    Element.ALIGN_CENTER, background);
            addTeacherBlankPautaCell(table, row.getStudentName(), cellFont, Element.ALIGN_LEFT, background);
            for (int discipline = 0; discipline < disciplineCount; discipline++) {
                addTeacherBlankPautaCell(table, "", cellFont, Element.ALIGN_CENTER, background);
                addTeacherBlankPautaCell(table, "", cellFont, Element.ALIGN_CENTER, background);
            }
        }

        return table;
    }

    private void addTeacherBlankPautaHeader(
            PdfPTable table, String value, Font font, int colspan, int rowspan) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setBackgroundColor(PDF_HEADER_BG);
        cell.setBorderColor(PDF_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        table.addCell(cell);
    }

    private void addTeacherBlankPautaCell(
            PdfPTable table, String value, Font font, int alignment, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setBorderColor(PDF_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(16f);
        cell.setPadding(3f);
        cell.setBackgroundColor(background);
        table.addCell(cell);
    }

    private PdfPCell pdfHeaderCell(String text, Font font, int colspan, int rowspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PDF_HEADER_BG);
        cell.setBorderColor(PDF_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        return cell;
    }

    private PdfPCell pdfDataCell(String text, Font font, int alignment, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setBackgroundColor(background);
        cell.setBorderColor(PDF_BORDER);
        cell.setNoWrap(false);
        return cell;
    }

    private void headerCell(XSSFWorkbook wb, Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    /** Nome da turma selecionada (para uso na view). */
    public String getSelectedClassName() {
        return resolveSelectedClassName();
    }

    public boolean isSelectedSextaClasse() {
        SchoolClass selectedClass = resolveSelectedClass();
        return selectedClass != null && selectedClass.getClasse() == Classe.SEXTA;
    }

    private String resolveSelectedClassName() {
        if (selectedSchoolClassId == null) {
            return "—";
        }
        return schoolClasses.stream()
                .filter(t -> t.getPkSchoolClass().equals(selectedSchoolClassId))
                .findFirst()
                .map(SchoolClass::getClassName)
                .orElse("—");
    }

    private String buildFileNameBase() {
        String turma = resolveSelectedClassName();
        String normalized = Normalizer.normalize(turma, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .toLowerCase();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String suffix = blankMode ? "-em-branco" : "";
        return "mini-pauta" + suffix + "-" + (normalized.isBlank() ? "turma" : normalized) + "-" + timestamp;
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
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // VIEW MODELS INTERNOS
    // ═══════════════════════════════════════════════════════════════

    /** Um bloco = uma disciplina, com os 3 trimestres lado a lado. */
    public static class MiniPautaBlock implements Serializable {
        private static final long serialVersionUID = 1L;
        private String disciplineName;
        private String scaleLabel;
        private String educationLevel;
        private List<MiniPautaRow> rows = new ArrayList<>();

        public boolean isEmpty() { return rows == null || rows.isEmpty(); }

        public String getDisciplineName() { return disciplineName; }
        public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }
        public String getScaleLabel() { return scaleLabel; }
        public void setScaleLabel(String scaleLabel) { this.scaleLabel = scaleLabel; }
        public String getEducationLevel() { return educationLevel; }
        public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
        public List<MiniPautaRow> getRows() { return rows; }
        public void setRows(List<MiniPautaRow> rows) { this.rows = rows; }
    }

    /** Uma linha = um aluno, com as notas de cada trimestre. */
    public static class MiniPautaRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private int rowNumber;
        private String studentName;
        /** key = "1", "2", "3" (número do trimestre) */
        private Map<String, TrimesterScore> scores = new LinkedHashMap<>();

        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Map<String, TrimesterScore> getScores() { return scores; }
        public void setScores(Map<String, TrimesterScore> scores) { this.scores = scores; }
    }

    /** Notas de um aluno num único trimestre. */
    public static class TrimesterScore implements Serializable {
        private static final long serialVersionUID = 1L;
        private Double mac;
        private Double npt;
        private Double mt;
        private String situation;
        private String observation;

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
        public String getObservation() { return observation; }
        public void setObservation(String observation) { this.observation = observation; }

        // Alterado: nota ausente agora devolve célula em branco ("") em vez
        // de "—", para que tanto o PDF já preenchido como a pauta em branco
        // (blankMode) fiquem com espaço limpo para o professor escrever à mão.
        private static String formatScore(Double v) {
            if (v == null) return "";
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
    public boolean isBlankMode() { return blankMode; }
    public void setBlankMode(boolean blankMode) { this.blankMode = blankMode; }
    public String getFinalSextaClasseMode() { return finalSextaClasseMode; }
    public void setFinalSextaClasseMode(String finalSextaClasseMode) {
        this.finalSextaClasseMode = finalSextaClasseMode;
    }
    public List<MiniPautaBlock> getReportBlocks() { return reportBlocks; }
    public void setReportBlocks(List<MiniPautaBlock> reportBlocks) { this.reportBlocks = reportBlocks; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public String getEducationDirectorate() { return educationDirectorate; }
    public void setEducationDirectorate(String educationDirectorate) { this.educationDirectorate = educationDirectorate; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
}