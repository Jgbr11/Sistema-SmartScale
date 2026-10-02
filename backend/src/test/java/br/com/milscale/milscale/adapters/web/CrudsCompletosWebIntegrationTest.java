package br.com.milscale.milscale.adapters.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CrudsCompletosWebIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000001")
    void feriadoSemTipo_400() throws Exception {
        mvc.perform(post("/api/feriados").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-01\",\"descricao\":\"Ano novo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void feriadoInexistente_404() throws Exception {
        mvc.perform(put("/api/feriados/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-01\",\"descricao\":\"Ano novo\",\"tipo\":\"NACIONAL\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("00000000002")
    void caboNaoExcluiCurso_403() throws Exception {
        mvc.perform(delete("/api/qualificacoes/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscaladoNaoEditaAfastamento_403() throws Exception {
        mvc.perform(put("/api/afastamentos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"MISSAO\",\"descricao\":\"x\",\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-01\"}"))
                .andExpect(status().isForbidden());
    }
}
