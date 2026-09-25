package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoletimRequest(
        @Size(max = 20, message = "Número com no máximo 20 caracteres") String numero,
        @NotBlank(message = "Informe um título") @Size(max = 150, message = "Título com no máximo 150 caracteres") String titulo,
        @NotBlank(message = "O boletim não pode ficar vazio") String conteudoHtml,
        @Size(max = 60) String avisoRelacionado,
        @Size(max = 150) String avisoRelacionadoDescricao) {}
