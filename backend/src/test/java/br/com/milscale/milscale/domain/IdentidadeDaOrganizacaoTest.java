package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentidadeDaOrganizacaoTest {

    @Test
    void existeUmaUnicaIdentidade() {
        assertThat(IdentidadeDaOrganizacao.values()).hasSize(1);
        assertThat(IdentidadeDaOrganizacao.valueOf("INSTANCIA")).isSameAs(IdentidadeDaOrganizacao.INSTANCIA);
    }

    @Test
    void assinaturaJuntaSistemaENome() {
        assertThat(IdentidadeDaOrganizacao.INSTANCIA.assinatura()).isEqualTo("MilScale, 5º Batalhão de Suprimento");
    }
}
