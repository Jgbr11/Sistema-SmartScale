package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.PostoGraduacaoRepository;
import br.com.milscale.milscale.adapters.persistence.RequisitoServicoRepository;
import br.com.milscale.milscale.domain.PostoGraduacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostoGraduacaoService {

    private static final Long SEM_ID = -1L;

    private final PostoGraduacaoRepository postoRepository;
    private final MilitarRepository militarRepository;
    private final RequisitoServicoRepository requisitoRepository;

    public PostoGraduacaoService(PostoGraduacaoRepository postoRepository, MilitarRepository militarRepository,
                                 RequisitoServicoRepository requisitoRepository) {
        this.postoRepository = postoRepository;
        this.militarRepository = militarRepository;
        this.requisitoRepository = requisitoRepository;
    }

    public List<PostoGraduacao> listar() {
        return postoRepository.findAllByOrderByNivelHierarquicoAsc();
    }

    @Transactional
    public PostoGraduacao cadastrar(DadosPostoGraduacao dados) {
        validarUnicidade(dados, SEM_ID);
        PostoGraduacao novo = new PostoGraduacao();
        aplicar(novo, dados);
        return postoRepository.save(novo);
    }

    @Transactional
    public PostoGraduacao atualizar(Long id, DadosPostoGraduacao dados) {
        PostoGraduacao existente = buscar(id);
        validarUnicidade(dados, id);
        aplicar(existente, dados);
        return postoRepository.save(existente);
    }

    @Transactional
    public PostoGraduacao excluir(Long id) {
        PostoGraduacao posto = buscar(id);
        if (militarRepository.existsByPosto_Id(id) || requisitoRepository.existsByPosto_Id(id)) {
            throw new IllegalArgumentException("Esse posto está em uso por militares ou por requisitos de serviço");
        }
        postoRepository.delete(posto);
        return posto;
    }

    private PostoGraduacao buscar(Long id) {
        return postoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Posto/graduação não encontrado"));
    }

    private void validarUnicidade(DadosPostoGraduacao dados, Long id) {
        if (postoRepository.existsBySiglaIgnoreCaseAndIdNot(dados.sigla().trim(), id)) {
            throw new IllegalArgumentException("Já existe um posto com essa sigla");
        }
        if (postoRepository.existsByNivelHierarquicoAndIdNot(dados.nivelHierarquico(), id)) {
            throw new IllegalArgumentException("Já existe um posto com esse nível hierárquico");
        }
    }

    private void aplicar(PostoGraduacao posto, DadosPostoGraduacao dados) {
        posto.setSigla(dados.sigla().trim());
        posto.setDescricao(dados.descricao().trim());
        posto.setNivelHierarquico(dados.nivelHierarquico());
    }
}
