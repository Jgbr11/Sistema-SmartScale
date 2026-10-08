package br.com.milscale.milscale.application;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class SanitizadorHtml {

    private static final Safelist PERMITIDO = Safelist.relaxed()
            .addAttributes("img", "style")
            .addProtocols("img", "src", "data");

    private static final Document.OutputSettings SAIDA = new Document.OutputSettings().prettyPrint(false);

    public String sanitizar(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, "", PERMITIDO, SAIDA);
    }
}
