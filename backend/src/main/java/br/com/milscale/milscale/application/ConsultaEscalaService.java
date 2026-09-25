package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.EscalaRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.Escala;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** RF13/RF14 - leituras da escala (mes completo e um dia). */
@Service
public class ConsultaEscalaService {

    private final EscalaRepository escalaRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;

    public ConsultaEscalaService(EscalaRepository escalaRepository, ServicoEscaladoRepository servicoEscaladoRepository) {
        this.escalaRepository = escalaRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
    }

    public List<Escala> listar() {
        return escalaRepository.findAllByOrderByDataInicioDesc();
    }

    public Escala buscar(Long id) {
        return escalaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Escala não encontrada"));
    }

    public List<ServicoEscalado> doDia(LocalDate data) {
        return servicoEscaladoRepository.findByData(data);
    }
}
