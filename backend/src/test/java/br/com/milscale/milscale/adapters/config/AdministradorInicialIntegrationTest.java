package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"milscale.seed.demo=false",
        "milscale.admin.cpf=123.456.789-09", "milscale.admin.senha=senhaInicial1",
        "spring.datasource.url=jdbc:h2:mem:seed_admin;MODE=MySQL;DATABASE_TO_LOWER=TRUE"})
@ActiveProfiles("test")
class AdministradorInicialIntegrationTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void criaSargenteanteComSenhaTemporaria() {
        Usuario admin = usuarioRepository.findByLogin("12345678909").orElseThrow();
        assertThat(admin.getPerfil().getNome()).isEqualTo("SARGENTEANTE");
        assertThat(admin.isSenhaTemporaria()).isTrue();
        assertThat(passwordEncoder.matches("senhaInicial1", admin.getSenhaHash())).isTrue();
    }
}
