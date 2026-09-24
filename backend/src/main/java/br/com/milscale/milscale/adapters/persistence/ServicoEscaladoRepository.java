package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ServicoEscaladoRepository extends JpaRepository<ServicoEscalado, Long> {

    List<ServicoEscalado> findByEscala_Id(Long escalaId);

    List<ServicoEscalado> findByMilitar_IdAndDataBetween(Long militarId, LocalDate inicio, LocalDate fim);

    List<ServicoEscalado> findByDataAndEscala_Id(LocalDate data, Long escalaId);

    boolean existsByMilitar_IdAndData(Long militarId, LocalDate data);

    List<ServicoEscalado> findByMilitar_IdAndDataBetweenOrderByDataDesc(Long militarId, LocalDate inicio, LocalDate fim);

    List<ServicoEscalado> findByDataBetween(LocalDate inicio, LocalDate fim);

    /** RF04 - historico completo de servicos da pessoa, pra Ficha do Militar. */
    List<ServicoEscalado> findByMilitar_IdOrderByDataDesc(Long militarId);

    /** RF14 - roster de um unico dia, independente de qual escala cobre a data (linha do tempo continua). */
    List<ServicoEscalado> findByData(LocalDate data);

    /** RF15 - candidatos a troca mutua: todo mundo com servico do mesmo tipo, ainda previsto. */
    List<ServicoEscalado> findByTipoServico_IdAndSituacao(Long tipoServicoId, String situacao);
}
