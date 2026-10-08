package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.FeriadoRepository;
import br.com.milscale.milscale.domain.Feriado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
    public Feriado cadastrar(DadosFeriado dados) {
        Feriado novo = new Feriado();
        aplicar(novo, dados);
        return feriadoRepository.save(novo);
    }

    @Transactional
    public Feriado atualizar(Long id, DadosFeriado dados) {
        Feriado existente = feriadoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Feriado não encontrado"));
        aplicar(existente, dados);
        return feriadoRepository.save(existente);
    }

    private void aplicar(Feriado feriado, DadosFeriado dados) {
        if (dados.dataFim().isBefore(dados.dataInicio())) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }
        feriado.setDataInicio(dados.dataInicio());
        feriado.setDataFim(dados.dataFim());
        feriado.setDescricao(dados.descricao().trim());
        feriado.setTipo(dados.tipo());
    }

    @Transactional
    public void remover(Long id) {
        if (!feriadoRepository.existsById(id)) throw new NoSuchElementException("Feriado não encontrado");
        feriadoRepository.deleteById(id);
    }
}
