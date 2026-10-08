package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.IdentidadeDaOrganizacao;

import java.util.List;

public abstract class RelatorioCsv<T> {

    private static final String SEPARADOR = ";";
    private static final String QUEBRA = "\r\n";
    private static final String BOM_PARA_EXCEL = "﻿";
    private static final String INICIO_DE_FORMULA = "=+-@\t\r";

    public final String gerar() {
        StringBuilder csv = new StringBuilder(BOM_PARA_EXCEL);
        csv.append(linha(cabecalho())).append(QUEBRA);
        for (T item : itens()) {
            csv.append(linha(colunas(item))).append(QUEBRA);
        }
        csv.append(QUEBRA).append(escapar(rodape())).append(QUEBRA);
        return csv.toString();
    }

    public abstract String nomeDoArquivo();

    protected abstract List<String> cabecalho();

    protected abstract List<T> itens();

    protected abstract List<String> colunas(T item);

    protected String rodape() {
        return "Gerado por " + IdentidadeDaOrganizacao.INSTANCIA.assinatura();
    }

    private String linha(List<String> valores) {
        return String.join(SEPARADOR, valores.stream().map(this::escapar).toList());
    }

    private String escapar(String valor) {
        if (valor == null || valor.isEmpty()) return "";
        String seguro = INICIO_DE_FORMULA.indexOf(valor.charAt(0)) >= 0 ? "'" + valor : valor;
        boolean precisaDeAspas = seguro.contains(SEPARADOR) || seguro.contains("\"") || seguro.contains("\n") || seguro.contains("\r");
        return precisaDeAspas ? "\"" + seguro.replace("\"", "\"\"") + "\"" : seguro;
    }
}
