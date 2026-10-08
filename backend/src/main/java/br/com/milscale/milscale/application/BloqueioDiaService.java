package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BloqueioDiaService {

    private final ServicoEscaladoRepository servicoEscaladoRepository;

    public BloqueioDiaService(ServicoEscaladoRepository servicoEscaladoRepository) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
    }

    @Transactional
    public List<ServicoEscalado> travar(LocalDate data) {
        return alternarTravamento(data, true);
    }

    @Transactional
    public List<ServicoEscalado> destravar(LocalDate data) {
        return alternarTravamento(data, false);
    }

    private List<ServicoEscalado> alternarTravamento(LocalDate data, boolean travado) {
        List<ServicoEscalado> doDia = servicoEscaladoRepository.findByData(data);
        if (doDia.stream().anyMatch(ServicoEscalado::isJaComecou)) {
            throw new IllegalArgumentException("Esse dia já começou — ele já está confirmado e não pode mais ser travado ou destravado");
        }
        for (ServicoEscalado s : doDia) {
            s.setTravado(travado);
        }
        return servicoEscaladoRepository.saveAll(doDia);
    }
}
