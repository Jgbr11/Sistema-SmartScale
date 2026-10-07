package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.web.dto.ConfirmacaoSubstitutoRequest;
import br.com.milscale.milscale.adapters.web.dto.CriarSolicitacaoRequest;
import br.com.milscale.milscale.adapters.web.dto.CriarTrocaMutuaRequest;
import br.com.milscale.milscale.adapters.web.dto.DecisaoRequest;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.SolicitacaoService;
import br.com.milscale.milscale.domain.Solicitacao;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import br.com.milscale.milscale.domain.Militar;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final AuditoriaService auditoriaService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, AuditoriaService auditoriaService) {
        this.solicitacaoService = solicitacaoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/minhas")
    public List<Solicitacao> minhas(Authentication auth) {
        return solicitacaoService.minhas(auth.getName());
    }

    @GetMapping("/aguardando-minha-confirmacao")
    public List<Solicitacao> aguardandoMinhaConfirmacao(Authentication auth) {
        return solicitacaoService.aguardandoMinhaConfirmacao(auth.getName());
    }

    @PostMapping("/{id}/confirmar-substituto")
    public Solicitacao confirmarSubstituto(@PathVariable Long id, @Valid @RequestBody ConfirmacaoSubstitutoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.confirmarSubstituto(id, req.aceito(), req.comentario(), auth.getName());
        auditoriaService.registrar(auth.getName(), req.aceito() ? "TROCA_SUBSTITUTO_ACEITOU" : "TROCA_SUBSTITUTO_RECUSOU", "solicitação " + id);
        return s;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping("/triagem")
    public List<Solicitacao> emTriagem() {
        return solicitacaoService.emTriagem();
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @GetMapping("/autorizacao")
    public List<Solicitacao> aguardandoAutorizacao() {
        return solicitacaoService.aguardandoAutorizacao();
    }

    @PostMapping
    public Solicitacao criar(@Valid @RequestBody CriarSolicitacaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.criar(req.servicoOrigemId(), req.substitutoId(), req.justificativa(), auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_PEDIDA", "serviço " + req.servicoOrigemId());
        return s;
    }

    @PostMapping("/troca-mutua")
    public Solicitacao criarTrocaMutua(@Valid @RequestBody CriarTrocaMutuaRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.criarTrocaMutua(req.servicoOrigemId(), req.servicoDestinoId(), req.justificativa(), auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_PEDIDA", "serviço " + req.servicoOrigemId() + " (mútua)");
        return s;
    }

    @GetMapping("/elegiveis")
    public List<Militar> elegiveis(@RequestParam Long servicoId, @RequestParam Long militarId) {
        return solicitacaoService.listarElegiveisParaTroca(servicoId, militarId);
    }

    @GetMapping("/elegiveis-troca-mutua")
    public List<SolicitacaoService.CandidatoTrocaMutua> elegiveisTrocaMutua(@RequestParam Long servicoId, @RequestParam Long militarId) {
        return solicitacaoService.listarElegiveisParaTrocaMutua(servicoId, militarId);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/{id}/triagem")
    public Solicitacao triagem(@PathVariable Long id, @Valid @RequestBody DecisaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.triagem(id, req.aprovado(), req.comentario());
        auditoriaService.registrar(auth.getName(), req.aprovado() ? "TROCA_TRIAGEM_APROVADA" : "TROCA_TRIAGEM_NEGADA", "solicitação " + id);
        return s;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/autorizacao")
    public Solicitacao autorizar(@PathVariable Long id, @Valid @RequestBody DecisaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.autorizar(id, req.aprovado(), req.comentario());
        auditoriaService.registrar(auth.getName(), req.aprovado() ? "TROCA_AUTORIZADA" : "TROCA_NEGADA", "solicitação " + id);
        return s;
    }

    @PostMapping("/{id}/cancelar")
    public Solicitacao cancelar(@PathVariable Long id, Authentication auth) {
        Solicitacao s = solicitacaoService.cancelar(id, auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_CANCELADA", "solicitação " + id);
        return s;
    }
}
