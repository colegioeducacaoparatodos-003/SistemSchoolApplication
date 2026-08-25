package com.SistemSchool.modulo_pedagogico.dto;

import java.io.Serializable;

public class MiniPautaStudentRowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer studentNumber;
    private Long studentPk;
    private String studentFullName;

    // Notas do trimestre corrente
    private Double mac;   // Média Avaliação Contínua
    private Double npp;   // Nota Prova do Professor
    private Double npt;   // Nota Prova Trimestral
    private Double mt;    // Média Trimestral (calculada)

    // Acumulado até ao momento
    private Double mfd;   // Média Final Disciplina

    private String observation;

    public MiniPautaStudentRowDTO() {}

    // Getters / Setters
    public Integer getStudentNumber() { return studentNumber; }
    public void setStudentNumber(Integer studentNumber) { this.studentNumber = studentNumber; }

    public Long getStudentPk() { return studentPk; }
    public void setStudentPk(Long studentPk) { this.studentPk = studentPk; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public Double getMac() { return mac; }
    public void setMac(Double mac) { this.mac = mac; }

    public Double getNpp() { return npp; }
    public void setNpp(Double npp) { this.npp = npp; }

    public Double getNpt() { return npt; }
    public void setNpt(Double npt) { this.npt = npt; }

    public Double getMt() { return mt; }
    public void setMt(Double mt) { this.mt = mt; }

    public Double getMfd() { return mfd; }
    public void setMfd(Double mfd) { this.mfd = mfd; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getMtFormatted() {
        return mt != null ? String.format("%.1f", mt) : "-";
    }

    public String getMfdFormatted() {
        return mfd != null ? String.format("%.1f", mfd) : "-";
    }
}
