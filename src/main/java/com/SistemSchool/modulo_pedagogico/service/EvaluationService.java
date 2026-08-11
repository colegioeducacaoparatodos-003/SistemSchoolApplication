package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.EvaluationTableProjection;
import com.SistemSchool.modulo_pedagogico.io.EvaluationStatus;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.EvaluationRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EvaluationService {

    private final EvaluationRepository repository;
    private final DisciplineRepository disciplineRepository;
    private final ScheduleRepository scheduleRepository;

    public EvaluationService(EvaluationRepository evaluationRepository,
            DisciplineRepository disciplineRepository, ScheduleRepository scheduleRepository) {
        this.repository = evaluationRepository;
        this.disciplineRepository = disciplineRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Evaluation save(Evaluation evaluation) {
        if (evaluation.getDiscipline() == null || evaluation.getDiscipline().getPkDiscipline() == null) {
            throw new RuntimeException("É necessário indicar a disciplina da avaliação.");
        }
        if (evaluation.getSchedule() == null || evaluation.getSchedule().getPkSchedule() == null) {
            throw new RuntimeException("É necessário indicar o horário da avaliação.");
        }
        if (repository.existsByDiscipline_PkDisciplineAndTitleAndTrimester(
                evaluation.getDiscipline().getPkDiscipline(), evaluation.getTitle(), evaluation.getTrimester())) {
            throw new RuntimeException("Já existe uma avaliação com este título para esta disciplina e trimestre.");
        }

        Discipline discipline = disciplineRepository.findById(evaluation.getDiscipline().getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        Schedule schedule = scheduleRepository.findById(evaluation.getSchedule().getPkSchedule())
                .orElseThrow(() -> new RuntimeException("Horário não encontrado."));

        evaluation.setDiscipline(discipline);
        evaluation.setSchedule(schedule);

        return repository.save(evaluation);
    }

    public void update(EvaluationDTO dto) {
        Evaluation evaluation = repository.findById(dto.getPkEvaluation())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada com id: " + dto.getPkEvaluation()));

        if (dto.getDisciplinePk() != null
                && !dto.getDisciplinePk().equals(evaluation.getDiscipline().getPkDiscipline())) {
            Discipline discipline = disciplineRepository.findById(dto.getDisciplinePk())
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
            evaluation.setDiscipline(discipline);
        }

        if (dto.getSchedulePk() != null
                && !dto.getSchedulePk().equals(evaluation.getSchedule().getPkSchedule())) {
            Schedule schedule = scheduleRepository.findById(dto.getSchedulePk())
                    .orElseThrow(() -> new RuntimeException("Horário não encontrado."));
            evaluation.setSchedule(schedule);
        }

        evaluation.setTitle(dto.getTitle());
        evaluation.setType(dto.getType());
        evaluation.setWeight(dto.getWeight());
        evaluation.setEvaluationDate(dto.getEvaluationDate());
        evaluation.setStatus(dto.getStatus());
        evaluation.setTrimester(dto.getTrimester());

        repository.save(evaluation);
    }

    public void delete(Long id) {
        try {
            Evaluation evaluation = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada"));
            repository.delete(evaluation);
            repository.flush(); // força o erro aqui, dentro do try
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível eliminar esta avaliação porque já existem notas lançadas para ela.");
        }
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
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EvaluationTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new EvaluationDTO(
                p.getPkEvaluation(),
                p.getDisciplinePk(),
                p.getDisciplineName(),
                p.getSchedulePk(),
                p.getScheduleWeekDay() != null ? WeekDay.valueOf(p.getScheduleWeekDay()) : null,
                p.getTitle(),
                p.getType() != null ? EvaluationType.valueOf(p.getType()) : null,
                p.getWeight(),
                p.getEvaluationDate(),
                p.getStatus() != null ? EvaluationStatus.valueOf(p.getStatus()) : null,
                p.getTrimester(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Evaluation> getByDiscipline(Long disciplinePk) {
        return repository.findByDiscipline_PkDiscipline(disciplinePk);
    }

    public List<Evaluation> getBySchedule(Long schedulePk) {
        return repository.findBySchedule_PkSchedule(schedulePk);
    }

    public List<Evaluation> getByType(EvaluationType type) {
        return repository.findByType(type);
    }

    public List<Evaluation> getByStatus(EvaluationStatus status) {
        return repository.findByStatus(status);
    }

    public List<Evaluation> getByTrimester(Integer trimester) {
        return repository.findByTrimester(trimester);
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