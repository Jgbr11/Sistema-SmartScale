package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.relatorios.RelatorioArquivo;
import br.com.milscale.milscale.application.relatorios.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
public class RelatorioController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/escala-do-dia.csv")
    public ResponseEntity<byte[]> escalaDoDia(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return csv(relatorioService.escalaDoDia(data));
    }

    @GetMapping("/militares/{id}/servicos.csv")
    public ResponseEntity<byte[]> servicosDoMilitar(@PathVariable Long id) {
        return csv(relatorioService.servicosDoMilitar(id));
    }

    @GetMapping("/afastamentos.csv")
    public ResponseEntity<byte[]> afastamentos(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return csv(relatorioService.afastamentosDoMes(mes));
    }

    private ResponseEntity<byte[]> csv(RelatorioArquivo arquivo) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nomeDoArquivo(), StandardCharsets.UTF_8).build().toString())
                .body(arquivo.conteudo().getBytes(StandardCharsets.UTF_8));
    }
}
