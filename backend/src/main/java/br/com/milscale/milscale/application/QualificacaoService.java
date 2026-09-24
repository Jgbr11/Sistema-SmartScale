package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.QualificacaoRepository;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Qualificacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** RF05 - catalogo de qualificacoes (cursos/habilitacoes) e o vinculo com militares. */
@Service
public class QualificacaoService {

    private final QualificacaoRepository qualificacaoRepository;
    private final MilitarRepository militarRepository;

    public QualificacaoService(QualificacaoRepository qualificacaoRepository, MilitarRepository militarRepository) {
        this.qualificacaoRepository = qualificacaoRepository;
        this.militarRepository = militarRepository;
    }

    public List<Qualificacao> listar() {
        return qualificacaoRepository.findAll();
    }

    /** Privativo do Sargenteante (catalogo, mesmo nivel de Tipos de Servico - RN11). */
    @Transactional
    public Qualificacao cadastrar(Qualificacao q) {
        q.setId(null);
        return qualificacaoRepository.save(q);
    }

    @Transactional
    public Qualificacao atualizar(Long id, Qualificacao dados) {
        Qualificacao existente = qualificacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Qualificacao nao encontrada"));
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        return qualificacaoRepository.save(existente);
    }

    /** Vincular/desvincular e parte do cadastro do militar (RF04) - Cabo ou Sargenteante. */
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
