package br.com.milscale.milscale.domain;

import java.util.EnumSet;
import java.util.Set;

/** RF15-RF19 - etapas do fluxo de troca. Nomes gravados no banco: nao renomear. */
public enum SituacaoSolicitacao {
    AGUARDANDO_SUBSTITUTO, EM_TRIAGEM, AGUARDANDO_AUTORIZACAO, AUTORIZADA, NEGADA, CANCELADA;

    public static final Set<SituacaoSolicitacao> EM_ANDAMENTO =
            EnumSet.of(AGUARDANDO_SUBSTITUTO, EM_TRIAGEM, AGUARDANDO_AUTORIZACAO);

    public boolean emAndamento() {
        return EM_ANDAMENTO.contains(this);
    }

    /** Texto legivel pra mensagens de erro ("em triagem", "aguardando autorizacao"...). */
    public String legivel() {
        return name().toLowerCase().replace('_', ' ');
    }
}
