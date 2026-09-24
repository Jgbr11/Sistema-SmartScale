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

/**
 * RF01 - autentica o usuario por identificador (aqui: login) e senha,
 * atribuindo-lhe as permissoes do seu perfil (RF25).
 */
@Service
public class MilScaleUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public MilScaleUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // RF01 - login por CPF. Aceita com ou sem formatacao (com pontos/traco
        // ou so os numeros) - normaliza tirando tudo que nao e digito antes
        // de comparar, pra nao depender de como a pessoa digitou.
        String cpfNormalizado = login == null ? "" : login.replaceAll("\\D", "");
        Usuario usuario = usuarioRepository.findByLogin(cpfNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario ou senha invalidos"));

        String perfil = usuario.getPerfil().getNome().toUpperCase().replace(" ", "_");
        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + perfil)))
                .disabled(!usuario.isAtivo())
                .build();
    }
}
