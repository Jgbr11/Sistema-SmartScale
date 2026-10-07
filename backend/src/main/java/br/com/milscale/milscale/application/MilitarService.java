package br.com.milscale.milscale.application;

import br.com.smartscale.core.SituacaoPessoa;
import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.PerfilAcessoRepository;
import br.com.milscale.milscale.adapters.persistence.PostoGraduacaoRepository;
import br.com.milscale.milscale.adapters.persistence.SubunidadeRepository;
import br.com.milscale.milscale.adapters.persistence.RequisitoServicoRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.adapters.persistence.SolicitacaoRepository;
import br.com.milscale.milscale.adapters.persistence.TipoServicoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PerfilAcesso;
import br.com.milscale.milscale.domain.RequisitoServico;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.SituacaoEscala;
import br.com.milscale.milscale.domain.Solicitacao;
import br.com.milscale.milscale.domain.TipoServico;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
    private final PerfilAcessoRepository perfilAcessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeradorDeSenha geradorDeSenha;
    private final PostoGraduacaoRepository postoGraduacaoRepository;
    private final SubunidadeRepository subunidadeRepository;

    public MilitarService(MilitarRepository militarRepository, TipoServicoRepository tipoServicoRepository,
                           RequisitoServicoRepository requisitoServicoRepository, ElegibilidadeService elegibilidadeService,
                           ServicoEscaladoRepository servicoEscaladoRepository, AfastamentoRepository afastamentoRepository,
                           SolicitacaoRepository solicitacaoRepository, UsuarioRepository usuarioRepository,
                           PerfilAcessoRepository perfilAcessoRepository, PasswordEncoder passwordEncoder,
                           GeradorDeSenha geradorDeSenha, PostoGraduacaoRepository postoGraduacaoRepository,
                           SubunidadeRepository subunidadeRepository) {
        this.militarRepository = militarRepository;
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.elegibilidadeService = elegibilidadeService;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.afastamentoRepository = afastamentoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
        this.passwordEncoder = passwordEncoder;
        this.geradorDeSenha = geradorDeSenha;
        this.postoGraduacaoRepository = postoGraduacaoRepository;
        this.subunidadeRepository = subunidadeRepository;
    }

    public List<Militar> listar() {
        return militarRepository.findAll();
    }

    public Militar buscar(Long id) {
        return militarRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Militar nao encontrado"));
    }

    public List<TipoServico> funcoesElegiveis(Long militarId) {
        Militar m = buscar(militarId);
        List<TipoServico> todosOsTipos = tipoServicoRepository.findByAtivoTrue();
        Map<Long, List<RequisitoServico>> requisitosPorTipo = requisitoServicoRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getTipoServico().getId()));
        return todosOsTipos.stream()
                .filter(tipo -> elegibilidadeService.elegivel(m, requisitosPorTipo.getOrDefault(tipo.getId(), List.of())))
                .toList();
    }

    public List<ServicoEscalado> historicoServicos(Long militarId, boolean incluirRascunho) {
        return incluirRascunho
                ? servicoEscaladoRepository.findByMilitar_IdOrderByDataDesc(militarId)
                : servicoEscaladoRepository.findByMilitar_IdAndEscala_SituacaoOrderByDataDesc(militarId, SituacaoEscala.PUBLICADA);
    }

    public List<Afastamento> historicoAfastamentos(Long militarId) {
        return afastamentoRepository.findByMilitar_IdOrderByDataInicioDesc(militarId);
    }

    public List<Solicitacao> historicoTrocas(Long militarId) {
        return solicitacaoRepository.findBySolicitante_IdOrSubstituto_IdOrderByDataSolicitacaoDesc(militarId, militarId);
    }

    public Afastamento afastamentoAtual(Long militarId) {
        LocalDate hoje = LocalDate.now();
        return afastamentoRepository.findByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(militarId, hoje, hoje)
                .stream().findFirst().orElse(null);
    }

    public boolean ehOProprio(Long militarId, String loginUsuario) {
        return usuarioRepository.findByLogin(loginUsuario)
                .map(u -> u.getMilitar().getId().equals(militarId))
                .orElse(false);
    }

    @Transactional
    public MilitarCadastrado cadastrar(DadosMilitar dados) {
        Militar militar = Militar.builder().situacao(SituacaoPessoa.ATIVO).build();
        aplicar(dados, militar);
        validarCpfUnico(militar.getCpf(), null);
        validarNomeGuerraUnicoNoPosto(militar.getPosto().getId(), militar.getNomeGuerra(), null);
        Militar salvo = militarRepository.save(militar);
        PerfilAcesso perfil = perfilAcessoRepository.findByNome("MILITAR_ESCALADO")
                .orElseThrow(() -> new IllegalStateException("Perfil MILITAR_ESCALADO não cadastrado"));
        String senha = geradorDeSenha.gerar();
        usuarioRepository.save(Usuario.builder()
                .militar(salvo).perfil(perfil).login(salvo.getCpf())
                .senhaHash(passwordEncoder.encode(senha)).senhaTemporaria(true)
                .build());
        return new MilitarCadastrado(salvo, senha);
    }

    public record MilitarCadastrado(Militar militar, String senhaTemporaria) {}

    private static String normalizarCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("O CPF precisa ter 11 números");
        }
        return digitos;
    }

    @Transactional
    public Militar atualizar(Long id, DadosMilitar dados) {
        Militar existente = buscar(id);
        String cpfAnterior = existente.getCpf();
        String fotoAnterior = existente.getFotoBase64();
        aplicar(dados, existente);
        if (dados.fotoBase64() == null) existente.setFotoBase64(fotoAnterior);
        validarCpfUnico(existente.getCpf(), id);
        validarNomeGuerraUnicoNoPosto(existente.getPosto().getId(), existente.getNomeGuerra(), id);
        Militar salvo = militarRepository.save(existente);
        if (!cpfAnterior.equals(salvo.getCpf())) {
            usuarioRepository.findByMilitar_Id(id).ifPresent(u -> {
                u.setLogin(salvo.getCpf());
                usuarioRepository.save(u);
            });
        }
        return salvo;
    }

    private void aplicar(DadosMilitar d, Militar m) {
        m.setNomeCompleto(d.nomeCompleto().trim());
        m.setNomeGuerra(d.nomeGuerra().trim());
        m.setCpf(normalizarCpf(d.cpf()));
        m.setNumeroRegistro(d.numeroRegistro());
        m.setDataNascimento(d.dataNascimento());
        m.setFusex(d.fusex());
        m.setEmail(d.email());
        m.setTelefone(d.telefone() == null ? null : d.telefone().replaceAll("\\D", ""));
        m.setFotoBase64(d.fotoBase64());
        m.setPosto(postoGraduacaoRepository.findById(d.postoId())
                .orElseThrow(() -> new NoSuchElementException("Posto/graduação não encontrado")));
        m.setSubunidade(subunidadeRepository.findById(d.subunidadeId())
                .orElseThrow(() -> new NoSuchElementException("Subunidade não encontrada")));
    }

    private void validarCpfUnico(String cpf, Long idParaIgnorar) {
        militarRepository.findByCpf(cpf).ifPresent(outro -> {
            if (!outro.getId().equals(idParaIgnorar)) {
                throw new IllegalArgumentException("Já existe outro militar cadastrado com esse CPF");
            }
        });
    }

    private void validarNomeGuerraUnicoNoPosto(Long postoId, String nomeGuerra, Long idParaIgnorar) {
        boolean conflito = militarRepository.findByPosto_IdAndNomeGuerraIgnoreCase(postoId, nomeGuerra).stream()
                .anyMatch(m -> idParaIgnorar == null || !m.getId().equals(idParaIgnorar));
        if (conflito) {
            throw new IllegalArgumentException(
                    "Já existe alguém com esse nome de guerra no mesmo posto/graduação. Use outro nome de guerra ou ajuste o já cadastrado.");
        }
    }

    @Transactional
    public Militar desligar(Long id) {
        Militar existente = buscar(id);
        existente.setSituacao(SituacaoPessoa.DESLIGADO);
        usuarioRepository.findByMilitar_Id(id).ifPresent(usuario -> {
            usuario.setAtivo(false);
            usuarioRepository.save(usuario);
        });
        return militarRepository.save(existente);
    }
}
