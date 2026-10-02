package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.DadosSubunidade;
import br.com.milscale.milscale.application.SubunidadeService;
import br.com.milscale.milscale.domain.Subunidade;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subunidades")
public class SubunidadeController {

    private final SubunidadeService subunidadeService;
    private final AuditoriaService auditoriaService;

    public SubunidadeController(SubunidadeService subunidadeService, AuditoriaService auditoriaService) {
        this.subunidadeService = subunidadeService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Subunidade> listar() {
        return subunidadeService.listar();
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public Subunidade cadastrar(@Valid @RequestBody DadosSubunidade dados, Authentication auth) {
        Subunidade salva = subunidadeService.cadastrar(dados);
        auditoriaService.registrar(auth.getName(), "SUBUNIDADE_CADASTRADA", salva.getSigla() + " (id " + salva.getId() + ")");
        return salva;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public Subunidade atualizar(@PathVariable Long id, @Valid @RequestBody DadosSubunidade dados, Authentication auth) {
        Subunidade salva = subunidadeService.atualizar(id, dados);
        auditoriaService.registrar(auth.getName(), "SUBUNIDADE_EDITADA", salva.getSigla() + " (id " + id + ")");
        return salva;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @DeleteMapping("/{id}")
    public Map<String, Boolean> excluir(@PathVariable Long id, Authentication auth) {
        boolean desativada = subunidadeService.excluir(id);
        auditoriaService.registrar(auth.getName(), desativada ? "SUBUNIDADE_DESATIVADA" : "SUBUNIDADE_EXCLUIDA", "id " + id);
        return Map.of("desativada", desativada);
    }
}
