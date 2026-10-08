package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.SituacaoSolicitacao;
import br.com.milscale.milscale.domain.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.time.LocalDate;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    List<Solicitacao> findBySolicitante_IdOrderByDataSolicitacaoDesc(Long solicitanteId);
    List<Solicitacao> findBySituacaoOrderByDataSolicitacaoAsc(SituacaoSolicitacao situacao);
    List<Solicitacao> findByServicoOrigem_DataBetween(LocalDate inicio, LocalDate fim);

    List<Solicitacao> findByServicoDestino_DataBetween(LocalDate inicio, LocalDate fim);
    List<Solicitacao> findBySubstituto_IdAndSituacaoOrderByDataSolicitacaoAsc(Long substitutoId, SituacaoSolicitacao situacao);

    List<Solicitacao> findBySolicitante_IdOrSubstituto_IdOrderByDataSolicitacaoDesc(Long solicitanteId, Long substitutoId);

    boolean existsByServicoOrigem_IdAndSituacaoIn(Long servicoId, Collection<SituacaoSolicitacao> situacoes);

    boolean existsByServicoDestino_IdAndSituacaoIn(Long servicoId, Collection<SituacaoSolicitacao> situacoes);

    long countBySituacao(SituacaoSolicitacao situacao);
}
