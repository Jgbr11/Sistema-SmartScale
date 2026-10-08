package br.com.milscale.milscale.adapters.email;

import br.com.milscale.milscale.application.EnvioDeEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EnvioDeEmailSmtp implements EnvioDeEmail {

    private static final Logger log = LoggerFactory.getLogger(EnvioDeEmailSmtp.class);

    private final JavaMailSender mailSender;
    private final boolean habilitado;
    private final String remetente;

    public EnvioDeEmailSmtp(JavaMailSender mailSender,
                            @Value("${milscale.email.habilitado:false}") boolean habilitado,
                            @Value("${spring.mail.username:}") String remetente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remetente = remetente.isBlank() ? "milscale@exemplo.mil.br" : remetente;
    }

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        if (destinatario == null || destinatario.isBlank()) return;
        if (!habilitado) {
            log.info("[email desabilitado] para {} | {}", mascarar(destinatario), assunto);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.info("Email enviado para {}: {}", mascarar(destinatario), assunto);
        } catch (Exception e) {
            log.error("Falha ao enviar email para {}: {}", mascarar(destinatario), e.getMessage());
        }
    }

    static String mascarar(String email) {
        int arroba = email.indexOf('@');
        return arroba <= 1 ? "***" : email.charAt(0) + "***" + email.substring(arroba);
    }
}
