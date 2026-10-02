package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.application.CriterioMaisModernoPrimeiro;
import br.com.milscale.milscale.application.CriterioMenorCargaNaGeracao;
import br.com.milscale.milscale.application.MilitarEmGeracao;
import br.com.milscale.milscale.domain.CriterioOrdenacaoMilitar;
import br.com.smartscale.core.CatalogoDeCriterios;
import br.com.smartscale.core.CriterioDeOrdenacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VariabilidadeConfig {

    @Bean
    public CriterioDeOrdenacao<MilitarEmGeracao> criterioDeOrdenacao(
            @Value("${milscale.lps.criterio-ordenacao:maior-folga}") String nome) {
        CatalogoDeCriterios catalogo = CatalogoDeCriterios.instancia();
        catalogo.registrar("maior-folga", new CriterioOrdenacaoMilitar<MilitarEmGeracao>());
        catalogo.registrar("menor-carga", new CriterioMenorCargaNaGeracao());
        catalogo.registrar("mais-moderno", new CriterioMaisModernoPrimeiro());
        return catalogo.obter(nome);
    }
}
