package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaDeDescansoTest {

    private static final LocalDate D = LocalDate.of(2030, 1, 10);

    @Test
    void semServicoAnterior_sempreRespeita() {
        assertThat(PoliticaDeDescanso.respeitaIntervalo(null, D, 3)).isTrue();
    }

    @Test
    void intervalo3_exigeVaoDe4DiasDeCalendario() {
        // 3x1 = folga, folga, folga, servico
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.minusDays(3), D, 3)).isFalse();
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.minusDays(4), D, 3)).isTrue();
    }

    @Test
    void intervaloValeTambemParaServicoFuturo() {
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.plusDays(3), D, 3)).isFalse();
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.plusDays(4), D, 3)).isTrue();
    }

    @Test
    void troca_1x1ProibidoE2x1Permitido() {
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.plusDays(1), D)).isTrue();
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.plusDays(2), D)).isTrue();  // 1 dia de folga
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.plusDays(3), D)).isFalse(); // 2 dias de folga
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.minusDays(2), D)).isTrue();
    }

    @Test
    void semRegraCadastrada_usaIntervaloPadrao() {
        assertThat(PoliticaDeDescanso.intervaloMinimo(null)).isEqualTo(7);
        assertThat(PoliticaDeDescanso.intervaloMinimo(RegraEscala.builder().intervaloMinimo(3).build())).isEqualTo(3);
    }
}
