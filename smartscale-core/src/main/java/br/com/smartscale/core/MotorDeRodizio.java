package br.com.smartscale.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class MotorDeRodizio<P extends PessoaEscalada, T extends TipoTurno> {


    public List<P> preencherVagas(List<P> elegiveisDisponiveis, T turno, CriterioDeOrdenacao<P> criterio) {
        List<P> fila = new ArrayList<>(elegiveisDisponiveis);
        fila.sort(criterio.comparator());

        int vagas = turno.getEfetivoNecessario();
        List<P> escalados = new ArrayList<>();
        for (int i = 0; i < vagas && i < fila.size(); i++) {
            escalados.add(fila.get(i));
        }
        return escalados;
    }

  
    public List<P> preencherVagas(List<P> pool, Predicate<P> elegibilidadeExtra, T turno, CriterioDeOrdenacao<P> criterio) {
        List<P> filtrado = pool.stream().filter(elegibilidadeExtra).toList();
        return preencherVagas(filtrado, turno, criterio);
    }
}
