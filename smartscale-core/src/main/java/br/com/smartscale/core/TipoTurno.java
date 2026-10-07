package br.com.smartscale.core;

// Núcleo reutilizável (LPS)

public interface TipoTurno {

    Long getId();

    String getNome();

    int getEfetivoNecessario();

    boolean isAtivo();
}
