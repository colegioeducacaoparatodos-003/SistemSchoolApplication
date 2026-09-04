package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.io.SituationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class BoletimController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BoletimController.class.getName());

    // ═══════════════════════════════════════════════════════════════
    // FILTROS (multi-seleção)
    // ═══════════════════════════════════════════════════════════════
    private List<Long> selectedStudentIds = new ArrayList<>();
    private List<Long> selectedEnrolmentIds = new ArrayList<>();
    private List<Trimester> selectedTrimesters = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // LISTAS
    // ═══════════════════════════════════════════════════════════════
    private List<Student> students = new ArrayList<>();
    private List<EnrolmentOption> enrolmentOptions = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // ESTADO
    // ═══════════════════════════════════════════════════════════════
    private boolean rendered = false;
    private List<BoletimViewModel> boletins = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // SERVIÇOS
    // ═══════════════════════════════════════════════════════════════
    @Inject
    private StudentRepository studentRepository;
    @Inject
    private EnrolmentRepository enrolmentRepository;
    @Inject
    private TrimesterResultService trimesterResultService;

    @PostConstruct
    public void init() {
        loadStudents();

        // Pré-seleção automática quando vem da tabela de resultados trimestrais
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
            String enrolmentPkParam = params.get("enrolmentPk");
            String trimesterParam = params.get("trimester");

            if (enrolmentPkParam != null && !enrolmentPkParam.isBlank()) {
                try {
                    Long enrolmentPk = Long.valueOf(enrolmentPkParam);
                    Trimester trimester = (trimesterParam != null && !trimesterParam.isBlank())
                            ? Trimester.valueOf(trimesterParam)
                            : Trimester.PRIMEIRO;
                    abrirParaMatricula(enrolmentPk, trimester);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Parâmetros de boletim inválidos: enrolmentPk=" + enrolmentPkParam + ", trimester=" + trimesterParam, e);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    private void loadStudents() {
        try {
            students = studentRepository.findAll();
            if (students == null) {
                students = new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
        }
    }

    /**
     * Chamado via p:ajax quando o utilizador altera a seleção de Alunos.
     * Agrega as matrículas de TODOS os alunos selecionados e mantém, no
     * selectedEnrolmentIds, apenas as que continuam válidas.
     */
    public void onStudentsChange() {
        enrolmentOptions = new ArrayList<>();

        if (selectedStudentIds == null || selectedStudentIds.isEmpty()) {
            selectedEnrolmentIds = new ArrayList<>();
            return;
        }

        try {
            List<EnrolmentOption> options = new ArrayList<>();

            for (Long studentId : selectedStudentIds) {
                List<Enrolment> list = enrolmentRepository.findByStudent_PkStudentWithSchoolClass(studentId);
                if (list == null) {
                    continue;
                }
                for (Enrolment e : list) {
                    options.add(toEnrolmentOption(e));
                }
            }

            options.sort(Comparator
                    .comparing(EnrolmentOption::getStudentName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                    .thenComparing(EnrolmentOption::getEnrolmentNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

            enrolmentOptions = options;

            // Mantém apenas as matrículas ainda válidas dentro da nova seleção de alunos
            Set<Long> validIds = options.stream()
                    .map(EnrolmentOption::getEnrolmentId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (selectedEnrolmentIds != null) {
                selectedEnrolmentIds = selectedEnrolmentIds.stream()
                        .filter(validIds::contains)
                        .collect(Collectors.toList());
            }

            // Se sobrou exatamente uma matrícula disponível, pré-seleciona
            if (enrolmentOptions.size() == 1
                    && (selectedEnrolmentIds == null || selectedEnrolmentIds.isEmpty())) {
                selectedEnrolmentIds = new ArrayList<>(List.of(enrolmentOptions.get(0).getEnrolmentId()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar matrículas dos alunos", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matrículas", e.getMessage());
        }
    }

    private EnrolmentOption toEnrolmentOption(Enrolment enrolment) {
        EnrolmentOption option = new EnrolmentOption();
        option.setEnrolmentId(enrolment.getPhEnrolment());
        option.setStudentName(enrolment.getStudent() != null ? enrolment.getStudent().getFullName() : "—");
        option.setEnrolmentNumber(enrolment.getEnrolmentNumer());
        option.setSchoolClassName(enrolment.getSchoolClass() != null ? enrolment.getSchoolClass().getClassName() : "—");
        return option;
    }

    // ═══════════════════════════════════════════════════════════════
    // ABERTURA A PARTIR DA AÇÃO DA LINHA (tabela de Resultados Trimestrais)
    // ═══════════════════════════════════════════════════════════════

    public void abrirParaMatricula(Long enrolmentPk, Trimester trimester) {
        clear();

        if (enrolmentPk == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Matrícula inválida.");
            return;
        }

        try {
            Enrolment enrolment = enrolmentRepository.findById(enrolmentPk).orElse(null);
            if (enrolment == null || enrolment.getStudent() == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Matrícula não encontrada.");
                return;
            }

            selectedStudentIds = new ArrayList<>(List.of(enrolment.getStudent().getPkStudent()));
            onStudentsChange();
            selectedEnrolmentIds = new ArrayList<>(List.of(enrolmentPk));
            selectedTrimesters = new ArrayList<>(List.of(trimester != null ? trimester : Trimester.PRIMEIRO));

            gerarBoletins();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao abrir boletim para a matrícula", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GERAÇÃO (multi-seleção: aluno × matrícula × trimestre)
    // ═══════════════════════════════════════════════════════════════

    public void gerarBoletins() {
        if (selectedEnrolmentIds == null || selectedEnrolmentIds.isEmpty()
                || selectedTrimesters == null || selectedTrimesters.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                    "Selecione pelo menos uma matrícula e um trimestre.");
            return;
        }

        try {
            List<TrimesterResultDTO> all = trimesterResultService.getAllResults();
            if (all == null) {
                all = new ArrayList<>();
            }

            List<BoletimViewModel> generated = new ArrayList<>();

            for (Long enrolmentId : selectedEnrolmentIds) {
                Enrolment enrolment = enrolmentRepository.findById(enrolmentId).orElse(null);
                if (enrolment == null || enrolment.getStudent() == null) {
                    continue;
                }

                String studentName = enrolment.getStudent().getFullName();
                String enrolmentNumber = enrolment.getEnrolmentNumer();
                String schoolClassName = enrolment.getSchoolClass() != null
                        ? enrolment.getSchoolClass().getClassName() : "—";

                for (Trimester trimester : selectedTrimesters) {
                    final Long fEnrolmentId = enrolmentId;
                    List<TrimesterResultDTO> results = all.stream()
                            .filter(r -> fEnrolmentId.equals(r.getEnrolmentPk()))
                            .filter(r -> trimester.equals(r.getTrimester()))
                            .collect(Collectors.toList());

                    generated.add(buildBoletim(studentName, enrolmentNumber, schoolClassName, trimester, results));
                }
            }

            generated.sort(Comparator
                    .comparing(BoletimViewModel::getStudentName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                    .thenComparing(BoletimViewModel::getTrimester, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

            boletins = generated;
            rendered = true;

            if (boletins.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                        "Nenhum boletim pôde ser gerado para a seleção atual.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar boletins", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar boletins", e.getMessage());
            rendered = true;
            boletins = new ArrayList<>();
        }
    }

    public void clear() {
        selectedStudentIds = new ArrayList<>();
        selectedEnrolmentIds = new ArrayList<>();
        selectedTrimesters = new ArrayList<>();
        enrolmentOptions = new ArrayList<>();
        boletins = new ArrayList<>();
        rendered = false;
    }

    private BoletimViewModel buildBoletim(String studentName, String enrolmentNumber, String schoolClassName,
                                           Trimester trimester, List<TrimesterResultDTO> results) {
        BoletimViewModel vm = new BoletimViewModel();
        vm.setStudentName(studentName);
        vm.setSchoolClassName(schoolClassName);
        vm.setEnrolmentNumber(enrolmentNumber);
        vm.setTrimester(trimester.toString());

        List<BoletimDisciplineRow> rows = results.stream()
                .sorted(Comparator.comparing(TrimesterResultDTO::getDisciplineName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(r -> {
                    BoletimDisciplineRow row = new BoletimDisciplineRow();
                    row.setDisciplineName(r.getDisciplineName());
                    row.setMac(r.getMac());
                    row.setNpt(r.getNpt());
                    row.setMt(r.getMt());
                    row.setFinalSituation(formatSituation(r.getSituation()));
                    return row;
                })
                .collect(Collectors.toList());
        vm.setDisciplines(rows);

        double sum = 0;
        int count = 0;
        for (TrimesterResultDTO r : results) {
            if (r.getMt() != null) {
                sum += r.getMt();
                count++;
            }
        }
        vm.setGeneralAverage(count > 0 ? sum / count : null);

        if (results.isEmpty()) {
            vm.setGeneralSituation("—");
        } else {
            boolean anyReprovado = results.stream().anyMatch(r -> r.getSituation() == SituationType.REPROVADO);
            boolean allAprovado = results.stream().allMatch(r -> r.getSituation() == SituationType.APROVADO);
            vm.setGeneralSituation(anyReprovado ? "Reprovado" : (allAprovado ? "Aprovado" : "Em Curso"));
        }

        return vm;
    }

    private String formatSituation(SituationType situation) {
        if (situation == null) {
            return "—";
        }
        return switch (situation) {
            case APROVADO -> "Aprovado";
            case REPROVADO -> "Reprovado";
            default -> "—";
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF (combinado — um documento com todos os boletins gerados)
    // ═══════════════════════════════════════════════════════════════

    public StreamedContent getPdfFile() {
        if (boletins == null || boletins.isEmpty()) {
            return DefaultStreamedContent.builder().build();
        }

        byte[] bytes = gerarPdfBytes();
        String nomeArquivo = boletins.size() == 1
                ? "boletim_" + sanitize(boletins.get(0).getStudentName()) + "_T" + boletins.get(0).getTrimester() + ".pdf"
                : "boletins_" + boletins.size() + "_gerados.pdf";

        return DefaultStreamedContent.builder()
                .name(nomeArquivo)
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(bytes))
                .build();
    }

    private byte[] gerarPdfBytes() {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Color brandBlack = new Color(20, 20, 20);
            Color brandYellow = new Color(245, 180, 0);
            Color mutedGray = new Color(102, 106, 112);

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, brandBlack);
            Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD, mutedGray);
            Font valueFont = new Font(Font.HELVETICA, 11, Font.BOLD, brandBlack);
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, brandYellow);
            Font cellFont = new Font(Font.HELVETICA, 10, Font.NORMAL, brandBlack);
            Font footerLabelFont = new Font(Font.HELVETICA, 10, Font.BOLD, mutedGray);
            Font footerValueFont = new Font(Font.HELVETICA, 14, Font.BOLD, brandBlack);
            Font emptyFont = new Font(Font.HELVETICA, 11, Font.ITALIC, mutedGray);

            boolean first = true;
            for (BoletimViewModel boletim : boletins) {
                if (!first) {
                    document.newPage();
                }
                first = false;

                Paragraph title = new Paragraph("Boletim de Notas", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(4);
                document.add(title);

                Paragraph subtitle = new Paragraph(boletim.getTrimester() + "º Trimestre",
                        new Font(Font.HELVETICA, 10, Font.NORMAL, mutedGray));
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(18);
                document.add(subtitle);

                PdfPTable info = new PdfPTable(3);
                info.setWidthPercentage(100);
                info.setSpacingAfter(18);
                addInfoCell(info, "ALUNO", boletim.getStudentName(), labelFont, valueFont);
                addInfoCell(info, "Nº MATRÍCULA", boletim.getEnrolmentNumber(), labelFont, valueFont);
                addInfoCell(info, "TURMA", boletim.getSchoolClassName(), labelFont, valueFont);
                document.add(info);

                if (boletim.isEmpty()) {
                    Paragraph empty = new Paragraph("Nenhum resultado encontrado para este aluno neste trimestre.", emptyFont);
                    empty.setAlignment(Element.ALIGN_CENTER);
                    empty.setSpacingBefore(20);
                    document.add(empty);
                    continue;
                }

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 1f, 1f, 1f, 1.4f});
                addHeaderCell(table, "Disciplina", headerFont, brandBlack);
                addHeaderCell(table, "MAC", headerFont, brandBlack);
                addHeaderCell(table, "NPT", headerFont, brandBlack);
                addHeaderCell(table, "MT", headerFont, brandBlack);
                addHeaderCell(table, "Situação", headerFont, brandBlack);

                boolean alt = false;
                for (BoletimDisciplineRow row : boletim.getDisciplines()) {
                    Color bg = alt ? new Color(247, 247, 248) : Color.WHITE;
                    addBodyCell(table, row.getDisciplineName(), cellFont, Element.ALIGN_LEFT, bg);
                    addBodyCell(table, row.getMacFormatted(), cellFont, Element.ALIGN_CENTER, bg);
                    addBodyCell(table, row.getNptFormatted(), cellFont, Element.ALIGN_CENTER, bg);
                    addBodyCell(table, row.getMtFormatted(), cellFont, Element.ALIGN_CENTER, bg);
                    addBodyCell(table, row.getFinalSituation(), cellFont, Element.ALIGN_CENTER, bg);
                    alt = !alt;
                }
                document.add(table);

                PdfPTable footer = new PdfPTable(2);
                footer.setWidthPercentage(100);
                footer.setSpacingBefore(20);
                footer.addCell(footerCell("Média Geral do Trimestre", boletim.getGeneralAverageFormatted(),
                        footerLabelFont, footerValueFont, Element.ALIGN_LEFT));
                footer.addCell(footerCell("Situação Final", boletim.getGeneralSituation(),
                        footerLabelFont, footerValueFont, Element.ALIGN_RIGHT));
                document.add(footer);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF dos boletins", e);
            return new byte[0];
        }
    }

    private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(6);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value != null ? value : "—", valueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private PdfPCell footerCell(String label, String value, Font labelFont, Font valueFont, int align) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        Paragraph p = new Paragraph();
        p.setAlignment(align);
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value != null ? value : "—", valueFont));
        cell.addElement(p);
        return cell;
    }

    private String sanitize(String value) {
        if (value == null) {
            return "aluno";
        }
        return value.trim().replaceAll("[^a-zA-Z0-9]+", "_");
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // VIEW MODELS
    // ═══════════════════════════════════════════════════════════════

    public static class EnrolmentOption implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long enrolmentId;
        private String studentName;
        private String enrolmentNumber;
        private String schoolClassName;

        public Long getEnrolmentId() { return enrolmentId; }
        public void setEnrolmentId(Long enrolmentId) { this.enrolmentId = enrolmentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getEnrolmentNumber() { return enrolmentNumber; }
        public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

        public String getSchoolClassName() { return schoolClassName; }
        public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }
    }

    public static class BoletimViewModel implements Serializable {
        private static final long serialVersionUID = 1L;

        private String studentName;
        private String enrolmentNumber;
        private String schoolClassName;
        private String trimester;
        private List<BoletimDisciplineRow> disciplines = new ArrayList<>();
        private Double generalAverage;
        private String generalSituation;

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getEnrolmentNumber() { return enrolmentNumber; }
        public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

        public String getSchoolClassName() { return schoolClassName; }
        public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

        public String getTrimester() { return trimester; }
        public void setTrimester(String trimester) { this.trimester = trimester; }

        public List<BoletimDisciplineRow> getDisciplines() { return disciplines; }
        public void setDisciplines(List<BoletimDisciplineRow> disciplines) { this.disciplines = disciplines; }

        public boolean isEmpty() { return disciplines == null || disciplines.isEmpty(); }

        public Double getGeneralAverage() { return generalAverage; }
        public void setGeneralAverage(Double generalAverage) { this.generalAverage = generalAverage; }

        public String getGeneralAverageFormatted() { return formatScore(generalAverage); }

        public String getGeneralSituation() { return generalSituation; }
        public void setGeneralSituation(String generalSituation) { this.generalSituation = generalSituation; }
    }

    public static class BoletimDisciplineRow implements Serializable {
        private static final long serialVersionUID = 1L;

        private String disciplineName;
        private Double mac;
        private Double npt;
        private Double mt;
        private String finalSituation;

        public String getDisciplineName() { return disciplineName; }
        public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

        public Double getMac() { return mac; }
        public void setMac(Double mac) { this.mac = mac; }
        public String getMacFormatted() { return formatScore(mac); }

        public Double getNpt() { return npt; }
        public void setNpt(Double npt) { this.npt = npt; }
        public String getNptFormatted() { return formatScore(npt); }

        public Double getMt() { return mt; }
        public void setMt(Double mt) { this.mt = mt; }
        public String getMtFormatted() { return formatScore(mt); }

        public String getFinalSituation() { return finalSituation; }
        public void setFinalSituation(String finalSituation) { this.finalSituation = finalSituation; }
    }

    private static String formatScore(Double v) {
        if (v == null) {
            return "—";
        }
        return String.format(Locale.forLanguageTag("pt"), "%.1f", v).replace('.', ',');
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public List<Long> getSelectedStudentIds() { return selectedStudentIds; }
    public void setSelectedStudentIds(List<Long> selectedStudentIds) { this.selectedStudentIds = selectedStudentIds; }

    public List<Long> getSelectedEnrolmentIds() { return selectedEnrolmentIds; }
    public void setSelectedEnrolmentIds(List<Long> selectedEnrolmentIds) { this.selectedEnrolmentIds = selectedEnrolmentIds; }

    public List<Trimester> getSelectedTrimesters() { return selectedTrimesters; }
    public void setSelectedTrimesters(List<Trimester> selectedTrimesters) { this.selectedTrimesters = selectedTrimesters; }

    public List<Student> getStudents() { return students; }
    public List<EnrolmentOption> getEnrolmentOptions() { return enrolmentOptions; }
    public Trimester[] getTrimesters() { return Trimester.values(); }

    public boolean isRendered() { return rendered; }
    public List<BoletimViewModel> getBoletins() { return boletins; }
}