package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.NotificacaoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Notificacao;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * O sininho. Diferente do log de auditoria (histórico, só pro
 * Sargenteante), aqui é um aviso direcionado que aparece pra pessoa
 * certa na hora que ela abre o sistema - principalmente trocas
 * esperando decisão, que hoje só apareciam se alguém fosse
 * manualmente na tela de Trocas conferir.
 */
@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UsuarioRepository usuarioRepository,
                              UsuarioLogadoService usuarioLogadoService) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @Transactional
    public void registrarParaUsuario(Usuario destinatario, String tipo, String mensagem, String link) {
        notificacaoRepository.save(Notificacao.builder()
                .destinatario(destinatario).tipo(tipo).mensagem(mensagem).link(link).build());
    }

    /** Notifica todo mundo que tem um dos perfis informados (ex.: todo Cabo e todo Sargenteante). */
    @Transactional
    public void registrarParaPerfis(List<String> nomesPerfis, String tipo, String mensagem, String link) {
        List<Usuario> destinatarios = usuarioRepository.findAll().stream()
                .filter(u -> u.isAtivo() && nomesPerfis.contains(u.getPerfil().getNome()))
                .toList();
        for (Usuario u : destinatarios) {
            registrarParaUsuario(u, tipo, mensagem, link);
        }
    }

    public List<Notificacao> listarRecentes(String loginUsuario, int limite) {
        Long usuarioId = usuarioLogadoService.usuario(loginUsuario).getId();
        return notificacaoRepository.findByDestinatario_IdOrderByDataCriacaoDesc(usuarioId, PageRequest.of(0, limite));
    }

    public long contarNaoLidas(String loginUsuario) {
        Long usuarioId = usuarioLogadoService.usuario(loginUsuario).getId();
        return notificacaoRepository.countByDestinatario_IdAndLidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLida(Long id, String loginUsuario) {
        Notificacao n = notificacaoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Notificação não encontrada"));
        if (!n.getDestinatario().getLogin().equals(loginUsuario)) {
            throw new IllegalArgumentException("Essa notificação não é sua");
        }
        n.setLida(true);
        notificacaoRepository.save(n);
    }

    @Transactional
    public void marcarTodasComoLidas(String loginUsuario) {
        Long usuarioId = usuarioLogadoService.usuario(loginUsuario).getId();
        List<Notificacao> naoLidas = notificacaoRepository.findByDestinatario_IdAndLidaFalse(usuarioId);
        for (Notificacao n : naoLidas) {
            n.setLida(true);
        }
        notificacaoRepository.saveAll(naoLidas);
    }
}
