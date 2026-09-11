package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // Paleta reaproveitada do CSS da view (--enr-black, --yellow-lt, --enr-muted)
    private static final Color PDF_HEADER_BG = new Color(20, 20, 20);
    private static final Color PDF_HEADER_TEXT = new Color(255, 210, 63);
    private static final Color PDF_TITLE_TEXT = new Color(20, 20, 20);
    private static final Color PDF_MUTED_TEXT = new Color(102, 106, 112);
    private static final byte[] XLS_HEADER_BG = new byte[]{20, 20, 20};
    private static final byte[] XLS_GROUP_BG = new byte[]{38, 38, 38};
    private static final byte[] XLS_HEADER_TEXT = new byte[]{(byte) 255, (byte) 210, 63};
    private static final String[] SUB_HEADERS = {"MAC", "NPT", "MT"};

    // Caminho do logotipo institucional dentro do webapp (webapp/resource/imgs/logo.png)
    private static final String LOGO_RESOURCE_PATH = "/resources/imgs/logo.jpg";
    private static final float LOGO_MAX_WIDTH = 60f;
    private static final float LOGO_MAX_HEIGHT = 60f;

    // ── CABEÇALHO INSTITUCIONAL (preencher / ligar a um bean de configuração) ──
    private String schoolName = "ESCOLA EDUCAÇÃO PARA TODOS";
    private String municipality = "__________";
    private String educationDirectorate = "__________";
    private String schoolYear = "2026";

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
                            score.setSituation(r.getSituation() != null ? r.getSituation().toString() : null);
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

    public void clear() {
        selectedSchoolClassId = null;
        selectedEducationLevel = null;
        selectedDisciplineIds = new ArrayList<>();
        selectedTrimesters = new ArrayList<>();
        rendered = false;
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
            // de cada página via PdfPageEventHelper (ver LogoPageEvent).
            Document document = new Document(PageSize.A4, 36, 36, 90, 36);
            OutputStream out = facesContext.getExternalContext().getResponseOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new LogoPageEvent(loadLogoBytes()));
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
                    headerCell(workbook, h1, 2 + tIdx * 3, (tIdx + 1) + "º TRIMESTRE", groupStyle);
                    sheet.addMergedRegion(new CellRangeAddress(headerRowIdx, headerRowIdx,
                            2 + tIdx * 3, 4 + tIdx * 3));
                }
                headerCell(workbook, h1, 11, "OBSERVAÇÃO", headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(headerRowIdx, headerRowIdx + 1, 11, 11));

                // Linha 2 do cabeçalho: MAC | NPT | MT ×3
                Row h2 = sheet.createRow(headerRowIdx + 1);
                for (int tIdx = 0; tIdx < 3; tIdx++) {
                    for (int s = 0; s < SUB_HEADERS.length; s++) {
                        headerCell(workbook, h2, 2 + tIdx * 3 + s, SUB_HEADERS[s], headerStyle);
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
                        excelRow.createCell(1 + tIdx * 3 - 1).setCellValue(s.getMacFormatted());
                        excelRow.createCell(1 + tIdx * 3).setCellValue(s.getNptFormatted());
                        excelRow.createCell(1 + tIdx * 3 + 1).setCellValue(s.getMtFormatted());
                    }
                    excelRow.createCell(11).setCellValue("");
                }

                // Assinatura do professor
                Row sigRow = sheet.createRow(rowIdx + 1);
                Cell sig = sigRow.createCell(4);
                sig.setCellValue("O Professor");

                for (int i = 0; i < 12; i++) {
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
                    sb.append(t).append("ºT MAC;").append(t).append("ºT NPT;").append(t).append("ºT MT;");
                }
                sb.append("OBSERVAÇÃO\n");

                for (MiniPautaRow row : block.getRows()) {
                    sb.append(row.getRowNumber()).append(';')
                            .append(csvEscape(row.getStudentName())).append(';');
                    for (int t = 1; t <= 3; t++) {
                        TrimesterScore s = row.getScores().get(String.valueOf(t));
                        sb.append(csvEscape(s.getMacFormatted())).append(';')
                                .append(csvEscape(s.getNptFormatted())).append(';')
                                .append(csvEscape(s.getMtFormatted())).append(';');
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
    private static class LogoPageEvent extends PdfPageEventHelper {
        private final byte[] logoBytes;

        LogoPageEvent(byte[] logoBytes) {
            this.logoBytes = logoBytes;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            if (logoBytes == null) {
                return;
            }
            try {
                Image logo = Image.getInstance(logoBytes);
                float scale = Math.min(
                        LOGO_MAX_WIDTH / logo.getWidth(),
                        LOGO_MAX_HEIGHT / logo.getHeight()) * 100f;
                logo.scalePercent(scale);

                float x = document.left();
                float y = document.getPageSize().getHeight() - document.topMargin() + 8f;
                logo.setAbsolutePosition(x, y);

                writer.getDirectContent().addImage(logo);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao desenhar o logotipo no PDF", e);
            }
        }
    }

    /** Linhas do cabeçalho institucional (oficial). */
    private String[] institutionLines(MiniPautaBlock block) {
        return new String[]{
                "REPÚBLICA DE ANGOLA",
                "ADMINISTRAÇÃO MUNICIPAL DE " + municipality,
                "DIRECÇÃO MUNICIPAL DA EDUCAÇÃO DE " + educationDirectorate,
                "MINI-PAUTA",
                schoolName
        };
    }

    private void addPdfInstitutionHeader(Document document, MiniPautaBlock block) throws com.lowagie.text.DocumentException {
        String[] lines = institutionLines(block);
        for (String line : lines) {
            Paragraph p = new Paragraph(line, FontFactory.getFont(
                    "MINI-PAUTA".equals(line) ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                    "MINI-PAUTA".equals(line) ? 13 : 10,
                    PDF_TITLE_TEXT));
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(1f);
            document.add(p);
        }

        Paragraph meta = new Paragraph(
                "DISCIPLINA: " + block.getDisciplineName()
                        + "        *CLASSE / TURMA: " + resolveSelectedClassName()
                        + "        ANO LECTIVO: " + schoolYear
                        + "        (Escala " + block.getScaleLabel() + ")",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PDF_TITLE_TEXT));
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingBefore(6f);
        meta.setSpacingAfter(8f);
        document.add(meta);
    }

    private void addPdfSignature(Document document) throws com.lowagie.text.DocumentException {
        Paragraph sig = new Paragraph("O Professor",
                FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_TITLE_TEXT));
        sig.setAlignment(Element.ALIGN_CENTER);
        sig.setSpacingBefore(24f);
        document.add(sig);
        Paragraph line = new Paragraph("_____________________________",
                FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_MUTED_TEXT));
        line.setAlignment(Element.ALIGN_CENTER);
        document.add(line);
    }

    private PdfPTable buildPdfTable(MiniPautaBlock block) {
        // Nº | Nome | (MAC NPT MT)×3 | Observação
        PdfPTable table = new PdfPTable(new float[]{1.1f, 4.6f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 2.2f});
        table.setWidthPercentage(100);
        table.setHeaderRows(2);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PDF_HEADER_TEXT);

        // Linha 1: Nº | NOME COMPLETO | 1º TRIMESTRE (span3) | 2º | 3º | OBSERVAÇÃO (rowspan2)
        table.addCell(pdfHeaderCell("Nº", headerFont, 1, 2));
        table.addCell(pdfHeaderCell("NOME COMPLETO", headerFont, 1, 2));
        for (int t = 1; t <= 3; t++) {
            table.addCell(pdfHeaderCell(t + "º TRIMESTRE", headerFont, 3, 1));
        }
        table.addCell(pdfHeaderCell("OBSERVAÇÃO", headerFont, 1, 2));

        // Linha 2: MAC | NPT | MT ×3
        for (int t = 1; t <= 3; t++) {
            for (String h : SUB_HEADERS) {
                table.addCell(pdfHeaderCell(h, headerFont, 1, 1));
            }
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, PDF_TITLE_TEXT);
        for (MiniPautaRow row : block.getRows()) {
            table.addCell(pdfDataCell(String.valueOf(row.getRowNumber()), cellFont, Element.ALIGN_CENTER));
            table.addCell(pdfDataCell(row.getStudentName(), cellFont, Element.ALIGN_LEFT));
            for (int t = 1; t <= 3; t++) {
                TrimesterScore s = row.getScores().get(String.valueOf(t));
                table.addCell(pdfDataCell(s.getMacFormatted(), cellFont, Element.ALIGN_CENTER));
                table.addCell(pdfDataCell(s.getNptFormatted(), cellFont, Element.ALIGN_CENTER));
                table.addCell(pdfDataCell(s.getMtFormatted(), cellFont, Element.ALIGN_CENTER));
            }
            table.addCell(pdfDataCell("", cellFont, Element.ALIGN_LEFT));
        }
        return table;
    }

    private PdfPCell pdfHeaderCell(String text, Font font, int colspan, int rowspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PDF_HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        return cell;
    }

    private PdfPCell pdfDataCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(3.5f);
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
        return "mini-pauta-" + (normalized.isBlank() ? "turma" : normalized) + "-" + timestamp;
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

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public String getEducationDirectorate() { return educationDirectorate; }
    public void setEducationDirectorate(String educationDirectorate) { this.educationDirectorate = educationDirectorate; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
}