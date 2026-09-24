package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.RequisitoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequisitoServicoRepository extends JpaRepository<RequisitoServico, Long> {
    List<RequisitoServico> findByTipoServico_Id(Long tipoServicoId);
}
