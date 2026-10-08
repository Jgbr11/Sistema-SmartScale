package br.com.milscale.milscale.domain;

public enum IdentidadeDaOrganizacao {
    INSTANCIA;

    public String nome() { return "5º Batalhão de Suprimento"; }
    public String sigla() { return "5º B Sup"; }
    public String sistema() { return "MilScale"; }
    public String assinatura() { return sistema() + ", " + nome(); }
}
