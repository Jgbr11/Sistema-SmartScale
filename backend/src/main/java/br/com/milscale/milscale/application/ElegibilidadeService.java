package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.RequisitoServico;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElegibilidadeService {

    public boolean elegivel(Militar m, List<RequisitoServico> requisitos) {
        for (RequisitoServico r : requisitos) {
            if (!r.getPosto().getId().equals(m.getPosto().getId())) continue;
            if (r.getSubunidade() != null && !r.getSubunidade().getId().equals(m.getSubunidade().getId())) continue;
            if (r.getSubunidadeExcluida() != null && r.getSubunidadeExcluida().getId().equals(m.getSubunidade().getId())) continue;
            if (temQualificacaoExcluida(m, r)) continue;
            if (r.getQualificacao() == null) return true;
            if (m.getQualificacoes().contains(r.getQualificacao())) return true;
        }
        return false;
    }

    private boolean temQualificacaoExcluida(Militar m, RequisitoServico r) {
        if (r.getQualificacoesExcluidas().isEmpty()) return false;
        return m.getQualificacoes().stream().anyMatch(r.getQualificacoesExcluidas()::contains);
    }
}
