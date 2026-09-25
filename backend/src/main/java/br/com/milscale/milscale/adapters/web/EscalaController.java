package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.web.dto.GerarEscalaRequest;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.BloqueioDiaService;
import br.com.milscale.milscale.application.ConsultaEscalaService;
import br.com.milscale.milscale.application.GerarEscalaService;
import br.com.milscale.milscale.application.PublicarEscalaService;
import br.com.milscale.milscale.application.UsuarioLogadoService;
import br.com.milscale.milscale.domain.Escala;
import br.com.milscale.milscale.domain.ServicoEscalado;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/escalas")
public class EscalaController {

    private final GerarEscalaService gerarEscalaService;
    private final PublicarEscalaService publicarEscalaService;
    private final BloqueioDiaService bloqueioDiaService;
    private final ConsultaEscalaService consultaEscalaService;
    private final UsuarioLogadoService usuarioLogadoService;
    private final AuditoriaService auditoriaService;

    public EscalaController(GerarEscalaService gerarEscalaService,
                             PublicarEscalaService publicarEscalaService,
                             BloqueioDiaService bloqueioDiaService,
                             ConsultaEscalaService consultaEscalaService,
                             UsuarioLogadoService usuarioLogadoService,
                             AuditoriaService auditoriaService) {
        this.gerarEscalaService = gerarEscalaService;
        this.publicarEscalaService = publicarEscalaService;
        this.bloqueioDiaService = bloqueioDiaService;
        this.consultaEscalaService = consultaEscalaService;
        this.usuarioLogadoService = usuarioLogadoService;
        this.auditoriaService = auditoriaService;
    }

    /** RF14 - visão completa do mês. Militar Escalado NÃO entra aqui (RF13 é o dele: só a própria escala + Escala do dia). */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping
    public List<Escala> listar() {
        return consultaEscalaService.listar();
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping("/{id}")
    public Escala buscar(@PathVariable Long id) {
        return consultaEscalaService.buscar(id);
    }

    /** RF14 - roster de UM dia especifico, aberto a qualquer autenticado (inclusive Militar Escalado
     *  pela tela "Escala do dia") - nunca devolve o mes inteiro, só a data pedida. */
    @GetMapping("/dia")
    public List<ServicoEscalado> escalaDoDia(@RequestParam String data) {
        return consultaEscalaService.doDia(LocalDate.parse(data));
    }

    /** RF08 - gerar automaticamente. Privativo de Cabo da Sargenteacao ou Sargenteante. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/gerar")
    public Escala gerar(@Valid @RequestBody GerarEscalaRequest req, Authentication auth) {
        var usuario = usuarioLogadoService.usuario(auth.getName());
        Escala escala = gerarEscalaService.gerar(req.dataInicio(), req.dataFim(), usuario);
        auditoriaService.registrar(auth.getName(), "ESCALA_GERADA", req.dataInicio() + " a " + req.dataFim());
        return escala;
    }

    /** RF11/RN14 - publicar. Privativo do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/publicar")
    public Escala publicar(@PathVariable Long id, Authentication auth) {
        Escala escala = publicarEscalaService.publicar(id);
        auditoriaService.registrar(auth.getName(), "ESCALA_PUBLICADA", escala.getDescricao());
        return escala;
    }

    /** RF12/RN04 - travar um dia (nenhuma troca ou alteracao manual e aceita). Privativo do Sargenteante. */
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/dias/{data}/travar")
    public List<ServicoEscalado> travarDia(@PathVariable Long id, @PathVariable String data, Authentication auth) {
        List<ServicoEscalado> resultado = bloqueioDiaService.travar(id, LocalDate.parse(data));
        auditoriaService.registrar(auth.getName(), "DIA_TRAVADO", data);
        return resultado;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/dias/{data}/destravar")
    public List<ServicoEscalado> destravarDia(@PathVariable Long id, @PathVariable String data, Authentication auth) {
        List<ServicoEscalado> resultado = bloqueioDiaService.destravar(id, LocalDate.parse(data));
        auditoriaService.registrar(auth.getName(), "DIA_DESTRAVADO", data);
        return resultado;
    }
}
