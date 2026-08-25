package com.SistemSchool.modulo_secrtaria.io;

public enum EnrolmentType {

    ENROLMENT("Matrícula"),
    CONFIRMATION("Confirmação");

    private final String descricao;

    EnrolmentType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isENROLMENT(){
        return this == ENROLMENT;
    }

    public boolean isCONFIRMATION(){
        return this == CONFIRMATION;
    }
}