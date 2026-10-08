package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.QualificacaoRepository;
import br.com.milscale.milscale.adapters.persistence.RequisitoServicoRepository;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Qualificacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class QualificacaoService {

    private final QualificacaoRepository qualificacaoRepository;
    private final MilitarRepository militarRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;

    public QualificacaoService(QualificacaoRepository qualificacaoRepository, MilitarRepository militarRepository,
                               RequisitoServicoRepository requisitoServicoRepository) {
        this.qualificacaoRepository = qualificacaoRepository;
        this.militarRepository = militarRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
    }

    public List<Qualificacao> listar() {
        return qualificacaoRepository.findAll();
    }

    @Transactional
    public Qualificacao cadastrar(DadosQualificacao dados) {
        qualificacaoRepository.findByNome(dados.nome().trim()).ifPresent(q -> {
            throw new IllegalArgumentException("Já existe um curso com esse nome");
        });
        return qualificacaoRepository.save(Qualificacao.builder().nome(dados.nome().trim()).descricao(dados.descricao()).build());
    }

    @Transactional
    public Qualificacao atualizar(Long id, DadosQualificacao dados) {
        Qualificacao existente = qualificacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Qualificacao nao encontrada"));
        qualificacaoRepository.findByNome(dados.nome().trim())
                .filter(outra -> !outra.getId().equals(id))
                .ifPresent(outra -> { throw new IllegalArgumentException("Já existe um curso com esse nome"); });
        existente.setNome(dados.nome().trim());
        existente.setDescricao(dados.descricao());
        return qualificacaoRepository.save(existente);
    }

    @Transactional
    public Qualificacao excluir(Long id) {
        Qualificacao qualificacao = qualificacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Qualificacao nao encontrada"));
        if (militarRepository.existsByQualificacoes_Id(id) || requisitoServicoRepository.existsByQualificacao_Id(id)
                || requisitoServicoRepository.existsByQualificacoesExcluidas_Id(id)) {
            throw new IllegalArgumentException("Esse curso está em uso por militares ou por requisitos de serviço — desvincule antes de excluir");
        }
        qualificacaoRepository.delete(qualificacao);
        return qualificacao;
    }

    @Transactional
    public Militar vincular(Long militarId, Long qualificacaoId) {
        Militar m = militarRepository.findById(militarId).orElseThrow(() -> new NoSuchElementException("Militar nao encontrado"));
        Qualificacao q = qualificacaoRepository.findById(qualificacaoId).orElseThrow(() -> new NoSuchElementException("Qualificacao nao encontrada"));
        m.getQualificacoes().add(q);
        return militarRepository.save(m);
    }

    @Transactional
    public Militar desvincular(Long militarId, Long qualificacaoId) {
        Militar m = militarRepository.findById(militarId).orElseThrow(() -> new NoSuchElementException("Militar nao encontrado"));
        Qualificacao q = qualificacaoRepository.findById(qualificacaoId).orElseThrow(() -> new NoSuchElementException("Qualificacao nao encontrada"));
        m.getQualificacoes().remove(q);
        return militarRepository.save(m);
    }
}
