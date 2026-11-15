package com.SistemSchool.util;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * Valida localmente o formato de endereços de email. A confirmação enviada
 * durante o registo verifica se o utilizador consegue receber mensagens.
 */
public final class EmailValidator {

    private EmailValidator() {
    }

    public static final class Resultado {

        private final boolean valido;
        private final String motivo;

        public Resultado(boolean valido, String motivo) {
            this.valido = valido;
            this.motivo = motivo;
        }

        public boolean isValido() {
            return valido;
        }

        public String getMotivo() {
            return motivo;
        }
    }

    /**
     * Verifica sintaxe sem enviar o endereço para um serviço externo.
     */
    public static Resultado validar(String email) {
        if (email == null || email.isBlank()) {
            return new Resultado(false, "O email não foi indicado.");
        }

        try {
            String normalizedEmail = email.trim();
            if (normalizedEmail.length() > 254) {
                return new Resultado(false, "O email excede o tamanho máximo permitido.");
            }
            InternetAddress address = new InternetAddress(normalizedEmail, true);
            address.validate();
            String parsedAddress = address.getAddress();
            int atIndex = parsedAddress == null ? -1 : parsedAddress.lastIndexOf('@');
            if (atIndex <= 0 || atIndex == parsedAddress.length() - 1) {
                return new Resultado(false, "O formato do email é inválido.");
            }
            String domain = parsedAddress.substring(atIndex + 1);
            if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
                return new Resultado(false, "Indique um domínio de email válido.");
            }
            return new Resultado(true, "Formato do email válido.");
        } catch (AddressException e) {
            return new Resultado(false, "O formato do email é inválido.");
        }
    }
}