package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(
        @NotBlank(message = "Informe a senha atual e a nova senha") String senhaAtual,
        @NotBlank(message = "Informe a senha atual e a nova senha")
        @Size(min = 6, message = "A nova senha precisa ter pelo menos 6 caracteres") String senhaNova) {}
