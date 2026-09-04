package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.EnrolmentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.EnrolmentType;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class EnrolmentService {

    private final EnrolmentRepository repository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;

    public EnrolmentService(EnrolmentRepository enrolmentRepository, StudentRepository studentRepository,
            SchoolClassRepository schoolClassRepository) {
        this.repository = enrolmentRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    public String generateNextEnrolmentNumber() {
        int year = java.time.Year.now().getValue();
        String prefix = "MAT-" + year + "-";

        Optional<String> lastOpt = repository.findLastEnrolmentNumberByPrefix(prefix + "%");

        int nextSeq = 1;
        if (lastOpt.isPresent()) {
            String seqPart = lastOpt.get().substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1; // fallback se o formato mudar
            }
        }
        return prefix + String.format("%04d", nextSeq);
    }

    public String getCurrentSchoolYear() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (month >= 9) {
            return year + "/" + (year + 1);
        } else {
            return (year - 1) + "/" + year;
        }
    }

    public List<EnrolmentDTO> getEnrolmentsBySchoolClassDTO(Long schoolClassPk) {
        if (schoolClassPk == null) {
            throw new RuntimeException("É necessário indicar a turma.");
        }
        return repository.findAllEnrolmentsDTOBySchoolClass(schoolClassPk);
    }
    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Enrolment save(Enrolment enrolment) {
        if (repository.existsByEnrolmentNumer(enrolment.getEnrolmentNumer())) {
            throw new RuntimeException("Número de matrícula já existe: " + enrolment.getEnrolmentNumer());
        }
        if (enrolment.getStudent() == null || enrolment.getStudent().getPkStudent() == null) {
            throw new RuntimeException("É necessário indicar o aluno para a matrícula.");
        }
        if (enrolment.getSchoolClass() == null || enrolment.getSchoolClass().getPkSchoolClass() == null) {
            throw new RuntimeException("É necessário indicar a turma para a matrícula.");
        }

        Long studentPk = enrolment.getStudent().getPkStudent();
        Long schoolClassPk = enrolment.getSchoolClass().getPkSchoolClass();

        if (repository.existsByStudent_PkStudentAndSchoolClass_PkSchoolClassAndShift(
                studentPk, schoolClassPk, enrolment.getShift())) {
            throw new RuntimeException("Este aluno já está matriculado nesta turma e turno.");
        }

        Student student = studentRepository.findById(studentPk)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + studentPk));
        enrolment.setStudent(student);

        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassPk)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + schoolClassPk));
        enrolment.setSchoolClass(schoolClass);

        return repository.save(enrolment);
    }

    public void update(EnrolmentDTO dto) {
        Enrolment enrolment = repository.findById(dto.getPhEnrolment())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + dto.getPhEnrolment()));

        if (dto.getStudentPk() != null
                && !dto.getStudentPk().equals(enrolment.getStudent().getPkStudent())) {
            Student student = studentRepository.findById(dto.getStudentPk())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + dto.getStudentPk()));
            enrolment.setStudent(student);
        }

        if (dto.getSchoolClassPk() != null
                && !dto.getSchoolClassPk().equals(enrolment.getSchoolClass().getPkSchoolClass())) {
            SchoolClass schoolClass = schoolClassRepository.findById(dto.getSchoolClassPk())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + dto.getSchoolClassPk()));
            enrolment.setSchoolClass(schoolClass);
        }

        enrolment.setEnrolmentNumer(dto.getEnrolmentNumber());
        enrolment.setShift(dto.getShift());
        enrolment.setEnrolmentType(dto.getEnrolmentType());
        enrolment.setEnrolmentData(dto.getEnrolmentData());
        enrolment.setObs(dto.getObs());
        enrolment.onUpdateString();

        repository.save(enrolment);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Matrícula não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }

    public List<Enrolment> findBySchoolClassId(Long schoolClassPk) {
        return repository.findBySchoolClass_PkSchoolClassWithStudent(schoolClassPk);
    }
    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<EnrolmentDTO> getAllEnrolments() {
        return repository.findAllEnrolmentsDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<EnrolmentDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        List<EnrolmentDTO> all = getAllEnrolments();
        List<EnrolmentDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<EnrolmentDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<EnrolmentDTO> applyFilters(List<EnrolmentDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<EnrolmentDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (EnrolmentDTO dto : source) {
            boolean matches = true;

            if (!globalTerm.isEmpty()) {
                matches = matchesGlobal(dto, globalTerm);
            }

            if (matches) {
                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    if ("global".equals(field)) {
                        continue;
                    }
                    String expected = normalize(entry.getValue());
                    if (expected.isEmpty()) {
                        continue;
                    }

                    boolean fieldMatches = switch (field) {
                        case "enrolmentNumer" -> contains(dto.getEnrolmentNumber(), expected);
                        case "studentFullName" -> contains(dto.getStudentFullName(), expected);
                        case "studentNumber" -> contains(dto.getStudentNumber(), expected);
                        case "schoolclasscode" -> contains(dto.getSchoolClassCode(), expected);
                        case "schoolclassnome" -> contains(dto.getSchoolClassName(), expected);
                        case "shift" -> contains(dto.getShift() != null ? dto.getShift().name() : null, expected);
                        case "enrolmentType" ->
                            contains(dto.getEnrolmentType() != null ? dto.getEnrolmentType().name() : null, expected);
                        default -> true;
                    };

                    if (!fieldMatches) {
                        matches = false;
                        break;
                    }
                }
            }

            if (matches) {
                filtered.add(dto);
            }
        }

        return filtered;
    }

    private boolean matchesGlobal(EnrolmentDTO dto, String globalTerm) {
        return contains(dto.getEnrolmentNumber(), globalTerm)
                || contains(dto.getStudentFullName(), globalTerm)
                || contains(dto.getStudentNumber(), globalTerm)
                || contains(dto.getSchoolClassCode(), globalTerm)
                || contains(dto.getSchoolClassName(), globalTerm)
                || contains(dto.getObs(), globalTerm)
                || contains(dto.getShift() != null ? dto.getShift().name() : null, globalTerm)
                || contains(dto.getEnrolmentType() != null ? dto.getEnrolmentType().name() : null, globalTerm);
    }

    private boolean contains(String value, String expected) {
        return value != null && value.toLowerCase().contains(expected.toLowerCase());
    }

    private String normalize(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    // ─────────────────────────────────────────────────────────────
    // CONTAGEM
    // ─────────────────────────────────────────────────────────────

    public long count() {
        return repository.count();
    }
    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Enrolment> getByShift(ShiftType shift) {
        return repository.findByShift(shift);
    }

    public List<Enrolment> getByEnrolmentType(EnrolmentType enrolmentType) {
        return repository.findByEnrolmentType(enrolmentType);
    }

    public List<Enrolment> getByStudent(Long studentPk) {
        return repository.findByStudent_PkStudent(studentPk);
    }

    public List<Enrolment> getBySchoolClass(Long schoolClassPk) {
        return repository.findBySchoolClass_PkSchoolClass(schoolClassPk);
    }

    public List<Enrolment> getBySchoolClassCode(String classCode) {
        return repository.findBySchoolClass_ClassCode(classCode);
    }

    public Enrolment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + id));
    }

    public Enrolment findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + id));
    }
}