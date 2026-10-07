package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cenario montado pra o limite mensal ser o UNICO motivo da escolha:
 * 7 Tenentes com prioridade maxima na fila, mas que ja tiraram 1 Oficial
 * de Dia no mes; 1 Tenente com prioridade menor e nenhum servico no mes.
 * Com maxServicosMes=1, so o oitavo pode ser escalado.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MaxServicosMesIntegrationTest {

    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private RegraEscalaService regraEscalaService;
    @Autowired private RegraEscalaRepository regraEscalaRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private TipoServico oficialDeDia;
    private RegraEscala regra;
    private Militar oitavo;
    private LocalDate dia;
    private Usuario sargenteante;

    @BeforeEach
    void montar() {
        sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        oficialDeDia = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Oficial de Dia")).findFirst().orElseThrow();
        regra = regraEscalaRepository.findByTipoServico_Id(oficialDeDia.getId()).orElseThrow();
        List<Militar> tenentes = militarRepository.findAll().stream()
                .filter(m -> "Ten".equals(m.getPosto().getSigla())).toList();
        assertThat(tenentes).hasSize(8);

        dia = LocalDate.now().plusMonths(2).withDayOfMonth(20);
        Escala anterior = escalaRepository.save(Escala.builder().descricao("anterior")
                .dataInicio(dia.withDayOfMonth(1)).dataFim(dia.withDayOfMonth(7)).usuarioGeracao(sargenteante).build());
        for (int i = 0; i < 7; i++) {
            Militar t = tenentes.get(i);
            t.setDataUltimoServico(LocalDate.of(2000, 1, 1)); // topo da fila
            militarRepository.save(t);
            servicoEscaladoRepository.save(ServicoEscalado.builder().escala(anterior)
                    .data(dia.withDayOfMonth(i + 1)).tipoServico(oficialDeDia).militar(t).build());
        }
        oitavo = tenentes.get(7);
        oitavo.setDataUltimoServico(dia.withDayOfMonth(5)); // fila mais baixa, mas sem servico no mes
        militarRepository.save(oitavo);
    }

    private Militar oficialDeDiaGerado() {
        Escala e = gerarEscalaService.gerar(dia, dia, sargenteante);
        return servicoEscaladoRepository.findByEscala_Id(e.getId()).stream()
                .filter(s -> s.getTipoServico().getId().equals(oficialDeDia.getId()))
                .findFirst().orElseThrow().getMilitar();
    }

    @Test
    void comLimite_escolheQuemAindaNaoAtingiu() {
        regra.setMaxServicosMes(1);
        regraEscalaRepository.save(regra);
        assertThat(oficialDeDiaGerado().getId()).isEqualTo(oitavo.getId());
    }

    @Test
    void semLimite_prioridadeDaFilaDecide_controleDoCenario() {
        regra.setMaxServicosMes(null);
        regraEscalaRepository.save(regra);
        assertThat(oficialDeDiaGerado().getId()).isNotEqualTo(oitavo.getId());
    }

    @Test
    void atualizarRegra_validaValores() {
        RegraEscala invalida = RegraEscala.builder().intervaloMinimo(-1).build();
        assertThatThrownBy(() -> regraEscalaService.atualizar(regra.getId(), invalida))
                .isInstanceOf(IllegalArgumentException.class);
        RegraEscala maxZero = RegraEscala.builder().intervaloMinimo(3).maxServicosMes(0).build();
        assertThatThrownBy(() -> regraEscalaService.atualizar(regra.getId(), maxZero))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
