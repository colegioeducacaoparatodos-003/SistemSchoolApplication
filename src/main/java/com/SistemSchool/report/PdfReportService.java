package com.SistemSchool.report;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_Financeiro.dto.FinancialMovementDTO;
import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Geração centralizada de PDFs responsivos.
 * Formato de papel adaptado ao contexto:
 *   • A4 Landscape  → Listagens completas com filtros
 *   • A4 Portrait   → Relatórios por turma / Ficha do Aluno (matrícula)
 *   • A6            → Cartão de matrícula pocket
 */
public final class PdfReportService {

    private static final Logger LOGGER = Logger.getLogger(PdfReportService.class.getName());

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /* ── Paleta visual da view ── */
    private static final BaseColor BRAND_BLACK  = new BaseColor(20, 20, 20);      // #141414
    private static final BaseColor BRAND_RED    = new BaseColor(200, 16, 46);     // #c8102e
    private static final BaseColor BRAND_BLUE   = new BaseColor(26, 95, 180);     // #1a5fb4
    private static final BaseColor BRAND_YELLOW = new BaseColor(245, 180, 0);     // #f5b400
    private static final BaseColor ROW_ALT_BG   = new BaseColor(247, 247, 248);   // #f7f7f8
    private static final BaseColor BORDER_COLOR = new BaseColor(230, 230, 232);   // #e6e6e8

    /* ── Fontes padrão (outros relatórios) ── */
    private static final Font F_TITLE      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND_BLACK);
    private static final Font F_SUBTITLE   = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
    private static final Font F_FILTER     = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, BRAND_BLUE);
    private static final Font F_HEADER     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
    private static final Font F_CELL       = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font F_LABEL      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.DARK_GRAY);
    private static final Font F_VALUE      = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font F_FOOTER     = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
    private static final Font F_SECTION    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_RED);
    private static final Font F_FIELD_LBL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
    private static final Font F_FIELD_VAL  = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font F_SCHOOL     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font F_FICHA      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND_RED);
    private static final Font F_CHECK      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
    private static final Font F_PHOTO      = FontFactory.getFont(FontFactory.HELVETICA, 7, BaseColor.GRAY);
    private static final Font F_SIGN       = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

    /* A6 fontes reduzidas */
    private static final Font F6_TITLE  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK);
    private static final Font F6_SCHOOL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
    private static final Font F6_LABEL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.DARK_GRAY);
    private static final Font F6_VALUE  = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font F6_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY);

    /* ═══════════════════════════════════════════════════════════════
       FONTES A4 FICHA DE MATRÍCULA — Arial/Helvetica, mínimo 14
       ═══════════════════════════════════════════════════════════════ */
    private static final Font F_A4_SCHOOL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font F_A4_FICHA   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND_RED);
    private static final Font F_A4_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_RED);
    private static final Font F_A4_LBL     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
    private static final Font F_A4_VAL     = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
    private static final Font F_A4_SIGN    = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
    private static final Font F_A4_PHOTO   = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);

    private static final String LOGO_PATH = "/resources/imgs/logo.jpg";

    /**
     * Deve coincidir com a pasta real em webapp usada para guardar as fotos,
     * que em produção é "student_img". Mantém também o caminho legado
     * "alunosFotos" como fallback para ficheiros antigos.
     */
    private static final String[] STUDENT_PHOTO_BASE_PATHS = { "/student_img/", "/alunosFotos/" };

    /**
     * Todos os relatórios PDF do sistema devem ser gerados em orientação
     * retrato para manter consistência visual e legibilidade em impressões.
     */

    private PdfReportService() {}

    /* ═══════════════════════════════════════════════════════════════
       STREAMING  →  Browser
       ═══════════════════════════════════════════════════════════════ */
    public static void streamToResponse(byte[] pdfBytes, String fileName) throws IOException {
        streamToResponse(pdfBytes, fileName, false);
    }

    public static void streamToResponse(byte[] pdfBytes, String fileName, boolean inline) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        ec.responseReset();
        ec.setResponseContentType("application/pdf");
        ec.setResponseContentLength(pdfBytes.length);
        String disp = inline ? "inline" : "attachment";
        ec.setResponseHeader("Content-Disposition", disp + "; filename=\"" + fileName + "\"");
        try (OutputStream out = ec.getResponseOutputStream()) {
            out.write(pdfBytes);
            out.flush();
        }
        fc.responseComplete();
    }

    /* ═══════════════════════════════════════════════════════════════
       A4 PORTRAIT  →  Lista completa filtrada
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentListReport(List<EnrolmentDTO> list) throws DocumentException {
        return generateEnrolmentListReport(list, null);
    }

    public static byte[] generateEnrolmentListReport(List<EnrolmentDTO> list,
                                                     String filterDescription) throws DocumentException {
        Document doc = new Document(PageSize.A4, 24, 24, 30, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new PdfAuditPageEvent());
        doc.open();

        addEnrolmentListHeader(doc);

        if (filterDescription != null && !filterDescription.isBlank()) {
            Paragraph f = new Paragraph("Filtros activos: " + filterDescription, F_FILTER);
            f.setAlignment(Element.ALIGN_CENTER);
            f.setSpacingAfter(4);
            doc.add(f);
        }

        Paragraph generated = new Paragraph("Emitido em: " + LocalDateTime.now().format(DATETIME_FMT), F_SUBTITLE);
        generated.setAlignment(Element.ALIGN_RIGHT);
        generated.setSpacingAfter(10);
        doc.add(generated);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.55f, 1.45f, 3.0f, 1.35f, 1.65f, 1.3f, 1.3f, 1.25f });
        table.setHeaderRows(1);
        table.setSplitLate(false);

        String[] headers = { "Nº", "Nº Matrícula", "Aluno", "Nº Aluno", "Turma", "Turno", "Tipo", "Data" };
        for (String h : headers) table.addCell(createHeaderCell(h));

        boolean alt = false;
        int rowNumber = 1;
        for (EnrolmentDTO e : list) {
            BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
            alt = !alt;
            addCell(table, String.valueOf(rowNumber++), bg, Element.ALIGN_CENTER);
            addCell(table, fmt(e.getEnrolmentNumber()), bg);
            addCell(table, fmt(e.getStudentFullName()), bg);
            addCell(table, fmt(e.getStudentNumber()), bg);
            addCell(table, formatClass(e), bg);
            addCell(table, fmt(e.getShift()), bg);
            addCell(table, fmt(e.getEnrolmentType()), bg);
            addCell(table, fmt(e.getEnrolmentData()), bg);
        }
        doc.add(table);

        Paragraph resumo = new Paragraph("Total de matrículas: " + list.size(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        resumo.setAlignment(Element.ALIGN_RIGHT);
        resumo.setSpacingBefore(10);
        doc.add(resumo);

        addFooter(doc, writer);
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generateFinancialMovementListReport(List<FinancialMovementDTO> list)
            throws DocumentException {
        Document doc = new Document(PageSize.A4, 24, 24, 82, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent(
                "Lista de Movimentos Financeiros", "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", tryLoadLogo()));
        doc.open();

        Paragraph summary = new Paragraph("Movimentos exportados: " + list.size(), F_SUBTITLE);
        summary.setAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingAfter(10);
        doc.add(summary);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.4f, 1.3f, 1.4f, 2.8f, 1.3f, 1.2f, 1.4f, 1.3f });
        table.setHeaderRows(1);

        String[] headers = { "Nº Movimento", "Caixa", "Pagamento", "Descrição", "Valor", "Tipo", "Estado", "Data" };
        for (String header : headers) table.addCell(createHeaderCell(header));

        boolean alternate = false;
        for (FinancialMovementDTO movement : list) {
            BaseColor background = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addCell(table, fmt(movement.getMovementNumber()), background);
            addCell(table, fmt(movement.getCashBoxNumber()), background);
            addCell(table, fmt(movement.getPagamentoNumeroDocumento()), background);
            addCell(table, fmt(movement.getDescription()), background);
            addCell(table, fmt(movement.getAmount()), background);
            addCell(table, fmt(movement.getType()), background);
            addCell(table, fmt(movement.getStatus()), background);
            addCell(table, fmt(movement.getMovementDate()), background);
        }
        doc.add(table);

        Paragraph resumo = new Paragraph("Total de registos: " + list.size(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        resumo.setSpacingBefore(10);
        doc.add(resumo);

        addFooter(doc, writer);
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generateEvaluationListReport(List<EvaluationDTO> list)
            throws DocumentException {
        Document doc = new Document(PageSize.A4, 30, 30, 55, 45);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent(
            "Lista de Avaliações", "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", tryLoadLogo()));
        doc.open();

        addBrandTitle(doc, "LISTA DE AVALIACOES");

        Paragraph generated = new Paragraph("Gerado em: " + LocalDateTime.now().format(DATETIME_FMT), F_SUBTITLE);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(12);
        doc.add(generated);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2.2f, 2.5f, 1.5f, 1.4f, 1.4f, 1.3f });
        table.setHeaderRows(1);

        String[] headers = { "Disciplina", "Avaliacao", "Tipo", "Trimestre", "Data", "Ano letivo" };
        for (String header : headers) table.addCell(createHeaderCell(header));

        boolean alternate = false;
        for (EvaluationDTO evaluation : list) {
            BaseColor background = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addCell(table, fmt(evaluation.getDisciplineName()), background);
            addCell(table, fmt(evaluation.getEvaluationName()), background);
            addCell(table, fmt(evaluation.getEvaluationType()), background);
            addCell(table, fmt(evaluation.getTrimester()), background);
            addCell(table, fmt(evaluation.getEvaluationDate()), background);
            addCell(table, fmt(evaluation.getAnoLectivo()), background);
        }
        doc.add(table);

        Paragraph summary = new Paragraph("Total de avaliacoes: " + list.size(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        summary.setSpacingBefore(10);
        summary.setAlignment(Element.ALIGN_RIGHT);
        doc.add(summary);

        addFooter(doc, writer);
        doc.close();
        return baos.toByteArray();
    }

    /* ═══════════════════════════════════════════════════════════════
       A4 PORTRAIT  →  Relatório por Turma
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentsByClassReport(SchoolClass schoolClass,
                                                         List<EnrolmentDTO> list) throws DocumentException {
        Document doc = new Document(PageSize.A4, 24, 24, 30, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new PdfAuditPageEvent());
        doc.open();

        addClassReportHeader(doc);

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[] { 1, 3 });
        info.setSpacingAfter(12);
        info.setSplitLate(false);

        addInfoSectionHeader(info, "IDENTIFICAÇÃO DA TURMA");
        addInfoRow(info, "Turma", schoolClass.getClassCode() + " - " + schoolClass.getClassName());
        addInfoRow(info, "Classe", schoolClass.getClasse());
        addInfoRow(info, "Sala", schoolClass.getRoom());
        addInfoRow(info, "Turno", schoolClass.getTurno());
        addInfoRow(info, "Ano Lectivo", schoolClass.getAnoLectivo());
        addInfoRow(info, "Capacidade", String.valueOf(schoolClass.getCapacidade()));
        addInfoRow(info, "Matriculados", String.valueOf(list.size()));
        doc.add(info);

        if (list.isEmpty()) {
            Paragraph empty = new Paragraph("Nenhuma matrícula encontrada para esta turma.", F_SUBTITLE);
            empty.setAlignment(Element.ALIGN_CENTER);
            doc.add(empty);
        } else {
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 0.55f, 1.4f, 3.5f, 1.6f, 1.6f, 1.4f });
            table.setHeaderRows(1);
            table.setSplitLate(false);

            String[] headers = { "Nº", "Nº Matrícula", "Aluno", "Nº Aluno", "Tipo", "Data" };
            for (String h : headers) table.addCell(createHeaderCell(h));

            boolean alt = false;
            int rowNumber = 1;
            for (EnrolmentDTO e : list) {
                BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
                alt = !alt;
                addCell(table, String.valueOf(rowNumber++), bg, Element.ALIGN_CENTER);
                addCell(table, fmt(e.getEnrolmentNumber()), bg);
                addCell(table, fmt(e.getStudentFullName()), bg);
                addCell(table, fmt(e.getStudentNumber()), bg);
                addCell(table, fmt(e.getEnrolmentType()), bg);
                addCell(table, fmt(e.getEnrolmentData()), bg);
            }
            doc.add(table);
        }

            Paragraph summary = new Paragraph(
                "Total de alunos: " + list.size() + "   |   Documento emitido em: "
                    + LocalDateTime.now().format(DATETIME_FMT), F_FOOTER);
            summary.setAlignment(Element.ALIGN_RIGHT);
            summary.setSpacingBefore(8);
            doc.add(summary);

        addFooter(doc, writer);
        doc.close();
        return baos.toByteArray();
    }

    /* ═══════════════════════════════════════════════════════════════
       A4 PORTRAIT  →  Ficha Individual de Matrícula (única página)
       Fonte Arial/Helvetica, tamanho mínimo 14 — INCLUI FOTO DO ALUNO
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentReport(EnrolmentDTO enrolment, StudentDTO student)
            throws DocumentException {
        // Margens mínimas e layout compacto para garantir 1 página A4
        Document doc = new Document(PageSize.A4, 18, 18, 10, 10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new PdfAuditPageEvent());

        doc.open();
        doc.add(buildFichaHeaderA4(student));
        doc.add(spacerSmallA4());
        doc.add(buildTopInfoTableA4(enrolment));
        doc.add(spacerSmallA4());

        addSectionTitleA4(doc, "DADOS PESSOAIS");
        doc.add(buildFichaPersonalDataTableA4(student));
        doc.add(spacerSmallA4());

        doc.add(buildFiliacaoTableA4(student));
        doc.add(spacerSmallA4());

        addSectionTitleA4(doc, "OBSERVAÇÃO");
        doc.add(buildObservacaoSectionA4(enrolment));
        doc.add(spacerSmallA4());
        doc.add(buildSignatureTableA4());

        doc.close();
        return baos.toByteArray();
    }

    /* ═══════════════════════════════════════════════════════════════
       A6  →  Cartão de Matrícula Pocket (responsivo, compacto)
       INCLUI FOTO DO ALUNO (com fallback para placeholder "FOTO 3x4")
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentCardA6(EnrolmentDTO enrolment, StudentDTO student)
            throws DocumentException {
        float w = 105f * 2.8346f;
        float h = 148f * 2.8346f;
        Document doc = new Document(new Rectangle(0, 0, w, h), 10, 10, 10, 10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new PdfAuditPageEvent());
        doc.open();

        Paragraph school = new Paragraph("ESCOLA EDUCAÇÃO PARA TODOS", F6_SCHOOL);
        school.setAlignment(Element.ALIGN_CENTER);
        doc.add(school);

        Paragraph title = new Paragraph("CARTÃO DE MATRÍCULA", F6_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6);
        doc.add(title);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(60);
        PdfPCell lc = new PdfPCell();
        lc.setBorder(Rectangle.TOP);
        lc.setBorderColor(BRAND_RED);
        lc.setBorderWidth(1.5f);
        line.addCell(lc);
        doc.add(line);

        PdfPTable main = new PdfPTable(2);
        main.setWidthPercentage(100);
        main.setWidths(new float[] { 1, 2 });
        main.setSpacingBefore(8);

        // ── Foto do aluno (ajustada para um visual mais limpo e proporcional) ──
        Image cardPhoto = tryLoadStudentPhoto(student.getUploadPhoto());
        PdfPCell photoCell;
        if (cardPhoto != null) {
            cardPhoto.setAlignment(Element.ALIGN_CENTER);
            cardPhoto.scaleToFit(46f, 56f);
            photoCell = new PdfPCell(cardPhoto, true);
        } else {
            photoCell = new PdfPCell(new Phrase("FOTO\n3x4", F_PHOTO));
        }
        photoCell.setBorder(Rectangle.BOX);
        photoCell.setBorderColor(BRAND_BLACK);
        photoCell.setPadding(4f);
        photoCell.setFixedHeight(54f);
        photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        main.addCell(photoCell);

        PdfPCell dataCell = new PdfPCell();
        dataCell.setBorder(Rectangle.NO_BORDER);
        dataCell.setPaddingLeft(6);

        Paragraph pNum = new Paragraph();
        pNum.add(new Chunk("Nº: ", F6_LABEL));
        pNum.add(new Chunk(fmt(enrolment.getEnrolmentNumber()), F6_VALUE));
        dataCell.addElement(pNum);

        Paragraph pClass = new Paragraph();
        pClass.add(new Chunk("Turma: ", F6_LABEL));
        pClass.add(new Chunk(fmt(enrolment.getSchoolClassCode()), F6_VALUE));
        dataCell.addElement(pClass);

        Paragraph pShift = new Paragraph();
        pShift.add(new Chunk("Turno: ", F6_LABEL));
        pShift.add(new Chunk(fmt(enrolment.getShift()), F6_VALUE));
        dataCell.addElement(pShift);

        main.addCell(dataCell);
        doc.add(main);

        Paragraph name = new Paragraph(fmt(student.getFullName()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingBefore(6);
        name.setSpacingAfter(2);
        doc.add(name);

        Paragraph bi = new Paragraph("BI: " + fmt(student.getBiNumber()), F6_LABEL);
        bi.setAlignment(Element.ALIGN_CENTER);
        doc.add(bi);

        Paragraph footer = new Paragraph("Ano Lectivo " + currentAcademicYear(), F6_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(8f);
        doc.add(footer);

        Paragraph valid = new Paragraph("Válido mediante apresentação de BI", F6_FOOTER);
        valid.setAlignment(Element.ALIGN_CENTER);
        doc.add(valid);

        doc.close();
        return baos.toByteArray();
    }

    /* ═══════════════════════════════════════════════════════════════
       OUTROS RELATÓRIOS (mantidos para compatibilidade)
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateStudentReport(StudentDTO student) throws DocumentException {
        Document doc = new Document(PageSize.A4, 40, 40, 90, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent("Ficha do Aluno"));
        doc.open();
        addSectionTitle(doc, "Dados do Aluno");
        doc.add(buildStudentTable(student));
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generateStudentListReport(List<StudentDTO> students) throws DocumentException {
        Document doc = new Document(PageSize.A4, 30, 30, 90, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent("Lista de Alunos"));
        doc.open();
        addSectionTitle(doc, "Lista de Alunos (" + students.size() + ")");
        doc.add(buildStudentListTable(students));
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generateDisciplineListReport(List<DisciplineDTO> disciplines) throws DocumentException {
        Document doc = new Document(PageSize.A4, 30, 30, 55, 45);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent(
            "Lista de Disciplinas", "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", tryLoadLogo()));
        doc.open();
        addBrandTitle(doc, "LISTA DE DISCIPLINAS");

        Paragraph generated = new Paragraph("Gerado em: " + LocalDateTime.now().format(DATETIME_FMT), F_SUBTITLE);
        generated.setAlignment(Element.ALIGN_RIGHT);
        generated.setSpacingAfter(10);
        doc.add(generated);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.65f, 1.4f, 2.4f, 4.2f, 1.2f, 1.8f });
        table.setHeaderRows(1);
        String[] headers = { "Nº", "Código", "Disciplina", "Descrição", "Estado", "Criada em" };
        for (String header : headers) table.addCell(createHeaderCell(header));

        boolean alternate = false;
        int rowNumber = 1;
        for (DisciplineDTO discipline : disciplines) {
            BaseColor background = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addCell(table, String.valueOf(rowNumber++), background, Element.ALIGN_CENTER);
            addCell(table, discipline.getDisciplineCode(), background);
            addCell(table, discipline.getDisciplineName(), background);
            addCell(table, discipline.getDescription(), background);
            addCell(table, fmt(discipline.getStatus()), background, Element.ALIGN_CENTER);
            addCell(table, discipline.getCreatedAt() == null
                    ? null : discipline.getCreatedAt().format(DATETIME_FMT), background);
        }
        doc.add(table);

        Paragraph total = new Paragraph("Total de disciplinas: " + disciplines.size(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        doc.add(total);
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generateScheduleListReport(List<ScheduleDTO> schedules) throws DocumentException {
        Document doc = new Document(PageSize.A4, 24, 24, 30, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent(
                "Lista de Horários", "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", tryLoadLogo()));
        doc.open();

        addBrandTitle(doc, "LISTA DE HORÁRIOS");

        Paragraph generated = new Paragraph("Gerado em: " + LocalDateTime.now().format(DATETIME_FMT), F_SUBTITLE);
        generated.setAlignment(Element.ALIGN_RIGHT);
        generated.setSpacingAfter(10);
        doc.add(generated);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.6f, 1.8f, 1.8f, 2.0f, 1.2f, 1.2f, 1.2f, 1.2f });
        table.setHeaderRows(1);
        table.setSplitLate(false);

        String[] headers = { "Nº", "Disciplina", "Turma", "Professor", "Dia", "Início", "Fim", "Ano" };
        for (String header : headers) table.addCell(createHeaderCell(header));

        boolean alternate = false;
        int rowNumber = 1;
        for (ScheduleDTO schedule : schedules) {
            BaseColor background = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addCell(table, String.valueOf(rowNumber++), background, Element.ALIGN_CENTER);
            addCell(table, fmt(schedule.getDisciplineName()), background);
            addCell(table, formatScheduleClass(schedule), background);
            addCell(table, fmt(schedule.getTeacherName() != null ? schedule.getTeacherName() : "Não atribuído"), background);
            addCell(table, fmt(schedule.getWeekDay()), background, Element.ALIGN_CENTER);
            addCell(table, fmt(schedule.getStartTime()), background, Element.ALIGN_CENTER);
            addCell(table, fmt(schedule.getEndTime()), background, Element.ALIGN_CENTER);
            addCell(table, fmt(schedule.getAnoLectivo()), background, Element.ALIGN_CENTER);
        }
        doc.add(table);

        Paragraph total = new Paragraph("Total de horários: " + schedules.size(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        doc.add(total);

        addFooter(doc, writer);
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generateGradeListReport(List<GradeDTO> grades) throws DocumentException {
        Document doc = new Document(PageSize.A4, 30, 30, 82, 42);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent(
                "Lista de Notas", "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", tryLoadLogo()));
        doc.open();

        addBrandTitle(doc, "LISTA DE NOTAS");

        Paragraph summary = new Paragraph(
                "Total de registos: " + grades.size(), F_SUBTITLE);
        summary.setAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingAfter(10);
        doc.add(summary);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.55f, 2.3f, 1.35f, 2.1f, 1.35f, 1.05f });
        table.setHeaderRows(1);
        table.setSplitLate(false);

        String[] headers = { "Nº", "Aluno / Matrícula", "Turma", "Disciplina / Avaliação", "Trimestre / Tipo", "Nota" };
        for (String header : headers) {
            table.addCell(createHeaderCell(header));
        }

        boolean alternate = false;
        int rowNumber = 1;
        double scoreTotal = 0;
        int scoreCount = 0;
        for (GradeDTO grade : grades) {
            BaseColor background = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;

            addCell(table, String.valueOf(rowNumber++), background, Element.ALIGN_CENTER);
            table.addCell(createGradeDetailCell(grade.getStudentFullName(), grade.getStudentNumber(), background));
            addCell(table, fmt(grade.getSchoolClassName()), background);
            table.addCell(createGradeDetailCell(grade.getDisciplineName(), grade.getEvaluationName(), background));
            table.addCell(createGradeDetailCell(
                    grade.getTrimester() != null ? grade.getTrimester().getDescricao() : null,
                    grade.getEvaluationType() != null ? grade.getEvaluationType().getDescricao() : null,
                    background));
            table.addCell(createGradeDetailCell(
                    grade.getScore() != null ? String.format(java.util.Locale.forLanguageTag("pt"),
                            "%.1f", grade.getScore()).replace('.', ',') : null,
                    grade.getLaunchDate() != null ? grade.getLaunchDate().format(DATE_FMT) : null,
                    background, true));

            if (grade.getScore() != null) {
                scoreTotal += grade.getScore();
                scoreCount++;
            }
        }
        doc.add(table);

        String average = scoreCount == 0 ? "-" : String.format(java.util.Locale.forLanguageTag("pt"),
                "%.1f", scoreTotal / scoreCount).replace('.', ',');
        Paragraph total = new Paragraph(
                "Média das notas: " + average + "    |    Total: " + grades.size(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_BLACK));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        doc.add(total);

        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generatePagamentoListReport(List<PagamentoDTO> pagamentos) throws DocumentException {
        Document doc = new Document(PageSize.A4, 30, 30, 90, 75);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent(
                "Lista de Pagamentos", "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", tryLoadLogo(), 58, 48, 30));
        doc.open();
        addSectionTitle(doc, "Lista de Pagamentos (" + pagamentos.size() + ")");
        doc.add(buildPagamentoSummary(pagamentos));
        doc.add(buildPagamentoListTable(pagamentos));
        doc.close();
        return baos.toByteArray();
    }

    private static String formatScheduleClass(ScheduleDTO schedule) {
        String code = fmt(schedule.getSchoolClassCode());
        String name = fmt(schedule.getSchoolClassName());
        if ("-".equals(code)) return name;
        if ("-".equals(name) || code.equals(name)) return code;
        return code + " - " + name;
    }

    /* ═══════════════════════════════════════════════════════════════
       HELPERS  →  Layout comum
       ═══════════════════════════════════════════════════════════════ */

    private static void addBrandTitle(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, F_TITLE);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(3);
        doc.add(p);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(35);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.TOP);
        c.setBorderColor(BRAND_RED);
        c.setBorderWidth(2.2f);
        c.setPaddingTop(3);
        line.addCell(c);
        doc.add(line);
        doc.add(Chunk.NEWLINE);
    }

    private static PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_HEADER));
        cell.setBackgroundColor(BRAND_BLACK);
        cell.setPadding(7);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static void addCell(PdfPTable table, String text, BaseColor bg) {
        addCell(table, text, bg, Element.ALIGN_LEFT);
    }

    private static void addCell(PdfPTable table, String text, BaseColor bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", F_CELL));
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private static PdfPCell createGradeDetailCell(String primary, String secondary, BaseColor background) {
        return createGradeDetailCell(primary, secondary, background, false);
    }

    private static PdfPCell createGradeDetailCell(String primary, String secondary, BaseColor background,
            boolean emphasizePrimary) {
        Font primaryFont = emphasizePrimary
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND_BLACK)
                : F_CELL;
        Font secondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 7, BaseColor.GRAY);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(5);
        cell.setBackgroundColor(background);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.addElement(new Paragraph(fmt(primary), primaryFont));

        String detail = fmt(secondary);
        if (!"-".equals(detail)) {
            Paragraph secondaryParagraph = new Paragraph(detail, secondaryFont);
            secondaryParagraph.setSpacingBefore(2);
            cell.addElement(secondaryParagraph);
        }
        return cell;
    }

    private static String formatClass(EnrolmentDTO enrolment) {
        String code = fmt(enrolment.getSchoolClassCode());
        String name = fmt(enrolment.getSchoolClassName());
        if ("-".equals(code)) return name;
        if ("-".equals(name) || code.equals(name)) return code;
        return code + " - " + name;
    }

    private static void addEnrolmentListHeader(Document doc) throws DocumentException {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[] { 1.1f, 5.4f, 1.5f });
        header.setSpacingAfter(10);

        Image logo = tryLoadLogo();
        PdfPCell logoCell = logo == null ? new PdfPCell(new Phrase("", F_CELL)) : new PdfPCell(logo, true);
        if (logo != null) logo.scaleToFit(54f, 54f);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph school = new Paragraph("COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS", F_SCHOOL);
        school.setAlignment(Element.ALIGN_CENTER);
        Paragraph title = new Paragraph("LISTA DE MATRÍCULAS", F_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(4);
        titleCell.addElement(school);
        titleCell.addElement(title);
        header.addCell(titleCell);

        PdfPCell dateCell = new PdfPCell(new Phrase(
                LocalDateTime.now().format(DATETIME_FMT), F_FOOTER));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(dateCell);

        doc.add(header);
        LineSeparator separator = new LineSeparator(2f, 100f, BRAND_RED, Element.ALIGN_LEFT, -1);
        doc.add(new Chunk(separator));
        doc.add(Chunk.NEWLINE);
    }

    private static void addClassReportHeader(Document doc) throws DocumentException {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[] { 1.1f, 5.4f, 1.5f });
        header.setSpacingAfter(10);

        Image logo = tryLoadLogo();
        PdfPCell logoCell = logo == null ? new PdfPCell(new Phrase("", F_CELL)) : new PdfPCell(logo, true);
        if (logo != null) logo.scaleToFit(54f, 54f);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph school = new Paragraph("COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS", F_SCHOOL);
        school.setAlignment(Element.ALIGN_CENTER);
        Paragraph title = new Paragraph("LISTA DE ALUNOS POR TURMA", F_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(4);
        titleCell.addElement(school);
        titleCell.addElement(title);
        header.addCell(titleCell);

        PdfPCell dateCell = new PdfPCell(new Phrase(
                LocalDateTime.now().format(DATETIME_FMT), F_FOOTER));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(dateCell);

        doc.add(header);
        LineSeparator separator = new LineSeparator(2f, 100f, BRAND_RED, Element.ALIGN_LEFT, -1);
        doc.add(new Chunk(separator));
        doc.add(Chunk.NEWLINE);
    }

    private static void addInfoSectionHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_HEADER));
        cell.setColspan(2);
        cell.setBackgroundColor(BRAND_BLUE);
        cell.setBorderColor(BRAND_BLUE);
        cell.setPadding(7);
        table.addCell(cell);
    }

    private static void addInfoRow(PdfPTable table, String label, Object value) {
        PdfPCell l = new PdfPCell(new Phrase(label + ":", F_LABEL));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(4);
        PdfPCell v = new PdfPCell(new Phrase(fmt(value), F_VALUE));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPadding(4);
        table.addCell(l);
        table.addCell(v);
    }

    private static void addFooter(Document doc, PdfWriter writer) throws DocumentException {
        int page = writer.getPageNumber();
        Paragraph f = new Paragraph(
                String.format("Página %d  |  SistemSchool  |  %s", page, LocalDateTime.now().format(DATETIME_FMT)),
                F_FOOTER);
        f.setAlignment(Element.ALIGN_CENTER);
        f.setSpacingBefore(14);
        doc.add(f);
    }

    private static void addSectionTitle(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, F_SECTION);
        p.setSpacingBefore(4);
        p.setSpacingAfter(4);
        doc.add(p);
        LineSeparator sep = new LineSeparator(1f, 100f, BORDER_COLOR, Element.ALIGN_LEFT, -2);
        doc.add(new Chunk(sep));
        doc.add(Chunk.NEWLINE);
    }

    private static Paragraph spacer() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(10);
        return p;
    }

    private static Paragraph spacerSmall() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(4);
        return p;
    }

    /**
     * Formata valores para exibição. Para enums, tenta chamar getDescricao()
     * antes de cair no name(). Funciona para ShiftType e EnrolmentType.
     */
    private static String fmt(Object value) {
        if (value == null) return "-";
        if (value instanceof LocalDate) return ((LocalDate) value).format(DATE_FMT);
        if (value instanceof LocalDateTime) return ((LocalDateTime) value).format(DATETIME_FMT);
        if (value instanceof Enum) {
            Enum<?> e = (Enum<?>) value;
            try {
                Method m = e.getDeclaringClass().getMethod("getDescricao");
                Object result = m.invoke(value);
                if (result != null) {
                    String s = result.toString().trim();
                    return s.isEmpty() ? "-" : s;
                }
            } catch (Exception ignored) {
                // não possui getDescricao(), cai no name()
            }
            return e.name();
        }
        String s = value.toString().trim();
        return s.isEmpty() ? "-" : s;
    }

    /* ═══════════════════════════════════════════════════════════════
       FICHA DO(A) ALUNO(A) A4  →  blocos do formulário físico
       Fonte Arial/Helvetica, tamanho mínimo 14, uma única página
       ═══════════════════════════════════════════════════════════════ */

    private static PdfPTable buildFichaHeaderA4(StudentDTO student) {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        try { header.setWidths(new float[] { 18f, 52f, 30f }); } catch (DocumentException ignored) {}
        header.setSpacingAfter(4f);

        // ── Logo ──
        Image logo = tryLoadLogo();
        PdfPCell logoCell;
        if (logo != null) {
            logo.scaleToFit(60f, 60f);
            logoCell = new PdfPCell(logo, false);
        } else {
            logoCell = new PdfPCell(new Phrase(""));
        }
        logoCell.setBorder(Rectangle.BOX);
        logoCell.setBorderColor(BORDER_COLOR);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPadding(6f);
        logoCell.setBackgroundColor(new BaseColor(246, 248, 250));
        header.addCell(logoCell);

        // ── Títulos ──
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.BOX);
        titleCell.setBorderColor(BORDER_COLOR);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPadding(8f);
        titleCell.setBackgroundColor(new BaseColor(250, 250, 251));

        Paragraph schoolName = new Paragraph("COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS", F_A4_SCHOOL);
        schoolName.setAlignment(Element.ALIGN_CENTER);
        Paragraph fichaTitle = new Paragraph("FICHA DO(A) ALUNO(A)", F_A4_FICHA);
        fichaTitle.setAlignment(Element.ALIGN_CENTER);
        fichaTitle.setSpacingBefore(4f);

        Paragraph subTitle = new Paragraph("Sistema de Gestão Escolar", F_SUBTITLE);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingBefore(2f);

        titleCell.addElement(schoolName);
        titleCell.addElement(fichaTitle);
        titleCell.addElement(subTitle);
        header.addCell(titleCell);

        // ── Foto do aluno ──
        Image studentPhoto = tryLoadStudentPhoto(student.getUploadPhoto());
        PdfPCell photoCell;

        if (studentPhoto != null) {
            studentPhoto.setAlignment(Element.ALIGN_CENTER);
            studentPhoto.scaleToFit(76f, 90f);
            photoCell = new PdfPCell(studentPhoto, true);
        } else {
            photoCell = new PdfPCell(new Phrase("FOTO\n3x4", F_A4_PHOTO));
        }

        photoCell.setBorder(Rectangle.BOX);
        photoCell.setBorderColor(BRAND_BLACK);
        photoCell.setBackgroundColor(new BaseColor(245, 247, 249));
        photoCell.setPadding(6f);
        photoCell.setFixedHeight(80f);
        photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(photoCell);

        return header;
    }

    private static Image tryLoadLogo() {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc == null) return null;
            String realPath = fc.getExternalContext().getRealPath(LOGO_PATH);
            if (realPath == null) return null;
            File f = new File(realPath);
            return f.exists() ? Image.getInstance(realPath) : null;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Não foi possível carregar o logo da escola", ex);
            return null;
        }
    }

    /**
     * Tenta carregar a foto do aluno a partir da pasta "alunosFotos"
     * (a mesma usada por {@code StudentService} para gravar o upload).
     * Retorna null se não encontrar, se o FacesContext não estiver
     * disponível, ou se o aluno não tiver foto associada — nesses casos o
     * caller deve mostrar o placeholder "FOTO 3x4".
     */
    public static String resolveStudentPhotoPath(String photoFileName) {
        if (photoFileName == null || photoFileName.trim().isEmpty()) {
            return null;
        }
        String normalized = photoFileName.trim();

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            for (String basePath : STUDENT_PHOTO_BASE_PATHS) {
                String candidate = basePath + normalized;
                String realPath = fc.getExternalContext().getRealPath(candidate);
                if (realPath != null) {
                    File file = new File(realPath);
                    if (file.exists()) {
                        return candidate;
                    }
                }
            }
        }

        return "/student_img/" + normalized;
    }

    private static Image tryLoadStudentPhoto(String photoFileName) {
        if (photoFileName == null || photoFileName.trim().isEmpty()) {
            return null;
        }
        try {
            String photoPath = resolveStudentPhotoPath(photoFileName);
            if (photoPath == null) {
                return null;
            }

            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc == null) {
                return null;
            }

            String realPath = fc.getExternalContext().getRealPath(photoPath);
            if (realPath == null) {
                return null;
            }

            File f = new File(realPath);
            if (!f.exists()) {
                LOGGER.log(Level.WARNING, "Foto do aluno não encontrada em: {0}", realPath);
                return null;
            }

            return Image.getInstance(realPath);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Erro ao carregar a foto do aluno (" + photoFileName + ")", ex);
            return null;
        }
    }

    /** Cabeçalho de informações — sem checkboxes de Matrícula/Confirmação */
    private static PdfPTable buildTopInfoTableA4(EnrolmentDTO e) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[] { 50f, 50f }); } catch (DocumentException ignored) {}
        table.setSpacingBefore(2f);
        table.setSpacingAfter(2f);
        table.setSplitRows(false);
        table.setKeepTogether(true);

        PdfPCell sectionHeader = new PdfPCell(new Phrase("DADOS DA MATRÍCULA", F_A4_SECTION));
        sectionHeader.setColspan(2);
        sectionHeader.setBorder(Rectangle.NO_BORDER);
        sectionHeader.setPaddingTop(6f);
        sectionHeader.setPaddingBottom(4f);
        sectionHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(sectionHeader);

        addPlainFieldCellA4(table, "CLASSE", e.getSchoolClassName());
        addPlainFieldCellA4(table, "Nº DA FICHA", e.getEnrolmentNumber());
        addPlainFieldCellA4(table, "PERÍODO", e.getShift());
        addPlainFieldCellA4(table, "ANO LECTIVO", currentAcademicYear());

        return table;
    }

    /** Dados pessoais conforme layout do formulário físico */
    private static PdfPTable buildFichaPersonalDataTableA4(StudentDTO s) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[] { 50f, 50f }); } catch (DocumentException ignored) {}
        table.setSpacingAfter(3f);
        table.setSplitRows(false);
        table.setKeepTogether(true);

        // Nome ocupa linha completa
        addFullLineCellA4(table, "NOME", s.getFullName(), 2);

        // Nº ID e Data Emissão lado a lado
        addPlainFieldCellA4(table, "Nº DE IDENTIFICAÇÃO", s.getBiNumber());
        addPlainFieldCellA4(table, "DATA EMISSÃO", LocalDate.now());

        // Data Nascimento e Gênero lado a lado
        addPlainFieldCellA4(table, "DATA DE NASCIMENTO", s.getNascDate());
        addPlainFieldCellA4(table, "GÉNERO", s.getGender());

        // Natural e Morada em linhas completas
        addFullLineCellA4(table, "NATURAL", s.getAddressProvice(), 2);
        addFullLineCellA4(table, "MORADA", s.getAddressStreet(), 2);

        return table;
    }

    private static PdfPTable buildFiliacaoTableA4(StudentDTO s) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(3f);
        table.setSplitRows(false);
        table.setKeepTogether(true);

        addFullLineCellA4(table, "NOME DO PAI", s.getNameFather(), 1);
        addFullLineCellA4(table, "NOME DA MÃE", s.getNameMather(), 1);

        String contacto = (s.getPhone_1() != null && !s.getPhone_1().trim().isEmpty())
                ? s.getPhone_1() : s.getPhone_2();
        addFullLineCellA4(table, "CONTACTO DO ENCARREGADO", contacto, 1);
        return table;
    }

    /** Observação livre — exibe o campo obs da entidade Enrolment.
     *  Remove as perguntas de saúde/deficiência/alergia. */
    private static PdfPTable buildObservacaoSectionA4(EnrolmentDTO e) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSplitRows(false);
        table.setKeepTogether(true);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(6f);
        cell.setMinimumHeight(26f);

        String obsText = (e.getObs() != null && !e.getObs().trim().isEmpty())
                ? e.getObs() : " ";
        cell.addElement(new Phrase(obsText, F_A4_VAL));

        table.addCell(cell);
        return table;
    }

    private static PdfPTable buildSignatureTableA4() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(6f);
        table.setSplitRows(false);
        table.setKeepTogether(true);
        try { table.setWidths(new float[] { 50f, 50f }); } catch (DocumentException ignored) {}

        PdfPCell funcCell = emptyCellA4(22f);
        funcCell.setBackgroundColor(new BaseColor(247, 247, 249));
        PdfPCell encCell = emptyCellA4(22f);
        encCell.setBackgroundColor(new BaseColor(247, 247, 249));

        table.addCell(funcCell);
        table.addCell(encCell);
        table.addCell(signatureLabelCellA4("O FUNCIONÁRIO"));
        table.addCell(signatureLabelCellA4("O ENCARREGADO"));
        return table;
    }

    /* ── Helpers A4 ── */

    private static void addPlainFieldCellA4(PdfPTable table, String label, Object value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(7f);
        cell.setBackgroundColor(new BaseColor(251, 252, 253));
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + ": ", F_A4_LBL));
        phrase.add(new Chunk(fmt(value), F_A4_VAL));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static void addFullLineCellA4(PdfPTable table, String label, Object value, int colspan) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(5f);
        cell.setBackgroundColor(new BaseColor(251, 252, 253));
        cell.setColspan(colspan);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + ": ", F_A4_LBL));
        phrase.add(new Chunk(fmt(value), F_A4_VAL));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static PdfPCell emptyCellA4(float height) {
        PdfPCell cell = new PdfPCell(new Phrase(" ", F_A4_VAL));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        return cell;
    }

    private static PdfPCell signatureLabelCellA4(String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, F_A4_SIGN));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setPaddingTop(5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static void addSectionTitleA4(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, F_A4_SECTION);
        p.setSpacingBefore(4f);
        p.setSpacingAfter(2f);
        doc.add(p);
        LineSeparator sep = new LineSeparator(1f, 100f, BRAND_RED, Element.ALIGN_LEFT, -2);
        doc.add(new Chunk(sep));
    }

    private static Paragraph spacerSmallA4() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(2);
        return p;
    }

    /* ═══════════════════════════════════════════════════════════════
       TABELAS LEGADAS (mantidas para compatibilidade)
       ═══════════════════════════════════════════════════════════════ */

    private static PdfPTable buildStudentTable(StudentDTO s) {
        PdfPTable table = newFormTable();
        addRow(table, "Nº de Estudante", s.getSudentNumber());
        addRow(table, "Nome Completo", s.getFullName());
        addRow(table, "Primeiro Nome", s.getFristName());
        addRow(table, "Último Nome", s.getLastName());
        addRow(table, "Género", s.getGender());
        addRow(table, "Nº do BI", s.getBiNumber());
        addRow(table, "Data de Nascimento", s.getNascDate());
        addRow(table, "Validade do BI", s.getBiExpiryData());
        addRow(table, "Endereço", s.getAddressStreet());
        addRow(table, "Província", s.getAddressProvice());
        addRow(table, "Nome do Pai", s.getNameFather());
        addRow(table, "Nome da Mãe", s.getNameMather());
        addRow(table, "Email", s.getEmail());
        addRow(table, "Telefone 1", s.getPhone_1());
        addRow(table, "Telefone 2", s.getPhone_2());
        addRow(table, "Estado", s.getStatus());
        addRow(table, "Observações", s.getObs());
        addRow(table, "Criado em", s.getCreatedAt());
        addRow(table, "Actualizado em", s.getUpdatedAt());
        return table;
    }

    private static PdfPTable buildStudentListTable(List<StudentDTO> students) {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[] { 14f, 26f, 10f, 16f, 12f, 14f, 10f }); } catch (DocumentException ignored) {}

        String[] headers = { "Nº Aluno", "Nome Completo", "", "Nº BI", "Data Nasc.", "Telefone", "Estado" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, F_HEADER));
            cell.setBackgroundColor(BRAND_BLACK);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean alternate = false;
        for (StudentDTO s : students) {
            BaseColor bg = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addListCell(table, fmt(s.getSudentNumber()), bg);
            addListCell(table, fmt(s.getFullName()), bg);
            addListCell(table, fmt(s.getGender()), bg);
            addListCell(table, fmt(s.getBiNumber()), bg);
            addListCell(table, fmt(s.getNascDate()), bg);
            addListCell(table, fmt(s.getPhone_1()), bg);
            addListCell(table, fmt(s.getStatus()), bg);
        }
        return table;
    }

    private static PdfPTable buildPagamentoListTable(List<PagamentoDTO> pagamentos) {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSpacingBefore(8f);
        try { table.setWidths(new float[] { 14f, 24f, 13f, 14f, 13f, 12f, 16f }); } catch (DocumentException ignored) {}

        String[] headers = { "Documento", "Aluno", "Mês", "Data", "Método", "Estado", "Total (Kz)" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, F_HEADER));
            cell.setBackgroundColor(BRAND_BLACK);
            cell.setPadding(4f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        boolean alternate = false;
        for (PagamentoDTO p : pagamentos) {
            BaseColor bg = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addCompactPaymentCell(table, fmt(p.getNumeroDocumento()), bg, Element.ALIGN_LEFT);
            addCompactPaymentCell(table, fmt(p.getStudentFullName()), bg, Element.ALIGN_LEFT);
            addCompactPaymentCell(table, fmt(p.getMesReferencia()), bg, Element.ALIGN_CENTER);
            addCompactPaymentCell(table, fmt(p.getDataPagamento()), bg, Element.ALIGN_CENTER);
            addCompactPaymentCell(table, fmt(p.getFormaPagamento()), bg, Element.ALIGN_CENTER);
            addCompactPaymentCell(table, fmt(p.getEstado()), bg, Element.ALIGN_CENTER);
            addCompactPaymentCell(table, formatMoney(p.getTotal()), bg, Element.ALIGN_RIGHT);
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL GERAL", F_LABEL));
        totalLabel.setColspan(6);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPadding(4f);
        totalLabel.setBackgroundColor(new BaseColor(255, 244, 214));
        totalLabel.setBorderColor(BRAND_YELLOW);
        table.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase(formatMoney(sumTotal(pagamentos)), F_LABEL));
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValue.setPadding(4f);
        totalValue.setBackgroundColor(new BaseColor(255, 244, 214));
        totalValue.setBorderColor(BRAND_YELLOW);
        table.addCell(totalValue);
        return table;
    }

    private static PdfPTable buildPagamentoSummary(List<PagamentoDTO> pagamentos) {
        PdfPTable summary = new PdfPTable(3);
        summary.setWidthPercentage(100);
        summary.setSpacingAfter(2f);
        try { summary.setWidths(new float[] { 33f, 33f, 34f }); } catch (DocumentException ignored) {}

        addPaymentSummaryCell(summary, "REGISTOS", String.valueOf(pagamentos.size()));
        addPaymentSummaryCell(summary, "VALOR", formatMoney(sumField(pagamentos, true)));
        addPaymentSummaryCell(summary, "MULTAS", formatMoney(sumField(pagamentos, false)));
        return summary;
    }

    private static void addPaymentSummaryCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(5f);
        cell.setBackgroundColor(ROW_ALT_BG);
        cell.setBorderColor(BORDER_COLOR);
        Paragraph labelParagraph = new Paragraph(label, F_LABEL);
        labelParagraph.setAlignment(Element.ALIGN_CENTER);
        Paragraph valueParagraph = new Paragraph(value, F_SECTION);
        valueParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelParagraph);
        cell.addElement(valueParagraph);
        table.addCell(cell);
    }

    private static BigDecimal sumTotal(List<PagamentoDTO> pagamentos) {
        return pagamentos.stream()
                .map(PagamentoDTO::getTotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumField(List<PagamentoDTO> pagamentos, boolean valor) {
        return pagamentos.stream()
                .map(pagamento -> valor ? pagamento.getValor() : pagamento.getMulta())
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "-";
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " Kz";
    }

    private static PdfPTable newFormTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[] { 30f, 70f }); } catch (DocumentException ignored) {}
        table.setSpacingAfter(4f);
        return table;
    }

    private static void addRow(PdfPTable table, String label, Object value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, F_LABEL));
        labelCell.setBackgroundColor(ROW_ALT_BG);
        labelCell.setPadding(6f);
        labelCell.setBorderColor(BORDER_COLOR);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valueCell = new PdfPCell(new Phrase(fmt(value), F_VALUE));
        valueCell.setPadding(6f);
        valueCell.setBorderColor(BORDER_COLOR);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static void addListCell(PdfPTable table, String text, BaseColor bg) {
        addListCell(table, text, bg, Element.ALIGN_LEFT);
    }

    private static void addListCell(PdfPTable table, String text, BaseColor bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_CELL));
        cell.setBackgroundColor(bg);
        cell.setPadding(5f);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private static void addCompactPaymentCell(PdfPTable table, String text, BaseColor bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_CELL));
        cell.setBackgroundColor(bg);
        cell.setPadding(3f);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private static void addCheckboxCell(PdfPTable table, String label, boolean checked) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(6f);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + " ", F_FIELD_LBL));
        phrase.add(new Chunk(checked ? "(X)" : "( )", F_CHECK));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static void addPlainFieldCell(PdfPTable table, String label, Object value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(6f);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + ": ", F_FIELD_LBL));
        phrase.add(new Chunk(fmt(value), F_FIELD_VAL));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static void addFullLineCell(PdfPTable table, String label, Object value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(5f);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + ": ", F_FIELD_LBL));
        phrase.add(new Chunk(fmt(value), F_FIELD_VAL));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static void addObservacaoQuestionRow(PdfPTable table, String question) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(question + "   ", F_FIELD_LBL));
        phrase.add(new Chunk("SIM ( )     NÃO ( )", F_FIELD_VAL));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static void addQualLine(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(4f);
        cell.addElement(new Phrase("QUAL: ", F_FIELD_LBL));
        table.addCell(cell);
    }

    private static PdfPCell emptyCell(float height) {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        return cell;
    }

    private static PdfPCell signatureLabelCell(String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, F_SIGN));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setPaddingTop(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    /**
     * Ano lectivo actual. Calendário lectivo angolano corre de Fevereiro a
     * Novembro/Dezembro — por isso o corte é em Fevereiro (mês 2), não
     * Setembro. Ex.: em Agosto/2026 devolve "2026/2027".
     */
    private static String currentAcademicYear() {
        LocalDate today = LocalDate.now();
        int startYear = today.getMonthValue() >= 2 ? today.getYear() : today.getYear() - 1;
        return startYear + "/" + (startYear + 1);
    }
}