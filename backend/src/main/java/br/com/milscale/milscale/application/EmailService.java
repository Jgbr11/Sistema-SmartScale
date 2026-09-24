package br.com.milscale.milscale.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de email - hoje usado só pelo lembrete de serviço. Fica
 * desligado por padrão (milscale.email.habilitado=false) porque este
 * projeto não tem credenciais de SMTP reais configuradas: com o
 * interruptor desligado, o sistema só REGISTRA no log o que teria
 * sido enviado, sem tentar conectar em servidor nenhum - assim a
 * tarefa agendada nunca quebra por falta de configuração, e dá pra
 * ver nos logs que o mecanismo está funcionando mesmo sem enviar de
 * verdade. Pra ligar de verdade: configurar MAIL_HOST/MAIL_USER/
 * MAIL_PASSWORD e MILSCALE_EMAIL_HABILITADO=true nas variáveis de
 * ambiente.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean habilitado;
    private final String remetente;

    public EmailService(JavaMailSender mailSender,
                         @Value("${milscale.email.habilitado:false}") boolean habilitado,
                         @Value("${spring.mail.username:milscale@exemplo.mil.br}") String remetente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remetente = remetente.isBlank() ? "milscale@exemplo.mil.br" : remetente;
    }

    public void enviar(String destinatario, String assunto, String corpo) {
        if (destinatario == null || destinatario.isBlank()) {
            return;
        }
        if (!habilitado) {
            log.info("[EMAIL DESABILITADO - não enviado de verdade] Para: {} | Assunto: {}", destinatario, assunto);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.info("Email enviado para {}: {}", destinatario, assunto);
        } catch (Exception e) {
            // Uma falha de envio nunca pode derrubar a tarefa que chamou isso -
            // vira log de erro, não exceção propagada.
            log.error("Falha ao enviar email para {}: {}", destinatario, e.getMessage());
        }
    }
}
