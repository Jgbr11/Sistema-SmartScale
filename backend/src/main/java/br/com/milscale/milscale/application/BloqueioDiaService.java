package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * RF12 - travar um dia da escala impede troca ou alteracao manual nesse
 * dia, mesmo pelo Sargenteante (RN04). Privativo do Sargenteante (RN11).
 */
@Service
public class BloqueioDiaService {

    private final ServicoEscaladoRepository servicoEscaladoRepository;

    public BloqueioDiaService(ServicoEscaladoRepository servicoEscaladoRepository) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
    }

    @Transactional
    public List<ServicoEscalado> travar(Long escalaId, LocalDate data) {
        return alternarTravamento(escalaId, data, true);
    }

    @Transactional
    public List<ServicoEscalado> destravar(Long escalaId, LocalDate data) {
        return alternarTravamento(escalaId, data, false);
    }

    private List<ServicoEscalado> alternarTravamento(Long escalaId, LocalDate data, boolean travado) {
        List<ServicoEscalado> doDia = servicoEscaladoRepository.findByDataAndEscala_Id(data, escalaId);
        for (ServicoEscalado s : doDia) {
            s.setTravado(travado);
        }
        return servicoEscaladoRepository.saveAll(doDia);
    }
}
