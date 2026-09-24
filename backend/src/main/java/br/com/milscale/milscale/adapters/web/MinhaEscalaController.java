package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    public MinhaEscalaController(MinhaEscalaService minhaEscalaService, UsuarioRepository usuarioRepository) {
        this.minhaEscalaService = minhaEscalaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<ServicoEscalado> doMes(@RequestParam(required = false) String mes, Authentication auth) {
        YearMonth alvo = mes != null ? YearMonth.parse(mes) : YearMonth.now();
        Long militarId = usuarioRepository.findByLogin(auth.getName()).orElseThrow().getMilitar().getId();
        return minhaEscalaService.doMes(militarId, alvo);
    }
}
