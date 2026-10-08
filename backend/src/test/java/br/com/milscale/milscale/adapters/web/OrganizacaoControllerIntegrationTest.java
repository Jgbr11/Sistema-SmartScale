package br.com.milscale.milscale.adapters.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizacaoControllerIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    void identidadeEPublica_paraATelaDeLogin() throws Exception {
        mvc.perform(get("/api/organizacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("5º Batalhão de Suprimento"))
                .andExpect(jsonPath("$.sigla").value("5º B Sup"))
                .andExpect(jsonPath("$.sistema").value("MilScale"));
    }
}
