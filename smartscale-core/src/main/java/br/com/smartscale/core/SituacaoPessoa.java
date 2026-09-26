package br.com.smartscale.core;

/**
 * ===================== NUCLEO REUTILIZAVEL (LPS) =====================
 * Situacoes possiveis de uma pessoa escalada, independente do produto
 * (RF04). O MilScale usa estes tres valores diretamente (ver coluna
 * `situacao` da tabela `militar` no modelo logico do banco).
 * =======================================================================
 */
public enum SituacaoPessoa {
    ATIVO,
    AFASTADO,
    DESLIGADO
}
