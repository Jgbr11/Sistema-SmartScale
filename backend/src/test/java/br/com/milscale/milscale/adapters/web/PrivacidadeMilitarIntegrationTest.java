package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrivacidadeMilitarIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;

    private Long idDe(String login) {
        return usuarioRepository.findByLogin(login).orElseThrow().getMilitar().getId();
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_naoVeDadosPessoaisDeColega() throws Exception {
        mvc.perform(get("/api/militares/" + idDe("00000000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeGuerra").value("Zeni"))
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.dataNascimento").doesNotExist())
                .andExpect(jsonPath("$.fusex").doesNotExist())
                .andExpect(jsonPath("$.fotoBase64").doesNotExist());
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_veOsProprios() throws Exception {
        mvc.perform(get("/api/militares/" + idDe("00000000004")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("00000000004"));
    }

    @Test
    @WithUserDetails("00000000001")
    void sargenteante_veDadosCompletos() throws Exception {
        mvc.perform(get("/api/militares/" + idDe("00000000004")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("00000000004"))
                .andExpect(jsonPath("$.dataNascimento").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void escalaDoDiaENaListagem_naoCarregamCpfNemFoto() throws Exception {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        Militar m = sargenteante.getMilitar();
        m.setFotoBase64("data:image/png;base64,AAAA");
        militarRepository.save(m);
        TipoServico tipo = tipoServicoRepository.findAll().get(0);
        LocalDate dia = LocalDate.now().plusMonths(2).withDayOfMonth(3);
        Escala escala = escalaRepository.save(Escala.builder().descricao("t").dataInicio(dia).dataFim(dia)
                .situacao(SituacaoEscala.PUBLICADA).usuarioGeracao(sargenteante).build());
        servicoEscaladoRepository.save(ServicoEscalado.builder().escala(escala).data(dia).tipoServico(tipo).militar(m).build());

        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"cpf\""))))
                .andExpect(content().string(not(containsString("fotoBase64"))))
                .andExpect(jsonPath("$[0].militar.temFoto").value(true));
        mvc.perform(get("/api/militares"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("fotoBase64"))));
    }

    @Test
    @WithUserDetails("00000000004")
    void fotoTemEndpointProprio() throws Exception {
        Militar m = militarRepository.findById(idDe("00000000001")).orElseThrow();
        m.setFotoBase64("data:image/png;base64,AAAA");
        militarRepository.save(m);

        mvc.perform(get("/api/militares/" + m.getId() + "/foto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoBase64").value("data:image/png;base64,AAAA"));
        mvc.perform(get("/api/militares/" + idDe("00000000002") + "/foto"))
                .andExpect(status().isNoContent());
    }
}
