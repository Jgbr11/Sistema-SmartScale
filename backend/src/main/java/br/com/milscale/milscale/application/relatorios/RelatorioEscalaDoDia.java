package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.ServicoEscalado;

import java.time.LocalDate;
import java.util.List;

public class RelatorioEscalaDoDia extends RelatorioCsv<ServicoEscalado> {

    private final LocalDate dia;
    private final List<ServicoEscalado> servicos;

    public RelatorioEscalaDoDia(LocalDate dia, List<ServicoEscalado> servicos) {
        this.dia = dia;
        this.servicos = servicos;
    }

    @Override
    public String nomeDoArquivo() {
        return "escala-" + dia + ".csv";
    }

    @Override
    protected List<String> cabecalho() {
        return List.of("Função", "Posto", "Nome de guerra", "Situação");
    }

    @Override
    protected List<ServicoEscalado> itens() {
        return servicos;
    }

    @Override
    protected List<String> colunas(ServicoEscalado servico) {
        boolean vagaEmAberto = servico.getMilitar() == null;
        return List.of(
                servico.getTipoServico().getNome(),
                vagaEmAberto ? "" : servico.getMilitar().getPosto().getSigla(),
                vagaEmAberto ? "VAGA EM ABERTO" : servico.getMilitar().getNomeGuerra(),
                servico.getSituacao().name());
    }
}
