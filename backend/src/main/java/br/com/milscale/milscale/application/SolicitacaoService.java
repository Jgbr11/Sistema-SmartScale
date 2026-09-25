package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PoliticaDeDescanso;
import br.com.milscale.milscale.domain.RequisitoServico;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.SituacaoServico;
import br.com.milscale.milscale.domain.SituacaoSolicitacao;
import br.com.milscale.milscale.domain.Solicitacao;
import br.com.milscale.milscale.domain.TipoTroca;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * RF15/RF17/RF18/RF19 - fluxo de troca de servico (permuta):
 * pedir -> SUBSTITUTO CONFIRMA -> triagem do Cabo -> autorizacao do
 * Sargenteante -> troca efetivada.
 *
 * A confirmacao do substituto foi adicionada porque, sem ela, o pedido
 * decidia o destino de uma terceira pessoa sem consulta-la - agora o
 * proprio substituto precisa aceitar antes de qualquer triagem.
 *
 * Dois TIPOS de troca, ambos reais no quartel: SUBSTITUICAO (o substituto
 * assume e o solicitante fica sem nada ate o proximo servico normal) e
 * TROCA_MUTUA (os dois trocam de dia entre si, dentro do MESMO tipo de
 * servico). A troca mutua exige checar 1x1 dos DOIS lados.
 */
@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final MilitarRepository militarRepository;
    private final UsuarioRepository usuarioRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final RegraEscalaRepository regraEscalaRepository;
    private final ElegibilidadeService elegibilidadeService;
    private final NotificacaoService notificacaoService;
    private final UsuarioLogadoService usuarioLogadoService;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                               ServicoEscaladoRepository servicoEscaladoRepository,
                               MilitarRepository militarRepository,
                               UsuarioRepository usuarioRepository,
                               RequisitoServicoRepository requisitoServicoRepository,
                               RegraEscalaRepository regraEscalaRepository,
                               ElegibilidadeService elegibilidadeService,
                               NotificacaoService notificacaoService,
                               UsuarioLogadoService usuarioLogadoService) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.militarRepository = militarRepository;
        this.usuarioRepository = usuarioRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.regraEscalaRepository = regraEscalaRepository;
        this.elegibilidadeService = elegibilidadeService;
        this.notificacaoService = notificacaoService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<Solicitacao> minhas(String loginSolicitante) {
        Long militarId = usuarioLogadoService.militar(loginSolicitante).getId();
        return solicitacaoRepository.findBySolicitante_IdOrderByDataSolicitacaoDesc(militarId);
    }

    /** RF15 - pedidos onde EU sou o substituto sugerido e ainda preciso aceitar ou recusar. */
    public List<Solicitacao> aguardandoMinhaConfirmacao(String loginUsuario) {
        Long militarId = usuarioLogadoService.militar(loginUsuario).getId();
        return solicitacaoRepository.findBySubstituto_IdAndSituacaoOrderByDataSolicitacaoAsc(militarId, SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO);
    }

    public List<Solicitacao> emTriagem() {
        return solicitacaoRepository.findBySituacaoOrderByDataSolicitacaoAsc(SituacaoSolicitacao.EM_TRIAGEM);
    }

    public List<Solicitacao> aguardandoAutorizacao() {
        return solicitacaoRepository.findBySituacaoOrderByDataSolicitacaoAsc(SituacaoSolicitacao.AGUARDANDO_AUTORIZACAO);
    }

    /** RF15 - "Passar meu serviço": o substituto assume, e o solicitante fica sem nada até o próximo. */
    @Transactional
    public Solicitacao criar(Long servicoOrigemId, Long substitutoId, String justificativa, String loginSolicitante) {
        ServicoEscalado servico = servicoEscaladoRepository.findById(servicoOrigemId)
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        Militar solicitante = usuarioLogadoService.militar(loginSolicitante);

        if (servico.getMilitar() == null || !servico.getMilitar().getId().equals(solicitante.getId())) {
            throw new IllegalArgumentException("Esse serviço não é seu — só quem está escalado pode pedir a troca");
        }
        if (servico.isTravado()) {
            throw new IllegalArgumentException("Esse dia está travado — não é possível pedir troca (RN04)");
        }
        if (servico.isJaComecou()) {
            throw new IllegalArgumentException("Esse serviço já começou (ou já passou) — não dá mais pra pedir troca dele");
        }
        if (substitutoId.equals(solicitante.getId())) {
            throw new IllegalArgumentException("O substituto não pode ser você mesmo");
        }
        Militar substituto = militarRepository.findById(substitutoId)
                .orElseThrow(() -> new NoSuchElementException("Substituto não encontrado"));
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Informe uma justificativa");
        }
        // RN06 - mesmo numa troca combinada espontaneamente, o 1x1 (so 1 dia de
        // folga entre dois servicos) e proibido. 2x1 pra cima e aceitavel numa
        // troca (e a unica flexibilidade além da regra normal, que exige 3x1).
        if (ficariaEm1x1(substituto.getId(), servico.getData(), servico.getId())) {
            throw new IllegalArgumentException(
                    "Esse substituto ficaria com apenas 1 dia de folga entre serviços (1x1) — proibido mesmo em troca combinada. O mínimo aceitável é 2x1 (2 dias de folga).");
        }

        Solicitacao s = Solicitacao.builder()
                .servicoOrigem(servico)
                .solicitante(solicitante)
                .substituto(substituto)
                .justificativa(justificativa)
                .tipoTroca(TipoTroca.SUBSTITUICAO)
                .situacao(SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO)
                .build();
        Solicitacao salva = solicitacaoRepository.save(s);
        usuarioRepository.findByMilitar_Id(substituto.getId()).ifPresent(usuarioSubstituto ->
                notificacaoService.registrarParaUsuario(usuarioSubstituto, "TROCA_AGUARDANDO_CONFIRMACAO",
                        solicitante.getNomeExibicao() + " pediu pra você assumir o serviço de " + servico.getTipoServico().getNome(),
                        "/trocas"));
        return salva;
    }

    /** RF15 - a TROCA MÚTUA é um pedido diferente da substituição: os dois
     *  lados assumem o dia um do outro, dentro do MESMO tipo de serviço
     *  (não faria sentido trocar Cabo da Guarda por Motorista de Dia).
     *  Por isso a checagem de 1x1 (RN06 relaxada) precisa valer pros DOIS
     *  lados - o solicitante assumindo o dia do outro, E o outro assumindo
     *  o dia do solicitante - cada um contra os PRÓPRIOS outros serviços
     *  (excluindo o que está sendo trocado, que deixa de ser dele). */
    @Transactional
    public Solicitacao criarTrocaMutua(Long servicoOrigemId, Long servicoDestinoId, String justificativa, String loginSolicitante) {
        ServicoEscalado servicoOrigem = servicoEscaladoRepository.findById(servicoOrigemId)
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        ServicoEscalado servicoDestino = servicoEscaladoRepository.findById(servicoDestinoId)
                .orElseThrow(() -> new NoSuchElementException("Serviço do outro militar não encontrado"));
        Militar solicitante = usuarioLogadoService.militar(loginSolicitante);

        if (servicoOrigem.getMilitar() == null || !servicoOrigem.getMilitar().getId().equals(solicitante.getId())) {
            throw new IllegalArgumentException("Esse serviço não é seu — só quem está escalado pode pedir a troca");
        }
        if (servicoOrigem.isTravado() || servicoDestino.isTravado()) {
            throw new IllegalArgumentException("Um dos dois dias está travado — não é possível pedir troca (RN04)");
        }
        if (servicoOrigem.isJaComecou() || servicoDestino.isJaComecou()) {
            throw new IllegalArgumentException("Um dos dois serviços já começou (ou já passou) — não dá mais pra trocar");
        }
        if (servicoDestino.getMilitar() == null) {
            throw new IllegalArgumentException("Esse serviço está com vaga em aberto — não tem quem trocar com você");
        }
        if (servicoDestino.getMilitar().getId().equals(solicitante.getId())) {
            throw new IllegalArgumentException("Você não pode trocar consigo mesmo");
        }
        if (!servicoOrigem.getTipoServico().getId().equals(servicoDestino.getTipoServico().getId())) {
            throw new IllegalArgumentException("Troca mútua só entre serviços do mesmo tipo");
        }
        Militar outroMilitar = servicoDestino.getMilitar();
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Informe uma justificativa");
        }
        if (ficariaEm1x1(solicitante.getId(), servicoDestino.getData(), servicoOrigem.getId())) {
            throw new IllegalArgumentException(
                    "Você ficaria com apenas 1 dia de folga entre serviços (1x1) ao assumir esse dia — proibido mesmo em troca combinada.");
        }
        if (ficariaEm1x1(outroMilitar.getId(), servicoOrigem.getData(), servicoDestino.getId())) {
            throw new IllegalArgumentException(
                    "O outro militar ficaria com apenas 1 dia de folga entre serviços (1x1) ao assumir seu dia — proibido mesmo em troca combinada.");
        }

        Solicitacao s = Solicitacao.builder()
                .servicoOrigem(servicoOrigem)
                .servicoDestino(servicoDestino)
                .solicitante(solicitante)
                .substituto(outroMilitar)
                .justificativa(justificativa)
                .tipoTroca(TipoTroca.TROCA_MUTUA)
                .situacao(SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO)
                .build();
        Solicitacao salva = solicitacaoRepository.save(s);
        usuarioRepository.findByMilitar_Id(outroMilitar.getId()).ifPresent(usuarioOutro ->
                notificacaoService.registrarParaUsuario(usuarioOutro, "TROCA_AGUARDANDO_CONFIRMACAO",
                        solicitante.getNomeExibicao() + " quer trocar de dia com você (os dois assumem o dia um do outro) — " + servicoOrigem.getTipoServico().getNome(),
                        "/trocas"));
        return salva;
    }

    /** RF15 - o substituto sugerido precisa aceitar antes de qualquer triagem. */
    @Transactional
    public Solicitacao confirmarSubstituto(Long id, boolean aceito, String comentario, String loginUsuario) {
        Solicitacao s = buscar(id);
        exigirSituacao(s, SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO);
        Militar quemConfirma = usuarioLogadoService.militar(loginUsuario);
        if (!s.getSubstituto().getId().equals(quemConfirma.getId())) {
            throw new IllegalArgumentException("Só a pessoa sugerida como substituta pode confirmar esse pedido");
        }
        if (aceito) {
            s.setSituacao(SituacaoSolicitacao.EM_TRIAGEM);
            notificacaoService.registrarParaPerfis(List.of("CABO_SARGENTEACAO", "SARGENTEANTE"), "TROCA_AGUARDANDO_TRIAGEM",
                    s.getSolicitante().getNomeExibicao() + " quer trocar o serviço de " + s.getServicoOrigem().getTipoServico().getNome(),
                    "/trocas");
        } else {
            s.setSituacao(SituacaoSolicitacao.NEGADA);
            s.setComentarioCabo(comentario != null ? comentario : "Substituto não aceitou a troca");
            s.setDataDecisaoFinal(LocalDateTime.now());
        }
        return solicitacaoRepository.save(s);
    }

    @Transactional
    public Solicitacao triagem(Long id, boolean aprovado, String comentario) {
        Solicitacao s = buscar(id);
        exigirSituacao(s, SituacaoSolicitacao.EM_TRIAGEM);
        s.setComentarioCabo(comentario);
        if (aprovado) {
            s.setSituacao(SituacaoSolicitacao.AGUARDANDO_AUTORIZACAO);
            notificacaoService.registrarParaPerfis(List.of("SARGENTEANTE"), "TROCA_AGUARDANDO_AUTORIZACAO",
                    "Troca de " + s.getSolicitante().getNomeExibicao() + " aprovada pelo Cabo, esperando sua autorização",
                    "/trocas");
        } else {
            s.setSituacao(SituacaoSolicitacao.NEGADA);
            s.setDataDecisaoFinal(LocalDateTime.now());
        }
        return solicitacaoRepository.save(s);
    }

    /** RF18/RN14 - autorizacao final. Se aprovado, a troca e efetivada de verdade no ServicoEscalado.
     *  Em TROCA_MUTUA os DOIS lados trocam de dono; em SUBSTITUICAO so o servicoOrigem muda. */
    @Transactional
    public Solicitacao autorizar(Long id, boolean aprovado, String comentario) {
        Solicitacao s = buscar(id);
        exigirSituacao(s, SituacaoSolicitacao.AGUARDANDO_AUTORIZACAO);
        s.setComentarioSargenteante(comentario);
        s.setDataDecisaoFinal(LocalDateTime.now());
        if (aprovado) {
            ServicoEscalado servico = s.getServicoOrigem();
            if (servico.isTravado()) {
                throw new IllegalArgumentException("O dia foi travado depois do pedido — não é possível autorizar (RN04)");
            }
            if (s.getTipoTroca() == TipoTroca.TROCA_MUTUA) {
                ServicoEscalado destino = s.getServicoDestino();
                if (destino.isTravado()) {
                    throw new IllegalArgumentException("O dia do outro militar foi travado depois do pedido — não é possível autorizar (RN04)");
                }
                Militar solicitanteOriginal = s.getSolicitante();
                servico.setMilitar(s.getSubstituto());
                servico.setObservacao("Troca mútua autorizada — assumiu no lugar de " + solicitanteOriginal.getNomeExibicao());
                destino.setMilitar(solicitanteOriginal);
                destino.setObservacao("Troca mútua autorizada — assumiu no lugar de " + s.getSubstituto().getNomeExibicao());
                servicoEscaladoRepository.save(servico);
                servicoEscaladoRepository.save(destino);
            } else {
                servico.setMilitar(s.getSubstituto());
                servico.setObservacao("Troca autorizada — assumiu no lugar de " + s.getSolicitante().getNomeExibicao());
                servicoEscaladoRepository.save(servico);
            }
            s.setSituacao(SituacaoSolicitacao.AUTORIZADA);
        } else {
            s.setSituacao(SituacaoSolicitacao.NEGADA);
        }
        return solicitacaoRepository.save(s);
    }

    @Transactional
    public Solicitacao cancelar(Long id, String loginSolicitante) {
        Solicitacao s = buscar(id);
        Militar quemPediu = usuarioLogadoService.militar(loginSolicitante);
        if (!s.getSolicitante().getId().equals(quemPediu.getId())) {
            throw new IllegalArgumentException("Só quem pediu a troca pode cancelá-la");
        }
        if (s.getSituacao() != SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO && s.getSituacao() != SituacaoSolicitacao.EM_TRIAGEM) {
            throw new IllegalArgumentException("Só é possível cancelar enquanto ainda não foi autorizada");
        }
        s.setSituacao(SituacaoSolicitacao.CANCELADA);
        s.setDataDecisaoFinal(LocalDateTime.now());
        return solicitacaoRepository.save(s);
    }

    /**
     * RF15 - lista quem mais e elegivel pro mesmo tipo de servico, pra sugerir substituto.
     * Aberto a qualquer autenticado (e so uma lista minima, nao o cadastro completo).
     *
     * A regra normal do motor de geracao exige 3x1 (3 dias de folga entre dois
     * servicos - RN06, intervaloMinimo=3). Mas numa TROCA, por ser escolha
     * propria de quem assume, o minimo aceitavel e mais frouxo: 2x1 (2 dias de
     * folga) e permitido, so o 1x1 (1 dia de folga ou menos) e proibido mesmo
     * em troca. Essa exclusao vale so pra SUGESTAO automatica: se os dois
     * militares combinarem espontaneamente uma troca fora dessa lista (mas
     * ainda respeitando o 2x1), o cadastro (criar) nao bloqueia isso - a
     * decisao de quem assume e sempre de quem pede.
     */
    public List<Militar> listarElegiveisParaTroca(Long servicoOrigemId, Long excluirMilitarId) {
        ServicoEscalado servico = servicoEscaladoRepository.findById(servicoOrigemId)
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        List<RequisitoServico> requisitos = requisitoServicoRepository.findByTipoServico_Id(servico.getTipoServico().getId());
        return militarRepository.findAll().stream()
                .filter(m -> m.getSituacao() == br.com.milscale.core.domain.SituacaoPessoa.ATIVO)
                .filter(m -> !m.getId().equals(excluirMilitarId))
                .filter(m -> elegibilidadeService.elegivel(m, requisitos))
                .filter(m -> !ficariaEm1x1(m.getId(), servico.getData(), servico.getId()))
                .toList();
    }

    /** RF15 - candidatos pra troca mútua: gente com serviço do MESMO tipo
     *  que o solicitante quer trocar, onde os dois lados continuam
     *  respeitando pelo menos 2x1 depois da troca (ver criarTrocaMutua). */
    public List<CandidatoTrocaMutua> listarElegiveisParaTrocaMutua(Long servicoOrigemId, Long militarSolicitanteId) {
        ServicoEscalado servicoOrigem = servicoEscaladoRepository.findById(servicoOrigemId)
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        List<ServicoEscalado> candidatosBrutos = servicoEscaladoRepository
                .findByTipoServico_IdAndSituacao(servicoOrigem.getTipoServico().getId(), SituacaoServico.PREVISTO);

        List<CandidatoTrocaMutua> candidatos = new java.util.ArrayList<>();
        for (ServicoEscalado candidato : candidatosBrutos) {
            if (candidato.getId().equals(servicoOrigem.getId())) continue;
            if (candidato.getMilitar() == null) continue;
            if (candidato.getMilitar().getId().equals(militarSolicitanteId)) continue;
            if (candidato.isTravado() || candidato.isJaComecou()) continue;
            if (ficariaEm1x1(militarSolicitanteId, candidato.getData(), servicoOrigem.getId())) continue;
            if (ficariaEm1x1(candidato.getMilitar().getId(), servicoOrigem.getData(), candidato.getId())) continue;
            candidatos.add(new CandidatoTrocaMutua(candidato.getMilitar(), candidato.getId(), candidato.getData()));
        }
        return candidatos;
    }

    public record CandidatoTrocaMutua(Militar militar, Long servicoId, LocalDate data) {}

    /** Verdadeiro se MILITAR, ao assumir um serviço em NOVADATA, ficaria com
     *  só 1 dia de folga (1x1) contra algum outro serviço PREVISTO dele -
     *  ignorando o servico EXCLUIRSERVICOID (o que ele está abrindo mão). */
    private boolean ficariaEm1x1(Long militarId, LocalDate novaData, Long excluirServicoId) {
        int janela = PoliticaDeDescanso.DISTANCIA_MINIMA_EM_TROCA - 1;
        return servicoEscaladoRepository.findByMilitar_IdAndDataBetween(militarId, novaData.minusDays(janela), novaData.plusDays(janela)).stream()
                .filter(s -> !s.getId().equals(excluirServicoId))
                .anyMatch(s -> PoliticaDeDescanso.ficariaEm1x1(s.getData(), novaData));
    }

    private Solicitacao buscar(Long id) {
        return solicitacaoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Solicitação não encontrada"));
    }

    private void exigirSituacao(Solicitacao s, SituacaoSolicitacao esperada) {
        if (s.getSituacao() != esperada) {
            throw new IllegalArgumentException("Esta solicitação já não está mais em " + esperada.legivel());
        }
    }
}
