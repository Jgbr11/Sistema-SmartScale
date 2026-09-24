package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.BoletimService;
import br.com.milscale.milscale.domain.Boletim;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Boletim Interno - leitura aberta a todo mundo, manutencao privativa de Cabo/Sargenteante. */
@RestController
@RequestMapping("/api/boletins")
public class BoletimController {

    private final BoletimService boletimService;
    private final AuditoriaService auditoriaService;

    public BoletimController(BoletimService boletimService, AuditoriaService auditoriaService) {
        this.boletimService = boletimService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Boletim> listar() {
        return boletimService.listar();
    }

    @GetMapping("/{id}")
    public Boletim buscar(@PathVariable Long id) {
        return boletimService.buscar(id);
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping
    public Boletim criar(@RequestBody Map<String, String> body, Authentication auth) {
        Boletim salvo = boletimService.criar(body.get("numero"), body.get("titulo"), body.get("conteudoHtml"),
                body.get("avisoRelacionado"), body.get("avisoRelacionadoDescricao"), auth.getName());
        auditoriaService.registrar(auth.getName(), "BOLETIM_PUBLICADO", salvo.getTitulo());
        return salvo;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PutMapping("/{id}")
    public Boletim atualizar(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        Boletim salvo = boletimService.atualizar(id, body.get("numero"), body.get("titulo"), body.get("conteudoHtml"),
                body.get("avisoRelacionado"), body.get("avisoRelacionadoDescricao"));
        auditoriaService.registrar(auth.getName(), "BOLETIM_EDITADO", salvo.getTitulo());
        return salvo;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id, Authentication auth) {
        Boletim b = boletimService.buscar(id);
        boletimService.remover(id);
        auditoriaService.registrar(auth.getName(), "BOLETIM_REMOVIDO", b.getTitulo());
    }
}
