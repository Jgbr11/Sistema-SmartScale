package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.TipoServicoService;
import br.com.milscale.milscale.domain.TipoServico;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-servico")
public class TipoServicoController {

    private final TipoServicoService tipoServicoService;

    public TipoServicoController(TipoServicoService tipoServicoService) {
        this.tipoServicoService = tipoServicoService;
    }

    @GetMapping
    public List<TipoServico> listar() {
        return tipoServicoService.listar();
    }

    /** RN11 - manutencao dos tipos de servico e privativa do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public TipoServico cadastrar(@RequestBody TipoServico tipo) {
        return tipoServicoService.cadastrar(tipo);
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public TipoServico atualizar(@PathVariable Long id, @RequestBody TipoServico tipo) {
        return tipoServicoService.atualizar(id, tipo);
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/desativar")
    public TipoServico desativar(@PathVariable Long id) {
        return tipoServicoService.desativar(id);
    }
}
