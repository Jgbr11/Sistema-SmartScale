package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.application.AfastamentoService;
import br.com.milscale.milscale.domain.TipoAfastamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioNaoVazaIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private AfastamentoService afastamentoService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    @WithUserDetails("00000000002")
    void afastamentos_naoTrazemAContaDeQuemRegistrou() throws Exception {
        Long militarId = usuarioRepository.findByLogin("00000000004").orElseThrow().getMilitar().getId();
        LocalDate dia = LocalDate.now().plusMonths(2);
        afastamentoService.cadastrarMissao(List.of(militarId), TipoAfastamento.DISPENSA, "teste", dia, dia, "00000000001");

        mvc.perform(get("/api/afastamentos"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("usuarioRegistro"))))
                .andExpect(content().string(not(containsString("\"login\""))));
    }
}
