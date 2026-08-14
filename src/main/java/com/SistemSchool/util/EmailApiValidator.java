package com.SistemSchool.util;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Valida endereços de email através da API pública e gratuita Disify
 * (https://disify.com/api/email/{email}), que verifica o formato, a
 * existência de registos DNS/MX no domínio e se o email pertence a um
 * serviço de email temporário/descartável.
 *
 * Não requer chave de API.
 */
public final class EmailApiValidator {

    private EmailApiValidator() {
    }

    private static final String API_URL = "https://www.disify.com/api/email/";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
     * Consulta a API pública para validar o email indicado.
     * Em caso de indisponibilidade do serviço ou erro de rede, devolve um
     * resultado inválido com o motivo da falha, para não bloquear
     * indevidamente o utilizador nem assumir que o email é válido.
     */
    public static Resultado validar(String email) {
        if (email == null || email.isBlank()) {
            return new Resultado(false, "O email não foi indicado.");
        }

        String emailNormalizado = email.trim();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + emailNormalizado))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return new Resultado(false,
                        "Não foi possível validar o email neste momento (serviço indisponível).");
            }

            try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                JsonObject json = reader.readObject();

                boolean formatoValido = json.getBoolean("format", false);
                boolean dnsValido = json.getBoolean("dns", false);
                boolean descartavel = json.getBoolean("disposable", false);

                if (!formatoValido) {
                    return new Resultado(false, "O formato do email é inválido.");
                }
                if (!dnsValido) {
                    return new Resultado(false, "O domínio do email não possui registos DNS válidos.");
                }
                if (descartavel) {
                    return new Resultado(false, "Não são permitidos emails temporários/descartáveis.");
                }

                return new Resultado(true, "Email válido.");
            }

        } catch (HttpTimeoutException e) {
            return new Resultado(false, "Tempo limite excedido ao validar o email.");
        } catch (IOException e) {
            return new Resultado(false, "Erro ao contactar o serviço de validação de email.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Resultado(false, "A validação do email foi interrompida.");
        } catch (Exception e) {
            return new Resultado(false, "Erro ao validar o email: " + e.getMessage());
        }
    }
}