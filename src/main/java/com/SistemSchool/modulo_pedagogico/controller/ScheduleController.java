package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.lazy.ScheduleLazyModel;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.ScheduleService;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ScheduleController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ScheduleController.class.getName());

    // ── MODELOS ──
    private Schedule schedule = new Schedule();
    private ScheduleDTO editDto = new ScheduleDTO();
    private ScheduleDTO selectedSchedule = new ScheduleDTO();
    private Long selectedId;
    private Long selectedDisciplineId;
    private Long selectedSchoolClassId;
    private Long selectedTeacherId;

    // ── FILTROS AVANÇADOS ──
    private Long filterDisciplineId;
    private Long filterSchoolClassId;
    private String filterWeekDay;
    private String filterAnoLectivo;

    // ── LISTAS ──
    private List<Discipline> disciplines = new ArrayList<>();
    private List<SchoolClass> schoolClasses = new ArrayList<>();
    private List<Teacher> teachers = new ArrayList<>();

    // ── ESTATÍSTICAS ──
    private long totalScheduleCount;
    private long distinctDisciplinesCount;
    private long distinctClassesCount;

    // ── SERVIÇOS ──
    @Inject
    private ScheduleService scheduleService;
    @Inject
    private DisciplineService disciplineService;
    @Inject
    private SchoolClassRepository schoolClassRepository;
    private transient ScheduleLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new ScheduleLazyModel(scheduleService);
        loadDisciplines();
        loadSchoolClasses();
        loadTeachers();
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

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterDisciplineId = null;
        filterSchoolClassId = null;
        filterWeekDay = null;
        filterAnoLectivo = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    public List<ScheduleDTO> getFilteredSchedules() {
        List<ScheduleDTO> all = scheduleService.getAllSchedules();

        return all.stream()
                .filter(s -> filterDisciplineId == null ||
                        (s.getDisciplinePk() != null && s.getDisciplinePk().equals(filterDisciplineId)))
                .filter(s -> filterSchoolClassId == null ||
                        (s.getSchoolClassPk() != null && s.getSchoolClassPk().equals(filterSchoolClassId)))
                .filter(s -> filterWeekDay == null || filterWeekDay.isBlank() ||
                        (s.getWeekDay() != null && s.getWeekDay().toString().equalsIgnoreCase(filterWeekDay)))
                .filter(s -> filterAnoLectivo == null || filterAnoLectivo.isBlank() ||
                        (s.getAnoLectivo() != null && s.getAnoLectivo().contains(filterAnoLectivo)))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllActive();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
        }
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassRepository.findAll();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas", e);
        }
    }

    private void loadTeachers() {
        try {
            teachers = scheduleService.getAllActiveTeachers();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar professores", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar professores", e);
        }
    }

    private void computeStatistics() {
        try {
            List<ScheduleDTO> all = getFilteredSchedules();
            totalScheduleCount = all.size();
            distinctDisciplinesCount = all.stream().map(ScheduleDTO::getDisciplinePk).distinct().count();
            distinctClassesCount = all.stream().map(ScheduleDTO::getSchoolClassPk).distinct().count();
        } catch (Exception e) {
            totalScheduleCount = 0;
            distinctDisciplinesCount = 0;
            distinctClassesCount = 0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    public void prepareNewSchedule() {
        schedule = new Schedule();
        selectedDisciplineId = null;
        selectedSchoolClassId = null;
        selectedTeacherId = null;
        loadDisciplines();
        loadSchoolClasses();
        loadTeachers();
    }

    public String saveSchedule() {
        try {
            if (selectedDisciplineId == null || selectedSchoolClassId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Horário", "Preencha todos os campos obrigatórios.");
                return null;
            }
            Discipline discipline = disciplines.stream()
                    .filter(d -> selectedDisciplineId.equals(d.getPkDiscipline()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
            schedule.setDiscipline(discipline);

            SchoolClass schoolClass = schoolClasses.stream()
                    .filter(sc -> selectedSchoolClassId.equals(sc.getPkSchoolClass()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Turma não encontrada."));
            schedule.setSchoolClass(schoolClass);

            if (selectedTeacherId != null) {
                Teacher teacher = teachers.stream()
                        .filter(t -> selectedTeacherId.equals(t.getPkTeacher()))
                        .findFirst().orElseThrow(() -> new RuntimeException("Professor não encontrado."));
                schedule.setTeacher(teacher);
            } else {
                schedule.setTeacher(null);
            }

            scheduleService.save(schedule);
            schedule = new Schedule();
            selectedDisciplineId = null;
            selectedSchoolClassId = null;
            selectedTeacherId = null;
            init();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Horário registado com sucesso");
            return "/management/pedagogico/schedules.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar horário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
            return null;
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum horário selecionado");
            return;
        }
        this.selectedId = id;
        ScheduleDTO dto = scheduleService.getAllSchedules().stream()
                .filter(s -> id.equals(s.getPkSchedule())).findFirst().orElse(null);
        if (dto != null) {
            mapDtoFields(dto, editDto = new ScheduleDTO());
            mapDtoFields(dto, selectedSchedule);
            selectedDisciplineId = dto.getDisciplinePk();
            selectedSchoolClassId = dto.getSchoolClassPk();
            selectedTeacherId = dto.getTeacherPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Horário não encontrado");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum horário selecionado");
            return;
        }
        this.selectedId = id;
    }

    public void deleteSchedule() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum horário selecionado para eliminar");
            return;
        }
        try {
            scheduleService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Horário eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar horário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            if (selectedDisciplineId != null)
                editDto.setDisciplinePk(selectedDisciplineId);
            if (selectedSchoolClassId != null)
                editDto.setSchoolClassPk(selectedSchoolClassId);
            editDto.setTeacherPk(selectedTeacherId);
            scheduleService.update(editDto);
            init();
            editDto = new ScheduleDTO();
            selectedId = null;
            selectedDisciplineId = null;
            selectedSchoolClassId = null;
            selectedTeacherId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Horário atualizado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar horário", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void viewScheduleDetails(Long id) {
        if (id == null) return;
        ScheduleDTO dto = scheduleService.getAllSchedules().stream()
                .filter(s -> id.equals(s.getPkSchedule())).findFirst().orElse(null);
        if (dto != null) {
            this.selectedSchedule = dto;
            this.selectedId = id;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

    private void mapDtoFields(ScheduleDTO source, ScheduleDTO target) {
        target.setPkSchedule(source.getPkSchedule());
        target.setDisciplinePk(source.getDisciplinePk());
        target.setDisciplineName(source.getDisciplineName());
        target.setSchoolClassPk(source.getSchoolClassPk());
        target.setSchoolClassName(source.getSchoolClassName());
        target.setSchoolClassCode(source.getSchoolClassCode());
        target.setTeacherPk(source.getTeacherPk());
        target.setTeacherName(source.getTeacherName());
        target.setWeekDay(source.getWeekDay());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setAnoLectivo(source.getAnoLectivo());
        target.setObs(source.getObs());
    }

    /**
     * Adiciona mensagem ao FacesContext se estiver dentro de uma requisição JSF.
     * Fora de requisição (ex: @PostConstruct no startup), loga no console.
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(severity, summary, detail));
        } else {
            Level level = (severity == FacesMessage.SEVERITY_ERROR) ? Level.SEVERE : Level.INFO;
            LOGGER.log(level, "[JSF Message - no context] " + summary + ": " + detail);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public ScheduleDTO getEditDto() { return editDto; }
    public void setEditDto(ScheduleDTO editDto) { this.editDto = editDto; }
    public ScheduleDTO getSelectedSchedule() { return selectedSchedule; }
    public void setSelectedSchedule(ScheduleDTO selectedSchedule) { this.selectedSchedule = selectedSchedule; }
    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }
    public Long getSelectedDisciplineId() { return selectedDisciplineId; }
    public void setSelectedDisciplineId(Long selectedDisciplineId) { this.selectedDisciplineId = selectedDisciplineId; }
    public Long getSelectedSchoolClassId() { return selectedSchoolClassId; }
    public void setSelectedSchoolClassId(Long selectedSchoolClassId) { this.selectedSchoolClassId = selectedSchoolClassId; }
    public Long getSelectedTeacherId() { return selectedTeacherId; }
    public void setSelectedTeacherId(Long selectedTeacherId) { this.selectedTeacherId = selectedTeacherId; }
    public ScheduleLazyModel getLazyModel() { return lazyModel; }
    public void setLazyModel(ScheduleLazyModel lazyModel) { this.lazyModel = lazyModel; }
    public Long getFilterDisciplineId() { return filterDisciplineId; }
    public void setFilterDisciplineId(Long filterDisciplineId) { this.filterDisciplineId = filterDisciplineId; }
    public Long getFilterSchoolClassId() { return filterSchoolClassId; }
    public void setFilterSchoolClassId(Long filterSchoolClassId) { this.filterSchoolClassId = filterSchoolClassId; }
    public String getFilterWeekDay() { return filterWeekDay; }
    public void setFilterWeekDay(String filterWeekDay) { this.filterWeekDay = filterWeekDay; }
    public String getFilterAnoLectivo() { return filterAnoLectivo; }
    public void setFilterAnoLectivo(String filterAnoLectivo) { this.filterAnoLectivo = filterAnoLectivo; }
    public long getTotalScheduleCount() { return totalScheduleCount; }
    public long getDistinctDisciplinesCount() { return distinctDisciplinesCount; }
    public long getDistinctClassesCount() { return distinctClassesCount; }
    public List<Discipline> getDisciplines() { return disciplines; }
    public List<SchoolClass> getSchoolClasses() { return schoolClasses; }
    public List<Teacher> getTeachers() { return teachers; }
    public WeekDay[] getWeekDays() { return WeekDay.values(); }
    public List<ScheduleDTO> getSchedules() { return scheduleService.getAllSchedules(); }
}