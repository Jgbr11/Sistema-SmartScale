package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.ServicoEscalado;

import java.util.List;
import java.util.Locale;

public class RelatorioServicosDoMilitar extends RelatorioCsv<ServicoEscalado> {

    private final Militar militar;
    private final List<ServicoEscalado> servicos;

    public RelatorioServicosDoMilitar(Militar militar, List<ServicoEscalado> servicos) {
        this.militar = militar;
        this.servicos = servicos;
    }

    @Override
    public String nomeDoArquivo() {
        String nome = militar.getNomeGuerra().toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{Nd}]+", "-");
        return "servicos-" + nome + ".csv";
    }

    @Override
    protected List<String> cabecalho() {
        return List.of("Data", "Serviço", "Situação");
    }

    @Override
    protected List<ServicoEscalado> itens() {
        return servicos;
    }

    @Override
    protected List<String> colunas(ServicoEscalado servico) {
        return List.of(servico.getData().toString(), servico.getTipoServico().getNome(), servico.getSituacao().name());
    }

    @Override
    protected String rodape() {
        return "Serviços de " + militar.getNomeExibicao() + " — " + super.rodape();
    }
}
