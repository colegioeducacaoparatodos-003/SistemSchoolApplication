package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DisciplineService {

    private static final String CODE_PREFIX = "DISC-";
    private static final int SEQUENCE_LENGTH = 5;

    private final DisciplineRepository repository;

    public DisciplineService(DisciplineRepository repository) {
        this.repository = repository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Discipline save(Discipline discipline) {
        discipline.setDisciplineCode(generateNextCode());
        return repository.save(discipline);
    }

    public void update(DisciplineDTO dto) {
        Discipline discipline = repository.findById(dto.getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + dto.getPkDiscipline()));

        // O código não é editável após a criação — mantém o valor já gravado.
        discipline.setDisciplineName(dto.getDisciplineName());
        discipline.setDescription(dto.getDescription());
        discipline.setStatus(dto.getStatus());
        discipline.setObs(dto.getObs());
        discipline.onUpdate();

        repository.save(discipline);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Disciplina não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO DE CÓDIGO (DISC-ANO-SEQUENCIA)
    // ─────────────────────────────────────────────────────────────

    /**
     * Gera o próximo código sequencial do ano corrente, ex.: DISC-2026-00001.
     * A sequência reinicia a cada ano civil. Depende do FOR UPDATE na query
     * do repositório para evitar colisões em gravações concorrentes.
     */
    private String generateNextCode() {
        String prefix = CODE_PREFIX + Year.now().getValue() + "-";
        Optional<String> lastCode = repository.findLastCodeByPrefixForUpdate(prefix);

        int nextSequence = 1;
        if (lastCode.isPresent()) {
            String sequencePart = lastCode.get().substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                // Código em formato inesperado — segue a partir de 1 para não travar a gravação.
                nextSequence = 1;
            }
        }

        return prefix + String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<DisciplineDTO> getAllDisciplines() {
        return repository.findAllDisciplinesDTO();
    }

    public List<Discipline> getAllActive() {
        return repository.findAllActive();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<DisciplineDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        List<DisciplineDTO> all = getAllDisciplines();
        List<DisciplineDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<DisciplineDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<DisciplineDTO> applyFilters(List<DisciplineDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<DisciplineDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (DisciplineDTO dto : source) {
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
                        case "disciplineCode" -> contains(dto.getDisciplineCode(), expected);
                        case "disciplineName" -> contains(dto.getDisciplineName(), expected);
                        case "description" -> contains(dto.getDescription(), expected);
                        case "status" -> contains(dto.getStatus() != null ? dto.getStatus().name() : null, expected);
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

    private boolean matchesGlobal(DisciplineDTO dto, String globalTerm) {
        return contains(dto.getDisciplineCode(), globalTerm)
                || contains(dto.getDisciplineName(), globalTerm)
                || contains(dto.getDescription(), globalTerm)
                || contains(dto.getStatus() != null ? dto.getStatus().name() : null, globalTerm)
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

    public Discipline getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + id));
    }

    public Discipline findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + id));
    }
}