package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AfastamentoService;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.TipoAfastamento;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping
    public List<Afastamento> cadastrar(@RequestBody Map<String, Object> body, Authentication auth) {
        List<Long> militarIds = ((List<Object>) body.get("militarIds")).stream()
                .map(v -> Long.valueOf(String.valueOf(v))).toList();
        TipoAfastamento tipo = TipoAfastamento.valueOf(String.valueOf(body.get("tipo")));
        String descricao = String.valueOf(body.get("descricao"));
        LocalDate dataInicio = LocalDate.parse(String.valueOf(body.get("dataInicio")));
        LocalDate dataFim = LocalDate.parse(String.valueOf(body.get("dataFim")));
        List<Afastamento> criados = afastamentoService.cadastrarMissao(militarIds, tipo, descricao, dataInicio, dataFim, auth.getName());
        auditoriaService.registrar(auth.getName(), "AFASTAMENTO_CADASTRADO",
                tipo + " (" + criados.size() + " militar(es)) - " + dataInicio + " a " + dataFim + " - " + descricao);
        return criados;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Long id, Authentication auth) {
        afastamentoService.cancelar(id);
        auditoriaService.registrar(auth.getName(), "AFASTAMENTO_CANCELADO", "id " + id);
    }
}
