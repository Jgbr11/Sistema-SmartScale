package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarPerfilRequest(@NotNull(message = "Escolha o perfil") Long perfilId) {}
