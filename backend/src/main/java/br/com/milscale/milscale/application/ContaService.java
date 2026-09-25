package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** "Minha conta" - dados da sessao e troca da propria senha. */
@Service
public class ContaService {

    private final UsuarioLogadoService usuarioLogadoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(UsuarioLogadoService usuarioLogadoService, UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder) {
        this.usuarioLogadoService = usuarioLogadoService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarAcesso(String login) {
        Usuario usuario = usuarioLogadoService.usuario(login);
        usuario.setUltimoAcesso(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void trocarSenha(String login, String senhaAtual, String senhaNova) {
        Usuario usuario = usuarioLogadoService.usuario(login);
        if (senhaAtual == null || senhaNova == null || senhaNova.isBlank()) {
            throw new IllegalArgumentException("Informe a senha atual e a nova senha");
        }
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        if (senhaNova.length() < 6) {
            throw new IllegalArgumentException("A nova senha precisa ter pelo menos 6 caracteres");
        }
        usuario.setSenhaHash(passwordEncoder.encode(senhaNova));
        usuarioRepository.save(usuario);
    }
}
