package com.SistemSchool.modulo_secrtaria.io;

public enum MesReferencia {

    CONFIRMACAO("Confirmação"),
    MATRICULA("Matrícula"),
    BOLETIN("Boletim"),
    CARTAO("Cartão"),
    JANEIRO("Janeiro"),
    FEVEREIRO("Fevereiro"),
    MARCO("Março"),
    ABRIL("Abril"),
    MAIO("Maio"),
    JUNHO("Junho"),
    JULHO("Julho"),
    AGOSTO("Agosto"),
    SETEMBRO("Setembro"),
    OUTUBRO("Outubro"),
    NOVEMBRO("Novembro"),
    DEZEMBRO("Dezembro");

    private final String descricao;

    MesReferencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Indica se este item de referência corresponde a uma mensalidade (mês letivo).
     * Itens avulsos (Confirmação, Matrícula, Boletim, Cartão) NUNCA sofrem multa,
     * independentemente da data de pagamento.
     */
    public boolean isMensalidade() {
        switch (this) {
            case JANEIRO:
            case FEVEREIRO:
            case MARCO:
            case ABRIL:
            case MAIO:
            case JUNHO:
            case JULHO:
            case AGOSTO:
            case SETEMBRO:
            case OUTUBRO:
            case NOVEMBRO:
            case DEZEMBRO:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return descricao;
    }
}