package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class EnumsContratoTest {

    private static String[] nomes(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).toArray(String[]::new);
    }

    @Test
    void situacaoEscala() {
        assertThat(nomes(SituacaoEscala.values())).containsExactly("RASCUNHO", "PUBLICADA", "ENCERRADA");
    }

    @Test
    void situacaoServico() {
        assertThat(nomes(SituacaoServico.values())).containsExactly("PREVISTO", "CUMPRIDO", "SUBSTITUIDO");
    }

    @Test
    void situacaoSolicitacao() {
        assertThat(nomes(SituacaoSolicitacao.values())).containsExactly(
                "AGUARDANDO_SUBSTITUTO", "EM_TRIAGEM", "AGUARDANDO_AUTORIZACAO", "AUTORIZADA", "NEGADA", "CANCELADA");
    }

    @Test
    void tipoTroca() {
        assertThat(nomes(TipoTroca.values())).containsExactly("SUBSTITUICAO", "TROCA_MUTUA");
    }

    @Test
    void tipoAfastamento() {
        assertThat(nomes(TipoAfastamento.values())).containsExactly("MISSAO", "DISPENSA", "FERIAS", "LICENCA", "CURSO", "OUTRO");
    }

    @Test
    void tipoFeriado() {
        assertThat(nomes(TipoFeriado.values())).containsExactly("NACIONAL", "MILITAR", "OM");
    }
}
