package br.com.milscale.milscale.adapters.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    private static final String CPF = "00000000003";

    @Autowired private MockMvc mvc;
    @Autowired private ProtecaoContraForcaBruta protecao;

    @AfterEach
    void limpar() { protecao.limpar(CPF); }

    @Test
    void senhaErrada_volta401ComMensagem() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", CPF).param("password", "errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("CPF ou senha inválidos"));
    }

    @Test
    void cincoFalhas_bloqueiaMesmoComSenhaCerta() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").param("username", CPF).param("password", "errada"));
        }
        mvc.perform(post("/api/auth/login").param("username", CPF).param("password", "milscale123"))
                .andExpect(status().is(423))
                .andExpect(jsonPath("$.erro").value("Muitas tentativas erradas. Tente de novo em 15 minutos."));
    }
}
