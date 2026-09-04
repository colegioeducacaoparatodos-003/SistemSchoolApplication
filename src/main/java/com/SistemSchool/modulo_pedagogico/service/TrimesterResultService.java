package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.TrimesterResultTableProjection;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.SituationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.model.TrimesterResult;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.EvaluationRepository;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;
import com.SistemSchool.modulo_pedagogico.repository.TrimesterResultRepository;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TrimesterResultService {

    private final TrimesterResultRepository repository;
    private final EnrolmentRepository enrolmentRepository;
    private final DisciplineRepository disciplineRepository;
    private final GradeRepository gradeRepository;
    private final EvaluationRepository evaluationRepository;

    public TrimesterResultService(TrimesterResultRepository repository, EnrolmentRepository enrolmentRepository,
                                  DisciplineRepository disciplineRepository, GradeRepository gradeRepository,
                                  EvaluationRepository evaluationRepository) {
        this.repository = repository;
        this.enrolmentRepository = enrolmentRepository;
        this.disciplineRepository = disciplineRepository;
        this.gradeRepository = gradeRepository;
        this.evaluationRepository = evaluationRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CÁLCULO DE RESULTADOS
    // ─────────────────────────────────────────────────────────────

    public void calcularTrimesterResult(Long enrolmentPk, Long disciplinePk, Trimester trimester) {
        Enrolment enrolment = enrolmentRepository.findById(enrolmentPk)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));
        Discipline discipline = disciplineRepository.findById(disciplinePk)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));

        // Buscar avaliações contínuas
        List<Grade> continuas = gradeRepository.findByEnrolmentDisciplineTrimesterType(
                enrolmentPk, disciplinePk, trimester, EvaluationType.CONTINUA);

        // Buscar prova trimestral
        List<Grade> provas = gradeRepository.findByEnrolmentDisciplineTrimesterType(
                enrolmentPk, disciplinePk, trimester, EvaluationType.PROVA_TRIMESTRAL);

        double mac = 0.0;
        if (!continuas.isEmpty()) {
            double soma = continuas.stream().mapToDouble(Grade::getScore).sum();
            mac = soma / continuas.size();
        }

        double npt = 0.0;
        if (!provas.isEmpty()) {
            npt = provas.get(0).getScore();
        }

        double mt = (mac + npt) / 2.0;

        // Arredondar conforme regra do ensino primário/secundário
        BigDecimal mtRounded = BigDecimal.valueOf(mt).setScale(0, RoundingMode.HALF_UP);

        TrimesterResult result = repository
                .findByEnrolment_PhEnrolmentAndDiscipline_PkDisciplineAndTrimester(enrolmentPk, disciplinePk, trimester)
                .orElse(new TrimesterResult());

        result.setEnrolment(enrolment);
        result.setDiscipline(discipline);
        result.setTrimester(trimester);
        result.setMac(mac);
        result.setNpt(npt);
        result.setMt(mtRounded.doubleValue());

        // Situação
        int maxScore = getMaxScore(enrolment.getSchoolClass());
        double approvalThreshold = maxScore == 20 ? 10.0 : 5.0;
        result.setSituation(mtRounded.doubleValue() >= approvalThreshold ? SituationType.APROVADO : SituationType.REPROVADO);

        // NÃO chamar onCreate/onUpdate manualmente — o JPA @PrePersist cuida disso
        repository.save(result);
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

    public TrimesterResult save(TrimesterResult result) {
        if (result.getEnrolment() == null || result.getEnrolment().getPhEnrolment() == null) {
            throw new RuntimeException("É necessário indicar a matrícula para o resultado.");
        }
        if (result.getDiscipline() == null || result.getDiscipline().getPkDiscipline() == null) {
            throw new RuntimeException("É necessário indicar a disciplina para o resultado.");
        }

        Enrolment enrolment = enrolmentRepository.findById(result.getEnrolment().getPhEnrolment())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));
        result.setEnrolment(enrolment);

        Discipline discipline = disciplineRepository.findById(result.getDiscipline().getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
        result.setDiscipline(discipline);

        return repository.save(result);
    }

    public void update(TrimesterResultDTO dto) {
        TrimesterResult result = repository.findById(dto.getPkTrimesterResult())
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado com id: " + dto.getPkTrimesterResult()));

        result.setMac(dto.getMac());
        result.setNpt(dto.getNpt());
        result.setMt(dto.getMt());
        result.setSituation(dto.getSituation());
        result.setObs(dto.getObs());
        result.onUpdate();

        repository.save(result);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Resultado não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<TrimesterResultDTO> getAllResults() {
        return repository.findAllResultsDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<TrimesterResultDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        List<TrimesterResultDTO> all = getAllResults();
        List<TrimesterResultDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<TrimesterResultDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<TrimesterResultDTO> applyFilters(List<TrimesterResultDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<TrimesterResultDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (TrimesterResultDTO dto : source) {
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
                        case "schoolClassName" -> contains(dto.getSchoolClassName(), expected);
                        case "trimester" -> contains(dto.getTrimester() != null ? dto.getTrimester().toString() : null, expected);
                        case "situation" -> contains(dto.getSituation() != null ? dto.getSituation().toString() : null, expected);
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

    private boolean matchesGlobal(TrimesterResultDTO dto, String globalTerm) {
        return contains(dto.getStudentFullName(), globalTerm)
                || contains(dto.getStudentNumber(), globalTerm)
                || contains(dto.getDisciplineName(), globalTerm)
                || contains(dto.getSchoolClassName(), globalTerm)
                || contains(dto.getTrimester() != null ? dto.getTrimester().toString() : null, globalTerm)
                || contains(dto.getSituation() != null ? dto.getSituation().toString() : null, globalTerm)
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

    public List<TrimesterResult> getByEnrolment(Long enrolmentPk) {
        return repository.findByEnrolment_PhEnrolment(enrolmentPk);
    }

    public List<TrimesterResult> getBySchoolClassAndTrimester(Long schoolClassPk, Trimester trimester) {
        return repository.findBySchoolClassAndTrimester(schoolClassPk, trimester);
    }

    public TrimesterResult getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado com id: " + id));
    }

    public TrimesterResult findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado com id: " + id));
    }
}