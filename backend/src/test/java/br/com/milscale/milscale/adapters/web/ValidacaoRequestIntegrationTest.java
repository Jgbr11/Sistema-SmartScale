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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ValidacaoRequestIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000001")
    void gerarEscala_semDataFim_volta400ComMensagem() throws Exception {
        mvc.perform(post("/api/escalas/gerar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInicio\":\"2030-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Informe a data final"));
    }

    @Test
    @WithUserDetails("00000000001")
    void afastamento_semMilitares_volta400() throws Exception {
        mvc.perform(post("/api/afastamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"militarIds\":[],\"tipo\":\"FERIAS\",\"descricao\":\"x\",\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-02\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Selecione ao menos um militar"));
    }

    @Test
    @WithUserDetails("00000000001")
    void afastamento_tipoDesconhecido_volta400() throws Exception {
        mvc.perform(post("/api/afastamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"militarIds\":[5],\"tipo\":\"PASSEIO\",\"descricao\":\"x\",\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-02\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000004")
    void pedirTroca_semSubstituto_volta400EmVezDe500() throws Exception {
        mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoOrigemId\":1,\"justificativa\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Escolha o substituto"));
    }

    @Test
    @WithUserDetails("00000000004")
    void trocarSenha_curta_volta400ComMensagemAntiga() throws Exception {
        mvc.perform(post("/api/auth/senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"milscale123\",\"senhaNova\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("pelo menos 6")));
    }
}
