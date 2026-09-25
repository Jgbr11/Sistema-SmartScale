package br.com.milscale.milscale.adapters.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que todo erro da API volta com status previsivel e corpo
 * {"erro": ...} - o frontend (api/client.ts) depende dessa chave pra
 * mostrar a mensagem certa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TratadorDeErrosIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000004") // Militar Escalado
    void acessoNegado_volta403ComCorpo() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    @WithUserDetails("00000000001") // Sargenteante
    void idInexistente_volta404() throws Exception {
        mvc.perform(get("/api/militares/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void idComTipoErrado_volta400EmVezDe500() throws Exception {
        mvc.perform(get("/api/militares/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void rotaInexistente_continua404EmVezDe500() throws Exception {
        mvc.perform(get("/api/rota-que-nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("00000000001")
    void parametroObrigatorioFaltando_continua400() throws Exception {
        mvc.perform(get("/api/avisos")) // exige ?mes=
                .andExpect(status().isBadRequest());
    }
}
