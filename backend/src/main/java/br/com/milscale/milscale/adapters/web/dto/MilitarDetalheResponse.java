package br.com.milscale.milscale.adapters.web.dto;

import br.com.smartscale.core.SituacaoPessoa;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.milscale.milscale.domain.Qualificacao;
import br.com.milscale.milscale.domain.Subunidade;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MilitarDetalheResponse(
        Long id, String nomeCompleto, String nomeGuerra, String nomeExibicao,
        PostoGraduacao posto, Subunidade subunidade, SituacaoPessoa situacao,
        long contadorRodizio, Set<Qualificacao> qualificacoes, boolean temFoto,
        String cpf, String numeroRegistro, LocalDate dataNascimento, String fusex,
        String email, String telefone) {

    public static MilitarDetalheResponse completo(Militar m) {
        return new MilitarDetalheResponse(m.getId(), m.getNomeCompleto(), m.getNomeGuerra(), m.getNomeExibicao(),
                m.getPosto(), m.getSubunidade(), m.getSituacao(), m.getContadorRodizio(), m.getQualificacoes(), m.isTemFoto(),
                m.getCpf(), m.getNumeroRegistro(), m.getDataNascimento(), m.getFusex(), m.getEmail(), m.getTelefone());
    }

    public static MilitarDetalheResponse publico(Militar m) {
        return new MilitarDetalheResponse(m.getId(), m.getNomeCompleto(), m.getNomeGuerra(), m.getNomeExibicao(),
                m.getPosto(), m.getSubunidade(), m.getSituacao(), m.getContadorRodizio(), m.getQualificacoes(), m.isTemFoto(),
                null, null, null, null, null, null);
    }
}
