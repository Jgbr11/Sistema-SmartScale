package br.com.milscale.milscale.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosPostoGraduacao(
        @NotBlank(message = "Informe a sigla") @Size(max = 10) String sigla,
        @NotBlank(message = "Informe a descrição") @Size(max = 60) String descricao,
        @NotNull(message = "Informe o nível hierárquico") @Min(1) @Max(99) Integer nivelHierarquico) {
}
