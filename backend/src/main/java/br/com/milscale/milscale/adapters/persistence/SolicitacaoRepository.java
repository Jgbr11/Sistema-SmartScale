package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.SituacaoSolicitacao;
import br.com.milscale.milscale.domain.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    List<Solicitacao> findBySolicitante_IdOrderByDataSolicitacaoDesc(Long solicitanteId);
    List<Solicitacao> findBySituacaoOrderByDataSolicitacaoAsc(SituacaoSolicitacao situacao);
    List<Solicitacao> findByServicoOrigem_DataBetween(java.time.LocalDate inicio, java.time.LocalDate fim);
    List<Solicitacao> findBySubstituto_IdAndSituacaoOrderByDataSolicitacaoAsc(Long substitutoId, SituacaoSolicitacao situacao);

    /** RF04 - historico completo de trocas da pessoa (pedidas ou recebidas), pra Ficha do Militar. */
    List<Solicitacao> findBySolicitante_IdOrSubstituto_IdOrderByDataSolicitacaoDesc(Long solicitanteId, Long substitutoId);
}
