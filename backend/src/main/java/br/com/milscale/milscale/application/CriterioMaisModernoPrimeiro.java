package br.com.milscale.milscale.application;

import br.com.smartscale.core.CriterioDeOrdenacao;

import java.util.Comparator;

public class CriterioMaisModernoPrimeiro implements CriterioDeOrdenacao<MilitarEmGeracao> {

    @Override
    public Comparator<MilitarEmGeracao> comparator() {
        return Comparator.comparingInt(MilitarEmGeracao::getNivelHierarquico)
                .thenComparing(Comparator.comparingLong(MilitarEmGeracao::getContadorRodizio).reversed());
    }
}
