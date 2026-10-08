package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class RequisitoServicoService {

    private final TipoServicoRepository tipoServicoRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final PostoGraduacaoRepository postoRepository;
    private final SubunidadeRepository subunidadeRepository;
    private final QualificacaoRepository qualificacaoRepository;

    public RequisitoServicoService(TipoServicoRepository tipoServicoRepository, RequisitoServicoRepository requisitoServicoRepository,
                                   PostoGraduacaoRepository postoRepository, SubunidadeRepository subunidadeRepository,
                                   QualificacaoRepository qualificacaoRepository) {
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.postoRepository = postoRepository;
        this.subunidadeRepository = subunidadeRepository;
        this.qualificacaoRepository = qualificacaoRepository;
    }

    public List<RequisitoServico> listar(Long tipoId) {
        buscarTipo(tipoId);
        return requisitoServicoRepository.findByTipoServico_Id(tipoId);
    }

    @Transactional
    public RequisitoServico adicionar(Long tipoId, NovoRequisito novo) {
        TipoServico tipo = buscarTipo(tipoId);
        PostoGraduacao posto = postoRepository.findById(novo.postoId())
                .orElseThrow(() -> new NoSuchElementException("Posto/graduação não encontrado"));
        Subunidade exigida = subunidadeOuNulo(novo.subunidadeId());
        Subunidade excluida = subunidadeOuNulo(novo.subunidadeExcluidaId());
        if (exigida != null && excluida != null) {
            throw new IllegalArgumentException("Escolha exigir OU excluir uma subunidade, não os dois");
        }
        Qualificacao curso = novo.qualificacaoId() == null ? null : qualificacaoRepository.findById(novo.qualificacaoId())
                .orElseThrow(() -> new NoSuchElementException("Curso não encontrado"));
        Set<Long> idsExcluidos = novo.qualificacoesExcluidasIds() == null ? Set.of() : novo.qualificacoesExcluidasIds();
        Set<Qualificacao> cursosExcluidos = new HashSet<>(qualificacaoRepository.findAllById(idsExcluidos));
        if (curso != null && cursosExcluidos.contains(curso)) {
            throw new IllegalArgumentException("O curso exigido não pode estar também entre os excluídos");
        }
        return requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipo).posto(posto).subunidade(exigida).qualificacao(curso)
                .qualificacoesExcluidas(cursosExcluidos).subunidadeExcluida(excluida)
                .build());
    }

    @Transactional
    public void remover(Long tipoId, Long requisitoId) {
        RequisitoServico r = requisitoServicoRepository.findById(requisitoId)
                .filter(x -> x.getTipoServico().getId().equals(tipoId))
                .orElseThrow(() -> new NoSuchElementException("Requisito não encontrado neste tipo de serviço"));
        r.getTipoServico().getRequisitos().remove(r);
        requisitoServicoRepository.delete(r);
    }

    private TipoServico buscarTipo(Long tipoId) {
        return tipoServicoRepository.findById(tipoId)
                .orElseThrow(() -> new NoSuchElementException("Tipo de servico nao encontrado"));
    }

    private Subunidade subunidadeOuNulo(Long id) {
        return id == null ? null : subunidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subunidade não encontrada"));
    }
}
