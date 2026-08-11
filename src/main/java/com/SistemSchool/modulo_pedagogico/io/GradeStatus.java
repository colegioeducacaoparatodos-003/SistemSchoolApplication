package com.SistemSchool.modulo_pedagogico.io;

public enum GradeStatus {

    PENDING("Pendente"),
    RELEASED("Lançada"),
    APPROVED("Aprovada"),
    FAILED("Reprovada");

    private final String description;

    GradeStatus(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}