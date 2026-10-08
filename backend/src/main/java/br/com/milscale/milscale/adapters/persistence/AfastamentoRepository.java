package br.com.milscale.milscale.adapters.persistence;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import br.com.milscale.milscale.domain.Afastamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AfastamentoRepository extends JpaRepository<Afastamento, Long> {

    List<Afastamento> findByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            Long militarId, LocalDate data1, LocalDate data2);

    boolean existsByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(Long militarId, LocalDate dia, LocalDate mesmoDia);

    List<Afastamento> findByDataFimGreaterThanEqual(LocalDate data);

    List<Afastamento> findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(LocalDate fim, LocalDate inicio);

    List<Afastamento> findByMilitar_IdOrderByDataInicioDesc(Long militarId);

    List<Afastamento> findByLoteMissao(String loteMissao);

    @Query("select count(distinct a.militar.id) from Afastamento a where a.dataInicio <= :dia and a.dataFim >= :dia")
    long contarMilitaresAfastadosEm(@Param("dia") LocalDate dia);
}
