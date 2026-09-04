package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.repository.TeacherRepository;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

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
public class ScheduleService {

    private final ScheduleRepository repository;
    private final DisciplineRepository disciplineRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeacherRepository teacherRepository;

    public ScheduleService(ScheduleRepository repository, DisciplineRepository disciplineRepository,
                           SchoolClassRepository schoolClassRepository, TeacherRepository teacherRepository) {
        this.repository = repository;
        this.disciplineRepository = disciplineRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.teacherRepository = teacherRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Schedule save(Schedule schedule) {
        if (schedule.getDiscipline() == null || schedule.getDiscipline().getPkDiscipline() == null) {
            throw new RuntimeException("É necessário indicar a disciplina para o horário.");
        }
        if (schedule.getSchoolClass() == null || schedule.getSchoolClass().getPkSchoolClass() == null) {
            throw new RuntimeException("É necessário indicar a turma para o horário.");
        }

        Discipline discipline = disciplineRepository.findById(schedule.getDiscipline().getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + schedule.getDiscipline().getPkDiscipline()));
        schedule.setDiscipline(discipline);

        SchoolClass schoolClass = schoolClassRepository.findById(schedule.getSchoolClass().getPkSchoolClass())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + schedule.getSchoolClass().getPkSchoolClass()));
        schedule.setSchoolClass(schoolClass);

        if (schedule.getTeacher() != null && schedule.getTeacher().getPkTeacher() != null) {
            Teacher teacher = teacherRepository.findById(schedule.getTeacher().getPkTeacher())
                    .orElseThrow(() -> new RuntimeException("Professor não encontrado com id: " + schedule.getTeacher().getPkTeacher()));
            schedule.setTeacher(teacher);
        } else {
            schedule.setTeacher(null);
        }

        return repository.save(schedule);
    }

    public void update(ScheduleDTO dto) {
        Schedule schedule = repository.findById(dto.getPkSchedule())
                .orElseThrow(() -> new RuntimeException("Horário não encontrado com id: " + dto.getPkSchedule()));

        if (dto.getDisciplinePk() != null && !dto.getDisciplinePk().equals(schedule.getDiscipline().getPkDiscipline())) {
            Discipline discipline = disciplineRepository.findById(dto.getDisciplinePk())
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + dto.getDisciplinePk()));
            schedule.setDiscipline(discipline);
        }

        if (dto.getSchoolClassPk() != null && !dto.getSchoolClassPk().equals(schedule.getSchoolClass().getPkSchoolClass())) {
            SchoolClass schoolClass = schoolClassRepository.findById(dto.getSchoolClassPk())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + dto.getSchoolClassPk()));
            schedule.setSchoolClass(schoolClass);
        }

        if (dto.getTeacherPk() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherPk())
                    .orElseThrow(() -> new RuntimeException("Professor não encontrado com id: " + dto.getTeacherPk()));
            schedule.setTeacher(teacher);
        } else {
            schedule.setTeacher(null);
        }

        schedule.setWeekDay(dto.getWeekDay());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setAnoLectivo(dto.getAnoLectivo());
        schedule.setObs(dto.getObs());
        schedule.onUpdate();

        repository.save(schedule);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Horário não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<ScheduleDTO> getAllSchedules() {
        return repository.findAllSchedulesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<ScheduleDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        List<ScheduleDTO> all = getAllSchedules();
        List<ScheduleDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<ScheduleDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<ScheduleDTO> applyFilters(List<ScheduleDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<ScheduleDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (ScheduleDTO dto : source) {
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
                        case "schoolclassnome" -> contains(dto.getSchoolClassName(), expected);
                        case "schoolclasscode" -> contains(dto.getSchoolClassCode(), expected);
                        case "teacherName" -> contains(dto.getTeacherName(), expected);
                        case "weekDay" -> contains(dto.getWeekDay() != null ? dto.getWeekDay().name() : null, expected);
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

    private boolean matchesGlobal(ScheduleDTO dto, String globalTerm) {
        return contains(dto.getDisciplineName(), globalTerm)
                || contains(dto.getSchoolClassName(), globalTerm)
                || contains(dto.getSchoolClassCode(), globalTerm)
                || contains(dto.getTeacherName(), globalTerm)
                || contains(dto.getWeekDay() != null ? dto.getWeekDay().name() : null, globalTerm)
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

    public List<Schedule> getBySchoolClass(Long schoolClassPk) {
        return repository.findBySchoolClass_PkSchoolClass(schoolClassPk);
    }

    public List<Schedule> getByDiscipline(Long disciplinePk) {
        return repository.findByDiscipline_PkDiscipline(disciplinePk);
    }

    public List<Schedule> getByAnoLectivo(String anoLectivo) {
        return repository.findByAnoLectivo(anoLectivo);
    }

    public List<Schedule> getByTeacher(Long teacherPk) {
        return repository.findByTeacher_PkTeacher(teacherPk);
    }

    public Schedule getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado com id: " + id));
    }

    public Schedule findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado com id: " + id));
    }

    public List<Teacher> getAllActiveTeachers() {
        return teacherRepository.findAll();
    }
}