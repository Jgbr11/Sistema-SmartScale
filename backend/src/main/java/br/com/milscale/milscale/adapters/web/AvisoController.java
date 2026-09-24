package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AvisoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

/** Aberto a qualquer autenticado - feriados e missoes/afastamentos do mes, unificados. */
@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    private final AvisoService avisoService;

    public AvisoController(AvisoService avisoService) {
        this.avisoService = avisoService;
    }

    @GetMapping
    public List<AvisoService.Aviso> listar(@RequestParam String mes) {
        return avisoService.listarDoMes(YearMonth.parse(mes));
    }
}
