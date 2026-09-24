package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Notificacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByDestinatario_IdOrderByDataCriacaoDesc(Long usuarioId, Pageable limite);
    long countByDestinatario_IdAndLidaFalse(Long usuarioId);
    List<Notificacao> findByDestinatario_IdAndLidaFalse(Long usuarioId);
}
