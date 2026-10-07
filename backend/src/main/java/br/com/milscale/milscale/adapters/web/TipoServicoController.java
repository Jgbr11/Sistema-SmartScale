package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.TipoServicoService;
import br.com.milscale.milscale.application.DadosTipoServico;
import jakarta.validation.Valid;
import br.com.milscale.milscale.domain.TipoServico;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-servico")
public class TipoServicoController {

    private final TipoServicoService tipoServicoService;
    private final AuditoriaService auditoriaService;

    public TipoServicoController(TipoServicoService tipoServicoService, AuditoriaService auditoriaService) {
        this.tipoServicoService = tipoServicoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<TipoServico> listar() {
        return tipoServicoService.listar();
    }

    /** RN11 - manutencao dos tipos de servico e privativa do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public TipoServico cadastrar(@Valid @RequestBody DadosTipoServico dados, Authentication auth) {
        TipoServico salvo = tipoServicoService.cadastrar(dados);
        auditoriaService.registrar(auth.getName(), "TIPO_SERVICO_CADASTRADO", salvo.getNome());
        return salvo;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public TipoServico atualizar(@PathVariable Long id, @Valid @RequestBody DadosTipoServico dados, Authentication auth) {
        TipoServico salvo = tipoServicoService.atualizar(id, dados);
        auditoriaService.registrar(auth.getName(), "TIPO_SERVICO_EDITADO", salvo.getNome() + " (efetivo " + salvo.getEfetivoNecessario() + ")");
        return salvo;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/desativar")
    public TipoServico desativar(@PathVariable Long id, Authentication auth) {
        TipoServico salvo = tipoServicoService.desativar(id);
        auditoriaService.registrar(auth.getName(), "TIPO_SERVICO_DESATIVADO", salvo.getNome());
        return salvo;
    }
}
