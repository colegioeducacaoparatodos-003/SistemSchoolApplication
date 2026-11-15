package com.SistemSchool.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String appUrl;
    private final String sender;

    public EmailService(JavaMailSender mailSender, @Value("${app.url:http://localhost:8080}") String appUrl,
            @Value("${spring.mail.username}") String sender) {
        this.mailSender = mailSender;
        this.appUrl = appUrl.replaceAll("/$", "");
        this.sender = sender;
    }

    public void sendVerificationEmail(String recipient, String token) {
        String link = appUrl + "/verify-email.xhtml?token=" + encodeToken(token);
        send(recipient, "Confirme o seu email - Gestão Escolar",
                "Bem-vindo à Gestão Escolar.\n\n"
                        + "Confirme o seu email através do link abaixo:\n" + link + "\n\n"
                        + "Este link expira em 24 horas.\n"
                        + "Se não criou esta conta, ignore esta mensagem.");
    }

    public void sendPasswordResetEmail(String recipient, String token) {
        String link = appUrl + "/components/dashboard/recuperar_senha.xhtml?token=" + encodeToken(token);
        send(recipient, "Recuperação de senha - Gestão Escolar",
                "Recebemos um pedido para alterar a senha da sua conta.\n\n"
                        + "Use o link abaixo para definir uma nova senha:\n" + link + "\n\n"
                        + "Este link expira em 30 minutos. Se não fez este pedido, ignore esta mensagem.");
    }

    private void send(String recipient, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);
        long startedAt = System.nanoTime();
        try {
            mailSender.send(message);
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            logger.info("Email SMTP aceito para {} em {} ms (assunto: {})", recipient, elapsedMillis, subject);
        } catch (MailException e) {
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            logger.error("Falha SMTP ao enviar email para {} após {} ms: {}", recipient, elapsedMillis,
                    e.getMessage(), e);
            throw new IllegalStateException("Não foi possível enviar o email. Verifique as credenciais SMTP.", e);
        }
    }

    private String encodeToken(String token) {
        return java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}