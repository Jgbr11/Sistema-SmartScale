package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.UsuarioService;
import br.com.milscale.milscale.domain.PerfilAcesso;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** RF25 - Perfis e permissões. Privativo do Sargenteante. */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('SARGENTEANTE')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    public UsuarioController(UsuarioService usuarioService, AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/perfis")
    public List<PerfilAcesso> listarPerfis() {
        return usuarioService.listarPerfis();
    }

    @PutMapping("/{id}/perfil")
    public Usuario alterarPerfil(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        Long perfilId = Long.valueOf(String.valueOf(body.get("perfilId")));
        Usuario salvo = usuarioService.alterarPerfil(id, perfilId, auth.getName());
        auditoriaService.registrar(auth.getName(), "PERFIL_ALTERADO", salvo.getLogin() + " -> " + salvo.getPerfil().getNome());
        return salvo;
    }

    @PutMapping("/{id}/ativo")
    public Usuario alterarAtivo(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        boolean ativo = Boolean.parseBoolean(String.valueOf(body.get("ativo")));
        Usuario salvo = usuarioService.alterarAtivo(id, ativo, auth.getName());
        auditoriaService.registrar(auth.getName(), ativo ? "USUARIO_REATIVADO" : "USUARIO_DESATIVADO", salvo.getLogin());
        return salvo;
    }

    @PostMapping("/{id}/resetar-senha")
    public Map<String, String> resetarSenha(@PathVariable Long id, Authentication auth) {
        usuarioService.resetarSenha(id);
        auditoriaService.registrar(auth.getName(), "SENHA_RESETADA", "usuário id " + id);
        return Map.of("mensagem", "Senha resetada para o padrão");
    }
}
