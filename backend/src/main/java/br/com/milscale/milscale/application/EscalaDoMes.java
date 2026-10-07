package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.ServicoEscalado;

import java.util.List;

public record EscalaDoMes(List<EscalaResumo> escalas, List<ServicoEscalado> servicos) {}
