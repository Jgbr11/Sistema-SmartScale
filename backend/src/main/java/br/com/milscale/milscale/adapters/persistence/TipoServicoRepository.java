package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.TipoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoServicoRepository extends JpaRepository<TipoServico, Long> {
    List<TipoServico> findByAtivoTrue();
}
