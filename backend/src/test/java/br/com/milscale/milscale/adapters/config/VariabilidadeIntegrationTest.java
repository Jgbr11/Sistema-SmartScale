package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.application.CriterioMenorCargaNaGeracao;
import br.com.milscale.milscale.application.MilitarEmGeracao;
import br.com.smartscale.core.CatalogoDeCriterios;
import br.com.smartscale.core.CriterioDeOrdenacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "milscale.lps.criterio-ordenacao=menor-carga")
@ActiveProfiles("test")
class VariabilidadeIntegrationTest {

    @Autowired private CriterioDeOrdenacao<MilitarEmGeracao> criterio;

    @Test
    void oCriterioDaFilaVemDaConfiguracao() {
        assertThat(criterio).isInstanceOf(CriterioMenorCargaNaGeracao.class);
    }

    @Test
    void asTresVariantesFicamRegistradasNoCatalogo() {
        assertThat(CatalogoDeCriterios.instancia().nomes()).contains("maior-folga", "menor-carga", "mais-moderno");
    }
}
