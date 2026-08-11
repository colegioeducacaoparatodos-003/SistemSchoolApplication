package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.service.TeacherService;
import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.lazy.ScheduleLazyModel;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.ScheduleService;
import com.SistemSchool.modulo_secrtaria.dto.SchoolClassDTO;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.service.SchoolClassService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ScheduleController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ScheduleController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Schedule schedule = new Schedule();

    private ScheduleDTO editDto = new ScheduleDTO();
    private Long selectedId;

    // Ids escolhidos nos dropdowns do formulário
    private Long selectedTeacherId;
    private Long selectedDisciplineId;
    private Long selectedSchoolClassId;

    // Listas para a view (dropdowns), carregadas uma vez
    private List<TeacherDTO> teachers = new java.util.ArrayList<>();
    private List<DisciplineDTO> disciplines = new java.util.ArrayList<>();
    private List<SchoolClassDTO> schoolClasses = new java.util.ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalScheduleCount;
    private long todayScheduleCount;
    private long activeTeachersCount;
    private long classroomsInUseCount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private TeacherService teacherService;

    @Inject
    private DisciplineService disciplineService;

    @Inject
    private SchoolClassService schoolClassService;

    private transient ScheduleLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new ScheduleLazyModel(scheduleService);
        loadTeachers();
        loadDisciplines();
        loadSchoolClasses();
        computeStatistics();
    }

    
    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar horários", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de horários", e);
        }
        return "/management/pedagogico/schedules.xhtml?faces-redirect=true";
    }
    private void loadTeachers() {
        try {
            teachers = teacherService.getAllTeachers();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar professores", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar professores para o formulário de horário", e);
        }
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllDisciplines();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas para o formulário de horário", e);
        }
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassService.getAllSchoolClasses();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas para o formulário de horário", e);
        }
    }

    private void computeStatistics() {
        try {
            List<ScheduleDTO> all = scheduleService.getAllSchedules();

            totalScheduleCount = all.size();

            WeekDay today = mapToWeekDay(LocalDate.now().getDayOfWeek());
            todayScheduleCount = today == null ? 0 : all.stream()
                    .filter(s -> s.getWeekDay() == today)
                    .count();

            activeTeachersCount = all.stream()
                    .map(ScheduleDTO::getTeacherPk)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet())
                    .size();

            Set<String> classrooms = all.stream()
                    .map(ScheduleDTO::getClassroom)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toSet());
            classroomsInUseCount = classrooms.size();

        } catch (Exception e) {
            totalScheduleCount = 0;
            todayScheduleCount = 0;
            activeTeachersCount = 0;
            classroomsInUseCount = 0;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de horários", e);
        }
    }

    private WeekDay mapToWeekDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> WeekDay.MONDAY;
            case TUESDAY -> WeekDay.TUESDAY;
            case WEDNESDAY -> WeekDay.WEDNESDAY;
            case THURSDAY -> WeekDay.THURSDAY;
            case FRIDAY -> WeekDay.FRIDAY;
            case SATURDAY -> WeekDay.SATURDAY;
            case SUNDAY -> null; // sem expediente
        };
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD — CREATE
    // ─────────────────────────────────────────────────────────────

    public void saveSchedule() {
        try {
            if (selectedTeacherId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Horário", "Selecione um professor antes de gravar.");
                return;
            }
            if (selectedDisciplineId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Horário", "Selecione uma disciplina antes de gravar.");
                return;
            }
            if (selectedSchoolClassId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Horário", "Selecione uma turma antes de gravar.");
                return;
            }

            Teacher teacher = teacherService.getById(selectedTeacherId);
            Discipline discipline = disciplineService.getById(selectedDisciplineId);
            SchoolClass schoolClass = schoolClassService.getById(selectedSchoolClassId);

            schedule.setTeacher(teacher);
            schedule.setDiscipline(discipline);
            schedule.setSchoolClass(schoolClass);

            scheduleService.save(schedule);

            schedule = new Schedule();
            selectedTeacherId = null;
            selectedDisciplineId = null;
            selectedSchoolClassId = null;

            init();

            addMessage(FacesMessage.SEVERITY_INFO, "Horário", "Horário registado com sucesso");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar horário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Horário", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhum horário selecionado!", "");
            return;
        }

        ScheduleDTO dto = scheduleService.getAllSchedules()
                .stream()
                .filter(s -> selectedId.equals(s.getPkSchedule()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            editDto = dto;
            selectedTeacherId = dto.getTeacherPk();
            selectedDisciplineId = dto.getDisciplinePk();
            selectedSchoolClassId = dto.getSchoolClassPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Horário não encontrado", "");
        }
    }

    public void saveUpdate() {
        try {
            if (selectedTeacherId != null) {
                editDto.setTeacherPk(selectedTeacherId);
            }
            if (selectedDisciplineId != null) {
                editDto.setDisciplinePk(selectedDisciplineId);
            }
            if (selectedSchoolClassId != null) {
                editDto.setSchoolClassPk(selectedSchoolClassId);
            }

            scheduleService.update(editDto);

            editDto = new ScheduleDTO();
            selectedId = null;
            selectedTeacherId = null;
            selectedDisciplineId = null;
            selectedSchoolClassId = null;

            init();

            addMessage(FacesMessage.SEVERITY_INFO, "Horário", "Horário atualizado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar horário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Horário", e.getMessage());
        }
    }

    public void delete() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum horário selecionado!", "");
            return;
        }
        try {
            scheduleService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Horário", "Horário eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar horário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Horário", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UTIL
    // ─────────────────────────────────────────────────────────────

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS E SETTERS
    // ─────────────────────────────────────────────────────────────

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public ScheduleDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(ScheduleDTO editDto) {
        this.editDto = editDto;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public Long getSelectedTeacherId() {
        return selectedTeacherId;
    }

    public void setSelectedTeacherId(Long selectedTeacherId) {
        this.selectedTeacherId = selectedTeacherId;
    }

    public Long getSelectedDisciplineId() {
        return selectedDisciplineId;
    }

    public void setSelectedDisciplineId(Long selectedDisciplineId) {
        this.selectedDisciplineId = selectedDisciplineId;
    }

    public Long getSelectedSchoolClassId() {
        return selectedSchoolClassId;
    }

    public void setSelectedSchoolClassId(Long selectedSchoolClassId) {
        this.selectedSchoolClassId = selectedSchoolClassId;
    }

    public void setLazyModel(ScheduleLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public ScheduleLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS — GETTERS
    // ─────────────────────────────────────────────────────────────

    public long getTotalScheduleCount() {
        return totalScheduleCount;
    }

    public long getTodayScheduleCount() {
        return todayScheduleCount;
    }

    public long getActiveTeachersCount() {
        return activeTeachersCount;
    }

    public long getClassroomsInUseCount() {
        return classroomsInUseCount;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS E LISTAS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public WeekDay[] getWeekDays() {
        return WeekDay.values();
    }

    public List<TeacherDTO> getTeachers() {
        return teachers;
    }

    public List<DisciplineDTO> getDisciplines() {
        return disciplines;
    }

    public List<SchoolClassDTO> getSchoolClasses() {
        return schoolClasses;
    }

    public void refreshTeachers() {
        loadTeachers();
    }

    public void refreshDisciplines() {
        loadDisciplines();
    }

    public void refreshSchoolClasses() {
        loadSchoolClasses();
    }

    public List<ScheduleDTO> getSchedules() {
        return scheduleService.getAllSchedules();
    }
}