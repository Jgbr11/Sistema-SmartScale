package br.com.milscale.milscale.adapters.web.dto;

import br.com.milscale.milscale.domain.TipoAfastamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CadastrarAfastamentoRequest(
        @NotEmpty(message = "Selecione ao menos um militar") List<Long> militarIds,
        @NotNull(message = "Informe o tipo") TipoAfastamento tipo,
        @NotBlank(message = "Informe a descrição") @Size(max = 150, message = "Descrição com no máximo 150 caracteres") String descricao,
        @NotNull(message = "Informe a data inicial") LocalDate dataInicio,
        @NotNull(message = "Informe a data final") LocalDate dataFim) {}
