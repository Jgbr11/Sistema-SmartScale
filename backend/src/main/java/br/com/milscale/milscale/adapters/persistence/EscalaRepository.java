package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Escala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalaRepository extends JpaRepository<Escala, Long> {
    List<Escala> findAllByOrderByDataInicioDesc();
}
