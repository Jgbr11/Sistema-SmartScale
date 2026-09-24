package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.TipoServicoRepository;
import br.com.milscale.milscale.domain.TipoServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** RF06 - tipos de turno (tipos de servico) com efetivo e requisitos. */
@Service
public class TipoServicoService {

    private final TipoServicoRepository tipoServicoRepository;

    public TipoServicoService(TipoServicoRepository tipoServicoRepository) {
        this.tipoServicoRepository = tipoServicoRepository;
    }

    public List<TipoServico> listar() {
        return tipoServicoRepository.findAll();
    }

    @Transactional
    public TipoServico cadastrar(TipoServico tipo) {
        tipo.setId(null);
        tipo.setAtivo(true);
        return tipoServicoRepository.save(tipo);
    }

    @Transactional
    public TipoServico atualizar(Long id, TipoServico dados) {
        TipoServico existente = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tipo de servico nao encontrado"));
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setEfetivoNecessario(dados.getEfetivoNecessario());
        existente.setHoraInicio(dados.getHoraInicio());
        existente.setDuracaoHoras(dados.getDuracaoHoras());
        return tipoServicoRepository.save(existente);
    }

    @Transactional
    public TipoServico desativar(Long id) {
        TipoServico existente = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tipo de servico nao encontrado"));
        existente.setAtivo(false);
        return tipoServicoRepository.save(existente);
    }
}
