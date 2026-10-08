package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.DadosPostoGraduacao;
import br.com.milscale.milscale.application.PostoGraduacaoService;
import br.com.milscale.milscale.domain.PostoGraduacao;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postos-graduacao")
public class PostoGraduacaoController {

    private final PostoGraduacaoService postoService;
    private final AuditoriaService auditoriaService;

    public PostoGraduacaoController(PostoGraduacaoService postoService, AuditoriaService auditoriaService) {
        this.postoService = postoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<PostoGraduacao> listar() {
        return postoService.listar();
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public PostoGraduacao cadastrar(@Valid @RequestBody DadosPostoGraduacao dados, Authentication auth) {
        PostoGraduacao salvo = postoService.cadastrar(dados);
        auditoriaService.registrar(auth.getName(), "POSTO_CADASTRADO", salvo.getSigla() + " (id " + salvo.getId() + ")");
        return salvo;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public PostoGraduacao atualizar(@PathVariable Long id, @Valid @RequestBody DadosPostoGraduacao dados, Authentication auth) {
        PostoGraduacao salvo = postoService.atualizar(id, dados);
        auditoriaService.registrar(auth.getName(), "POSTO_EDITADO", salvo.getSigla() + " (id " + id + ")");
        return salvo;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication auth) {
        PostoGraduacao excluido = postoService.excluir(id);
        auditoriaService.registrar(auth.getName(), "POSTO_EXCLUIDO", excluido.getSigla() + " (id " + id + ")");
        return ResponseEntity.noContent().build();
    }
}
