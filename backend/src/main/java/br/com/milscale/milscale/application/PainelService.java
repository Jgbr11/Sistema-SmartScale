package br.com.milscale.milscale.application;

import br.com.smartscale.core.SituacaoPessoa;
import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.SituacaoSolicitacao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class PainelService {

    private final MilitarRepository militarRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final AfastamentoRepository afastamentoRepository;

    public PainelService(MilitarRepository militarRepository, ServicoEscaladoRepository servicoEscaladoRepository,
                         SolicitacaoRepository solicitacaoRepository, AfastamentoRepository afastamentoRepository) {
        this.militarRepository = militarRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.afastamentoRepository = afastamentoRepository;
    }

    public PainelResumo resumo() {
        YearMonth mes = YearMonth.now();
        return new PainelResumo(
                militarRepository.countBySituacao(SituacaoPessoa.ATIVO),
                servicoEscaladoRepository.countByDataBetweenAndMilitarIsNull(mes.atDay(1), mes.atEndOfMonth()),
                solicitacaoRepository.countBySituacao(SituacaoSolicitacao.EM_TRIAGEM),
                solicitacaoRepository.countBySituacao(SituacaoSolicitacao.AGUARDANDO_AUTORIZACAO),
                afastamentoRepository.contarMilitaresAfastadosEm(LocalDate.now()));
    }
}
