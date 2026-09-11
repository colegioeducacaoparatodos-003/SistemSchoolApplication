package com.SistemSchool.modulo_Recursoa_Humano.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;

import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.interfaces.TeacherTableProjection;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.repository.TeacherRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherService.class);

    private static final String TEACHER_PREFIX = "PROF";
    private static final int SEQUENCE_LENGTH = 5;
    private static final String DEFAULT_PHOTO = "default.png";

    private final TeacherRepository repository;

    /**
     * Diretório onde as fotos dos professores são gravadas.
     *
     * IMPORTANTE: propositadamente NÃO usamos
     * FacesContext.getExternalContext().getRealPath("/") aqui.
     *
     * Quando a aplicação corre como JAR executável do Spring Boot (Tomcat
     * embutido), os recursos web ficam empacotados dentro do próprio JAR e
     * getRealPath("/") pode devolver null. Isso provocava um
     * NullPointerException dentro de Paths.get(webRoot, ...), que era
     * apanhado pelo catch (Exception e) genérico do save()/updatePhoto() e
     * relançado como uma IOException genérica — dando a sensação de "não dá
     * para gravar" sem nenhuma pista da causa real.
     *
     * Configurável via application.properties/yml:
     *   app.uploads.teacher-photos-dir=/caminho/absoluto/para/fotos
     * Por omissão usa uma pasta "uploads/teacher_img" ao lado do diretório
     * de trabalho (user.dir) da aplicação.
     */
    @Value("${app.uploads.teacher-photos-dir:${user.dir}/uploads/teacher_img}")
    private String teacherImgDir;

    public TeacherService(TeacherRepository teacherRepository) {
        this.repository = teacherRepository;
    }

    // ---------------------
    // ARMAZENAMENTO DE FOTOS
    // ---------------------

    private Path resolveTeacherImgFolder() throws IOException {
        Path folderPath = Paths.get(teacherImgDir);
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
            log.info("Pasta de fotos de professores criada em: {}", folderPath.toAbsolutePath());
        }
        return folderPath;
    }

    /**
     * Grava a foto enviada em disco com um nome único e devolve o nome do
     * ficheiro gravado (para persistir em Teacher.photoPhath).
     */
    private String saveTeacherPhoto(UploadedFile photo) throws IOException {
        Path folder = resolveTeacherImgFolder();

        String extension;
        String contentType = photo.getContentType();
        String originalName = photo.getFileName();

        if (contentType != null && contentType.contains("/")) {
            extension = contentType.substring(contentType.lastIndexOf('/') + 1);
        } else if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.') + 1);
        } else {
            extension = "png";
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date());
        String fileName = "0img-" + timestamp + "." + extension;
        Path destination = folder.resolve(fileName);

        try (InputStream inputStream = photo.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Foto de professor gravada em: {}", destination.toAbsolutePath());
        return fileName;
    }

    // ---------------------
    // CRUD
    // ---------------------

    public Teacher save(Teacher teacher, UploadedFile photo) throws IOException {

        teacher.setTeacherNumber(generateTeacherNumber());

        String newNameFile = DEFAULT_PHOTO;
        if (photo != null && photo.getSize() > 0) {
            try {
                newNameFile = saveTeacherPhoto(photo);
            } catch (IOException e) {
                log.error("Erro ao salvar imagem do professor", e);
                throw new IOException("Erro ao salvar imagem do professor: " + e.getMessage(), e);
            }
        }
        teacher.setPhotoPhath(newNameFile);

        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());

        try {
            Teacher savedTeacher = repository.save(teacher);
            log.info("Professor registado com sucesso: {}", teacher.getTeacherNumber());
            return savedTeacher;
        } catch (Exception e) {
            log.error("Erro ao salvar professor na base de dados", e);
            throw new IOException("Erro ao salvar professor na base de dados: " + e.getMessage(), e);
        }
    }

    public Teacher findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }

    public void update(TeacherDTO dto) {
        if (dto.getPkTeacher() == null) {
            throw new IllegalArgumentException(
                    "Não é possível atualizar: identificador do professor (pkTeacher) está nulo");
        }

        Teacher teacher = repository.findById(dto.getPkTeacher())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));

        teacher.setFristName(dto.getFristName());
        teacher.setLastName(dto.getLastName());
        teacher.setGender(dto.getGender());
        teacher.setQualificationLivel(dto.getQualificationLivel());
        teacher.setContractType(dto.getContractType());
        teacher.setStatus(dto.getStatus());
        teacher.setBiNumber(dto.getBiNumber());
        teacher.setBiExpiryDate(dto.getBiExpiryDate());
        teacher.setAddressStreet(dto.getAddressStreet());
        teacher.setAddressProvice(dto.getAddressProvice());
        teacher.setBaseSalary(dto.getBaseSalary());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setMobilePhone(dto.getMobilePhone());
        teacher.setObs(dto.getObs());
        teacher.setUpdatedAt(LocalDateTime.now());

        try {
            repository.save(teacher);
            log.info("Professor atualizado com sucesso: {}", teacher.getTeacherNumber());
        } catch (Exception e) {
            log.error("Erro ao atualizar professor na base de dados", e);
            throw new RuntimeException("Erro ao atualizar professor na base de dados: " + e.getMessage(), e);
        }
    }

    public void updatePhoto(Long pkTeacher, UploadedFile photo) throws IOException {
        if (photo == null || photo.getSize() <= 0) {
            return;
        }

        Teacher teacher = repository.findById(pkTeacher)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));

        try {
            String newNameFile = saveTeacherPhoto(photo);
            teacher.setPhotoPhath(newNameFile);
            teacher.setUpdatedAt(LocalDateTime.now());
            repository.save(teacher);
            log.info("Foto do professor atualizada com sucesso: {}", newNameFile);
        } catch (IOException e) {
            log.error("Erro ao atualizar imagem do professor", e);
            throw new IOException("Erro ao atualizar imagem do professor: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ---------------------
    // MÉTODO PARA LAZY LOADING (com filtros)
    // ---------------------

    public Page<TeacherDTO> findLazy(int page, int size, Sort sort,
            String teacherNumber, String name, String status) {

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TeacherTableProjection> projections = repository.findAllForTable(
                pageable, blankToNull(teacherNumber), blankToNull(name), blankToNull(status));

        return projections.map(p -> new TeacherDTO(
                p.getPkTeacher(),
                p.getTeacherNumber(),
                p.getFristName(),
                p.getLastName(),
                p.getQualificationLivel() != null ? QualificationLevel.valueOf(p.getQualificationLivel()) : null,
                p.getContractType() != null ? ContractType.valueOf(p.getContractType()) : null,
                p.getStatus() != null ? TeacherStatus.valueOf(p.getStatus()) : null,
                p.getPhotoPhath(),
                p.getEmail(),
                p.getPhone(),
                p.getCreatedAt()));
    }

    public long countFiltered(String teacherNumber, String name, String status) {
        return repository.countFiltered(blankToNull(teacherNumber), blankToNull(name), blankToNull(status));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    // ---------------------
    // ESTATÍSTICAS DO CABEÇALHO
    // ---------------------

    public long countTotal() {
        return repository.count();
    }

    public long countActive() {
        return repository.countByStatus(TeacherStatus.ACTIVE);
    }

    public long countNewThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        return repository.countByCreatedAtBetween(start, end);
    }

    public BigDecimal sumBaseSalary() {
        BigDecimal total = repository.sumBaseSalary();
        return total != null ? total : BigDecimal.ZERO;
    }

    // ---------------------
    // GERAÇÃO AUTOMÁTICA DO teacherNumber (PROF-ANO-SEQUENCIA)
    // ---------------------

    /**
     * Gera o próximo número de professor no formato PROF-ANO-SEQUENCIA
     * (ex: PROF-2026-00001).
     *
     * A sequência é reiniciada a cada ano. O bloqueio pessimista (FOR UPDATE)
     * aplicado na consulta do repositório evita que duas gravações
     * concorrentes gerem o mesmo número.
     */
    public String generateTeacherNumber() {

        int currentYear = Year.now().getValue();
        String prefix = TEACHER_PREFIX + "-" + currentYear + "-";

        String lastNumber = repository.findLastTeacherNumberForUpdate(prefix);

        int nextSequence = 1;
        if (lastNumber != null) {
            String sequencePart = lastNumber.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                nextSequence = 1;
            }
        }

        String sequenceFormatted = String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);
        return prefix + sequenceFormatted;
    }

    // ---------------------
    // EXPORTAÇÃO PDF DA LISTA
    // ---------------------

    /**
     * Gera um PDF simples com a lista de professores (respeitando os
     * mesmos filtros usados na tabela) e envia-o diretamente na resposta
     * HTTP corrente, terminando o ciclo de vida do JSF.
     */
    public void exportTeacherListPdf(String teacherNumber, String name, String status) throws IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"lista_professores.pdf\"");

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 30, 30);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Lista de Professores", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(14);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2f, 3f, 2.5f, 2.5f, 2f, 2.5f });

            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, java.awt.Color.WHITE);
            for (String header : new String[] { "Nº Professor", "Nome", "Qualificação", "Vínculo", "Estado", "Email" }) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(20, 20, 20));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font bodyFont = new Font(Font.HELVETICA, 8);
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
            Page<TeacherDTO> page = findLazy(0, Integer.MAX_VALUE, sort, teacherNumber, name, status);

            for (TeacherDTO t : page.getContent()) {
                table.addCell(new Paragraph(t.getTeacherNumber(), bodyFont));
                table.addCell(new Paragraph(t.getFristName() + " " + t.getLastName(), bodyFont));
                table.addCell(new Paragraph(
                        t.getQualificationLivel() != null ? t.getQualificationLivel().getDescricao() : "-", bodyFont));
                table.addCell(new Paragraph(
                        t.getContractType() != null ? t.getContractType().getDescricao() : "-", bodyFont));
                table.addCell(new Paragraph(
                        t.getStatus() != null ? t.getStatus().getDescricao() : "-", bodyFont));
                table.addCell(new Paragraph(t.getEmail() != null ? t.getEmail() : "-", bodyFont));
            }

            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar o PDF da lista de professores", e);
        } finally {
            document.close();
        }

        facesContext.responseComplete();
    }
}