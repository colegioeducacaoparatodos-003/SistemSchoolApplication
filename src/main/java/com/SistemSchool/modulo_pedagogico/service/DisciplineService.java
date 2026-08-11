package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.DisciplineTableProjection;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;

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
public class DisciplineService {

    private final DisciplineRepository repository;

    public DisciplineService(DisciplineRepository repository) {
        this.repository = repository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Discipline save(Discipline discipline) {
        if (repository.existsByDisciplineCode(discipline.getDisciplineCode())) {
            throw new RuntimeException("Código de disciplina já existe: " + discipline.getDisciplineCode());
        }
        return repository.save(discipline);
    }

    public void update(DisciplineDTO dto) {
        Discipline discipline = repository.findById(dto.getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + dto.getPkDiscipline()));

        discipline.setDisciplineCode(dto.getDisciplineCode());
        discipline.setDisciplineName(dto.getDisciplineName());
        discipline.setWorkload(dto.getWorkload());
        discipline.setStatus(dto.getStatus());

        // onUpdate() é disparado automaticamente pelo @PreUpdate no flush
        repository.save(discipline);
    }

    public void delete(Long id) {
        try {
            Discipline discipline = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada"));
            repository.delete(discipline);
            repository.flush(); // força o erro aqui, dentro do try
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível eliminar a Disciplina.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<DisciplineDTO> getAllDisciplines() {
        return repository.findAllDisciplinesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<DisciplineDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DisciplineTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new DisciplineDTO(
                p.getPkDiscipline(),
                p.getDisciplineCode(),
                p.getDisciplineName(),
                p.getWorkload(),
                p.getStatus() != null ? DisciplineStatus.valueOf(p.getStatus()) : null,
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Discipline> getByStatus(DisciplineStatus status) {
        return repository.findByStatus(status);
    }

    public Discipline getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + id));
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS (stat cards)
    // ─────────────────────────────────────────────────────────────

    public long getTotalDisciplineCount() {
        return repository.count();
    }

    public long getActiveDisciplineCount() {
        return repository.findByStatus(DisciplineStatus.ACTIVE).size();
    }

    public long getInactiveDisciplineCount() {
        return getTotalDisciplineCount() - getActiveDisciplineCount();
    }

    public int getTotalWorkloadCount() {
        return getAllDisciplines().stream()
                .filter(d -> d.getWorkload() != null)
                .mapToInt(DisciplineDTO::getWorkload)
                .sum();
    }
}