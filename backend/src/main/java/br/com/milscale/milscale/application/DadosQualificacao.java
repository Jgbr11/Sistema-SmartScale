package br.com.milscale.milscale.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosQualificacao(
        @NotBlank(message = "Informe o nome do curso") @Size(max = 60) String nome,
        @Size(max = 120) String descricao) {}
