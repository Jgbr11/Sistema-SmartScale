package br.com.milscale.milscale.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosSubunidade(
        @NotBlank(message = "Informe a sigla") @Size(max = 20) String sigla,
        @NotBlank(message = "Informe o nome") @Size(max = 80) String nome,
        boolean ativo) {
}
