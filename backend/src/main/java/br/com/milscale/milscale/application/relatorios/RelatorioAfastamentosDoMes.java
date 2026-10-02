package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.Afastamento;

import java.time.YearMonth;
import java.util.List;

public class RelatorioAfastamentosDoMes extends RelatorioCsv<Afastamento> {

    private final YearMonth mes;
    private final List<Afastamento> afastamentos;

    public RelatorioAfastamentosDoMes(YearMonth mes, List<Afastamento> afastamentos) {
        this.mes = mes;
        this.afastamentos = afastamentos;
    }

    @Override
    public String nomeDoArquivo() {
        return "afastamentos-" + mes + ".csv";
    }

    @Override
    protected List<String> cabecalho() {
        return List.of("Militar", "Tipo", "Descrição", "Início", "Fim");
    }

    @Override
    protected List<Afastamento> itens() {
        return afastamentos;
    }

    @Override
    protected List<String> colunas(Afastamento afastamento) {
        return List.of(
                afastamento.getMilitar().getNomeExibicao(),
                afastamento.getTipo().name(),
                afastamento.getDescricao(),
                afastamento.getDataInicio().toString(),
                afastamento.getDataFim().toString());
    }
}
