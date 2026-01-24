package com.RafaelDiaz.ClubJudoColombia.servicio;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    // Inyecta el usuario configurado en properties como remitente por defecto
    @Value("${spring.mail.username:info@clubjudo.com}")
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void enviarCodigoActivacion(String destinatario, String codigo, String nombre) {
        String asunto = "Código de Activación - Club Judo Colombia";
        // HTML simple y elegante
        String cuerpo = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #000;">Hola %s,</h2>
                    <p>Bienvenido al camino del Judoka. Para verificar tu cuenta, usa el siguiente código:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 8px; text-align: center; font-size: 24px; font-weight: bold; border: 2px solid #e9ecef; margin: 20px 0;">
                        %s
                    </div>
                    <p>Si no solicitaste este código, ignora este mensaje.</p>
                    <br>
                    <p><em>Club de Judo Colombia</em></p>
                </div>
                """, nombre, codigo);

        enviarCorreo(destinatario, asunto, cuerpo);
    }

    private void enviarCorreo(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = es HTML

            javaMailSender.send(message);
            logger.info("✅ Correo enviado a: {}", to);

        } catch (MessagingException e) {
            logger.error("❌ Error enviando correo a {}: {}", to, e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error inesperado enviando correo: {}", e.getMessage());
        }
    }

    @Async
    public void enviarInvitacionMagicLink(String destinatario, String nombre, String token, String baseUrl) {
        String asunto = "¡Invitación exclusiva! Únete al Club Judo Colombia";
        String urlRegistro = baseUrl + "/registro/" + token;

        String cuerpo = String.format("""
            <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #000;">¡Hola %s! Bienvenido al Dojo.</h2>
                <p>El Sensei te ha invitado a formar parte del Club Judo Colombia. Para completar tu registro y activar tu cuenta, haz clic en el siguiente botón:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #d32f2f; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;">
                        Completar mi Registro
                    </a>
                </div>
                <p style="font-size: 12px; color: #666;">* Este enlace es único y expirará en 48 horas por seguridad.</p>
                <br>
                <p><em>Club de Judo Colombia</em></p>
            </div>
            """, nombre, urlRegistro);

        enviarCorreo(destinatario, asunto, cuerpo);
    }
}