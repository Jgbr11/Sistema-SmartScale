package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.DadosMilitar;
import br.com.milscale.milscale.application.MilitarService;
import br.com.milscale.milscale.domain.Militar;
import jakarta.validation.Valid;
import br.com.milscale.milscale.adapters.web.dto.MilitarCadastradoResponse;
import br.com.milscale.milscale.adapters.web.dto.MilitarDetalheResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.Solicitacao;
import br.com.milscale.milscale.domain.TipoServico;

@RestController
@RequestMapping("/api/militares")
public class MilitarController {

    private final MilitarService militarService;
    private final AuditoriaService auditoriaService;

    public MilitarController(MilitarService militarService, AuditoriaService auditoriaService) {
        this.militarService = militarService;
        this.auditoriaService = auditoriaService;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping
    public List<Militar> listar() {
        return militarService.listar();
    }

    @GetMapping("/{id}")
    public MilitarDetalheResponse buscar(@PathVariable Long id, Authentication auth) {
        Militar militar = militarService.buscar(id);
        boolean completo = PerfisSargenteacao.ehSargenteacao(auth) || militarService.ehOProprio(id, auth.getName());
        return completo ? MilitarDetalheResponse.completo(militar) : MilitarDetalheResponse.publico(militar);
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<Map<String, String>> foto(@PathVariable Long id) {
        Militar militar = militarService.buscar(id);
        if (!militar.isTemFoto()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(Map.of("fotoBase64", militar.getFotoBase64()));
    }

    @GetMapping("/{id}/funcoes-elegiveis")
    public List<TipoServico> funcoesElegiveis(@PathVariable Long id) {
        return militarService.funcoesElegiveis(id);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE') or @militarService.ehOProprio(#id, authentication.name)")
    @GetMapping("/{id}/historico-servicos")
    public List<ServicoEscalado> historicoServicos(@PathVariable Long id, Authentication auth) {
        return militarService.historicoServicos(id, PerfisSargenteacao.ehSargenteacao(auth));
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE') or @militarService.ehOProprio(#id, authentication.name)")
    @GetMapping("/{id}/historico-afastamentos")
    public List<Afastamento> historicoAfastamentos(@PathVariable Long id) {
        return militarService.historicoAfastamentos(id);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE') or @militarService.ehOProprio(#id, authentication.name)")
    @GetMapping("/{id}/historico-trocas")
    public List<Solicitacao> historicoTrocas(@PathVariable Long id) {
        return militarService.historicoTrocas(id);
    }

    @GetMapping("/{id}/afastamento-atual")
    public Afastamento afastamentoAtual(@PathVariable Long id) {
        return militarService.afastamentoAtual(id);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping
    public MilitarCadastradoResponse cadastrar(@Valid @RequestBody DadosMilitar dados, Authentication auth) {
        MilitarService.MilitarCadastrado cadastrado = militarService.cadastrar(dados);
        Militar salvo = cadastrado.militar();
        auditoriaService.registrar(auth.getName(), "MILITAR_CADASTRADO", salvo.getNomeExibicao() + " (id " + salvo.getId() + ") - conta criada");
        return new MilitarCadastradoResponse(MilitarDetalheResponse.completo(salvo), cadastrado.senhaTemporaria());
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PutMapping("/{id}")
    public MilitarDetalheResponse atualizar(@PathVariable Long id, @Valid @RequestBody DadosMilitar dados, Authentication auth) {
        Militar salvo = militarService.atualizar(id, dados);
        auditoriaService.registrar(auth.getName(), "MILITAR_EDITADO", salvo.getNomeExibicao() + " (id " + id + ")");
        return MilitarDetalheResponse.completo(salvo);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/{id}/desligar")
    public Militar desligar(@PathVariable Long id, Authentication auth) {
        Militar salvo = militarService.desligar(id);
        auditoriaService.registrar(auth.getName(), "MILITAR_DESLIGADO", salvo.getNomeExibicao() + " (id " + id + ")");
        return salvo;
    }
}
