package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.FeriadoRepository;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Feriado;
import br.com.milscale.milscale.domain.Militar;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RF26/Feriados - visão unificada pra tela de Avisos: feriados e
 * afastamentos (missão, dispensa, férias...) que caem num mês, prontos
 * pra colorir um calendário e listar embaixo. Aberto a qualquer
 * autenticado - inclusive Militar Escalado, que não tem acesso às
 * telas de gestão (Feriados / Missões e dispensas) mas precisa saber
 * o que vem por aí.
 */
@Service
public class AvisoService {

    private final FeriadoRepository feriadoRepository;
    private final AfastamentoRepository afastamentoRepository;

    public AvisoService(FeriadoRepository feriadoRepository, AfastamentoRepository afastamentoRepository) {
        this.feriadoRepository = feriadoRepository;
        this.afastamentoRepository = afastamentoRepository;
    }

    public record Aviso(String chave, String tipo, LocalDate dataInicio, LocalDate dataFim, String descricao, List<Militar> militares) {}

    public List<Aviso> listarDoMes(YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();

        List<Aviso> avisos = new ArrayList<>();

        for (Feriado f : feriadoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(fim, inicio)) {
            avisos.add(new Aviso("feriado-" + f.getId(), "FERIADO", f.getDataInicio(), f.getDataFim(), f.getDescricao(), List.of()));
        }

        List<Afastamento> afastamentos = afastamentoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(fim, inicio);
        // Agrupa por lote (uma missao com varios militares vira 1 aviso so,
        // nao um por pessoa) - quem nao tem lote (afastamento antigo/individual
        // sem essa marcação) agrupa por id mesmo, cada um vira o seu proprio aviso.
        Map<String, List<Afastamento>> porLote = afastamentos.stream()
                .collect(Collectors.groupingBy(a -> a.getLoteMissao() != null ? a.getLoteMissao() : "solo-" + a.getId()));

        for (var entrada : porLote.entrySet()) {
            List<Afastamento> grupo = entrada.getValue();
            Afastamento primeiro = grupo.get(0);
            List<Militar> militares = grupo.stream().map(Afastamento::getMilitar).toList();
            avisos.add(new Aviso("afastamento-" + entrada.getKey(), primeiro.getTipo().name(), primeiro.getDataInicio(), primeiro.getDataFim(), primeiro.getDescricao(), militares));
        }

        avisos.sort(Comparator.comparing(Aviso::dataInicio));
        return avisos;
    }
}
