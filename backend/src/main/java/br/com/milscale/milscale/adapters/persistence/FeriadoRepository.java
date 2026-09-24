package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    List<Feriado> findAllByOrderByDataInicioAsc();

    /** Feriados cujo período tem alguma sobreposição com [inicio, fim]. */
    List<Feriado> findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(java.time.LocalDate fim, java.time.LocalDate inicio);
}
