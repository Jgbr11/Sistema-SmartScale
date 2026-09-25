package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rede de seguranca pra refatoracao da regra de descanso: afastamento
 * cadastrado DEPOIS da escala pronta realoca so a vaga da pessoa
 * afastada (dentro do periodo), pra alguem elegivel, sem violar o
 * intervalo minimo. Cenario montado a mao (como no
 * TrocaIntervaloIntegrationTest) pra ser deterministico.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AfastamentoReconciliacaoIntegrationTest {

    @Autowired private AfastamentoService afastamentoService;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    void afastamentoDepoisDaEscala_realocaSoAVagaDentroDoPeriodo() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        TipoServico caboDaGuarda = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Cabo da Guarda")).findFirst().orElseThrow();
        List<Militar> cabos = militarRepository.findAll().stream()
                .filter(m -> "Cb".equals(m.getPosto().getSigla()))
                .filter(m -> !"Aprov".equals(m.getSubunidade().getSigla()))
                .toList();
        Militar afastado = cabos.get(0);
        Militar vizinho = cabos.get(1); // tem servico a 2 dias do alvo: NAO pode ser o escolhido (RN06)

        LocalDate dia = LocalDate.now().plusMonths(2).withDayOfMonth(10);
        Escala escala = escalaRepository.save(Escala.builder().descricao("teste")
                .dataInicio(dia.withDayOfMonth(1)).dataFim(dia.withDayOfMonth(28)).usuarioGeracao(sargenteante).build());
        ServicoEscalado alvo = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia).tipoServico(caboDaGuarda).militar(afastado).build());
        ServicoEscalado foraDoPeriodo = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia.plusDays(10)).tipoServico(caboDaGuarda).militar(afastado).build());
        servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia.plusDays(2)).tipoServico(caboDaGuarda).militar(vizinho).build());

        afastamentoService.cadastrarMissao(List.of(afastado.getId()), TipoAfastamento.DISPENSA, "teste",
                dia, dia, "00000000001");

        ServicoEscalado depois = servicoEscaladoRepository.findById(alvo.getId()).orElseThrow();
        assertThat(depois.getMilitar()).as("vaga realocada pra alguem").isNotNull();
        assertThat(depois.getMilitar().getId()).isNotIn(afastado.getId(), vizinho.getId());
        assertThat(depois.getMilitar().getPosto().getSigla()).isEqualTo("Cb");
        assertThat(depois.getMilitar().getSubunidade().getSigla()).isNotEqualTo("Aprov");
        assertThat(depois.getObservacao()).startsWith("Realocado automaticamente");
        assertThat(servicoEscaladoRepository.findById(foraDoPeriodo.getId()).orElseThrow().getMilitar().getId())
                .as("servico fora do periodo do afastamento continua com ele")
                .isEqualTo(afastado.getId());
    }
}
