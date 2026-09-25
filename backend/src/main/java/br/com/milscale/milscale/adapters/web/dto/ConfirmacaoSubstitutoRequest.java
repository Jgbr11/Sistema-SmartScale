package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.Size;

/** Resposta do substituto sugerido. Campo "aceito" ausente = false (mesmo comportamento anterior). */
public record ConfirmacaoSubstitutoRequest(
        boolean aceito,
        @Size(max = 250, message = "Comentário com no máximo 250 caracteres") String comentario) {}
