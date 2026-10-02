package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.EscalaRepository;
import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.RequisitoServicoRepository;
import br.com.milscale.milscale.adapters.persistence.SubunidadeRepository;
import br.com.milscale.milscale.domain.Subunidade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SubunidadeService {

    private static final Long SEM_ID = -1L;

    private final SubunidadeRepository subunidadeRepository;
    private final MilitarRepository militarRepository;
    private final RequisitoServicoRepository requisitoRepository;
    private final EscalaRepository escalaRepository;

    public SubunidadeService(SubunidadeRepository subunidadeRepository, MilitarRepository militarRepository,
                             RequisitoServicoRepository requisitoRepository, EscalaRepository escalaRepository) {
        this.subunidadeRepository = subunidadeRepository;
        this.militarRepository = militarRepository;
        this.requisitoRepository = requisitoRepository;
        this.escalaRepository = escalaRepository;
    }

    public List<Subunidade> listar() {
        return subunidadeRepository.findAllByOrderBySiglaAsc();
    }

    @Transactional
    public Subunidade cadastrar(DadosSubunidade dados) {
        validarUnicidade(dados, SEM_ID);
        Subunidade nova = new Subunidade();
        aplicar(nova, dados);
        return subunidadeRepository.save(nova);
    }

    @Transactional
    public Subunidade atualizar(Long id, DadosSubunidade dados) {
        Subunidade existente = buscar(id);
        validarUnicidade(dados, id);
        aplicar(existente, dados);
        return subunidadeRepository.save(existente);
    }

    @Transactional
    public boolean excluir(Long id) {
        Subunidade subunidade = buscar(id);
        if (emUso(id)) {
            subunidade.setAtivo(false);
            subunidadeRepository.save(subunidade);
            return true;
        }
        subunidadeRepository.delete(subunidade);
        return false;
    }

    private boolean emUso(Long id) {
        return militarRepository.existsBySubunidade_Id(id)
                || requisitoRepository.existsBySubunidade_IdOrSubunidadeExcluida_Id(id, id)
                || escalaRepository.existsBySubunidade_Id(id);
    }

    private Subunidade buscar(Long id) {
        return subunidadeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Subunidade não encontrada"));
    }

    private void validarUnicidade(DadosSubunidade dados, Long id) {
        if (subunidadeRepository.existsBySiglaIgnoreCaseAndIdNot(dados.sigla().trim(), id)) {
            throw new IllegalArgumentException("Já existe uma subunidade com essa sigla");
        }
    }

    private void aplicar(Subunidade subunidade, DadosSubunidade dados) {
        subunidade.setSigla(dados.sigla().trim());
        subunidade.setNome(dados.nome().trim());
        subunidade.setAtivo(dados.ativo());
    }
}
