package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilScaleUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ProtecaoContraForcaBruta protecao;

    public MilScaleUserDetailsService(UsuarioRepository usuarioRepository, ProtecaoContraForcaBruta protecao) {
        this.usuarioRepository = usuarioRepository;
        this.protecao = protecao;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        String cpfNormalizado = login == null ? "" : login.replaceAll("\\D", "");
        Usuario usuario = usuarioRepository.findByLogin(cpfNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario ou senha invalidos"));

        String perfil = usuario.getPerfil().getNome().toUpperCase().replace(" ", "_");
        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + perfil)))
                .disabled(!usuario.isAtivo())
                .accountLocked(protecao.bloqueado(cpfNormalizado))
                .build();
    }
}
