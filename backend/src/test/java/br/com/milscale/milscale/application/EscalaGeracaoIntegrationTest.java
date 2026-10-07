package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Escala;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.TipoAfastamento;
import br.com.milscale.milscale.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EscalaGeracaoIntegrationTest {

    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AfastamentoRepository afastamentoRepository;

    private Escala gerarProximoMesCompleto() {
        Usuario usuario = usuarioRepository.findByLogin("00000000001").orElseThrow();
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return gerarEscalaService.gerar(inicio, fim, usuario);
    }

    @Test
    void gerarMesInteiro_naoDeixaVagaAberta() {
        Escala escala = gerarProximoMesCompleto();
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByEscala_Id(escala.getId());

        long vagasAbertas = servicos.stream().filter(s -> s.getMilitar() == null).count();

        assertThat(servicos).isNotEmpty();
        assertThat(vagasAbertas).as("vagas em aberto na escala gerada").isZero();
    }

    @Test
    void gerarMesInteiro_respeitaIntervaloMinimoDeTresDiasDeFolga() {

        Escala escala = gerarProximoMesCompleto();
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByEscala_Id(escala.getId());

        Map<Long, List<LocalDate>> datasPorMilitar = servicos.stream()
                .filter(s -> s.getMilitar() != null)
                .collect(Collectors.groupingBy(s -> s.getMilitar().getId(),
                        Collectors.mapping(ServicoEscalado::getData, Collectors.toList())));

        for (var entrada : datasPorMilitar.entrySet()) {
            List<LocalDate> datas = entrada.getValue().stream().sorted().toList();
            for (int i = 1; i < datas.size(); i++) {
                long gap = ChronoUnit.DAYS.between(datas.get(i - 1), datas.get(i));
                assertThat(gap)
                        .as("intervalo entre dois serviços do militar id %d (%s -> %s)", entrada.getKey(), datas.get(i - 1), datas.get(i))
                        .isGreaterThanOrEqualTo(4);
            }
        }
    }

    @Test
    void gerarMesInteiro_ninguemDoAprovisionamentoTiraServicoForaDoRancho() {
        Escala escala = gerarProximoMesCompleto();
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByEscala_Id(escala.getId());

        var funcoesDoRancho = Set.of("Rancheiro de Dia", "Cozinheiro de Dia", "Graduado do Rancho");

        List<ServicoEscalado> vazamentos = servicos.stream()
                .filter(s -> s.getMilitar() != null)
                .filter(s -> "Aprov".equals(s.getMilitar().getSubunidade().getSigla()))
                .filter(s -> !funcoesDoRancho.contains(s.getTipoServico().getNome()))
                .toList();

        assertThat(vazamentos)
                .as("militares do Aprovisionamento escalados fora do rancho")
                .isEmpty();
    }

    @Test
    void gerarMesInteiro_ninguemComCfcOuMotoristaTiraMonitoramento() {
        Escala escala = gerarProximoMesCompleto();
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByEscala_Id(escala.getId());

        List<Militar> vazamentos = servicos.stream()
                .filter(s -> "Monitoramento".equals(s.getTipoServico().getNome()))
                .filter(s -> s.getMilitar() != null)
                .map(ServicoEscalado::getMilitar)
                .filter(m -> m.getQualificacoes().stream().anyMatch(q -> q.getNome().equals("CFC") || q.getNome().equals("Motorista")))
                .toList();

        assertThat(vazamentos)
                .as("militares com CFC/Motorista escalados em Monitoramento")
                .isEmpty();
    }

    @Test
    void efetivoInsuficiente_apertaAEscalaEmVezDeDeixarVagaAberta() {

        List<Militar> tenentes = militarRepository.findAll().stream()
                .filter(m -> "Ten".equals(m.getPosto().getSigla()))
                .toList();
        Militar sobrevivente = tenentes.get(0);
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        Usuario usuario = usuarioRepository.findByLogin("00000000001").orElseThrow();
        for (Militar t : tenentes) {
            if (t.getId().equals(sobrevivente.getId())) continue;
            afastamentoRepository.save(Afastamento.builder()
                    .militar(t).tipo(TipoAfastamento.FERIAS).descricao("Teste de escassez").dataInicio(inicio).dataFim(fim)
                    .usuarioRegistro(usuario).build());
        }

        Escala escala = gerarEscalaService.gerar(inicio, fim, usuario);
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByEscala_Id(escala.getId());

        List<ServicoEscalado> oficialDeDia = servicos.stream()
                .filter(s -> "Oficial de Dia".equals(s.getTipoServico().getNome()))
                .toList();

        long vagasAbertas = oficialDeDia.stream().filter(s -> s.getMilitar() == null).count();
        assertThat(vagasAbertas).as("vagas abertas em Oficial de Dia mesmo com só 1 Tenente disponível").isZero();

        long vezesQueOSobreviventeServiu = oficialDeDia.stream()
                .filter(s -> s.getMilitar() != null && s.getMilitar().getId().equals(sobrevivente.getId()))
                .count();
        assertThat(vezesQueOSobreviventeServiu).as("o único Tenente disponível deveria ter coberto o mês inteiro sozinho")
                .isEqualTo(oficialDeDia.size());
    }
}
