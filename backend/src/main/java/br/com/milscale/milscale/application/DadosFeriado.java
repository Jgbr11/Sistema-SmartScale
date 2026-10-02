package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.TipoFeriado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DadosFeriado(
        @NotNull(message = "Informe a data inicial") LocalDate dataInicio,
        @NotNull(message = "Informe a data final") LocalDate dataFim,
        @NotBlank(message = "Informe a descrição") @Size(max = 100) String descricao,
        @NotNull(message = "Informe o tipo") TipoFeriado tipo) {}
