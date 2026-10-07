package br.com.smartscale.core;

// Núcleo reutilizável (LPS)

import java.util.Comparator;

@FunctionalInterface
public interface CriterioDeOrdenacao<T extends PessoaEscalada> {
    Comparator<T> comparator();
}
