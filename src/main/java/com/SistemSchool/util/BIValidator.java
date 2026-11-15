package com.SistemSchool.util;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

public final class BIValidator {

    private BIValidator() {
    }

    private static final String BI_BASE_URL = "https://consulta.edgarsingui.ao/consultar/%s/bilhete";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // 9 dígitos + 2 letras + 3 dígitos
    private static final Pattern BI_PATTERN =
            Pattern.compile("^\\d{9}[A-Z]{2}\\d{3}$");

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

    public static boolean isValid(String bi) {
        if (bi == null || bi.isBlank()) {
            return false;
        }
        return BI_PATTERN.matcher(bi.trim().toUpperCase()).matches();
    }

    public static Resultado validarOnline(String bi) {
        if (bi == null || bi.isBlank()) {
            return new Resultado(false, "O número de BI não foi indicado.");
        }

        String biNormalizado = bi.trim().toUpperCase();
        if (!isValid(biNormalizado)) {
            return new Resultado(false,
                    "Número de BI inválido. Formato esperado: 9 dígitos + 2 letras + 3 dígitos (ex.: 123456789LA041).");
        }

        try {
            String encodedBi = URLEncoder.encode(biNormalizado, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(BI_BASE_URL, encodedBi)))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return new Resultado(false,
                        "Não foi possível verificar o BI no serviço oficial neste momento.");
            }

            try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                JsonObject json = reader.readObject();
                boolean erro = json.containsKey("error") && json.getBoolean("error", false);
                boolean nomeValido = json.containsKey("name") && !json.isNull("name")
                        && !json.getString("name", "").isBlank();

                if (erro) {
                    return new Resultado(false, "Este número de BI não foi encontrado no registo oficial.");
                }

                if (!nomeValido) {
                    return new Resultado(false, "O número de BI não corresponde a um documento válido.");
                }

                return new Resultado(true, "BI validado com sucesso no registo oficial.");
            }

        } catch (HttpTimeoutException e) {
            return new Resultado(false, "Tempo limite excedido ao validar o BI no serviço oficial.");
        } catch (IOException e) {
            return new Resultado(false, "Erro ao contactar o serviço oficial de validação do BI.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Resultado(false, "A validação do BI foi interrompida.");
        } catch (Exception e) {
            return new Resultado(false, "Erro ao validar o BI: " + e.getMessage());
        }
    }
}