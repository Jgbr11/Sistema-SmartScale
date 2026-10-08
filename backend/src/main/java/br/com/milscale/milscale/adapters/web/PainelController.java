package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.PainelResumo;
import br.com.milscale.milscale.application.PainelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
@PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping("/resumo")
    public PainelResumo resumo() {
        return painelService.resumo();
    }
}
