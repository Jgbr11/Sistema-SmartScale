package br.com.milscale.milscale.application;

import br.com.smartscale.core.CriterioDeOrdenacao;

import java.util.Comparator;

public class CriterioMenorCargaNaGeracao implements CriterioDeOrdenacao<MilitarEmGeracao> {

    @Override
    public Comparator<MilitarEmGeracao> comparator() {
        return Comparator.comparingInt(MilitarEmGeracao::getServicosNaGeracao)
                .thenComparing(Comparator.comparingLong(MilitarEmGeracao::getContadorRodizio).reversed());
    }
}
