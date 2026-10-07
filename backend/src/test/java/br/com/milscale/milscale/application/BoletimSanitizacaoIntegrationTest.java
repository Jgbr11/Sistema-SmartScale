package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Boletim;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BoletimSanitizacaoIntegrationTest {

    @Autowired private BoletimService boletimService;

    @Test
    void criar_gravaHtmlLimpo() {
        Boletim b = boletimService.criar("1", "Teste", "<p>ok</p><img src=x onerror=alert(1)>", null, null, "00000000001");
        assertThat(b.getConteudoHtml()).contains("<p>ok</p>").doesNotContain("onerror");
    }

    @Test
    void atualizar_gravaHtmlLimpo() {
        Boletim b = boletimService.criar("1", "Teste", "<p>ok</p>", null, null, "00000000001");
        Boletim editado = boletimService.atualizar(b.getId(), "1", "Teste", "<p>novo</p><script>x()</script>", null, null);
        assertThat(editado.getConteudoHtml()).isEqualTo("<p>novo</p>");
    }

    @Test
    void conteudoQueSoTinhaScript_ficaVazioERecusado() {
        assertThatThrownBy(() -> boletimService.criar("1", "Teste", "<script>x()</script>", null, null, "00000000001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O boletim não pode ficar vazio");
    }
}
