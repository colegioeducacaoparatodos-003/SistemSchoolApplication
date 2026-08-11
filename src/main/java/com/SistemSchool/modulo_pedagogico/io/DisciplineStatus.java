package com.SistemSchool.modulo_pedagogico.io;

public enum DisciplineStatus {

    ACTIVE("Ativa"),
    INACTIVE("Inativa"),
    ARCHIVED("Arquivada");

    private final String description;

    DisciplineStatus(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

}