package br.com.milscale.milscale.domain;

// Especialização MilScale (LPS)

import br.com.smartscale.core.CriterioDeOrdenacao;
import br.com.smartscale.core.PessoaEscalada;

import java.util.Comparator;

public class CriterioOrdenacaoMilitar<P extends PessoaEscalada> implements CriterioDeOrdenacao<P> {
    @Override
    public Comparator<P> comparator() {

        return Comparator.comparingLong(P::getContadorRodizio).reversed();
    }
}
