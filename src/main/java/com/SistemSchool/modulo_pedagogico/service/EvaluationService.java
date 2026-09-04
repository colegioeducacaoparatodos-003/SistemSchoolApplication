package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.EvaluationTableProjection;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.EvaluationRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EvaluationService {

    private final EvaluationRepository repository;
    private final DisciplineRepository disciplineRepository;

    public EvaluationService(EvaluationRepository repository, DisciplineRepository disciplineRepository) {
        this.repository = repository;
        this.disciplineRepository = disciplineRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Evaluation save(Evaluation evaluation) {
        if (evaluation.getDiscipline() == null || evaluation.getDiscipline().getPkDiscipline() == null) {
            throw new RuntimeException("É necessário indicar a disciplina para a avaliação.");
        }

        Discipline discipline = disciplineRepository.findById(evaluation.getDiscipline().getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + evaluation.getDiscipline().getPkDiscipline()));
        evaluation.setDiscipline(discipline);

        return repository.save(evaluation);
    }

    public void update(EvaluationDTO dto) {
        Evaluation evaluation = repository.findById(dto.getPkEvaluation())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada com id: " + dto.getPkEvaluation()));

        if (dto.getDisciplinePk() != null && !dto.getDisciplinePk().equals(evaluation.getDiscipline().getPkDiscipline())) {
            Discipline discipline = disciplineRepository.findById(dto.getDisciplinePk())
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + dto.getDisciplinePk()));
            evaluation.setDiscipline(discipline);
        }

        evaluation.setEvaluationName(dto.getEvaluationName());
        evaluation.setEvaluationType(dto.getEvaluationType());
        evaluation.setTrimester(dto.getTrimester());
        evaluation.setEvaluationDate(dto.getEvaluationDate());
        evaluation.setAnoLectivo(dto.getAnoLectivo());
        evaluation.setObs(dto.getObs());
        evaluation.onUpdate();

        repository.save(evaluation);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Avaliação não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<EvaluationDTO> getAllEvaluations() {
        return repository.findAllEvaluationsDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<EvaluationDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        List<EvaluationDTO> all = getAllEvaluations();
        List<EvaluationDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<EvaluationDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<EvaluationDTO> applyFilters(List<EvaluationDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<EvaluationDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (EvaluationDTO dto : source) {
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
                        case "disciplineName" -> contains(dto.getDisciplineName(), expected);
                        case "evaluationName" -> contains(dto.getEvaluationName(), expected);
                        case "evaluationType" -> contains(dto.getEvaluationType() != null ? dto.getEvaluationType().name() : null, expected);
                        case "trimester" -> contains(dto.getTrimester() != null ? dto.getTrimester().name() : null, expected);
                        case "anoLectivo" -> contains(dto.getAnoLectivo(), expected);
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

    private boolean matchesGlobal(EvaluationDTO dto, String globalTerm) {
        return contains(dto.getDisciplineName(), globalTerm)
                || contains(dto.getEvaluationName(), globalTerm)
                || contains(dto.getEvaluationType() != null ? dto.getEvaluationType().name() : null, globalTerm)
                || contains(dto.getTrimester() != null ? dto.getTrimester().name() : null, globalTerm)
                || contains(dto.getAnoLectivo(), globalTerm)
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

    public List<Evaluation> getByDiscipline(Long disciplinePk) {
        return repository.findByDiscipline_PkDiscipline(disciplinePk);
    }

    public Evaluation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada com id: " + id));
    }

    public Evaluation findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada com id: " + id));
    }
}