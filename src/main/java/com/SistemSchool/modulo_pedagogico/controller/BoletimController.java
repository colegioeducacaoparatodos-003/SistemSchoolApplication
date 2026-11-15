package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.io.SituationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;
import com.SistemSchool.report.OpenPdfAuditPageEvent;

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
import java.io.IOException;
import java.io.InputStream;
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

                    generated.add(buildBoletim(enrolment.getStudent().getPkStudent(), studentName,
                            enrolmentNumber, schoolClassName, trimester, results));
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

    private BoletimViewModel buildBoletim(Long studentPk, String studentName, String enrolmentNumber,
                                           String schoolClassName, Trimester trimester,
                                           List<TrimesterResultDTO> results) {
        BoletimViewModel vm = new BoletimViewModel();
        vm.setStudentPk(studentPk);
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
    // PDF — dois boletins por página A4 em paisagem
    // ═══════════════════════════════════════════════════════════════

    public StreamedContent getPdfFile() {
        if (boletins == null || boletins.isEmpty()) {
            return DefaultStreamedContent.builder().build();
        }

        byte[] bytes = gerarPdfBytes();
        if (bytes == null || bytes.length == 0) {
            return DefaultStreamedContent.builder().build();
        }

        String nomeArquivo = boletins.size() == 1
                ? "boletim_" + sanitize(boletins.get(0).getStudentName()) + "_T" + boletins.get(0).getTrimester()
                        + "_A4_paisagem.pdf"
                : "boletins_" + boletins.size() + ".pdf";

        return DefaultStreamedContent.builder()
                .name(nomeArquivo)
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(bytes))
                .build();
    }

    private byte[] gerarPdfBytes() {
        try {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 20, 20);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new OpenPdfAuditPageEvent());
            document.open();

            String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            List<BoletimViewModel> pending = new ArrayList<>(boletins);
            boolean firstPage = true;

            while (!pending.isEmpty()) {
                BoletimViewModel firstBoletim = pending.remove(0);
                BoletimViewModel secondBoletim = null;

                for (int i = 0; i < pending.size(); i++) {
                    if (!java.util.Objects.equals(firstBoletim.getStudentPk(), pending.get(i).getStudentPk())) {
                        secondBoletim = pending.remove(i);
                        break;
                    }
                }

                if (!firstPage) {
                    document.newPage();
                }
                firstPage = false;

                PdfPTable row = new PdfPTable(3);
                row.setWidthPercentage(100);
                row.setWidths(new float[]{1, 0.08f, 1});
                row.setSplitRows(true);
                row.addCell(boletimCell(firstBoletim, today));
                row.addCell(gutterCell());
                row.addCell(secondBoletim != null
                        ? boletimCell(secondBoletim, today)
                        : emptyBoletimCell());
                document.add(row);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF dos boletins", e);
            return new byte[0];
        }
    }

    private PdfPCell boletimCell(BoletimViewModel boletim, String today) throws DocumentException {
        Color brandRed = new Color(200, 16, 46);
        Color brandGold = new Color(245, 180, 0);
        Color brandBlack = new Color(20, 20, 20);
        Color mutedGray = new Color(102, 106, 112);
        Color lineBlue = new Color(127, 168, 217);
        Color peach = new Color(252, 228, 196);
        Color lightBlue = new Color(220, 235, 247);
        Color zebraBlue = new Color(244, 248, 253);
        Color fieldGray = new Color(154, 160, 166);

        Font schoolFont = new Font(Font.HELVETICA, 11, Font.BOLD, brandBlack);
        Font schoolSub = new Font(Font.HELVETICA, 7, Font.NORMAL, mutedGray);
        Font sloganFont = new Font(Font.HELVETICA, 7.5f, Font.ITALIC, mutedGray);
        Font titleFont = new Font(Font.HELVETICA, 12, Font.BOLD, brandRed);
        Font labelFont = new Font(Font.HELVETICA, 7.5f, Font.BOLD, mutedGray);
        Font valueFont = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, brandBlack);
        Font discHeadFont = new Font(Font.HELVETICA, 8.5f, Font.BOLD, brandBlack);
        Font triHeadFont = new Font(Font.HELVETICA, 8.5f, Font.BOLD, brandBlack);
        Font colHeadFont = new Font(Font.HELVETICA, 7.5f, Font.BOLD, brandBlack);
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL, brandBlack);
        Font nameFont = new Font(Font.HELVETICA, 8, Font.BOLD, brandBlack);
        Font legendTitle = new Font(Font.HELVETICA, 7.5f, Font.BOLD, brandBlack);
        Font legendFont = new Font(Font.HELVETICA, 7, Font.NORMAL, brandBlack);
        Font summaryFont = new Font(Font.HELVETICA, 8, Font.BOLD, brandBlack);
        Font signFont = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, mutedGray);
        Font emptyFont = new Font(Font.HELVETICA, 8, Font.ITALIC, mutedGray);

        PdfPCell card = new PdfPCell();
        card.setBorderColor(new Color(220, 224, 230));
        card.setBorderWidth(0.8f);
        card.setPadding(8);
        card.setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable header = new PdfPTable(new float[]{0.55f, 2.4f, 1.3f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(2);

        PdfPCell logo = new PdfPCell(schoolLogo(), true);
        logo.setBorder(Rectangle.NO_BORDER);
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setPadding(2);
        header.addCell(logo);

        PdfPCell brand = new PdfPCell();
        brand.setBorder(Rectangle.NO_BORDER);
        brand.setPaddingLeft(8);
        brand.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph school = new Paragraph(SCHOOL_NAME, schoolFont);
        school.setSpacingAfter(2);
        brand.addElement(school);
        brand.addElement(new Paragraph(SCHOOL_SUB.toUpperCase(Locale.ROOT), schoolSub));
        header.addCell(brand);

        PdfPCell slogan = new PdfPCell();
        slogan.setBorder(Rectangle.NO_BORDER);
        slogan.setVerticalAlignment(Element.ALIGN_MIDDLE);
        slogan.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph sloganP = new Paragraph(SCHOOL_SLOGAN, sloganFont);
        sloganP.setAlignment(Element.ALIGN_RIGHT);
        slogan.addElement(sloganP);
        Paragraph documentLabel = new Paragraph("DOCUMENTO ESCOLAR", labelFont);
        documentLabel.setAlignment(Element.ALIGN_RIGHT);
        slogan.addElement(documentLabel);
        header.addCell(slogan);
        card.addElement(header);

        PdfPTable accentLine = new PdfPTable(new float[]{1, 5});
        accentLine.setWidthPercentage(100);
        accentLine.setSpacingAfter(5);
        PdfPCell redLine = new PdfPCell(new Phrase(""));
        redLine.setBorder(Rectangle.NO_BORDER);
        redLine.setBackgroundColor(brandRed);
        redLine.setFixedHeight(2.5f);
        accentLine.addCell(redLine);
        PdfPCell goldLine = new PdfPCell(new Phrase(""));
        goldLine.setBorder(Rectangle.NO_BORDER);
        goldLine.setBackgroundColor(brandGold);
        goldLine.setFixedHeight(2.5f);
        accentLine.addCell(goldLine);
        card.addElement(accentLine);

        Paragraph title = new Paragraph("BOLETIM DE NOTAS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        card.addElement(title);

        PdfPTable info = new PdfPTable(4);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1.4f, 1f, 1f, 1f});
        info.setSpacingAfter(7);

        PdfPCell nameCell = new PdfPCell();
        nameCell.setColspan(4);
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setBorderWidthBottom(0.8f);
        nameCell.setBorderColorBottom(fieldGray);
        nameCell.setPaddingBottom(4);
        Paragraph nameP = new Paragraph();
        nameP.add(new Chunk("Nome: ", labelFont));
        nameP.add(new Chunk(boletim.getStudentName() != null ? boletim.getStudentName() : "—", valueFont));
        nameCell.addElement(nameP);
        info.addCell(nameCell);
        info.addCell(infoField("Nº Matrícula: ", boletim.getEnrolmentNumber(), labelFont, valueFont));
        info.addCell(infoField("Turma: ", boletim.getSchoolClassName(), labelFont, valueFont));
        info.addCell(infoField("Trimestre: ", boletim.getTrimester() + "º", labelFont, valueFont));
        info.addCell(infoField("Data: ", today, labelFont, valueFont));
        card.addElement(info);

        if (boletim.isEmpty()) {
            Paragraph empty = new Paragraph("Nenhum resultado encontrado para este aluno neste trimestre.", emptyFont);
            empty.setAlignment(Element.ALIGN_CENTER);
            empty.setSpacingBefore(14);
            card.addElement(empty);
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
            discHead.setPadding(4);
            table.addCell(discHead);

            PdfPCell triHead = new PdfPCell(new Phrase(boletim.getTrimesterLabel(), triHeadFont));
            triHead.setColspan(3);
            triHead.setHorizontalAlignment(Element.ALIGN_CENTER);
            triHead.setBorderColor(lineBlue);
            triHead.setPadding(3);
            table.addCell(triHead);

            for (String heading : new String[]{"MAC", "NPT", "MT"}) {
                PdfPCell cell = new PdfPCell(new Phrase(heading, colHeadFont));
                cell.setBackgroundColor(lightBlue);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(lineBlue);
                cell.setPadding(3);
                table.addCell(cell);
            }

            boolean alternate = false;
            for (BoletimDisciplineRow discipline : boletim.getDisciplines()) {
                Color background = alternate ? zebraBlue : Color.WHITE;
                table.addCell(bodyCell(discipline.getDisciplineName(), nameFont, Element.ALIGN_LEFT, background, lineBlue));
                table.addCell(bodyCell(discipline.getMacFormatted(), cellFont, Element.ALIGN_CENTER, background, lineBlue));
                table.addCell(bodyCell(discipline.getNptFormatted(), cellFont, Element.ALIGN_CENTER, background, lineBlue));
                table.addCell(bodyCell(discipline.getMtFormatted(), cellFont, Element.ALIGN_CENTER, background, lineBlue));
                alternate = !alternate;
            }
            card.addElement(table);

            Paragraph legendHeading = new Paragraph("Observações", legendTitle);
            legendHeading.setSpacingBefore(6);
            legendHeading.setSpacingAfter(1);
            card.addElement(legendHeading);
            card.addElement(new Paragraph("MAC - Média da Avaliação Contínua (trabalhos de casa e sala de aula)", legendFont));
            card.addElement(new Paragraph("NPT - Nota da Prova do Trimestre", legendFont));
            card.addElement(new Paragraph("MT - Média Final do Trimestre", legendFont));

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(100);
            summary.setSpacingBefore(6);
            PdfPCell averageCell = new PdfPCell(new Phrase(
                    "Média Geral: " + boletim.getGeneralAverageFormatted(), summaryFont));
            averageCell.setBorder(Rectangle.NO_BORDER);
            summary.addCell(averageCell);
            PdfPCell situationCell = new PdfPCell(new Phrase(
                    "Situação: " + (boletim.getGeneralSituation() != null ? boletim.getGeneralSituation() : "—"),
                    summaryFont));
            situationCell.setBorder(Rectangle.NO_BORDER);
            situationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summary.addCell(situationCell);
            card.addElement(summary);
        }

        Paragraph sign = new Paragraph("\n________________________________\nO Director de Turma", signFont);
        sign.setAlignment(Element.ALIGN_CENTER);
        sign.setSpacingBefore(5);
        card.addElement(sign);
        return card;
    }

    private PdfPCell emptyBoletimCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell gutterCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private Image schoolLogo() throws DocumentException {
        String logoPath = "/META-INF/resources/resources/imgs/logo.jpg";
        try (InputStream logoStream = BoletimController.class.getResourceAsStream(logoPath)) {
            if (logoStream == null) {
                throw new IOException("Logotipo da escola não encontrado: " + logoPath);
            }
            Image logo = Image.getInstance(logoStream.readAllBytes());
            logo.scaleToFit(38, 38);
            logo.setAlignment(Image.ALIGN_CENTER);
            return logo;
        } catch (IOException e) {
            DocumentException exception = new DocumentException("Não foi possível carregar o logotipo da escola.");
            exception.initCause(e);
            throw exception;
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
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(severity, summary, detail));
        }
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

        private Long studentPk;
        private String studentName;
        private String enrolmentNumber;
        private String schoolClassName;
        private String trimester;
        private String trimesterLabel;
        private List<BoletimDisciplineRow> disciplines = new ArrayList<>();
        private Double generalAverage;
        private String generalSituation;

        public Long getStudentPk() { return studentPk; }
        public void setStudentPk(Long studentPk) { this.studentPk = studentPk; }

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