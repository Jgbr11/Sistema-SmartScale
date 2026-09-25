package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ContaServiceIntegrationTest {

    @Autowired private ContaService contaService;
    @Autowired private UsuarioLogadoService usuarioLogadoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void usuarioLogado_resolveMilitarPeloLogin() {
        assertThat(usuarioLogadoService.militar("00000000001").getNomeGuerra()).isEqualTo("Zeni");
    }

    @Test
    void usuarioLogado_loginDesconhecido_lancaNaoEncontrado() {
        assertThatThrownBy(() -> usuarioLogadoService.usuario("99999999999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void registrarAcesso_gravaUltimoAcesso() {
        assertThat(contaService.registrarAcesso("00000000004").getUltimoAcesso()).isNotNull();
    }

    @Test
    void trocarSenha_senhaAtualErrada_recusa() {
        assertThatThrownBy(() -> contaService.trocarSenha("00000000004", "errada", "novaSenha1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta");
    }

    @Test
    void trocarSenha_novaCurta_recusa() {
        assertThatThrownBy(() -> contaService.trocarSenha("00000000004", "milscale123", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A nova senha precisa ter pelo menos 6 caracteres");
    }

    @Test
    void trocarSenha_valida_gravaHashNovo() {
        contaService.trocarSenha("00000000004", "milscale123", "novaSenha1");
        String hash = usuarioRepository.findByLogin("00000000004").orElseThrow().getSenhaHash();
        assertThat(passwordEncoder.matches("novaSenha1", hash)).isTrue();
    }
}
