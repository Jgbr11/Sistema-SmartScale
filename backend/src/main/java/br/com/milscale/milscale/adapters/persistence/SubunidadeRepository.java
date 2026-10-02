package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Subunidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubunidadeRepository extends JpaRepository<Subunidade, Long> {

    List<Subunidade> findAllByOrderBySiglaAsc();

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
}
