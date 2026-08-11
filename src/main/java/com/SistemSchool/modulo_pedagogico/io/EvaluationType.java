package com.SistemSchool.modulo_pedagogico.io;

public enum EvaluationType {

    TEST("Teste"),
    EXAM("Prova"),
    WORK("Trabalho"),
    PROJECT("Projeto"),
    PARTICIPATION("Participação");

    private final String description;

    EvaluationType(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}