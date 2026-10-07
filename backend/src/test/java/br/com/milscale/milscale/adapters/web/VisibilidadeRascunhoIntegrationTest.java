package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.application.SolicitacaoService;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escala em RASCUNHO ainda pode mudar - so a sargenteacao enxerga.
 * Quem e escalado so ve (e so pede troca de) escala PUBLICADA.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VisibilidadeRascunhoIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SolicitacaoService solicitacaoService;

    private Escala escala;
    private ServicoEscalado servico;
    private Militar militarEscalado; // dono da conta 00000000004 (perfil MILITAR_ESCALADO)
    private LocalDate dia;

    @BeforeEach
    void montar() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        militarEscalado = usuarioRepository.findByLogin("00000000004").orElseThrow().getMilitar();
        TipoServico caboDaGuarda = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Cabo da Guarda")).findFirst().orElseThrow();
        dia = LocalDate.now().plusMonths(2).withDayOfMonth(10);
        escala = escalaRepository.save(Escala.builder().descricao("teste")
                .dataInicio(dia.withDayOfMonth(1)).dataFim(dia.withDayOfMonth(28))
                .situacao(SituacaoEscala.RASCUNHO).usuarioGeracao(sargenteante).build());
        servico = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia).tipoServico(caboDaGuarda).militar(militarEscalado).build());
    }

    private void publicar() {
        escala.setSituacao(SituacaoEscala.PUBLICADA);
        escalaRepository.save(escala);
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_naoVeRascunhoNaEscalaDoDia() throws Exception {
        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        publicar();
        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("00000000003") // Sd EP da Sargenteacao
    void sargenteacao_veRascunhoNaEscalaDoDia() throws Exception {
        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("00000000004")
    void minhaEscala_mostraSoPublicada() throws Exception {
        String mes = YearMonth.from(dia).toString();
        mvc.perform(get("/api/minha-escala").param("mes", mes))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        publicar();
        mvc.perform(get("/api/minha-escala").param("mes", mes))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("00000000004")
    void meuHistorico_naoMostraRascunho() throws Exception {
        mvc.perform(get("/api/militares/" + militarEscalado.getId() + "/historico-servicos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void pedirTrocaDeServicoEmRascunho_bloqueia() {
        Militar outro = usuarioRepository.findByLogin("00000000002").orElseThrow().getMilitar(); // Cabo
        assertThatThrownBy(() -> solicitacaoService.criar(servico.getId(), outro.getId(), "teste", "00000000004"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ainda não foi publicada");
    }
}
