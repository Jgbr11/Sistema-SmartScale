package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.NovoRequisito;
import br.com.milscale.milscale.application.RequisitoServicoService;
import br.com.milscale.milscale.domain.RequisitoServico;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-servico/{tipoId}/requisitos")
public class RequisitoServicoController {

    private final RequisitoServicoService requisitoServicoService;
    private final AuditoriaService auditoriaService;

    public RequisitoServicoController(RequisitoServicoService requisitoServicoService, AuditoriaService auditoriaService) {
        this.requisitoServicoService = requisitoServicoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<RequisitoServico> listar(@PathVariable Long tipoId) {
        return requisitoServicoService.listar(tipoId);
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public RequisitoServico adicionar(@PathVariable Long tipoId, @Valid @RequestBody NovoRequisito novo, Authentication auth) {
        RequisitoServico r = requisitoServicoService.adicionar(tipoId, novo);
        auditoriaService.registrar(auth.getName(), "REQUISITO_ADICIONADO",
                r.getTipoServico().getNome() + ": " + r.getPosto().getSigla() + " (requisito id " + r.getId() + ")");
        return r;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @DeleteMapping("/{requisitoId}")
    public void remover(@PathVariable Long tipoId, @PathVariable Long requisitoId, Authentication auth) {
        requisitoServicoService.remover(tipoId, requisitoId);
        auditoriaService.registrar(auth.getName(), "REQUISITO_REMOVIDO", "tipo " + tipoId + ", requisito id " + requisitoId);
    }
}
