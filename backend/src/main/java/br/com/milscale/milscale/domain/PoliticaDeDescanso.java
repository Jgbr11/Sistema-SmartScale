package br.com.milscale.milscale.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * RN06 num lugar so. Antes cada service (geracao, realocacao por
 * afastamento, troca) tinha a propria janela de datas e o proprio
 * default - qualquer ajuste de regra precisava ser replicado em tres.
 */
public final class PoliticaDeDescanso {

    /** Usado so quando o tipo de servico nao tem RegraEscala cadastrada. */
    public static final int INTERVALO_MINIMO_PADRAO = 7;

    /** Em troca o minimo aceitavel e 2x1: vao de calendario de 3 dias. 1x1 (vao <= 2) e proibido. */
    public static final int DISTANCIA_MINIMA_EM_TROCA = 3;

    private PoliticaDeDescanso() {}

    public static long distanciaEmDias(LocalDate a, LocalDate b) {
        return Math.abs(ChronoUnit.DAYS.between(a, b));
    }

    /**
     * "intervaloMinimo" e o numero de dias de FOLGA exigidos entre dois
     * servicos (3 = folga, folga, folga, servico). Um vao de calendario de
     * exatamente `intervaloMinimo` dias so da `intervaloMinimo - 1` folgas,
     * por isso a distancia precisa ser estritamente maior.
     */
    public static boolean respeitaIntervalo(LocalDate servicoAnterior, LocalDate dia, int intervaloMinimo) {
        return servicoAnterior == null || distanciaEmDias(servicoAnterior, dia) > intervaloMinimo;
    }

    public static boolean ficariaEm1x1(LocalDate servicoExistente, LocalDate novaData) {
        return distanciaEmDias(servicoExistente, novaData) < DISTANCIA_MINIMA_EM_TROCA;
    }

    public static int intervaloMinimo(RegraEscala regra) {
        return regra != null ? regra.getIntervaloMinimo() : INTERVALO_MINIMO_PADRAO;
    }
}
