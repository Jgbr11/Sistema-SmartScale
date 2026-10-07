package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.application.GerarEscalaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EscalaDoMesIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    @WithUserDetails("00000000001")
    void mesGeradoEmDuasPartes_mostraAsDuas() throws Exception {
        var sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        YearMonth mes = YearMonth.now().plusMonths(2);
        gerarEscalaService.gerar(mes.atDay(1), mes.atDay(3), sargenteante);
        gerarEscalaService.gerar(mes.atDay(4), mes.atDay(6), sargenteante);

        mvc.perform(get("/api/escalas/mes").param("mes", mes.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escalas", hasSize(2)))
                .andExpect(jsonPath("$.servicos[*].data", hasItems(mes.atDay(1).toString(), mes.atDay(6).toString())));
    }

    @Test
    @WithUserDetails("00000000001")
    void listaDeEscalas_naoTrazOsServicos() throws Exception {
        var sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        gerarEscalaService.gerar(inicio, inicio.plusDays(2), sargenteante);

        mvc.perform(get("/api/escalas"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"servicos\""))))
                .andExpect(jsonPath("$[0].totalServicos", greaterThan(0)));
    }
}
