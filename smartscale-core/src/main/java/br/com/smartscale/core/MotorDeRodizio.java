package br.com.smartscale.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * ===================== NUCLEO REUTILIZAVEL (LPS) =====================
 * O motor de geracao de escala por fila/rodizio. Este e o ativo mais
 * importante do catalogo: reune, num unico algoritmo generico, as
 * seguintes regras de negocio (numeracao conforme
 * "SMART SCALE - Modelo de Regras de Negocio v1"):
 *
 *  - RN01 (ponto de variacao): escala a pessoa elegivel mais bem
 *    posicionada na fila, segundo o {@link CriterioDeOrdenacao} injetado.
 *  - RN05 (invariante): uma pessoa nao pode ocupar mais de uma vaga por
 *    dia - garantido porque cada pessoa escalada e removida do pool
 *    assim que preenche uma vaga do dia.
 *  - RN15 (invariante): pessoa com impedimento na data nao e escalada,
 *    mesmo sendo a primeira da fila - o pool de "disponiveis" que entra
 *    no motor ja deve vir filtrado pelo adaptador (afastamentos).
 *  - RN20 (ponto de variacao): o motor nao decide sozinho o tamanho do
 *    ciclo; ele so preenche o `efetivoNecessario` de cada turno, dia a
 *    dia, com quem esta disponivel - quem decide o periodo e o
 *    aplicativo que o chama.
 *
 * O motor e deliberadamente "burro" sobre o que e um "militar" ou um
 * "tipo de servico" - ele so enxerga {@link PessoaEscalada} e
 * {@link TipoTurno}. Isso e o que permite reaproveita-lo em qualquer
 * produto da linha sem alterar uma linha sequer deste arquivo.
 * =======================================================================
 */
public class MotorDeRodizio<P extends PessoaEscalada, T extends TipoTurno> {

    /**
     * Preenche as vagas de um unico turno, em uma unica data, escolhendo
     * as pessoas mais bem posicionadas na fila entre as elegiveis e
     * disponiveis informadas.
     *
     * @param elegiveisDisponiveis pool de pessoas que JA passaram pelos
     *        filtros de elegibilidade (posto/qualificacao - RF06) e de
     *        disponibilidade (sem impedimento na data - RN15, sem outro
     *        servico no mesmo dia - RN05). O motor nao refaz esses
     *        filtros; isso e responsabilidade do adaptador MilScale, que
     *        conhece as tabelas requisito_servico e afastamento.
     * @param turno tipo de turno a preencher (define quantas vagas - RF06)
     * @param criterio estrategia de ordenacao da fila (RN01, injetada)
     * @return lista de pessoas escaladas para esse turno, na ordem em que
     *         preencheram as vagas (pode ter menos que o efetivo
     *         necessario, se nao houver gente suficiente - o chamador
     *         decide o que fazer com uma vaga em aberto)
     */
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

    /**
     * Variante que aceita um filtro adicional aplicado sobre o pool antes
     * de ordenar - util quando o chamador quer compor filtros (ex.: "so
     * quem tem a qualificacao X") sem duplicar a logica de ordenacao.
     */
    public List<P> preencherVagas(List<P> pool, Predicate<P> elegibilidadeExtra, T turno, CriterioDeOrdenacao<P> criterio) {
        List<P> filtrado = pool.stream().filter(elegibilidadeExtra).toList();
        return preencherVagas(filtrado, turno, criterio);
    }
}
