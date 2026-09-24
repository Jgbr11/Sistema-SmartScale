package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    /** RF13/RF25 - devolve o usuario logado e o perfil, para o front montar o menu certo. */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepository.findByLogin(auth.getName()).orElseThrow();
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);
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
    public ResponseEntity<?> trocarSenha(@RequestBody Map<String, String> body, Authentication auth) {
        Usuario usuario = usuarioRepository.findByLogin(auth.getName()).orElseThrow();
        String senhaAtual = body.get("senhaAtual");
        String senhaNova = body.get("senhaNova");
        if (senhaAtual == null || senhaNova == null || senhaNova.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe a senha atual e a nova senha"));
        }
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Senha atual incorreta"));
        }
        if (senhaNova.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A nova senha precisa ter pelo menos 6 caracteres"));
        }
        usuario.setSenhaHash(passwordEncoder.encode(senhaNova));
        usuarioRepository.save(usuario);
        auditoriaService.registrar(auth.getName(), "SENHA_TROCADA_PELO_PROPRIO", usuario.getLogin());
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso"));
    }
}
