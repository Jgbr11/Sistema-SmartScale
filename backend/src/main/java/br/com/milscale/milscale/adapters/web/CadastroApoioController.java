package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.PostoGraduacaoRepository;
import br.com.milscale.milscale.adapters.persistence.SubunidadeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Listas de apoio para preencher combos no front (posto/graduacao, subunidade). */
@RestController
@RequestMapping("/api")
public class CadastroApoioController {

    private final PostoGraduacaoRepository postoGraduacaoRepository;
    private final SubunidadeRepository subunidadeRepository;

    public CadastroApoioController(PostoGraduacaoRepository postoGraduacaoRepository, SubunidadeRepository subunidadeRepository) {
        this.postoGraduacaoRepository = postoGraduacaoRepository;
        this.subunidadeRepository = subunidadeRepository;
    }

    @GetMapping("/postos-graduacao")
    public Object postos() { return postoGraduacaoRepository.findAll(); }

    @GetMapping("/subunidades")
    public Object subunidades() { return subunidadeRepository.findAll(); }
}
