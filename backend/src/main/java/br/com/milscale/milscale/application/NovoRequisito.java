package br.com.milscale.milscale.application;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record NovoRequisito(
        @NotNull(message = "Escolha o posto/graduação") Long postoId,
        Long subunidadeId,
        Long qualificacaoId,
        Set<Long> qualificacoesExcluidasIds,
        Long subunidadeExcluidaId) {}
