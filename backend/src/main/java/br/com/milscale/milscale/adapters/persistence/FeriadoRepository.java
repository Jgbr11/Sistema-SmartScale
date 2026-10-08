package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    List<Feriado> findAllByOrderByDataInicioAsc();

    List<Feriado> findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(LocalDate fim, LocalDate inicio);
}
