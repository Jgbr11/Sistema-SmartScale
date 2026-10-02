package br.com.smartscale.core;

import java.util.Comparator;

@FunctionalInterface
public interface CriterioDeOrdenacao<T extends PessoaEscalada> {
    Comparator<T> comparator();
}
