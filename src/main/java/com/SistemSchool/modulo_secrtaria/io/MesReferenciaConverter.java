package com.SistemSchool.modulo_secrtaria.io;

import java.util.Locale;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MesReferenciaConverter implements AttributeConverter<MesReferencia, String> {

    @Override
    public String convertToDatabaseColumn(MesReferencia attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public MesReferencia convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = dbData.trim().toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_')
                .replace('.', '_')
                .replace('Ã', 'A')
                .replace('Ç', 'C');

        switch (normalized) {
            case "CONFIRMACAO":
                return MesReferencia.CONFIRMACAO;
            case "MATRICULA":
                return MesReferencia.MATRICULA;
            case "BOLETIM":
                return MesReferencia.BOLETIM;
            case "CARTAO":
                return MesReferencia.CARTAO;
            case "UNIFORME":
                return MesReferencia.UNIFORME;
            case "CERTIFICADO":
                return MesReferencia.CERTIFICADO;
            case "DECLARACAO":
                return MesReferencia.DECLARACAO;
            case "JANEIRO":
                return MesReferencia.JANEIRO;
            case "FEVEREIRO":
                return MesReferencia.FEVEREIRO;
            case "MARCO":
                return MesReferencia.MARCO;
            case "ABRIL":
                return MesReferencia.ABRIL;
            case "MAIO":
                return MesReferencia.MAIO;
            case "JUNHO":
                return MesReferencia.JUNHO;
            case "JULHO":
                return MesReferencia.JULHO;
            case "AGOSTO":
                return MesReferencia.AGOSTO;
            case "SETEMBRO":
                return MesReferencia.SETEMBRO;
            case "OUTUBRO":
                return MesReferencia.OUTUBRO;
            case "NOVEMBRO":
                return MesReferencia.NOVEMBRO;
            case "DEZEMBRO":
                return MesReferencia.DEZEMBRO;
            default:
                return null;
        }
    }
}
