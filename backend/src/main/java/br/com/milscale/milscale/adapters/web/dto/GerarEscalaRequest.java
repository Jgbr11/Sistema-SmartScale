package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GerarEscalaRequest(
        @NotNull(message = "Informe a data inicial") LocalDate dataInicio,
        @NotNull(message = "Informe a data final") LocalDate dataFim) {}
