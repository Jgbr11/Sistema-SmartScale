package br.com.milscale.milscale.application;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record DadosMilitar(
        @NotBlank(message = "Informe o nome completo") @Size(max = 120) String nomeCompleto,
        @NotBlank(message = "Informe o nome de guerra") @Size(max = 40) String nomeGuerra,
        @NotBlank(message = "Informe o CPF") String cpf,
        @Size(max = 20) String numeroRegistro,
        LocalDate dataNascimento,
        @Size(max = 20) String fusex,
        @Email(message = "Email inválido") @Size(max = 120) String email,
        @Size(max = 20) String telefone,
        @Size(max = 3_000_000, message = "Foto muito grande (máximo de 2 MB)") String fotoBase64,
        @NotNull(message = "Escolha o posto/graduação") Long postoId,
        @NotNull(message = "Escolha a subunidade") Long subunidadeId) {}
