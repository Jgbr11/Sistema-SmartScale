package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import br.com.milscale.milscale.adapters.persistence.RequisitoServicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MassAssignmentIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private RequisitoServicoRepository requisitoServicoRepository;

    @Test
    @WithUserDetails("00000000001")
    void cadastrarMilitar_ignoraCamposQueATelaNaoEnvia() throws Exception {
        mvc.perform(post("/api/militares").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeCompleto":"Teste Mass","nomeGuerra":"Massa","cpf":"52998224725",
                 "postoId":1,"subunidadeId":1,
                 "dataUltimoServico":"2000-01-01","situacao":"DESLIGADO","qualificacoes":[{"id":1}]}"""))
                .andExpect(status().isOk());
        var m = militarRepository.findByCpf("52998224725").orElseThrow();
        assertThat(m.getDataUltimoServico()).isNull();
        assertThat(m.getSituacao().name()).isEqualTo("ATIVO");
        assertThat(m.getQualificacoes()).isEmpty();
    }

    @Test
    @WithUserDetails("00000000001")
    void cadastrarTipoServico_ignoraRequisitosEAtivoNoCorpo() throws Exception {
        long requisitosAntes = requisitoServicoRepository.count();
        mvc.perform(post("/api/tipos-servico").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Sentinela Mass","efetivoNecessario":1,"ativo":false,
                 "requisitos":[{"posto":{"id":1}}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        assertThat(requisitoServicoRepository.count()).isEqualTo(requisitosAntes);
    }

    @Test
    @WithUserDetails("00000000001")
    void cadastrarMilitar_semPosto_volta400ComMensagem() throws Exception {
        mvc.perform(post("/api/militares").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeCompleto":"Sem Posto","nomeGuerra":"Semposto","cpf":"11144477735","subunidadeId":1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Escolha o posto/graduação"));
    }
}
