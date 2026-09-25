package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.ContaService;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ContaService contaService;
    private final AuditoriaService auditoriaService;

    public AuthController(ContaService contaService, AuditoriaService auditoriaService) {
        this.contaService = contaService;
        this.auditoriaService = auditoriaService;
    }

    /** RF13/RF25 - devolve o usuario logado e o perfil, para o front montar o menu certo. */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = contaService.registrarAcesso(auth.getName());
        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "login", usuario.getLogin(),
                "perfil", usuario.getPerfil().getNome(),
                "militarId", usuario.getMilitar().getId(),
                "nomeExibicao", usuario.getMilitar().getNomeExibicao()
        ));
    }

    /** "Minha conta" - qualquer usuario troca a propria senha, precisa confirmar a atual. */
    @PostMapping("/senha")
    public Map<String, String> trocarSenha(@RequestBody Map<String, String> body, Authentication auth) {
        contaService.trocarSenha(auth.getName(), body.get("senhaAtual"), body.get("senhaNova"));
        auditoriaService.registrar(auth.getName(), "SENHA_TROCADA_PELO_PROPRIO", auth.getName());
        return Map.of("mensagem", "Senha alterada com sucesso");
    }
}
