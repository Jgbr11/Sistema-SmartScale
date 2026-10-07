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
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TipoServicoRequisitoIntegrationTest {

    @Autowired private TipoServicoService tipoServicoService;
    @Autowired private RequisitoServicoService requisitoServicoService;
    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private RegraEscalaRepository regraEscalaRepository;
    @Autowired private PostoGraduacaoRepository postoRepository;
    @Autowired private SubunidadeRepository subunidadeRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private TipoServico criarTipo(String nome) {
        return tipoServicoService.cadastrar(new DadosTipoServico(nome, null, 1, null, null));
    }

    private Long id(String siglaPosto) {
        return postoRepository.findAll().stream().filter(p -> p.getSigla().equals(siglaPosto)).findFirst().orElseThrow().getId();
    }

    private Long subunidade(String sigla) {
        return subunidadeRepository.findAll().stream().filter(s -> s.getSigla().equals(sigla)).findFirst().orElseThrow().getId();
    }

    private List<ServicoEscalado> gerar3DiasDoTipo(TipoServico tipo) {
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        Escala e = gerarEscalaService.gerar(inicio, inicio.plusDays(2), usuarioRepository.findByLogin("00000000001").orElseThrow());
        return servicoEscaladoRepository.findByEscala_Id(e.getId()).stream()
                .filter(s -> s.getTipoServico().getId().equals(tipo.getId())).toList();
    }

    @Test
    void cadastrarTipo_criaRegraPadrao3x1() {
        TipoServico t = criarTipo("Sentinela Extra");
        RegraEscala regra = regraEscalaRepository.findByTipoServico_Id(t.getId()).orElseThrow();
        assertThat(regra.getIntervaloMinimo()).isEqualTo(3);
        assertThat(regra.getMaxServicosMes()).isNull();
    }

    @Test
    void tipoSemRequisito_geraVagaAberta_porIssoATelaAvisa() {
        TipoServico t = criarTipo("Sentinela Extra");
        assertThat(gerar3DiasDoTipo(t)).allMatch(s -> s.getMilitar() == null);
    }

    @Test
    void comRequisito_tipoNovoEPreenchidoPorQuemCumpre() {
        TipoServico t = criarTipo("Sentinela Extra");
        requisitoServicoService.adicionar(t.getId(),
                new NovoRequisito(id("Sd EV"), null, null, Set.of(), subunidade("Aprov")));

        List<ServicoEscalado> servicos = gerar3DiasDoTipo(t);
        assertThat(servicos).hasSize(3).allMatch(s -> s.getMilitar() != null);
        assertThat(servicos).allMatch(s -> s.getMilitar().getPosto().getSigla().equals("Sd EV")
                && !s.getMilitar().getSubunidade().getSigla().equals("Aprov"));
    }

    @Test
    void exigirEExcluirSubunidadeAoMesmoTempo_recusa() {
        TipoServico t = criarTipo("Sentinela Extra");
        assertThatThrownBy(() -> requisitoServicoService.adicionar(t.getId(),
                new NovoRequisito(id("Sd EV"), subunidade("CCAp"), null, Set.of(), subunidade("Aprov"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removerRequisitoDeOutroTipo_naoEncontra() {
        TipoServico t = criarTipo("Sentinela Extra");
        RequisitoServico r = requisitoServicoService.adicionar(t.getId(), new NovoRequisito(id("Sd EV"), null, null, Set.of(), null));
        TipoServico outro = tipoServicoRepository.findAll().stream().filter(x -> !x.getId().equals(t.getId())).findFirst().orElseThrow();
        assertThatThrownBy(() -> requisitoServicoService.remover(outro.getId(), r.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void removerRequisito_tiraDaLista() {
        TipoServico t = criarTipo("Sentinela Extra");
        RequisitoServico r = requisitoServicoService.adicionar(t.getId(), new NovoRequisito(id("Sd EV"), null, null, Set.of(), null));
        requisitoServicoService.remover(t.getId(), r.getId());
        assertThat(requisitoServicoService.listar(t.getId())).isEmpty();
    }
}
