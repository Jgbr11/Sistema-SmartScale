package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** RF13 - a pessoa escalada consulta a propria escala. */
@Service
public class MinhaEscalaService {

    private final ServicoEscaladoRepository servicoEscaladoRepository;

    public MinhaEscalaService(ServicoEscaladoRepository servicoEscaladoRepository) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
    }

    public List<ServicoEscalado> doMes(Long militarId, YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        return servicoEscaladoRepository.findByMilitar_IdAndDataBetween(militarId, inicio, fim);
    }
}
