package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.RegraEscalaRepository;
import br.com.milscale.milscale.domain.RegraEscala;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
        if (dados.getIntervaloMinimo() < 0 || dados.getIntervaloMinimo() > 30) {
            throw new IllegalArgumentException("O intervalo mínimo precisa estar entre 0 e 30 dias");
        }
        if (dados.getMaxServicosMes() != null && dados.getMaxServicosMes() < 1) {
            throw new IllegalArgumentException("O máximo de serviços por mês precisa ser pelo menos 1 (ou vazio pra sem limite)");
        }
        existente.setIntervaloMinimo(dados.getIntervaloMinimo());
        existente.setMaxServicosMes(dados.getMaxServicosMes());
        return regraEscalaRepository.save(existente);
    }
}
