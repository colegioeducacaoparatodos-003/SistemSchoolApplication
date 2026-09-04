package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.SituationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.TrimesterResult;

public class TrimesterResultDTO {

    private Long pkTrimesterResult;
    private Long enrolmentPk;
    private String studentFullName;
    private String studentNumber;
    private String schoolClassName;

    private Long disciplinePk;
    private String disciplineName;

    private Trimester trimester;
    private Double mac;
    private Double npt;
    private Double mt;
    private SituationType situation;

    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TrimesterResultDTO() {}

    public TrimesterResultDTO(Long pkTrimesterResult, Long enrolmentPk, String studentFullName, String studentNumber,
                              String schoolClassName, Long disciplinePk, String disciplineName, Trimester trimester,
                              Double mac, Double npt, Double mt, SituationType situation,
                              String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkTrimesterResult = pkTrimesterResult;
        this.enrolmentPk = enrolmentPk;
        this.studentFullName = studentFullName;
        this.studentNumber = studentNumber;
        this.schoolClassName = schoolClassName;
        this.disciplinePk = disciplinePk;
        this.disciplineName = disciplineName;
        this.trimester = trimester;
        this.mac = mac;
        this.npt = npt;
        this.mt = mt;
        this.situation = situation;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TrimesterResultDTO fromEntity(TrimesterResult tr) {
        return new TrimesterResultDTO(
            tr.getPkTrimesterResult(),
            tr.getEnrolment() != null ? tr.getEnrolment().getPhEnrolment() : null,
            tr.getEnrolment() != null && tr.getEnrolment().getStudent() != null
                ? tr.getEnrolment().getStudent().getFullName() : null,
            tr.getEnrolment() != null && tr.getEnrolment().getStudent() != null
                ? tr.getEnrolment().getStudent().getSudentNumber() : null,
            tr.getEnrolment() != null && tr.getEnrolment().getSchoolClass() != null
                ? tr.getEnrolment().getSchoolClass().getClassName() : null,
            tr.getDiscipline() != null ? tr.getDiscipline().getPkDiscipline() : null,
            tr.getDiscipline() != null ? tr.getDiscipline().getDisciplineName() : null,
            tr.getTrimester(), tr.getMac(), tr.getNpt(), tr.getMt(), tr.getSituation(),
            tr.getObs(), tr.getCreatedAt(), tr.getUpdatedAt()
        );
    }

    // Getters e Setters
    public Long getPkTrimesterResult() { return pkTrimesterResult; }
    public void setPkTrimesterResult(Long pkTrimesterResult) { this.pkTrimesterResult = pkTrimesterResult; }

    public Long getEnrolmentPk() { return enrolmentPk; }
    public void setEnrolmentPk(Long enrolmentPk) { this.enrolmentPk = enrolmentPk; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getSchoolClassName() { return schoolClassName; }
    public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

    public Long getDisciplinePk() { return disciplinePk; }
    public void setDisciplinePk(Long disciplinePk) { this.disciplinePk = disciplinePk; }

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

    public Trimester getTrimester() { return trimester; }
    public void setTrimester(Trimester trimester) { this.trimester = trimester; }

    public Double getMac() { return mac; }
    public void setMac(Double mac) { this.mac = mac; }

    public Double getNpt() { return npt; }
    public void setNpt(Double npt) { this.npt = npt; }

    public Double getMt() { return mt; }
    public void setMt(Double mt) { this.mt = mt; }

    public SituationType getSituation() { return situation; }
    public void setSituation(SituationType situation) { this.situation = situation; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}