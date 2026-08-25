package com.SistemSchool.modulo_pedagogico.io;

public enum EvaluationType {

    CONTINUOUS_ASSESSMENT("MAC – Média Avaliação Contínua"),
    TEACHER_TEST("NPP – Nota Prova do Professor"),
    FINAL_TEST("NPT – Nota Prova Trimestral"),
    TEST("Teste"),
    EXAM("Prova"),
    WORK("Trabalho"),
    PROJECT("Projeto"),
    PARTICIPATION("Participação");

    private final String description;

    EvaluationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Tipos que compõem a média trimestral (MT) no sistema angolano.
     */
    public static boolean isTrimesterComponent(EvaluationType type) {
        return type == CONTINUOUS_ASSESSMENT
            || type == TEACHER_TEST
            || type == FINAL_TEST;
    }
}
