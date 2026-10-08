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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CadastrosDeOrganizacaoWebIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000004")
    void qualquerLogadoConsultaPostosESubunidades() throws Exception {
        mvc.perform(get("/api/postos-graduacao")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nivelHierarquico").value(1));
        mvc.perform(get("/api/subunidades")).andExpect(status().isOk()).andExpect(jsonPath("$[0].sigla").exists());
    }

    @Test
    @WithUserDetails("00000000002")
    void caboNaoCadastraPosto_403() throws Exception {
        mvc.perform(post("/api/postos-graduacao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sigla\":\"Asp\",\"descricao\":\"Aspirante\",\"nivelHierarquico\":50}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("00000000001")
    void postoSemNivel_400() throws Exception {
        mvc.perform(post("/api/postos-graduacao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sigla\":\"Asp\",\"descricao\":\"Aspirante\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }
}
