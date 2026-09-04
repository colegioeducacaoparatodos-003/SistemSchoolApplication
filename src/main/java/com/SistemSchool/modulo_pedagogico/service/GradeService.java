package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.repository.EvaluationRepository;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GradeService {

    private final GradeRepository repository;
    private final EvaluationRepository evaluationRepository;
    private final EnrolmentRepository enrolmentRepository;

    public GradeService(GradeRepository repository, EvaluationRepository evaluationRepository,
                        EnrolmentRepository enrolmentRepository) {
        this.repository = repository;
        this.evaluationRepository = evaluationRepository;
        this.enrolmentRepository = enrolmentRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDAÇÃO DE NOTAS POR CLASSE
    // ─────────────────────────────────────────────────────────────

    private void validateScore(Double score, SchoolClass schoolClass) {
        if (score == null) {
            throw new RuntimeException("A nota é obrigatória.");
        }
        if (score < 0) {
            throw new RuntimeException("A nota não pode ser negativa.");
        }

        int maxScore = getMaxScore(schoolClass);
        if (score > maxScore) {
            throw new RuntimeException("A nota não pode ser superior a " + maxScore + " para esta classe.");
        }
    }

    private int getMaxScore(SchoolClass schoolClass) {
        String classeName = schoolClass.getClasse().name().toUpperCase();
        if (classeName.contains("7") || classeName.contains("SETIMA")
                || classeName.contains("8") || classeName.contains("OITAVA")
                || classeName.contains("9") || classeName.contains("NONA")) {
            return 20;
        }
        return 10;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Grade save(Grade grade) {
        if (grade.getEvaluation() == null || grade.getEvaluation().getPkEvaluation() == null) {
            throw new RuntimeException("É necessário indicar a avaliação para a nota.");
        }
        if (grade.getEnrolment() == null || grade.getEnrolment().getPhEnrolment() == null) {
            throw new RuntimeException("É necessário indicar a matrícula para a nota.");
        }

        Evaluation evaluation = evaluationRepository.findById(grade.getEvaluation().getPkEvaluation())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada com id: " + grade.getEvaluation().getPkEvaluation()));
        grade.setEvaluation(evaluation);

        Enrolment enrolment = enrolmentRepository.findById(grade.getEnrolment().getPhEnrolment())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + grade.getEnrolment().getPhEnrolment()));
        grade.setEnrolment(enrolment);

        validateScore(grade.getScore(), enrolment.getSchoolClass());

        return repository.save(grade);
    }

    public void update(GradeDTO dto) {
        Grade grade = repository.findById(dto.getPkGrade())
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + dto.getPkGrade()));

        if (dto.getScore() != null) {
            validateScore(dto.getScore(), grade.getEnrolment().getSchoolClass());
            grade.setScore(dto.getScore());
        }

        grade.setLaunchDate(dto.getLaunchDate());
        grade.setObs(dto.getObs());
        grade.onUpdate();

        repository.save(grade);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Nota não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS
    // ─────────────────────────────────────────────────────────────

    public List<GradeDTO> getAllGrades() {
        return repository.findAllGradesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING
    // ─────────────────────────────────────────────────────────────

    public Page<GradeDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        List<GradeDTO> all = getAllGrades();
        List<GradeDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<GradeDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<GradeDTO> applyFilters(List<GradeDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<GradeDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (GradeDTO dto : source) {
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
                        case "studentFullName" -> contains(dto.getStudentFullName(), expected);
                        case "studentNumber" -> contains(dto.getStudentNumber(), expected);
                        case "disciplineName" -> contains(dto.getDisciplineName(), expected);
                        case "evaluationName" -> contains(dto.getEvaluationName(), expected);
                        case "evaluationType" -> contains(dto.getEvaluationType() != null ? dto.getEvaluationType().name() : null, expected);
                        case "trimester" -> contains(dto.getTrimester() != null ? dto.getTrimester().name() : null, expected);
                        case "schoolclassName" -> contains(dto.getSchoolClassName(), expected);
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

    private boolean matchesGlobal(GradeDTO dto, String globalTerm) {
        return contains(dto.getStudentFullName(), globalTerm)
                || contains(dto.getStudentNumber(), globalTerm)
                || contains(dto.getDisciplineName(), globalTerm)
                || contains(dto.getEvaluationName(), globalTerm)
                || contains(dto.getSchoolClassName(), globalTerm)
                || contains(dto.getObs(), globalTerm);
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

    public List<Grade> getByEnrolment(Long enrolmentPk) {
        return repository.findByEnrolment_PhEnrolment(enrolmentPk);
    }

    public List<Grade> getByEvaluation(Long evaluationPk) {
        return repository.findByEvaluation_PkEvaluation(evaluationPk);
    }

    public Grade getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + id));
    }

    public Grade findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + id));
    }
}