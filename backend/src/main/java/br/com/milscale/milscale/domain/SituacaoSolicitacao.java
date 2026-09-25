package br.com.milscale.milscale.domain;

/** RF15-RF19 - etapas do fluxo de troca. Nomes gravados no banco: nao renomear. */
public enum SituacaoSolicitacao {
    AGUARDANDO_SUBSTITUTO, EM_TRIAGEM, AGUARDANDO_AUTORIZACAO, AUTORIZADA, NEGADA, CANCELADA;

    /** Texto legivel pra mensagens de erro ("em triagem", "aguardando autorizacao"...). */
    public String legivel() {
        return name().toLowerCase().replace('_', ' ');
    }
}
