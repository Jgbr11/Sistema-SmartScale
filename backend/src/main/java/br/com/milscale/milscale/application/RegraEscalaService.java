package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.RegraEscalaRepository;
import br.com.milscale.milscale.domain.RegraEscala;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** RF07 - regras da escala. Manutencao privativa do Sargenteante (RN11). */
@Service
public class RegraEscalaService {

    private final RegraEscalaRepository regraEscalaRepository;

    public RegraEscalaService(RegraEscalaRepository regraEscalaRepository) {
        this.regraEscalaRepository = regraEscalaRepository;
    }

    public List<RegraEscala> listar() {
        return regraEscalaRepository.findAll();
    }

    @Transactional
    public RegraEscala atualizar(Long id, RegraEscala dados) {
        RegraEscala existente = regraEscalaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Regra nao encontrada"));
        existente.setIntervaloMinimo(dados.getIntervaloMinimo());
        existente.setDiasFolga(dados.getDiasFolga());
        existente.setMaxServicosMes(dados.getMaxServicosMes());
        existente.setPesoFimSemana(dados.getPesoFimSemana());
        existente.setPesoFeriado(dados.getPesoFeriado());
        return regraEscalaRepository.save(existente);
    }
}
