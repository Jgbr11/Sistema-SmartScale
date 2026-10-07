package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.DadosFeriado;
import br.com.milscale.milscale.application.FeriadoService;
import br.com.milscale.milscale.domain.Feriado;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feriados")
public class FeriadoController {

    private final FeriadoService feriadoService;
    private final AuditoriaService auditoriaService;

    public FeriadoController(FeriadoService feriadoService, AuditoriaService auditoriaService) {
        this.feriadoService = feriadoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Feriado> listar() {
        return feriadoService.listar();
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public Feriado cadastrar(@Valid @RequestBody DadosFeriado dados, Authentication auth) {
        Feriado salvo = feriadoService.cadastrar(dados);
        auditoriaService.registrar(auth.getName(), "FERIADO_CADASTRADO", salvo.getDescricao() + " (" + salvo.getDataInicio() + " a " + salvo.getDataFim() + ")");
        return salvo;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/{id}")
    public Feriado atualizar(@PathVariable Long id, @Valid @RequestBody DadosFeriado dados, Authentication auth) {
        Feriado salvo = feriadoService.atualizar(id, dados);
        auditoriaService.registrar(auth.getName(), "FERIADO_EDITADO", salvo.getDescricao() + " (" + salvo.getDataInicio() + " a " + salvo.getDataFim() + ")");
        return salvo;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id, Authentication auth) {
        feriadoService.remover(id);
        auditoriaService.registrar(auth.getName(), "FERIADO_REMOVIDO", "id " + id);
    }
}
