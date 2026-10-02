package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.FeriadoRepository;
import br.com.milscale.milscale.adapters.persistence.QualificacaoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Feriado;
import br.com.milscale.milscale.domain.Qualificacao;
import br.com.milscale.milscale.domain.TipoAfastamento;
import br.com.milscale.milscale.domain.TipoFeriado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CrudsCompletosIntegrationTest {

    @Autowired private QualificacaoService qualificacaoService;
    @Autowired private QualificacaoRepository qualificacaoRepository;
    @Autowired private FeriadoService feriadoService;
    @Autowired private FeriadoRepository feriadoRepository;
    @Autowired private AfastamentoService afastamentoService;
    @Autowired private AfastamentoRepository afastamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    void excluirCursoSemUso_remove() {
        Qualificacao q = qualificacaoRepository.save(Qualificacao.builder().nome("Curso Avulso").build());
        qualificacaoService.excluir(q.getId());
        assertThat(qualificacaoRepository.findById(q.getId())).isEmpty();
    }

    @Test
    void excluirCursoEmUso_recusa() {
        Qualificacao cfc = qualificacaoRepository.findAll().stream().filter(q -> q.getNome().equals("CFC")).findFirst().orElseThrow();
        assertThatThrownBy(() -> qualificacaoService.excluir(cfc.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("em uso");
    }

    @Test
    void cadastrarEEditarFeriado() {
        Feriado criado = feriadoService.cadastrar(new DadosFeriado(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 1), "Ano novo", TipoFeriado.NACIONAL));
        feriadoService.atualizar(criado.getId(), new DadosFeriado(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2), "Ano novo + ponte", TipoFeriado.OM));

        Feriado depois = feriadoRepository.findById(criado.getId()).orElseThrow();
        assertThat(depois.getDataFim()).isEqualTo(LocalDate.of(2030, 1, 2));
        assertThat(depois.getDescricao()).isEqualTo("Ano novo + ponte");
        assertThat(depois.getTipo()).isEqualTo(TipoFeriado.OM);
    }

    @Test
    void editarFeriadoComDatasInvertidas_recusa() {
        Feriado criado = feriadoService.cadastrar(new DadosFeriado(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 1), "Ano novo", TipoFeriado.NACIONAL));
        assertThatThrownBy(() -> feriadoService.atualizar(criado.getId(),
                new DadosFeriado(LocalDate.of(2030, 1, 5), LocalDate.of(2030, 1, 1), "Ano novo", TipoFeriado.NACIONAL)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void editarAfastamentoDeLote_alteraTodosDoLote() {
        List<Long> dois = usuarioRepository.findAll().stream().limit(2).map(u -> u.getMilitar().getId()).toList();
        LocalDate dia = LocalDate.now().plusMonths(3);
        List<Afastamento> criados = afastamentoService.cadastrarMissao(dois, TipoAfastamento.MISSAO, "Missão A", dia, dia, "00000000001");

        afastamentoService.atualizar(criados.get(0).getId(), TipoAfastamento.MISSAO, "Missão A (estendida)", dia, dia.plusDays(2));

        assertThat(afastamentoRepository.findAllById(criados.stream().map(Afastamento::getId).toList()))
                .hasSize(2)
                .allMatch(a -> a.getDescricao().equals("Missão A (estendida)") && a.getDataFim().equals(dia.plusDays(2)));
    }

    @Test
    void editarAfastamentoComDatasInvertidas_recusa() {
        List<Long> um = usuarioRepository.findAll().stream().limit(1).map(u -> u.getMilitar().getId()).toList();
        LocalDate dia = LocalDate.now().plusMonths(3);
        Afastamento criado = afastamentoService.cadastrarMissao(um, TipoAfastamento.DISPENSA, "Dispensa", dia, dia, "00000000001").get(0);
        assertThatThrownBy(() -> afastamentoService.atualizar(criado.getId(), TipoAfastamento.DISPENSA, "Dispensa", dia, dia.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
