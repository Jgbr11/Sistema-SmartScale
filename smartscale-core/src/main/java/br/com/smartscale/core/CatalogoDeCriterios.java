package br.com.smartscale.core;

// Núcleo reutilizável (LPS)

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public final class CatalogoDeCriterios {

    private final Map<String, CriterioDeOrdenacao<?>> criterios = new ConcurrentHashMap<>();

    private CatalogoDeCriterios() {
    }

    private static final class Portador {
        private static final CatalogoDeCriterios INSTANCIA = new CatalogoDeCriterios();
    }

    public static CatalogoDeCriterios instancia() {
        return Portador.INSTANCIA;
    }

    public void registrar(String nome, CriterioDeOrdenacao<?> criterio) {
        criterios.put(nome, criterio);
    }

    @SuppressWarnings("unchecked")
    public <P extends PessoaEscalada> CriterioDeOrdenacao<P> obter(String nome) {
        CriterioDeOrdenacao<?> criterio = criterios.get(nome);
        if (criterio == null) {
            throw new IllegalArgumentException("Critério de ordenação desconhecido: " + nome + ". Opções: " + nomes());
        }
        return (CriterioDeOrdenacao<P>) criterio;
    }

    public Set<String> nomes() {
        return new TreeSet<>(criterios.keySet());
    }
}
