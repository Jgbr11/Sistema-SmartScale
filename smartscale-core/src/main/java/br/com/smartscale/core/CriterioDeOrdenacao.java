package br.com.milscale.core.domain;

import java.util.Comparator;

/**
 * ===================== NUCLEO REUTILIZAVEL (LPS) =====================
 * Corresponde a RN01 do modelo de regras de negocio:
 * "Para cada vaga, o sistema escala a pessoa elegivel mais bem
 * posicionada na fila, segundo o criterio de ordenacao vigente [...]"
 * Classificada no catalogo como PONTO DE VARIACAO (parametro
 * `criterio_ordenacao`).
 *
 * O MotorDeRodizio (nucleo) nao sabe qual criterio usar - ele so pede
 * "me de o comparator" e ordena. Cada produto da linha injeta o seu:
 *  - MilScale: maior numero de dias sem servico primeiro
 *    (ver br.com.milscale.milscale.domain.CriterioOrdenacaoMilitar)
 *  - Um hospital (segundo a Tabela 2 do modelo de regras): menor carga
 *    horaria acumulada primeiro
 * =======================================================================
 */
@FunctionalInterface
public interface CriterioDeOrdenacao<T extends PessoaEscalada> {
    Comparator<T> comparator();
}
