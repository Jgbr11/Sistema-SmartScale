package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.RegraEscalaService;
import org.springframework.security.core.Authentication;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.domain.RegraEscala;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regras-escala")
public class RegraEscalaController {

    private final RegraEscalaService regraEscalaService;
    private final AuditoriaService auditoriaService;

    public RegraEscalaController(RegraEscalaService regraEscalaService, AuditoriaService auditoriaService) {
        this.regraEscalaService = regraEscalaService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<RegraEscala> listar() {
        return regraEscalaService.listar();
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public RegraEscala atualizar(@PathVariable Long id, @RequestBody RegraEscala regra, Authentication auth) {
        RegraEscala salva = regraEscalaService.atualizar(id, regra);
        auditoriaService.registrar(auth.getName(), "REGRA_ESCALA_ALTERADA",
                salva.getTipoServico().getNome() + ": intervalo " + salva.getIntervaloMinimo()
                        + ", máx/mês " + (salva.getMaxServicosMes() == null ? "sem limite" : salva.getMaxServicosMes()));
        return salva;
    }
}
