package br.com.smartscale.core;

// Núcleo reutilizável (LPS)

public interface PessoaEscalada {

    Long getId();

    String getNomeExibicao();

    SituacaoPessoa getSituacao();

    long getContadorRodizio();
}
