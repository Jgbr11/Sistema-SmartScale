package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.EscalaRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.BloqueioDiaService;
import br.com.milscale.milscale.application.GerarEscalaService;
import br.com.milscale.milscale.application.PublicarEscalaService;
import br.com.milscale.milscale.domain.Escala;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/escalas")
public class EscalaController {

    private final GerarEscalaService gerarEscalaService;
    private final PublicarEscalaService publicarEscalaService;
    private final BloqueioDiaService bloqueioDiaService;
    private final EscalaRepository escalaRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public EscalaController(GerarEscalaService gerarEscalaService,
                             PublicarEscalaService publicarEscalaService,
                             BloqueioDiaService bloqueioDiaService,
                             EscalaRepository escalaRepository,
                             ServicoEscaladoRepository servicoEscaladoRepository,
                             UsuarioRepository usuarioRepository,
                             AuditoriaService auditoriaService) {
        this.gerarEscalaService = gerarEscalaService;
        this.publicarEscalaService = publicarEscalaService;
        this.bloqueioDiaService = bloqueioDiaService;
        this.escalaRepository = escalaRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    /** RF14 - visão completa do mês. Militar Escalado NÃO entra aqui (RF13 é o dele: só a própria escala + Escala do dia). */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping
    public List<Escala> listar() {
        return escalaRepository.findAllByOrderByDataInicioDesc();
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
    @GetMapping("/{id}")
    public Escala buscar(@PathVariable Long id) {
        return escalaRepository.findById(id).orElseThrow();
    }

    /** RF14 - roster de UM dia especifico, aberto a qualquer autenticado (inclusive Militar Escalado
     *  pela tela "Escala do dia") - nunca devolve o mes inteiro, só a data pedida. */
    @GetMapping("/dia")
    public List<ServicoEscalado> escalaDoDia(@RequestParam String data) {
        return servicoEscaladoRepository.findByData(LocalDate.parse(data));
    }

    /** RF08 - gerar automaticamente. Privativo de Cabo da Sargenteacao ou Sargenteante. */
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/gerar")
    public Escala gerar(@RequestBody Map<String, String> body, Authentication auth) {
        LocalDate inicio = LocalDate.parse(body.get("dataInicio"));
        LocalDate fim = LocalDate.parse(body.get("dataFim"));
        var usuario = usuarioRepository.findByLogin(auth.getName()).orElseThrow();
        Escala escala = gerarEscalaService.gerar(inicio, fim, usuario);
        auditoriaService.registrar(auth.getName(), "ESCALA_GERADA", inicio + " a " + fim);
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
