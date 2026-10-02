package br.com.smartscale.core;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogoDeCriteriosTest {

    @Test
    void sempreAMesmaInstancia() {
        assertThat(CatalogoDeCriterios.instancia()).isSameAs(CatalogoDeCriterios.instancia());
    }

    @Test
    void devolveOCriterioRegistradoPeloNome() {
        CriterioDeOrdenacao<PessoaEscalada> porId = () -> Comparator.comparing(PessoaEscalada::getId);
        CatalogoDeCriterios.instancia().registrar("por-id", porId);
        assertThat(CatalogoDeCriterios.instancia().<PessoaEscalada>obter("por-id")).isSameAs(porId);
        assertThat(CatalogoDeCriterios.instancia().nomes()).contains("por-id");
    }

    @Test
    void nomeDesconhecido_explicaAsOpcoes() {
        assertThatThrownBy(() -> CatalogoDeCriterios.instancia().obter("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inexistente");
    }
}
