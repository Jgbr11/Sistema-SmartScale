package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Afastamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AfastamentoRepository extends JpaRepository<Afastamento, Long> {

    List<Afastamento> findByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            Long militarId, LocalDate data1, LocalDate data2);

    /** RN15 - a pessoa esta afastada nesse dia? (passar o mesmo dia nos dois parametros) */
    boolean existsByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(Long militarId, LocalDate dia, LocalDate mesmoDia);

    List<Afastamento> findByDataFimGreaterThanEqual(LocalDate data);

    /** Afastamentos cujo periodo tem alguma sobreposicao com [inicio, fim] - usado pelos Avisos. */
    List<Afastamento> findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(LocalDate fim, LocalDate inicio);

    /** RF04 - historico completo de afastamentos da pessoa, pra Ficha do Militar. */
    List<Afastamento> findByMilitar_IdOrderByDataInicioDesc(Long militarId);
}
