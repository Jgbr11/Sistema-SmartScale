package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.QualificacaoService;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Qualificacao;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QualificacaoController {

    private final QualificacaoService qualificacaoService;

    public QualificacaoController(QualificacaoService qualificacaoService) {
        this.qualificacaoService = qualificacaoService;
    }

    @GetMapping("/qualificacoes")
    public List<Qualificacao> listar() {
        return qualificacaoService.listar();
    }

    /** RN11 - catalogo (criar um novo tipo de curso) e privativo do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/qualificacoes")
    public Qualificacao cadastrar(@RequestBody Qualificacao q) {
        return qualificacaoService.cadastrar(q);
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/qualificacoes/{id}")
    public Qualificacao atualizar(@PathVariable Long id, @RequestBody Qualificacao q) {
        return qualificacaoService.atualizar(id, q);
    }

    /** Vincular um curso a uma pessoa e cadastro (RF04) - Cabo ou Sargenteante. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/militares/{militarId}/qualificacoes/{qualificacaoId}")
    public Militar vincular(@PathVariable Long militarId, @PathVariable Long qualificacaoId) {
        return qualificacaoService.vincular(militarId, qualificacaoId);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @DeleteMapping("/militares/{militarId}/qualificacoes/{qualificacaoId}")
    public Militar desvincular(@PathVariable Long militarId, @PathVariable Long qualificacaoId) {
        return qualificacaoService.desvincular(militarId, qualificacaoId);
    }
}
