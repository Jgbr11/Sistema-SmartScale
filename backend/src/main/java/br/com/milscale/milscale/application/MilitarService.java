package br.com.milscale.milscale.application;

import br.com.milscale.core.domain.SituacaoPessoa;
import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.RequisitoServicoRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.adapters.persistence.SolicitacaoRepository;
import br.com.milscale.milscale.adapters.persistence.TipoServicoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.RequisitoServico;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.Solicitacao;
import br.com.milscale.milscale.domain.TipoServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** RF04 - cadastro das pessoas escaladas: incluir, consultar, alterar, inativar. */
@Service
public class MilitarService {

    private final MilitarRepository militarRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final ElegibilidadeService elegibilidadeService;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final AfastamentoRepository afastamentoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public MilitarService(MilitarRepository militarRepository, TipoServicoRepository tipoServicoRepository,
                           RequisitoServicoRepository requisitoServicoRepository, ElegibilidadeService elegibilidadeService,
                           ServicoEscaladoRepository servicoEscaladoRepository, AfastamentoRepository afastamentoRepository,
                           SolicitacaoRepository solicitacaoRepository, UsuarioRepository usuarioRepository) {
        this.militarRepository = militarRepository;
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.elegibilidadeService = elegibilidadeService;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.afastamentoRepository = afastamentoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Militar> listar() {
        return militarRepository.findAll();
    }

    public Militar buscar(Long id) {
        return militarRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Militar nao encontrado"));
    }

    /** RF06 - pra que tipos de servico esse militar e elegivel hoje (posto/subunidade/cursos). */
    public List<TipoServico> funcoesElegiveis(Long militarId) {
        Militar m = buscar(militarId);
        List<TipoServico> todosOsTipos = tipoServicoRepository.findByAtivoTrue();
        Map<Long, List<RequisitoServico>> requisitosPorTipo = requisitoServicoRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getTipoServico().getId()));
        return todosOsTipos.stream()
                .filter(tipo -> elegibilidadeService.elegivel(m, requisitosPorTipo.getOrDefault(tipo.getId(), List.of())))
                .toList();
    }

    /** RF04 - Ficha do Militar: historico completo de servicos, afastamentos e trocas da pessoa. */
    public List<ServicoEscalado> historicoServicos(Long militarId) {
        return servicoEscaladoRepository.findByMilitar_IdOrderByDataDesc(militarId);
    }

    public List<Afastamento> historicoAfastamentos(Long militarId) {
        return afastamentoRepository.findByMilitar_IdOrderByDataInicioDesc(militarId);
    }

    public List<Solicitacao> historicoTrocas(Long militarId) {
        return solicitacaoRepository.findBySolicitante_IdOrSubstituto_IdOrderByDataSolicitacaoDesc(militarId, militarId);
    }

    /** RF04/RF26 - se a pessoa está afastada hoje, pra mostrar uma tag curta (só o tipo,
     *  nunca a descrição inteira) no popup e na Ficha do militar. */
    public Afastamento afastamentoAtual(Long militarId) {
        LocalDate hoje = LocalDate.now();
        return afastamentoRepository.findByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(militarId, hoje, hoje)
                .stream().findFirst().orElse(null);
    }

    /** Usado no @PreAuthorize dos endpoints de histórico — libera a
     *  própria pessoa a ver o próprio histórico, mesmo sem perfil
     *  elevado, sem abrir pra ver o de qualquer outra pessoa. */
    public boolean ehOProprio(Long militarId, String loginUsuario) {
        return usuarioRepository.findByLogin(loginUsuario)
                .map(u -> u.getMilitar().getId().equals(militarId))
                .orElse(false);
    }

    @Transactional
    public Militar cadastrar(Militar militar) {
        militar.setId(null);
        militar.setSituacao(SituacaoPessoa.ATIVO);
        validarNomeGuerraUnicoNoPosto(militar.getPosto().getId(), militar.getNomeGuerra(), null);
        return militarRepository.save(militar);
    }

    @Transactional
    public Militar atualizar(Long id, Militar dados) {
        Militar existente = buscar(id);
        validarNomeGuerraUnicoNoPosto(dados.getPosto().getId(), dados.getNomeGuerra(), id);
        validarCpfUnico(dados.getCpf(), id);
        boolean cpfMudou = !existente.getCpf().equals(dados.getCpf());
        existente.setNomeCompleto(dados.getNomeCompleto());
        existente.setNomeGuerra(dados.getNomeGuerra());
        existente.setCpf(dados.getCpf());
        existente.setPosto(dados.getPosto());
        existente.setSubunidade(dados.getSubunidade());
        existente.setEmail(dados.getEmail());
        existente.setTelefone(dados.getTelefone());
        existente.setNumeroRegistro(dados.getNumeroRegistro());
        existente.setDataNascimento(dados.getDataNascimento());
        existente.setFusex(dados.getFusex());
        if (dados.getFotoBase64() != null) {
            existente.setFotoBase64(dados.getFotoBase64());
        }
        Militar salvo = militarRepository.save(existente);
        // Login e o CPF (RF01) - se o CPF foi corrigido, o login da conta
        // precisa acompanhar, senao a pessoa fica sem conseguir entrar com
        // o CPF novo.
        if (cpfMudou) {
            usuarioRepository.findByMilitar_Id(id).ifPresent(u -> {
                u.setLogin(dados.getCpf());
                usuarioRepository.save(u);
            });
        }
        return salvo;
    }

    private void validarCpfUnico(String cpf, Long idParaIgnorar) {
        militarRepository.findByCpf(cpf).ifPresent(outro -> {
            if (!outro.getId().equals(idParaIgnorar)) {
                throw new IllegalArgumentException("Já existe outro militar cadastrado com esse CPF");
            }
        });
    }

    /** RN - dentro do mesmo posto/graduação, nome de guerra não pode repetir (postos diferentes podem). */
    private void validarNomeGuerraUnicoNoPosto(Long postoId, String nomeGuerra, Long idParaIgnorar) {
        boolean conflito = militarRepository.findByPosto_IdAndNomeGuerraIgnoreCase(postoId, nomeGuerra).stream()
                .anyMatch(m -> idParaIgnorar == null || !m.getId().equals(idParaIgnorar));
        if (conflito) {
            throw new IllegalArgumentException(
                    "Já existe alguém com esse nome de guerra no mesmo posto/graduação. Use outro nome de guerra ou ajuste o já cadastrado.");
        }
    }

    /** RF04 - inativar (nunca excluir de verdade: preserva o historico). */
    @Transactional
    public Militar desligar(Long id) {
        Militar existente = buscar(id);
        existente.setSituacao(SituacaoPessoa.DESLIGADO);
        return militarRepository.save(existente);
    }
}
