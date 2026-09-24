package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Boletim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletimRepository extends JpaRepository<Boletim, Long> {
    List<Boletim> findAllByOrderByDataPublicacaoDesc();
}
