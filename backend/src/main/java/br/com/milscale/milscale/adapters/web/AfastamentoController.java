package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.web.dto.CadastrarAfastamentoRequest;
import br.com.milscale.milscale.application.AfastamentoService;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.domain.Afastamento;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF26 - missoes, dispensas, ferias e licencas. Privativo de Cabo da Sargenteacao e Sargenteante. */
@RestController
@RequestMapping("/api/afastamentos")
public class AfastamentoController {

    private final AfastamentoService afastamentoService;
    private final AuditoriaService auditoriaService;

    public AfastamentoController(AfastamentoService afastamentoService, AuditoriaService auditoriaService) {
        this.afastamentoService = afastamentoService;
        this.auditoriaService = auditoriaService;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping
    public List<Afastamento> listar() {
        return afastamentoService.listarVigentesEFuturos();
    }

    /** RF26 - aceita um ou varios militares no mesmo cadastro (ex.: uma
     *  missão com equipe inteira), agrupados internamente por lote. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping
    public List<Afastamento> cadastrar(@Valid @RequestBody CadastrarAfastamentoRequest req, Authentication auth) {
        List<Afastamento> criados = afastamentoService.cadastrarMissao(req.militarIds(), req.tipo(), req.descricao(),
                req.dataInicio(), req.dataFim(), auth.getName());
        auditoriaService.registrar(auth.getName(), "AFASTAMENTO_CADASTRADO",
                req.tipo() + " (" + criados.size() + " militar(es)) - " + req.dataInicio() + " a " + req.dataFim() + " - " + req.descricao());
        return criados;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Long id, Authentication auth) {
        afastamentoService.cancelar(id);
        auditoriaService.registrar(auth.getName(), "AFASTAMENTO_CANCELADO", "id " + id);
    }
}
