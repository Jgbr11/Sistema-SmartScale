package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.Size;

/** Triagem (Cabo) e autorizacao (Sargenteante). "aprovado" ausente = false. */
public record DecisaoRequest(
        boolean aprovado,
        @Size(max = 250, message = "Comentário com no máximo 250 caracteres") String comentario) {}
