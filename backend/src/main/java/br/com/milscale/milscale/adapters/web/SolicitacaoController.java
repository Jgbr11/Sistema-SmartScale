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

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final AuditoriaService auditoriaService;

    public SolicitacaoController(SolicitacaoService solicitacaoService, AuditoriaService auditoriaService) {
        this.solicitacaoService = solicitacaoService;
        this.auditoriaService = auditoriaService;
    }

    /** RF19 - qualquer um consulta as proprias solicitacoes. */
    @GetMapping("/minhas")
    public List<Solicitacao> minhas(Authentication auth) {
        return solicitacaoService.minhas(auth.getName());
    }

    /** RF15 - pedidos onde eu fui sugerido como substituto e ainda nao respondi. */
    @GetMapping("/aguardando-minha-confirmacao")
    public List<Solicitacao> aguardandoMinhaConfirmacao(Authentication auth) {
        return solicitacaoService.aguardandoMinhaConfirmacao(auth.getName());
    }

    /** RF15 - o substituto sugerido aceita ou recusa, antes de qualquer triagem. */
    @PostMapping("/{id}/confirmar-substituto")
    public Solicitacao confirmarSubstituto(@PathVariable Long id, @Valid @RequestBody ConfirmacaoSubstitutoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.confirmarSubstituto(id, req.aceito(), req.comentario(), auth.getName());
        auditoriaService.registrar(auth.getName(), req.aceito() ? "TROCA_SUBSTITUTO_ACEITOU" : "TROCA_SUBSTITUTO_RECUSOU", "solicitação " + id);
        return s;
    }

    /** RF17 - fila de triagem do Cabo. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping("/triagem")
    public List<Solicitacao> emTriagem() {
        return solicitacaoService.emTriagem();
    }

    /** RF18 - fila de autorizacao final do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @GetMapping("/autorizacao")
    public List<Solicitacao> aguardandoAutorizacao() {
        return solicitacaoService.aguardandoAutorizacao();
    }

    /** RF15 - qualquer pessoa escalada pede a troca do proprio servico (substituição:
     *  o outro assume e eu fico sem nada até o próximo serviço normal). */
    @PostMapping
    public Solicitacao criar(@Valid @RequestBody CriarSolicitacaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.criar(req.servicoOrigemId(), req.substitutoId(), req.justificativa(), auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_PEDIDA", "serviço " + req.servicoOrigemId());
        return s;
    }

    /** Troca mútua - os dois assumem o dia um do outro, dentro do mesmo tipo de serviço. */
    @PostMapping("/troca-mutua")
    public Solicitacao criarTrocaMutua(@Valid @RequestBody CriarTrocaMutuaRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.criarTrocaMutua(req.servicoOrigemId(), req.servicoDestinoId(), req.justificativa(), auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_PEDIDA", "serviço " + req.servicoOrigemId() + " (mútua)");
        return s;
    }

    /** RF15 - quem mais pode assumir esse serviço (pra sugerir substituto no pedido). */
    @GetMapping("/elegiveis")
    public List<br.com.milscale.milscale.domain.Militar> elegiveis(@RequestParam Long servicoId, @RequestParam Long militarId) {
        return solicitacaoService.listarElegiveisParaTroca(servicoId, militarId);
    }

    /** Candidatos a troca mútua - mesmo tipo de serviço, regra de 1x1 checada dos dois lados. */
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
