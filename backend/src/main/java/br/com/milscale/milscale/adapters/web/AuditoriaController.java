package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.domain.LogAuditoria;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Log de auditoria - privativo do Sargenteante. */
@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('SARGENTEANTE')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<LogAuditoria> listar(@RequestParam(defaultValue = "300") int limite) {
        return auditoriaService.listarRecentes(Math.min(limite, 1000));
    }
}
