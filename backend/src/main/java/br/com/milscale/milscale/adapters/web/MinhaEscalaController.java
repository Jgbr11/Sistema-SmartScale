package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.UsuarioLogadoService;
import br.com.milscale.milscale.application.MinhaEscalaService;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/** RF13 - a pessoa escalada consulta a propria escala. */
@RestController
@RequestMapping("/api/minha-escala")
public class MinhaEscalaController {

    private final MinhaEscalaService minhaEscalaService;
    private final UsuarioLogadoService usuarioLogadoService;

    public MinhaEscalaController(MinhaEscalaService minhaEscalaService, UsuarioLogadoService usuarioLogadoService) {
        this.minhaEscalaService = minhaEscalaService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    @GetMapping
    public List<ServicoEscalado> doMes(@RequestParam(required = false) String mes, Authentication auth) {
        YearMonth alvo = mes != null ? YearMonth.parse(mes) : YearMonth.now();
        Long militarId = usuarioLogadoService.militar(auth.getName()).getId();
        return minhaEscalaService.doMes(militarId, alvo);
    }
}
