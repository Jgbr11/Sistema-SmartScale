package br.com.milscale.milscale.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizadorHtmlTest {

    private final SanitizadorHtml sanitizador = new SanitizadorHtml();

    @Test
    void removeScript() {
        assertThat(sanitizador.sanitizar("<p>oi</p><script>alert(1)</script>")).isEqualTo("<p>oi</p>");
    }

    @Test
    void removeAtributoDeEvento() {
        assertThat(sanitizador.sanitizar("<img src=\"https://x/a.png\" onerror=\"alert(1)\">"))
                .doesNotContain("onerror");
    }

    @Test
    void removeLinkJavascript() {
        assertThat(sanitizador.sanitizar("<a href=\"javascript:alert(1)\">x</a>")).doesNotContain("javascript");
    }

    @Test
    void mantemImagemColadaEmBase64ComEstilo() {
        String img = "<img src=\"data:image/png;base64,AAAA\" style=\"max-width:100%\">";
        assertThat(sanitizador.sanitizar(img))
                .contains("src=\"data:image/png;base64,AAAA\"")
                .contains("style=\"max-width:100%\"");
    }

    @Test
    void mantemFormatacaoDoEditor() {
        String html = "<h3>Titulo</h3><p><b>negrito</b> <i>italico</i></p><ul><li>item</li></ul>";
        assertThat(sanitizador.sanitizar(html)).isEqualTo(html);
    }

    @Test
    void nuloContinuaNulo() {
        assertThat(sanitizador.sanitizar(null)).isNull();
    }
}
