package com.SistemSchool.report;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Geração centralizada de PDFs responsivos.
 * Formato de papel adaptado ao contexto:
 *   • A4 Landscape  → Listagens completas com filtros
 *   • A4 Portrait   → Relatórios por turma / Ficha do Aluno (matrícula)
 *   • A6            → Cartão de matrícula pocket
 */
public final class PdfReportService {

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
    private static final Font F_A4_SCHOOL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
    private static final Font F_A4_FICHA   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND_RED);
    private static final Font F_A4_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND_RED);
    private static final Font F_A4_LBL     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font F_A4_VAL     = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);
    private static final Font F_A4_SIGN    = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);
    private static final Font F_A4_PHOTO   = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);

    private static final String LOGO_PATH = "/resources/imgs/logo.jpg";

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
       A4 LANDSCAPE  →  Lista completa filtrada
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentListReport(List<EnrolmentDTO> list) throws DocumentException {
        return generateEnrolmentListReport(list, null);
    }

    public static byte[] generateEnrolmentListReport(List<EnrolmentDTO> list,
                                                     String filterDescription) throws DocumentException {
        Document doc = new Document(PageSize.A4.rotate(), 24, 24, 30, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        addBrandTitle(doc, "LISTA DE MATRÍCULAS");

        if (filterDescription != null && !filterDescription.isBlank()) {
            Paragraph f = new Paragraph("Filtros activos: " + filterDescription, F_FILTER);
            f.setAlignment(Element.ALIGN_CENTER);
            f.setSpacingAfter(4);
            doc.add(f);
        }

        Paragraph gerado = new Paragraph("Gerado em: " + LocalDateTime.now().format(DATETIME_FMT), F_SUBTITLE);
        gerado.setAlignment(Element.ALIGN_CENTER);
        gerado.setSpacingAfter(12);
        doc.add(gerado);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.6f, 3.2f, 1.4f, 1.4f, 1.3f, 1.4f, 1.3f });
        table.setHeaderRows(1);

        String[] headers = { "Nº Matrícula", "Aluno", "Nº Aluno", "Turma", "Turno", "Tipo", "Data" };
        for (String h : headers) table.addCell(createHeaderCell(h));

        boolean alt = false;
        for (EnrolmentDTO e : list) {
            BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
            alt = !alt;
            addCell(table, fmt(e.getEnrolmentNumber()), bg);
            addCell(table, fmt(e.getStudentFullName()), bg);
            addCell(table, fmt(e.getStudentNumber()), bg);
            addCell(table, fmt(e.getSchoolClassPk()), bg);
            addCell(table, fmt(e.getShift()), bg);
            addCell(table, fmt(e.getEnrolmentType()), bg);
            addCell(table, fmt(e.getEnrolmentData()), bg);
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

    /* ═══════════════════════════════════════════════════════════════
       A4 PORTRAIT  →  Relatório por Turma
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentsByClassReport(SchoolClass schoolClass,
                                                         List<EnrolmentDTO> list) throws DocumentException {
        Document doc = new Document(PageSize.A4, 24, 24, 30, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        addBrandTitle(doc, "MATRÍCULAS POR TURMA");

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[] { 1, 3 });
        info.setSpacingAfter(12);

        addInfoRow(info, "Turma", schoolClass.getClassCode() + " - " + schoolClass.getClassName());
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
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1.4f, 3.5f, 1.6f, 1.6f, 1.4f });
            table.setHeaderRows(1);

            String[] headers = { "Nº Matrícula", "Aluno", "Nº Aluno", "Tipo", "Data" };
            for (String h : headers) table.addCell(createHeaderCell(h));

            boolean alt = false;
            for (EnrolmentDTO e : list) {
                BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
                alt = !alt;
                addCell(table, fmt(e.getEnrolmentNumber()), bg);
                addCell(table, fmt(e.getStudentFullName()), bg);
                addCell(table, fmt(e.getStudentNumber()), bg);
                addCell(table, fmt(e.getEnrolmentType()), bg);
                addCell(table, fmt(e.getEnrolmentData()), bg);
            }
            doc.add(table);
        }

        addFooter(doc, writer);
        doc.close();
        return baos.toByteArray();
    }

    /* ═══════════════════════════════════════════════════════════════
       A4 PORTRAIT  →  Ficha Individual de Matrícula (única página)
       Fonte Arial/Helvetica, tamanho mínimo 14
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentReport(EnrolmentDTO enrolment, StudentDTO student)
            throws DocumentException {
        Document doc = new Document(PageSize.A4, 28, 28, 20, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);

        doc.open();
        doc.add(buildFichaHeaderA4());
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
       ═══════════════════════════════════════════════════════════════ */
    public static byte[] generateEnrolmentCardA6(EnrolmentDTO enrolment, StudentDTO student)
            throws DocumentException {
        float w = 105f * 2.8346f;
        float h = 148f * 2.8346f;
        Document doc = new Document(new Rectangle(0, 0, w, h), 10, 10, 10, 10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
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

        PdfPCell photoCell = new PdfPCell(new Phrase("FOTO\n3x4", F_PHOTO));
        photoCell.setBorder(Rectangle.BOX);
        photoCell.setBorderColor(BRAND_BLACK);
        photoCell.setFixedHeight(50f);
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
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 90, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent("Lista de Alunos"));
        doc.open();
        addSectionTitle(doc, "Lista de Alunos (" + students.size() + ")");
        doc.add(buildStudentListTable(students));
        doc.close();
        return baos.toByteArray();
    }

    public static byte[] generatePagamentoListReport(List<PagamentoDTO> pagamentos) throws DocumentException {
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 90, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new ReportPageEvent("Lista de Pagamentos"));
        doc.open();
        addSectionTitle(doc, "Lista de Pagamentos (" + pagamentos.size() + ")");
        doc.add(buildPagamentoListTable(pagamentos));
        doc.close();
        return baos.toByteArray();
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
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", F_CELL));
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
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

    private static PdfPTable buildFichaHeaderA4() {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        try { header.setWidths(new float[] { 18f, 57f, 25f }); } catch (DocumentException ignored) {}

        Image logo = tryLoadLogo();
        PdfPCell logoCell;
        if (logo != null) {
            logo.scaleToFit(80f, 80f);
            logoCell = new PdfPCell(logo, false);
        } else {
            logoCell = new PdfPCell(new Phrase(""));
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPadding(4f);
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPadding(4f);

        Paragraph schoolName = new Paragraph("COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS", F_A4_SCHOOL);
        schoolName.setAlignment(Element.ALIGN_CENTER);
        Paragraph fichaTitle = new Paragraph("FICHA DO(A) ALUNO(A)", F_A4_FICHA);
        fichaTitle.setAlignment(Element.ALIGN_CENTER);
        fichaTitle.setSpacingBefore(6f);

        titleCell.addElement(schoolName);
        titleCell.addElement(fichaTitle);
        header.addCell(titleCell);

        PdfPCell photoCell = new PdfPCell(new Phrase("FOTO", F_A4_PHOTO));
        photoCell.setBorder(Rectangle.BOX);
        photoCell.setBorderColor(BaseColor.BLACK);
        photoCell.setFixedHeight(90f);
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
        } catch (Exception ex) { return null; }
    }

    /** Cabeçalho de informações — sem checkboxes de Matrícula/Confirmação */
    private static PdfPTable buildTopInfoTableA4(EnrolmentDTO e) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[] { 50f, 50f }); } catch (DocumentException ignored) {}
        table.setSpacingBefore(4f);
        table.setSpacingAfter(4f);

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
        table.setSpacingAfter(4f);

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
        table.setSpacingAfter(4f);

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

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(10f);
        cell.setMinimumHeight(70f);

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
        table.setSpacingBefore(24f);
        try { table.setWidths(new float[] { 50f, 50f }); } catch (DocumentException ignored) {}

        table.addCell(emptyCellA4(36f));
        table.addCell(emptyCellA4(36f));
        table.addCell(signatureLabelCellA4("O FUNCIONÁRIO"));
        table.addCell(signatureLabelCellA4("O ENCARREGADO"));
        return table;
    }

    /* ── Helpers A4 ── */

    private static void addPlainFieldCellA4(PdfPTable table, String label, Object value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(7f);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + ": ", F_A4_LBL));
        phrase.add(new Chunk(fmt(value), F_A4_VAL));
        cell.addElement(phrase);
        table.addCell(cell);
    }

    private static void addFullLineCellA4(PdfPTable table, String label, Object value, int colspan) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(7f);
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
        cell.setPaddingTop(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static void addSectionTitleA4(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, F_A4_SECTION);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(4f);
        doc.add(p);
        LineSeparator sep = new LineSeparator(1f, 100f, BORDER_COLOR, Element.ALIGN_LEFT, -2);
        doc.add(new Chunk(sep));
    }

    private static Paragraph spacerSmallA4() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(3);
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
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[] { 18f, 34f, 24f, 24f }); } catch (DocumentException ignored) {}

        String[] headers = { "Nº Documento", "Aluno", "Método de Pagamento", "Data do Pagamento" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, F_HEADER));
            cell.setBackgroundColor(BRAND_BLACK);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean alternate = false;
        for (PagamentoDTO p : pagamentos) {
            BaseColor bg = alternate ? ROW_ALT_BG : BaseColor.WHITE;
            alternate = !alternate;
            addListCell(table, fmt(p.getNumeroDocumento()), bg);
            addListCell(table, fmt(p.getStudentFullName()), bg);
            addListCell(table, fmt(p.getFormaPagamento()), bg);
            addListCell(table, fmt(p.getDataPagamento()), bg);
        }
        return table;
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
        PdfPCell cell = new PdfPCell(new Phrase(text, F_CELL));
        cell.setBackgroundColor(bg);
        cell.setPadding(5f);
        cell.setBorderColor(BORDER_COLOR);
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

    private static String currentAcademicYear() {
        LocalDate today = LocalDate.now();
        int startYear = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        return startYear + "/" + (startYear + 1);
    }
}