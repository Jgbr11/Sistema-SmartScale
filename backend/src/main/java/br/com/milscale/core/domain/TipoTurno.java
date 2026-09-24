package br.com.milscale.core.domain;

/**
 * ===================== NUCLEO REUTILIZAVEL (LPS) =====================
 * Corresponde ao RF06 do catalogo: "Manter os tipos de turno com seus
 * requisitos de elegibilidade, horario de inicio, duracao e efetivo
 * necessario."
 *
 * No MilScale, "turno" vira "tipo de servico" (Guarda, Cabo de Dia,
 * Oficial de Dia...), todos de 24h. Em outro produto da linha o mesmo
 * contrato serviria para plantoes de 6/12h. Ver
 * {@code br.com.milscale.milscale.domain.TipoServico}.
 * =======================================================================
 */
public interface TipoTurno {

    Long getId();

    String getNome();

    /** Quantas pessoas esse turno exige por dia (RF06). */
    int getEfetivoNecessario();

    /** Se o tipo de turno esta ativo para novas geracoes de escala. */
    boolean isAtivo();
}
