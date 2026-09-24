package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.RegraEscala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegraEscalaRepository extends JpaRepository<RegraEscala, Long> {
    Optional<RegraEscala> findByTipoServico_Id(Long tipoServicoId);
}
