package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.BoletimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RespostasLevesIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private BoletimService boletimService;

    @Test
    @WithUserDetails("00000000004")
    void listaDeBoletins_naoTrazOConteudo() throws Exception {
        boletimService.criar("1", "Com imagem", "<p>" + "x".repeat(5000) + "</p>", null, null, "00000000001");
        mvc.perform(get("/api/boletins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Com imagem"))
                .andExpect(content().string(not(containsString("conteudoHtml"))));
    }

    @Test
    @WithUserDetails("00000000001")
    void resumoDoPainel_contaSemBaixarListas() throws Exception {
        mvc.perform(get("/api/painel/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.militaresAtivos", greaterThan(100)));
    }

    @Test
    @WithUserDetails("00000000004")
    void resumoDoPainel_eSoDaSargenteacao() throws Exception {
        mvc.perform(get("/api/painel/resumo")).andExpect(status().isForbidden());
    }
}
