package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.Size;

public record DecisaoRequest(
        boolean aprovado,
        @Size(max = 250, message = "Comentário com no máximo 250 caracteres") String comentario) {}
