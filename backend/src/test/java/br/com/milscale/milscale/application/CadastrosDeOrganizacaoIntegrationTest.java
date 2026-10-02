package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.PostoGraduacaoRepository;
import br.com.milscale.milscale.adapters.persistence.SubunidadeRepository;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.milscale.milscale.domain.Subunidade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CadastrosDeOrganizacaoIntegrationTest {

    @Autowired private PostoGraduacaoService postoService;
    @Autowired private PostoGraduacaoRepository postoRepository;
    @Autowired private SubunidadeService subunidadeService;
    @Autowired private SubunidadeRepository subunidadeRepository;

    private PostoGraduacao cabo() {
        return postoRepository.findAll().stream().filter(p -> p.getSigla().equals("Cb")).findFirst().orElseThrow();
    }

    @Test
    void postos_saoListadosPelaHierarquia() {
        assertThat(postoService.listar()).extracting(PostoGraduacao::getNivelHierarquico).isSorted();
    }

    @Test
    void criarEditarEExcluirPostoSemUso() {
        PostoGraduacao criado = postoService.cadastrar(new DadosPostoGraduacao("1 Ten", "Primeiro-Tenente", 99));
        postoService.atualizar(criado.getId(), new DadosPostoGraduacao("1º Ten", "Primeiro-Tenente", 99));
        assertThat(postoRepository.findById(criado.getId()).orElseThrow().getSigla()).isEqualTo("1º Ten");

        postoService.excluir(criado.getId());
        assertThat(postoRepository.findById(criado.getId())).isEmpty();
    }

    @Test
    void postoComSiglaOuNivelRepetido_recusa() {
        assertThatThrownBy(() -> postoService.cadastrar(new DadosPostoGraduacao("cb", "Outro", 98)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("sigla");
        assertThatThrownBy(() -> postoService.cadastrar(new DadosPostoGraduacao("Xx", "Outro", cabo().getNivelHierarquico())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nível");
    }

    @Test
    void editarPostoMantendoAPropriaSigla_aceita() {
        PostoGraduacao cb = cabo();
        postoService.atualizar(cb.getId(), new DadosPostoGraduacao("Cb", "Cabo (editado)", cb.getNivelHierarquico()));
        assertThat(postoRepository.findById(cb.getId()).orElseThrow().getDescricao()).isEqualTo("Cabo (editado)");
    }

    @Test
    void excluirPostoEmUso_recusa() {
        assertThatThrownBy(() -> postoService.excluir(cabo().getId()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("em uso");
    }

    @Test
    void criarEditarEExcluirSubunidadeSemUso() {
        Subunidade criada = subunidadeService.cadastrar(new DadosSubunidade("4ª Cia", "Quarta Companhia", true));
        subunidadeService.atualizar(criada.getId(), new DadosSubunidade("4ª Cia", "Quarta Cia de Fuzileiros", true));
        assertThat(subunidadeRepository.findById(criada.getId()).orElseThrow().getNome()).isEqualTo("Quarta Cia de Fuzileiros");

        assertThat(subunidadeService.excluir(criada.getId())).isFalse();
        assertThat(subunidadeRepository.findById(criada.getId())).isEmpty();
    }

    @Test
    void subunidadeComSiglaRepetida_recusa() {
        assertThatThrownBy(() -> subunidadeService.cadastrar(new DadosSubunidade("ccap", "Outra", true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("sigla");
    }

    @Test
    void excluirSubunidadeEmUso_soDesativa() {
        Subunidade emUso = subunidadeRepository.findAll().stream().filter(s -> s.getSigla().equals("CCAp")).findFirst().orElseThrow();
        assertThat(subunidadeService.excluir(emUso.getId())).isTrue();
        assertThat(subunidadeRepository.findById(emUso.getId()).orElseThrow().isAtivo()).isFalse();
    }
}
