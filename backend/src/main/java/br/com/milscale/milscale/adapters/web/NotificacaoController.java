package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.NotificacaoService;
import br.com.milscale.milscale.domain.Notificacao;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** O sininho - cada usuário só vê e mexe nas próprias notificações. */
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public List<Notificacao> listar(@RequestParam(defaultValue = "20") int limite, Authentication auth) {
        return notificacaoService.listarRecentes(auth.getName(), Math.min(limite, 50));
    }

    @GetMapping("/nao-lidas/contagem")
    public Map<String, Long> contarNaoLidas(Authentication auth) {
        return Map.of("total", notificacaoService.contarNaoLidas(auth.getName()));
    }

    @PostMapping("/{id}/marcar-lida")
    public void marcarComoLida(@PathVariable Long id, Authentication auth) {
        notificacaoService.marcarComoLida(id, auth.getName());
    }

    @PostMapping("/marcar-todas-lidas")
    public void marcarTodasComoLidas(Authentication auth) {
        notificacaoService.marcarTodasComoLidas(auth.getName());
    }
}
