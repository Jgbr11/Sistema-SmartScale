package br.com.milscale.milscale.adapters.agendamento;

import br.com.milscale.milscale.application.LembreteServicoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LembreteServicoAgendador {

    private static final Logger log = LoggerFactory.getLogger(LembreteServicoAgendador.class);

    private final LembreteServicoService lembreteServicoService;

    public LembreteServicoAgendador(LembreteServicoService lembreteServicoService) {
        this.lembreteServicoService = lembreteServicoService;
    }

    @Scheduled(cron = "${milscale.lembrete.cron:0 0 18 * * *}")
    public void enviarLembretesDeAmanha() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        log.info("Lembrete de serviço de {}: {} email(s) processado(s)", amanha, lembreteServicoService.enviarPara(amanha));
    }
}
