package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.LembreteServicoScheduler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Disparo manual do lembrete de serviço - além do agendamento diário
 * automático, o Sargenteante pode forçar o envio pra uma data
 * específica (útil pra reenviar, ou pra testar sem esperar o horário
 * do cron).
 */
@RestController
@RequestMapping("/api/lembretes")
@PreAuthorize("hasRole('SARGENTEANTE')")
public class LembreteController {

    private final LembreteServicoScheduler scheduler;

    public LembreteController(LembreteServicoScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/disparar")
    public Map<String, Object> disparar(@RequestParam(required = false) String data) {
        LocalDate dia = data != null ? LocalDate.parse(data) : LocalDate.now().plusDays(1);
        int processados = scheduler.enviarLembretesPara(dia);
        return Map.of("dia", dia.toString(), "processados", processados);
    }
}
