package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.DadosQualificacao;
import br.com.milscale.milscale.application.QualificacaoService;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Qualificacao;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QualificacaoController {

    private final QualificacaoService qualificacaoService;
    private final AuditoriaService auditoriaService;

    public QualificacaoController(QualificacaoService qualificacaoService, AuditoriaService auditoriaService) {
        this.qualificacaoService = qualificacaoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/qualificacoes")
    public List<Qualificacao> listar() {
        return qualificacaoService.listar();
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/qualificacoes")
    public Qualificacao cadastrar(@Valid @RequestBody DadosQualificacao dados, Authentication auth) {
        Qualificacao salva = qualificacaoService.cadastrar(dados);
        auditoriaService.registrar(auth.getName(), "QUALIFICACAO_CADASTRADA", salva.getNome());
        return salva;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PutMapping("/qualificacoes/{id}")
    public Qualificacao atualizar(@PathVariable Long id, @Valid @RequestBody DadosQualificacao dados, Authentication auth) {
        Qualificacao salva = qualificacaoService.atualizar(id, dados);
        auditoriaService.registrar(auth.getName(), "QUALIFICACAO_EDITADA", salva.getNome());
        return salva;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @DeleteMapping("/qualificacoes/{id}")
    public void excluir(@PathVariable Long id, Authentication auth) {
        Qualificacao excluida = qualificacaoService.excluir(id);
        auditoriaService.registrar(auth.getName(), "QUALIFICACAO_EXCLUIDA", excluida.getNome() + " (id " + id + ")");
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/militares/{militarId}/qualificacoes/{qualificacaoId}")
    public Militar vincular(@PathVariable Long militarId, @PathVariable Long qualificacaoId, Authentication auth) {
        Militar militar = qualificacaoService.vincular(militarId, qualificacaoId);
        auditoriaService.registrar(auth.getName(), "CURSO_VINCULADO", "militar " + militarId + ", curso " + qualificacaoId);
        return militar;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @DeleteMapping("/militares/{militarId}/qualificacoes/{qualificacaoId}")
    public Militar desvincular(@PathVariable Long militarId, @PathVariable Long qualificacaoId, Authentication auth) {
        Militar militar = qualificacaoService.desvincular(militarId, qualificacaoId);
        auditoriaService.registrar(auth.getName(), "CURSO_DESVINCULADO", "militar " + militarId + ", curso " + qualificacaoId);
        return militar;
    }
}
