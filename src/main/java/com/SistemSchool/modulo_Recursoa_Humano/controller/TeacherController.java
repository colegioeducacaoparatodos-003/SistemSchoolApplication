package com.SistemSchool.modulo_Recursoa_Humano.controller;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.lazy.TeacherLazyModel;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.service.TeacherService;

@Named
@ViewScoped
public class TeacherController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TeacherService teacherService;

    private TeacherLazyModel lazyModel;

    private Teacher teacher;
    private TeacherDTO editDto;
    private Teacher selectedTeacher;
    private Long selectedId;

    private transient UploadedFile uploadedPhoto;

    // ---------------- Filtros ----------------
    private String filterTeacherNumber;
    private String filterName;
    private String filterStatus;

    // ---------------- Estatísticas do cabeçalho ----------------
    private long totalTeacherCount;
    private long activeTeacherCount;
    private long newTeacherCountThisMonth;
    private BigDecimal totalBaseSalary;

    @PostConstruct
    public void init() {
        teacher = new Teacher();
        editDto = new TeacherDTO();
        lazyModel = new TeacherLazyModel(this);
        refreshStats();
    }

    private void refreshStats() {
        totalTeacherCount = teacherService.countTotal();
        activeTeacherCount = teacherService.countActive();
        newTeacherCountThisMonth = teacherService.countNewThisMonth();
        totalBaseSalary = teacherService.sumBaseSalary();
    }

    /*
     * ---------------- LOAD PAGE ----------------
     */
    public String load() {
        lazyModel = new TeacherLazyModel(this);
        refreshStats();
        return "/management/recursohumano/teachers.xhtml?faces-redirect=true";
    }

    /* ---------------- NOVO / EDITAR / VER ---------------- */
    public void prepareNewTeacher() {
        teacher = new Teacher();
        uploadedPhoto = null;
    }

    public void openEditDialog(Long pkTeacher) {
        Teacher found = teacherService.findById(pkTeacher);

        editDto = new TeacherDTO();
        editDto.setPkTeacher(found.getPkTeacher());
        editDto.setFristName(found.getFristName());
        editDto.setLastName(found.getLastName());
        editDto.setGender(found.getGender());
        editDto.setQualificationLivel(found.getQualificationLivel());
        editDto.setContractType(found.getContractType());
        editDto.setStatus(found.getStatus());
        editDto.setBiNumber(found.getBiNumber());
        editDto.setBiExpiryDate(found.getBiExpiryDate());
        editDto.setAddressStreet(found.getAddressStreet());
        editDto.setAddressProvice(found.getAddressProvice());
        editDto.setBaseSalary(found.getBaseSalary());
        editDto.setEmail(found.getEmail());
        editDto.setPhone(found.getPhone());
        editDto.setMobilePhone(found.getMobilePhone());
        editDto.setObs(found.getObs());

        uploadedPhoto = null;
    }

    public void viewTeacherDetails(Long pkTeacher) {
        selectedTeacher = teacherService.findById(pkTeacher);
    }

    /* ---------------- UPLOAD DE FOTO ---------------- */
    public void handlePhotoUpload(FileUploadEvent event) {
        this.uploadedPhoto = event.getFile();
    }

    /* ---------------- CRUD ---------------- */
    public void save() {
        try {
            // Validação de campos obrigatórios
            if (teacher.getFristName() == null || teacher.getFristName().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Professor", "O primeiro nome é obrigatório");
                return;
            }
            if (teacher.getLastName() == null || teacher.getLastName().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Professor", "O último nome é obrigatório");
                return;
            }
            if (teacher.getQualificationLivel() == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Professor", "Selecione o nível de qualificação");
                return;
            }
            if (teacher.getContractType() == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Professor", "Selecione o tipo de vínculo");
                return;
            }
            if (teacher.getStatus() == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Professor", "Selecione o estado");
                return;
            }
            
            System.out.println("Iniciando salvamento do professor: " + teacher.getFristName() + " " + teacher.getLastName());
            teacherService.save(teacher, uploadedPhoto);
            teacher = new Teacher();
            uploadedPhoto = null;
            lazyModel = new TeacherLazyModel(this);
            refreshStats();
            addMessage(FacesMessage.SEVERITY_INFO, "Professor", "Professor registado com sucesso");
            System.out.println("Professor registado com sucesso");
        } catch (IOException e) {
            System.err.println("Erro de I/O ao salvar professor: " + e.getMessage());
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", "Erro ao processar a imagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado ao salvar professor: " + e.getMessage());
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", "Erro ao registar o professor: " + e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            System.out.println("Iniciando atualização do professor: " + editDto.getPkTeacher());
            teacherService.update(editDto);

            if (uploadedPhoto != null && uploadedPhoto.getSize() > 0) {
                System.out.println("Atualizando foto do professor...");
                teacherService.updatePhoto(editDto.getPkTeacher(), uploadedPhoto);
            }

            lazyModel = new TeacherLazyModel(this);
            editDto = new TeacherDTO();
            uploadedPhoto = null;
            refreshStats();

            addMessage(FacesMessage.SEVERITY_INFO, "Professor", "Professor atualizado com sucesso");
            System.out.println("Professor atualizado com sucesso");
        } catch (IOException e) {
            System.err.println("Erro de I/O ao atualizar professor: " + e.getMessage());
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", "Erro ao processar a imagem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao atualizar professor: " + e.getMessage());
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", "Erro ao atualizar o professor: " + e.getMessage());
        }
    }

    public void delete(Long pkTeacher) {
        try {
            teacherService.delete(pkTeacher);
            lazyModel = new TeacherLazyModel(this);
            refreshStats();
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", e.getMessage());
        }
    }

    /* ---------------- FILTROS ---------------- */
    public void applyFilters() {
        // O recarregamento é feito automaticamente pelo PrimeFaces
        // ao atualizar a p:dataTable (update="dtTeachers") após o ajax.
    }

    public void clearFilters() {
        filterTeacherNumber = null;
        filterName = null;
        filterStatus = null;
    }

    /* ---------------- EXPORTAÇÃO ---------------- */
    public void exportTeacherListPdf() {
        try {
            teacherService.exportTeacherListPdf(filterTeacherNumber, filterName, filterStatus);
        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Professor", "Erro ao gerar o PDF da lista de professores");
        }
    }

    /* ---------------- OPÇÕES PARA OS COMBOS (p:selectOneMenu) ---------------- */
    public Gender[] getGenders() {
        return Gender.values();
    }

    public QualificationLevel[] getQualificationLevels() {
        return QualificationLevel.values();
    }

    public ContractType[] getContractTypes() {
        return ContractType.values();
    }

    public TeacherStatus[] getTeacherStatuses() {
        return TeacherStatus.values();
    }

    public String[] getStatuses() {
        TeacherStatus[] values = TeacherStatus.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    /* ---------------- UTIL ---------------- */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    /* ---------------- GETTERS E SETTERS ---------------- */
    public TeacherLazyModel getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(TeacherLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public TeacherDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(TeacherDTO editDto) {
        this.editDto = editDto;
    }

    public Teacher getSelectedTeacher() {
        return selectedTeacher;
    }

    public void setSelectedTeacher(Teacher selectedTeacher) {
        this.selectedTeacher = selectedTeacher;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public UploadedFile getUploadedPhoto() {
        return uploadedPhoto;
    }

    public void setUploadedPhoto(UploadedFile uploadedPhoto) {
        this.uploadedPhoto = uploadedPhoto;
    }

    public TeacherService getTeacherService() {
        return teacherService;
    }

    public void setTeacherService(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    public String getFilterTeacherNumber() {
        return filterTeacherNumber;
    }

    public void setFilterTeacherNumber(String filterTeacherNumber) {
        this.filterTeacherNumber = filterTeacherNumber;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }

    public long getTotalTeacherCount() {
        return totalTeacherCount;
    }

    public long getActiveTeacherCount() {
        return activeTeacherCount;
    }

    public long getNewTeacherCountThisMonth() {
        return newTeacherCountThisMonth;
    }

    public BigDecimal getTotalBaseSalary() {
        return totalBaseSalary;
    }

}