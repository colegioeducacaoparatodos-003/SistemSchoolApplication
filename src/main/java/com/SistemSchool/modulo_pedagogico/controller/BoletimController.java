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
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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

    // Ajuste conforme os dados reais da instituição
    private static final String SCHOOL_NAME = "ESCOLA EDUCAÇÃO PARA TODOS";
    private static final String SCHOOL_SUB  = "Gestão Escolar";
    private static final String SCHOOL_SLOGAN = "Ensinamos Valores para a Vida";

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

    @Transactional
    public void onStudentsChange() {
        enrolmentOptions = new ArrayList<>();

        if (selectedStudentIds == null || selectedStudentIds.isEmpty()) {
            selectedEnrolmentIds = new ArrayList<>();
            return;
        }

        try {
            List<EnrolmentOption> options = new ArrayList<>();

            for (Long studentId : selectedStudentIds) {
                List<Enrolment> list = enrolmentRepository.findByStudentPkWithStudentAndClass(studentId);
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

            Set<Long> validIds = options.stream()
                    .map(EnrolmentOption::getEnrolmentId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (selectedEnrolmentIds != null) {
                selectedEnrolmentIds = selectedEnrolmentIds.stream()
                        .filter(validIds::contains)
                        .collect(Collectors.toList());
            }

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

    @Transactional
    public void abrirParaMatricula(Long enrolmentPk, Trimester trimester) {
        clear();

        if (enrolmentPk == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Matrícula inválida.");
            return;
        }

        try {
            Enrolment enrolment = enrolmentRepository.findByIdWithStudentAndClass(enrolmentPk).orElse(null);
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

    @Transactional
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
                Enrolment enrolment = enrolmentRepository.findByIdWithStudentAndClass(enrolmentId).orElse(null);
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

    private int trimesterNumber(Trimester t) {
        if (t == null) {
            return 1;
        }
        return switch (t) {
            case SEGUNDO -> 2;
            case TERCEIRO -> 3;
            default -> 1;
        };
    }

    private String roman(int n) {
        return switch (n) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF — FORMATO A5, estrutura do modelo oficial
    // ═══════════════════════════════════════════════════════════════

    public StreamedContent getPdfFile() {
        if (boletins == null || boletins.isEmpty()) {
            return DefaultStreamedContent.builder().build();
        }

        byte[] bytes = gerarPdfBytes();
        String nomeArquivo = boletins.size() == 1
                ? "boletim_" + sanitize(boletins.get(0).getStudentName()) + "_T" + boletins.get(0).getTrimester() + ".pdf"
                : "boletins_" + boletins.size() + "_A5.pdf";

        return DefaultStreamedContent.builder()
                .name(nomeArquivo)
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(bytes))
                .build();
    }

    private byte[] gerarPdfBytes() {
        try {
            // A5 = 148 x 210 mm
            Document document = new Document(PageSize.A5, 26, 26, 26, 26);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Color brandBlack = new Color(20, 20, 20);
            Color mutedGray  = new Color(102, 106, 112);
            Color lineBlue   = new Color(127, 168, 217);
            Color peach      = new Color(252, 228, 196);
            Color lightBlue  = new Color(220, 235, 247);
            Color zebraBlue  = new Color(244, 248, 253);
            Color fieldGray  = new Color(154, 160, 166);

            Font schoolFont   = new Font(Font.HELVETICA, 13, Font.BOLD, brandBlack);
            Font schoolSub    = new Font(Font.HELVETICA, 7,  Font.NORMAL, mutedGray);
            Font sloganFont   = new Font(Font.HELVETICA, 8,  Font.ITALIC, mutedGray);
            Font titleFont    = new Font(Font.HELVETICA, 14, Font.BOLD, brandBlack);
            Font labelFont    = new Font(Font.HELVETICA, 8,  Font.BOLD, mutedGray);
            Font valueFont    = new Font(Font.HELVETICA, 9,  Font.NORMAL, brandBlack);
            Font discHeadFont = new Font(Font.HELVETICA, 9,  Font.BOLD, brandBlack);
            Font triHeadFont  = new Font(Font.HELVETICA, 9,  Font.BOLD, brandBlack);
            Font colHeadFont  = new Font(Font.HELVETICA, 8,  Font.BOLD, brandBlack);
            Font cellFont     = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, brandBlack);
            Font nameFont     = new Font(Font.HELVETICA, 8.5f, Font.BOLD, brandBlack);
            Font legendTitle  = new Font(Font.HELVETICA, 7.5f, Font.BOLD, brandBlack);
            Font legendFont   = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, brandBlack);
            Font summaryFont  = new Font(Font.HELVETICA, 8.5f, Font.BOLD, brandBlack);
            Font signFont     = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, mutedGray);
            Font emptyFont    = new Font(Font.HELVETICA, 9, Font.ITALIC, mutedGray);

            String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            boolean first = true;
            for (BoletimViewModel boletim : boletins) {
                if (!first) {
                    document.newPage();
                }
                first = false;

                // ── Cabeçalho: colégio + lema + linha ──
                PdfPTable header = new PdfPTable(2);
                header.setWidthPercentage(100);
                header.setWidths(new float[]{1.5f, 1f});

                PdfPCell brandCell = new PdfPCell();
                brandCell.setBorder(Rectangle.NO_BORDER);
                Paragraph brand = new Paragraph();
                brand.add(new Chunk(SCHOOL_NAME + "\n", schoolFont));
                brand.add(new Chunk(SCHOOL_SUB, schoolSub));
                brandCell.addElement(brand);
                header.addCell(brandCell);

                PdfPCell sloganCell = new PdfPCell(new Phrase(SCHOOL_SLOGAN, sloganFont));
                sloganCell.setBorder(Rectangle.NO_BORDER);
                sloganCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                sloganCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                header.addCell(sloganCell);
                document.add(header);

                PdfPTable headerLine = new PdfPTable(1);
                headerLine.setWidthPercentage(100);
                PdfPCell lineCell = new PdfPCell(new Phrase(""));
                lineCell.setBorder(Rectangle.NO_BORDER);
                lineCell.setBorderWidthBottom(1.4f);
                lineCell.setBorderColorBottom(brandBlack);
                lineCell.setFixedHeight(3);
                headerLine.addCell(lineCell);
                document.add(headerLine);

                // ── Título ──
                Paragraph title = new Paragraph("BOLETIM DE NOTAS", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingBefore(10);
                title.setSpacingAfter(10);
                document.add(title);

                // ── Dados do aluno ──
                PdfPTable info = new PdfPTable(4);
                info.setWidthPercentage(100);
                info.setWidths(new float[]{1.4f, 1f, 1f, 1f});
                info.setSpacingAfter(12);

                PdfPCell nameCell = new PdfPCell();
                nameCell.setColspan(4);
                nameCell.setBorder(Rectangle.NO_BORDER);
                nameCell.setBorderWidthBottom(0.8f);
                nameCell.setBorderColorBottom(fieldGray);
                Paragraph nameP = new Paragraph();
                nameP.add(new Chunk("Nome: ", labelFont));
                nameP.add(new Chunk(boletim.getStudentName() != null ? boletim.getStudentName() : "—", valueFont));
                nameCell.addElement(nameP);
                info.addCell(nameCell);

                info.addCell(infoField("Nº Matrícula: ", boletim.getEnrolmentNumber(), labelFont, valueFont));
                info.addCell(infoField("Turma: ", boletim.getSchoolClassName(), labelFont, valueFont));
                info.addCell(infoField("Trimestre: ", boletim.getTrimester() + "º", labelFont, valueFont));
                info.addCell(infoField("Data: ", today, labelFont, valueFont));
                document.add(info);

                // ── Corpo ──
                if (boletim.isEmpty()) {
                    Paragraph empty = new Paragraph("Nenhum resultado encontrado para este aluno neste trimestre.", emptyFont);
                    empty.setAlignment(Element.ALIGN_CENTER);
                    empty.setSpacingBefore(24);
                    document.add(empty);
                } else {
                    PdfPTable table = new PdfPTable(4);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{3f, 1f, 1f, 1f});

                    PdfPCell discHead = new PdfPCell(new Phrase("Disciplinas", discHeadFont));
                    discHead.setRowspan(2);
                    discHead.setBackgroundColor(peach);
                    discHead.setHorizontalAlignment(Element.ALIGN_CENTER);
                    discHead.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    discHead.setBorderColor(lineBlue);
                    discHead.setPadding(5);
                    table.addCell(discHead);

                    PdfPCell triHead = new PdfPCell(new Phrase(boletim.getTrimesterLabel(), triHeadFont));
                    triHead.setColspan(3);
                    triHead.setHorizontalAlignment(Element.ALIGN_CENTER);
                    triHead.setBorderColor(lineBlue);
                    triHead.setPadding(4);
                    table.addCell(triHead);

                    for (String h : new String[]{"MAC", "NPT", "MT"}) {
                        PdfPCell c = new PdfPCell(new Phrase(h, colHeadFont));
                        c.setBackgroundColor(lightBlue);
                        c.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c.setBorderColor(lineBlue);
                        c.setPadding(4);
                        table.addCell(c);
                    }

                    boolean alt = false;
                    for (BoletimDisciplineRow row : boletim.getDisciplines()) {
                        Color bg = alt ? zebraBlue : Color.WHITE;
                        table.addCell(bodyCell(row.getDisciplineName(), nameFont, Element.ALIGN_LEFT, bg, lineBlue));
                        table.addCell(bodyCell(row.getMacFormatted(), cellFont, Element.ALIGN_CENTER, bg, lineBlue));
                        table.addCell(bodyCell(row.getNptFormatted(), cellFont, Element.ALIGN_CENTER, bg, lineBlue));
                        table.addCell(bodyCell(row.getMtFormatted(), cellFont, Element.ALIGN_CENTER, bg, lineBlue));
                        alt = !alt;
                    }
                    document.add(table);

                    // ── Legenda ──
                    Paragraph legendTitleP = new Paragraph("Observações", legendTitle);
                    legendTitleP.setSpacingBefore(10);
                    legendTitleP.setSpacingAfter(2);
                    document.add(legendTitleP);
                    document.add(new Paragraph("MAC - Média da Avaliação Contínua (= Trabalhos de casa, sala de aula...)", legendFont));
                    document.add(new Paragraph("NPT - Nota da Prova do Trimestre", legendFont));
                    document.add(new Paragraph("MT - Média Final do Trimestre", legendFont));

                    // ── Resumo ──
                    PdfPTable summary = new PdfPTable(2);
                    summary.setWidthPercentage(100);
                    summary.setSpacingBefore(12);
                    PdfPCell avgCell = new PdfPCell();
                    avgCell.setBorder(Rectangle.NO_BORDER);
                    Paragraph avgP = new Paragraph();
                    avgP.add(new Chunk("Média Geral do Trimestre: ", summaryFont));
                    avgP.add(new Chunk(boletim.getGeneralAverageFormatted(), summaryFont));
                    avgCell.addElement(avgP);
                    summary.addCell(avgCell);

                    PdfPCell sitCell = new PdfPCell();
                    sitCell.setBorder(Rectangle.NO_BORDER);
                    sitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    Paragraph sitP = new Paragraph();
                    sitP.setAlignment(Element.ALIGN_RIGHT);
                    sitP.add(new Chunk("Situação Final: ", summaryFont));
                    sitP.add(new Chunk(boletim.getGeneralSituation() != null ? boletim.getGeneralSituation() : "—", summaryFont));
                    sitCell.addElement(sitP);
                    summary.addCell(sitCell);
                    document.add(summary);
                }

                // ── Assinatura ──
                Paragraph sign = new Paragraph("\n\n________________________________\nO Director de Turma", signFont);
                sign.setAlignment(Element.ALIGN_CENTER);
                document.add(sign);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF dos boletins", e);
            return new byte[0];
        }
    }

    private PdfPCell infoField(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(0.8f);
        cell.setBorderColorBottom(new Color(154, 160, 166));
        cell.setPaddingTop(4);
        cell.setPaddingBottom(4);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label, labelFont));
        p.add(new Chunk(value != null ? value : "—", valueFont));
        cell.addElement(p);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font, int align, Color bg, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(borderColor);
        cell.setPadding(4);
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
        private String trimesterLabel;
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

        public String getTrimesterLabel() { return trimesterLabel; }
        public void setTrimesterLabel(String trimesterLabel) { this.trimesterLabel = trimesterLabel; }

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
        private String finalSituation;   // ← REINTRODUZIR

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

        public String getFinalSituation() { return finalSituation; }          // ← REINTRODUZIR
        public void setFinalSituation(String finalSituation) { this.finalSituation = finalSituation; }  // ← REINTRODUZIR
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