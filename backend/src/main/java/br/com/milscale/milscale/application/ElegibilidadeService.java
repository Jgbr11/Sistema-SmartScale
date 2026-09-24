package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.RequisitoServico;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RF06 - regra unica de elegibilidade (posto + subunidade opcional +
 * qualificacao exigida opcional + qualificacoes excluidas opcional).
 *
 * Extraido pra um lugar so porque essa mesma checagem era repetida em
 * GerarEscalaService, AfastamentoService e SolicitacaoService - qualquer
 * ajuste de regra (como "CFC e Motorista nao tiram Monitoramento")
 * precisava ser replicado nos tres. Reuso de verdade: uma unica fonte.
 */
@Service
public class ElegibilidadeService {

    public boolean elegivel(Militar m, List<RequisitoServico> requisitos) {
        for (RequisitoServico r : requisitos) {
            if (!r.getPosto().getId().equals(m.getPosto().getId())) continue;
            if (r.getSubunidade() != null && !r.getSubunidade().getId().equals(m.getSubunidade().getId())) continue;
            if (r.getSubunidadeExcluida() != null && r.getSubunidadeExcluida().getId().equals(m.getSubunidade().getId())) continue;
            if (temQualificacaoExcluida(m, r)) continue;
            if (r.getQualificacao() == null) return true; // exige só posto (e subunidade, se houver)
            if (m.getQualificacoes().contains(r.getQualificacao())) return true; // exige posto + curso, e a pessoa tem
        }
        return false;
    }

    private boolean temQualificacaoExcluida(Militar m, RequisitoServico r) {
        if (r.getQualificacoesExcluidas().isEmpty()) return false;
        return m.getQualificacoes().stream().anyMatch(r.getQualificacoesExcluidas()::contains);
    }
}
