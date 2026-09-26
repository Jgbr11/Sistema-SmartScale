package br.com.smartscale.core;

/**
 * ===================== NUCLEO REUTILIZAVEL (LPS) =====================
 * Corresponde ao RF04 do catalogo de requisitos reutilizaveis:
 * "Manter o cadastro das pessoas escaladas [...] com identificacao,
 * funcao, contato e situacao."
 *
 * Qualquer produto da linha (MilScale, um futuro SmartScale hospitalar,
 * uma central de atendimento etc.) implementa esta interface com o seu
 * proprio vocabulario. O MilScale implementa isto em
 * {@code br.com.milscale.milscale.domain.Militar}, onde "pessoa escalada"
 * vira "militar", "funcao" vira "posto/graduacao" etc. (ver Tabela 1 do
 * catalogo de requisitos - correspondencia de termos).
 *
 * Esta interface nao conhece nada de JPA, Spring ou banco de dados: e
 * puro dominio, para poder ser reaproveitada por qualquer adaptador.
 * =======================================================================
 */
public interface PessoaEscalada {

    /** Identificador estavel da pessoa (ex.: id do militar no MilScale). */
    Long getId();

    /** Nome curto para exibicao nas telas de escala (ex.: nome de guerra). */
    String getNomeExibicao();

    /** Situacao atual: ativo, afastado, desligado (ver RF04 / RN15). */
    SituacaoPessoa getSituacao();

    /**
     * Contador de rodizio usado no criterio de ordenacao da fila (RN01).
     * No MilScale e "dias sem tirar servico" (quanto maior, mais prioridade).
     * Em outro produto da linha poderia ser "carga horaria acumulada"
     * (quanto menor, mais prioridade) - por isso o criterio de comparacao
     * fica a cargo de cada produto, nao desta interface (ver
     * {@link CriterioDeOrdenacao}).
     */
    long getContadorRodizio();
}
