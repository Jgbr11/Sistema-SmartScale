package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.PerfilAcessoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.PerfilAcesso;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** RF25 - gestão de perfis e permissões. Privativo do Sargenteante. */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilAcessoRepository perfilAcessoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PerfilAcessoRepository perfilAcessoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<PerfilAcesso> listarPerfis() {
        return perfilAcessoRepository.findAll();
    }

    @Transactional
    public Usuario alterarPerfil(Long usuarioId, Long perfilId, String loginDeQuemAltera) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getLogin().equals(loginDeQuemAltera)) {
            throw new IllegalArgumentException("Você não pode alterar o próprio perfil de acesso");
        }
        PerfilAcesso novoPerfil = perfilAcessoRepository.findById(perfilId)
                .orElseThrow(() -> new NoSuchElementException("Perfil não encontrado"));
        usuario.setPerfil(novoPerfil);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario alterarAtivo(Long usuarioId, boolean ativo, String loginDeQuemAltera) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getLogin().equals(loginDeQuemAltera)) {
            throw new IllegalArgumentException("Você não pode desativar o próprio acesso");
        }
        usuario.setAtivo(ativo);
        return usuarioRepository.save(usuario);
    }

    /** Reseta a senha de volta pro padrão (ex.: militar esqueceu a senha). */
    @Transactional
    public void resetarSenha(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        usuario.setSenhaHash(passwordEncoder.encode("milscale123"));
        usuarioRepository.save(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }
}
