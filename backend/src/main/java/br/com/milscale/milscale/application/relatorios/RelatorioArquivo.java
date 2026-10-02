package br.com.milscale.milscale.application.relatorios;

public record RelatorioArquivo(String nomeDoArquivo, String conteudo) {

    public static RelatorioArquivo de(RelatorioCsv<?> relatorio) {
        return new RelatorioArquivo(relatorio.nomeDoArquivo(), relatorio.gerar());
    }
}
