package br.com.milscale.milscale.adapters.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RelatorioControllerIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000001")
    void escalaDoDia_baixaComoCsv() throws Exception {
        mvc.perform(get("/api/relatorios/escala-do-dia.csv").param("data", "2030-01-10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("escala-2030-01-10.csv")))
                .andExpect(content().string(containsString("Função;Posto;Nome de guerra;Situação")));
    }

    @Test
    @WithUserDetails("00000000002")
    void servicosDoMilitar_eAfastamentos_tambemParaOCabo() throws Exception {
        mvc.perform(get("/api/relatorios/militares/1/servicos.csv"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Serviços de 2 Sgt Zeni")));
        mvc.perform(get("/api/relatorios/afastamentos.csv").param("mes", "2030-01"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("afastamentos-2030-01.csv")));
    }

    @Test
    @WithUserDetails("00000000001")
    void militarInexistente_404_eMesInvalido_400() throws Exception {
        mvc.perform(get("/api/relatorios/militares/999999/servicos.csv")).andExpect(status().isNotFound());
        mvc.perform(get("/api/relatorios/afastamentos.csv").param("mes", "janeiro")).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_naoBaixaRelatorio() throws Exception {
        mvc.perform(get("/api/relatorios/escala-do-dia.csv").param("data", "2030-01-10"))
                .andExpect(status().isForbidden());
    }
}
