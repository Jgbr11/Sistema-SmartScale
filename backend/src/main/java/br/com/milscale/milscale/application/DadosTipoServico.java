package br.com.milscale.milscale.application;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

public record DadosTipoServico(
        @NotBlank(message = "Informe o nome do serviço") @Size(max = 60) String nome,
        @Size(max = 150) String descricao,
        @Min(value = 1, message = "O efetivo necessário precisa ser pelo menos 1")
        @Max(value = 50, message = "O efetivo necessário pode ser no máximo 50") int efetivoNecessario,
        LocalTime horaInicio,
        Integer duracaoHoras) {}
