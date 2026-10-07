package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.TipoServicoRepository;
import br.com.milscale.milscale.domain.TipoServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
    public TipoServico cadastrar(DadosTipoServico dados) {
        TipoServico tipo = TipoServico.builder().ativo(true).build();
        aplicar(dados, tipo);
        return tipoServicoRepository.save(tipo);
    }

    @Transactional
    public TipoServico atualizar(Long id, DadosTipoServico dados) {
        TipoServico existente = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tipo de servico nao encontrado"));
        aplicar(dados, existente);
        return tipoServicoRepository.save(existente);
    }

    private void aplicar(DadosTipoServico d, TipoServico t) {
        t.setNome(d.nome().trim());
        t.setDescricao(d.descricao());
        t.setEfetivoNecessario(d.efetivoNecessario());
        t.setHoraInicio(d.horaInicio() != null ? d.horaInicio() : LocalTime.of(8, 0));
        t.setDuracaoHoras(d.duracaoHoras() != null ? d.duracaoHoras() : 24);
    }

    @Transactional
    public TipoServico desativar(Long id) {
        TipoServico existente = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tipo de servico nao encontrado"));
        existente.setAtivo(false);
        return tipoServicoRepository.save(existente);
    }
}
