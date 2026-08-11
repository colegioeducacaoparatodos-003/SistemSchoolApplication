package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.repository.TeacherRepository;
import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.ScheduleTableProjection;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

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
public class ScheduleService {

    private final ScheduleRepository repository;
    private final TeacherRepository teacherRepository;
    private final DisciplineRepository disciplineRepository;
    private final SchoolClassRepository schoolClassRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
            TeacherRepository teacherRepository,
            DisciplineRepository disciplineRepository,
            SchoolClassRepository schoolClassRepository) {
        this.repository = scheduleRepository;
        this.teacherRepository = teacherRepository;
        this.disciplineRepository = disciplineRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Schedule save(Schedule schedule) {
        if (schedule.getTeacher() == null || schedule.getTeacher().getPkTeacher() == null) {
            throw new RuntimeException("É necessário indicar o professor do horário.");
        }
        if (schedule.getDiscipline() == null || schedule.getDiscipline().getPkDiscipline() == null) {
            throw new RuntimeException("É necessário indicar a disciplina do horário.");
        }
        if (schedule.getSchoolClass() == null || schedule.getSchoolClass().getPkSchoolClass() == null) {
            throw new RuntimeException("É necessário indicar a turma do horário.");
        }
        if (repository.existsByTeacher_PkTeacherAndWeekDayAndStartTimeAndEndTime(
                schedule.getTeacher().getPkTeacher(), schedule.getWeekDay(),
                schedule.getStartTime(), schedule.getEndTime())) {
            throw new RuntimeException("O professor já possui um horário marcado neste dia e período.");
        }

        Teacher teacher = teacherRepository.findById(schedule.getTeacher().getPkTeacher())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado."));
        Discipline discipline = disciplineRepository.findById(schedule.getDiscipline().getPkDiscipline())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        SchoolClass schoolClass = schoolClassRepository.findById(schedule.getSchoolClass().getPkSchoolClass())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada."));

        schedule.setTeacher(teacher);
        schedule.setDiscipline(discipline);
        schedule.setSchoolClass(schoolClass);

        return repository.save(schedule);
    }

    public void update(ScheduleDTO dto) {
        Schedule schedule = repository.findById(dto.getPkSchedule())
                .orElseThrow(() -> new RuntimeException("Horário não encontrado com id: " + dto.getPkSchedule()));

        if (dto.getTeacherPk() != null
                && !dto.getTeacherPk().equals(schedule.getTeacher().getPkTeacher())) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherPk())
                    .orElseThrow(() -> new RuntimeException("Professor não encontrado."));
            schedule.setTeacher(teacher);
        }

        if (dto.getDisciplinePk() != null
                && !dto.getDisciplinePk().equals(schedule.getDiscipline().getPkDiscipline())) {
            Discipline discipline = disciplineRepository.findById(dto.getDisciplinePk())
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
            schedule.setDiscipline(discipline);
        }

        if (dto.getSchoolClassPk() != null
                && !dto.getSchoolClassPk().equals(schedule.getSchoolClass().getPkSchoolClass())) {
            SchoolClass schoolClass = schoolClassRepository.findById(dto.getSchoolClassPk())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada."));
            schedule.setSchoolClass(schoolClass);
        }

        schedule.setWeekDay(dto.getWeekDay());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setClassroom(dto.getClassroom());

        repository.save(schedule);
    }

    public void delete(Long id) {
        try {
            Schedule schedule = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada"));
            repository.delete(schedule);
            repository.flush(); // força o erro aqui, dentro do try
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível eliminar esta Schedule.");
        }
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
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ScheduleTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new ScheduleDTO(
                p.getPkSchedule(),
                p.getTeacherPk(),
                p.getTeacherName(),
                p.getDisciplinePk(),
                p.getDisciplineName(),
                p.getSchoolClassPk(),
                p.getSchoolClassName(),
                p.getWeekDay() != null ? WeekDay.valueOf(p.getWeekDay()) : null,
                p.getStartTime(),
                p.getEndTime(),
                p.getClassroom(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Schedule> getByTeacher(Long teacherPk) {
        return repository.findByTeacher_PkTeacher(teacherPk);
    }

    public List<Schedule> getByDiscipline(Long disciplinePk) {
        return repository.findByDiscipline_PkDiscipline(disciplinePk);
    }

    public List<Schedule> getBySchoolClass(Long schoolClassPk) {
        return repository.findBySchoolClass_PkSchoolClass(schoolClassPk);
    }

    public List<Schedule> getByWeekDay(WeekDay weekDay) {
        return repository.findByWeekDay(weekDay);
    }

    public Schedule getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado com id: " + id));
    }

    public Schedule findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado com id: " + id));
    }
}