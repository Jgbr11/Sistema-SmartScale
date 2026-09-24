package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.MilitarService;
import br.com.milscale.milscale.domain.Militar;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/militares")
public class MilitarController {

    private final MilitarService militarService;
    private final AuditoriaService auditoriaService;

    public MilitarController(MilitarService militarService, AuditoriaService auditoriaService) {
        this.militarService = militarService;
        this.auditoriaService = auditoriaService;
    }

    /** RF04 (RF14/RF19 - escopo de visibilidade) - Cabo e Sargenteante veem o efetivo completo. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping
    public List<Militar> listar() {
        return militarService.listar();
    }

    /** Aberto a qualquer autenticado - o popup de detalhes na Escala do dia
     *  usa isso, e Militar Escalado tambem precisa ver colegas dessa forma
     *  mesmo sem acesso ao cadastro completo (listar()) acima. */
    @GetMapping("/{id}")
    public Militar buscar(@PathVariable Long id) {
        return militarService.buscar(id);
    }

    /** RF06 - quais tipos de servico esse militar e elegivel pra assumir, dado
     *  posto/subunidade/cursos dele hoje. Usado no popup de detalhes. */
    @GetMapping("/{id}/funcoes-elegiveis")
    public List<br.com.milscale.milscale.domain.TipoServico> funcoesElegiveis(@PathVariable Long id) {
        return militarService.funcoesElegiveis(id);
    }

    /** RF04 - Ficha do Militar: historico completo. Privativo de quem mantem o cadastro,
     *  OU a propria pessoa vendo o proprio historico (RF - "Meu historico", aberto a todo mundo). */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE') or @militarService.ehOProprio(#id, authentication.name)")
    @GetMapping("/{id}/historico-servicos")
    public List<br.com.milscale.milscale.domain.ServicoEscalado> historicoServicos(@PathVariable Long id) {
        return militarService.historicoServicos(id);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE') or @militarService.ehOProprio(#id, authentication.name)")
    @GetMapping("/{id}/historico-afastamentos")
    public List<br.com.milscale.milscale.domain.Afastamento> historicoAfastamentos(@PathVariable Long id) {
        return militarService.historicoAfastamentos(id);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE') or @militarService.ehOProprio(#id, authentication.name)")
    @GetMapping("/{id}/historico-trocas")
    public List<br.com.milscale.milscale.domain.Solicitacao> historicoTrocas(@PathVariable Long id) {
        return militarService.historicoTrocas(id);
    }

    /** Aberto a qualquer autenticado - usado no popup e na Ficha pra mostrar
     *  a tag de afastamento (só o tipo, nunca a descrição inteira). */
    @GetMapping("/{id}/afastamento-atual")
    public br.com.milscale.milscale.domain.Afastamento afastamentoAtual(@PathVariable Long id) {
        return militarService.afastamentoAtual(id);
    }

    /** RF04 - manter o cadastro e privativo de Cabo da Sargenteacao e Sargenteante. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping
    public Militar cadastrar(@RequestBody Militar militar, Authentication auth) {
        Militar salvo = militarService.cadastrar(militar);
        auditoriaService.registrar(auth.getName(), "MILITAR_CADASTRADO", salvo.getNomeExibicao() + " (id " + salvo.getId() + ")");
        return salvo;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PutMapping("/{id}")
    public Militar atualizar(@PathVariable Long id, @RequestBody Militar militar, Authentication auth) {
        Militar salvo = militarService.atualizar(id, militar);
        auditoriaService.registrar(auth.getName(), "MILITAR_EDITADO", salvo.getNomeExibicao() + " (id " + id + ")");
        return salvo;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/{id}/desligar")
    public Militar desligar(@PathVariable Long id, Authentication auth) {
        Militar salvo = militarService.desligar(id);
        auditoriaService.registrar(auth.getName(), "MILITAR_DESLIGADO", salvo.getNomeExibicao() + " (id " + id + ")");
        return salvo;
    }
}
