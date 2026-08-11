package com.SistemSchool.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    // Novo: Formato com data no nome do arquivo
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Diretório fixo onde os logs serão gravados
    private static final String LOG_DIR = "logs";

    public static void logInfo(String message) {
        log("INFO", message);
    }

    public static void logError(String message) {
        log("ERROR", message);
    }

    public static void logWarning(String message) {
        log("WARNING", message);
    }

    private static synchronized void log(String level, String message) {
        String timestamp = "[" + LocalDateTime.now().format(LOG_FORMAT) + "]";
        String logMessage = String.format("%s %s: %s%n", timestamp, level, message);

        // Cria o diretório se ele não existir
        File logDirectory = new File(LOG_DIR);
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }

        // Define o caminho completo do arquivo de log
        String fileName = LOG_DIR + File.separator + "log-" + LocalDate.now().format(FILE_DATE_FORMAT) + ".txt";
        System.out.println("Gravando log em: " + fileName);

        try (FileWriter writer = new FileWriter(fileName, true)) { // append = true
            writer.write(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
