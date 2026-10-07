package br.com.milscale.milscale.adapters.config;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProtecaoContraForcaBrutaTest {

    static class RelogioAjustavel extends Clock {
        Instant agora = Instant.parse("2030-01-10T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @Test
    void bloqueiaNaQuintaFalhaELiberaDepoisDe15Minutos() {
        RelogioAjustavel relogio = new RelogioAjustavel();
        ProtecaoContraForcaBruta protecao = new ProtecaoContraForcaBruta(relogio);
        for (int i = 0; i < 4; i++) protecao.registrarFalha("123");
        assertThat(protecao.bloqueado("123")).isFalse();
        protecao.registrarFalha("123");
        assertThat(protecao.bloqueado("123")).isTrue();
        relogio.agora = relogio.agora.plus(Duration.ofMinutes(15));
        assertThat(protecao.bloqueado("123")).isFalse();
    }

    @Test
    void loginCertoZeraAsFalhas() {
        ProtecaoContraForcaBruta protecao = new ProtecaoContraForcaBruta(new RelogioAjustavel());
        for (int i = 0; i < 4; i++) protecao.registrarFalha("123");
        protecao.limpar("123");
        protecao.registrarFalha("123");
        assertThat(protecao.bloqueado("123")).isFalse();
    }
}
