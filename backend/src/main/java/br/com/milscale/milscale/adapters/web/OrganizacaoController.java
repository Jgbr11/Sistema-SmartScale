package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.domain.IdentidadeDaOrganizacao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrganizacaoController {

    @GetMapping("/api/organizacao")
    public Map<String, String> organizacao() {
        IdentidadeDaOrganizacao organizacao = IdentidadeDaOrganizacao.INSTANCIA;
        return Map.of("nome", organizacao.nome(), "sigla", organizacao.sigla(), "sistema", organizacao.sistema());
    }
}
