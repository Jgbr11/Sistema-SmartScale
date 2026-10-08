package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.SituacaoEscala;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EscalaResumo(Long id, String descricao, LocalDate dataInicio, LocalDate dataFim,
                           SituacaoEscala situacao, LocalDateTime dataPublicacao,
                           long totalServicos, long vagasAbertas) {}
