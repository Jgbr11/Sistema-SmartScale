package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os nomes destes enums sao contrato com o frontend (src/api/types.ts) e
 * com o que ja esta gravado no banco. Renomear qualquer valor quebra os
 * dois - este teste acusa isso na hora.
 */
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
}
