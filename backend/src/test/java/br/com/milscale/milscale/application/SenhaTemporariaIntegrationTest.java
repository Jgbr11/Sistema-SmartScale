package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SenhaTemporariaIntegrationTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private ContaService contaService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MockMvc mvc;

    private Usuario usuario4() {
        return usuarioRepository.findByLogin("00000000004").orElseThrow();
    }

    @Test
    void resetar_geraSenhaAleatoriaEMarcaComoTemporaria() {
        String senha = usuarioService.resetarSenha(usuario4().getId());
        String outra = usuarioService.resetarSenha(usuario4().getId());

        assertThat(senha).hasSize(10).isNotEqualTo("milscale123").isNotEqualTo(outra);
        Usuario u = usuario4();
        assertThat(passwordEncoder.matches(outra, u.getSenhaHash())).isTrue();
        assertThat(u.isSenhaTemporaria()).isTrue();
    }

    @Test
    void trocarSenha_tiraAMarcaDeTemporaria() {
        String temp = usuarioService.resetarSenha(usuario4().getId());
        contaService.trocarSenha("00000000004", temp, "minhaSenhaNova");
        assertThat(usuario4().isSenhaTemporaria()).isFalse();
    }

    @Test
    @WithUserDetails("00000000004")
    void comSenhaTemporaria_apiBloqueadaMenosAuth() throws Exception {
        Usuario u = usuario4();
        u.setSenhaTemporaria(true);
        usuarioRepository.save(u);

        mvc.perform(get("/api/boletins"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Troque sua senha temporária antes de continuar"));
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocarSenha").value(true));
    }

    @Test
    @WithUserDetails("00000000004")
    void semSenhaTemporaria_apiLiberada() throws Exception {
        mvc.perform(get("/api/boletins")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me")).andExpect(jsonPath("$.trocarSenha").value(false));
    }
}
