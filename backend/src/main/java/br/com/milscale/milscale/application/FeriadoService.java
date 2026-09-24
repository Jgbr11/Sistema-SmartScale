package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.FeriadoRepository;
import br.com.milscale.milscale.domain.Feriado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** Feriados - usados pelo peso_feriado das regras da escala (RegraEscala). */
@Service
public class FeriadoService {

    private final FeriadoRepository feriadoRepository;

    public FeriadoService(FeriadoRepository feriadoRepository) {
        this.feriadoRepository = feriadoRepository;
    }

    public List<Feriado> listar() {
        return feriadoRepository.findAllByOrderByDataInicioAsc();
    }

    @Transactional
    public Feriado cadastrar(Feriado f) {
        if (f.getDataFim().isBefore(f.getDataInicio())) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }
        f.setId(null);
        return feriadoRepository.save(f);
    }

    @Transactional
    public void remover(Long id) {
        if (!feriadoRepository.existsById(id)) throw new NoSuchElementException("Feriado não encontrado");
        feriadoRepository.deleteById(id);
    }
}
