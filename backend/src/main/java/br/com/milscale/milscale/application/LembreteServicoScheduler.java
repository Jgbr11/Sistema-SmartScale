package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.SituacaoEscala;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lembrete de serviço - roda uma vez por dia (padrão 18h, configurável
 * via milscale.lembrete.cron) e avisa por email quem está escalado pra
 * AMANHÃ, só se a escala que cobre o dia já estiver PUBLICADA (não
 * avisa sobre rascunho, que ainda pode mudar). Quem não tem email
 * cadastrado simplesmente não recebe nada - não é erro, é só que a
 * pessoa não informou email no cadastro ainda.
 */
@Component
public class LembreteServicoScheduler {

    private static final Logger log = LoggerFactory.getLogger(LembreteServicoScheduler.class);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final EmailService emailService;

    public LembreteServicoScheduler(ServicoEscaladoRepository servicoEscaladoRepository, EmailService emailService) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${milscale.lembrete.cron:0 0 18 * * *}")
    public void enviarLembretesDeAmanha() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        int enviados = enviarLembretesPara(amanha);
        log.info("Lembrete de serviço de {}: {} email(s) processado(s)", amanha.format(DATA_BR), enviados);
    }

    /** Separado do método agendado pra poder chamar direto num teste, sem esperar o cron. */
    public int enviarLembretesPara(LocalDate dia) {
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByData(dia);
        int processados = 0;
        for (ServicoEscalado s : servicos) {
            if (s.getMilitar() == null) continue;
            if (s.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) continue;
            String email = s.getMilitar().getEmail();
            if (email == null || email.isBlank()) continue;

            String assunto = "MilScale — você está escalado amanhã (" + dia.format(DATA_BR) + ")";
            String corpo = "Olá, " + s.getMilitar().getNomeExibicao() + ".\n\n"
                    + "Você está escalado para \"" + s.getTipoServico().getNome() + "\" no dia " + dia.format(DATA_BR) + ".\n\n"
                    + "Qualquer imprevisto, procure o Cabo ou o Sargenteante com antecedência.\n\n"
                    + "— MilScale, 5º Batalhão de Suprimento";
            emailService.enviar(email, assunto, corpo);
            processados++;
        }
        return processados;
    }
}
