package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarTrocaMutuaRequest(
        @NotNull(message = "Informe o serviço") Long servicoOrigemId,
        @NotNull(message = "Escolha o serviço do outro militar") Long servicoDestinoId,
        @NotBlank(message = "Informe uma justificativa") @Size(max = 250, message = "Justificativa com no máximo 250 caracteres") String justificativa) {}
