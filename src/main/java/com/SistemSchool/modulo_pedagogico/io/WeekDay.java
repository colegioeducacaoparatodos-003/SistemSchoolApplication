package com.SistemSchool.modulo_pedagogico.io;

public enum WeekDay {

    MONDAY("Segunda-feira"),
    TUESDAY("Terça-feira"),
    WEDNESDAY("Quarta-feira"),
    THURSDAY("Quinta-feira"),
    FRIDAY("Sexta-feira"),
    SATURDAY("Sábado");

    private final String description;

    WeekDay(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}