package br.com.milscale.milscale.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class PoliticaDeDescanso {

    public static final int INTERVALO_MINIMO_SEM_REGRA_CADASTRADA = 7;

    public static final int DISTANCIA_MINIMA_EM_TROCA = 3;

    private PoliticaDeDescanso() {}

    public static long distanciaEmDias(LocalDate a, LocalDate b) {
        return Math.abs(ChronoUnit.DAYS.between(a, b));
    }

    public static boolean respeitaIntervalo(LocalDate servicoAnterior, LocalDate dia, int intervaloMinimo) {
        return servicoAnterior == null || distanciaEmDias(servicoAnterior, dia) > intervaloMinimo;
    }

    public static boolean ficariaEm1x1(LocalDate servicoExistente, LocalDate novaData) {
        return distanciaEmDias(servicoExistente, novaData) < DISTANCIA_MINIMA_EM_TROCA;
    }

    public static int intervaloMinimo(RegraEscala regra) {
        return regra != null ? regra.getIntervaloMinimo() : INTERVALO_MINIMO_SEM_REGRA_CADASTRADA;
    }
}
