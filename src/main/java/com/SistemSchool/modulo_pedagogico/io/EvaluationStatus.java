package com.SistemSchool.modulo_pedagogico.io;

public enum EvaluationStatus {

    OPEN("Aberta"),
    CLOSED("Fechada"),
    CANCELLED("Cancelada");

    private final String description;

    EvaluationStatus(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}