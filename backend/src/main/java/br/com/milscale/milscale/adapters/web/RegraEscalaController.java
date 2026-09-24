package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.RegraEscalaService;
import br.com.milscale.milscale.domain.RegraEscala;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regras-escala")
public class RegraEscalaController {

    private final RegraEscalaService regraEscalaService;

    public RegraEscalaController(RegraEscalaService regraEscalaService) {
        this.regraEscalaService = regraEscalaService;
    }

    @GetMapping
    public List<RegraEscala> listar() {
        return regraEscalaService.listar();
    }

    /** RN11 - manutencao das regras e privativa do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public RegraEscala atualizar(@PathVariable Long id, @RequestBody RegraEscala regra) {
        return regraEscalaService.atualizar(id, regra);
    }
}
