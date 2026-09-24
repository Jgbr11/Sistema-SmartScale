package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * RF26 - missoes, dispensas, ferias e licencas. Um afastamento impede a
 * escalacao do militar no periodo informado (RN15), ja aplicado em
 * GerarEscalaService.temImpedimento para GERACOES FUTURAS.
 *
 * Mas se a pessoa JA estava escalada num dia dentro do novo periodo de
 * afastamento (a escala foi gerada antes do afastamento existir), essa
 * vaga fica orfa se ninguem cuidar dela. Por isso, ao cadastrar um
 * afastamento, esta classe tambem RECONCILIA os servicos ja marcados
 * dentro do periodo: tenta achar outra pessoa elegivel e disponivel pra
 * cobrir a vaga (mesmo criterio de justica do motor - RN01), e corrige
 * o contador de rodizio de quem foi afastado (ele nao "gastou" o
 * proprio lugar na fila por um servico que nao vai cumprir).
 */
@Service
public class AfastamentoService {

    private final AfastamentoRepository afastamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final MilitarRepository militarRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final RegraEscalaRepository regraEscalaRepository;
    private final ElegibilidadeService elegibilidadeService;

    public AfastamentoService(AfastamentoRepository afastamentoRepository, UsuarioRepository usuarioRepository,
                               ServicoEscaladoRepository servicoEscaladoRepository, MilitarRepository militarRepository,
                               RequisitoServicoRepository requisitoServicoRepository, RegraEscalaRepository regraEscalaRepository,
                               ElegibilidadeService elegibilidadeService) {
        this.afastamentoRepository = afastamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.militarRepository = militarRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.regraEscalaRepository = regraEscalaRepository;
        this.elegibilidadeService = elegibilidadeService;
    }

    /** Lista os afastamentos vigentes ou futuros (nao mostra o historico antigo por padrao). */
    public List<Afastamento> listarVigentesEFuturos() {
        return afastamentoRepository.findByDataFimGreaterThanEqual(LocalDate.now().minusYears(1));
    }

    @Transactional
    public List<Afastamento> cadastrarMissao(List<Long> militarIds, String tipo, String descricao,
                                              LocalDate dataInicio, LocalDate dataFim, String loginUsuarioRegistro) {
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }
        if (militarIds == null || militarIds.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um militar");
        }
        var usuario = usuarioRepository.findByLogin(loginUsuarioRegistro).orElseThrow();
        // Um lote agrupa os N afastamentos (um por militar) que vieram do
        // mesmo cadastro - assim a tela de Avisos consegue mostrar "Missão X:
        // fulano, beltrano, sicrano" como um evento só, não N linhas soltas.
        String loteMissao = java.util.UUID.randomUUID().toString();

        List<Afastamento> criados = new java.util.ArrayList<>();
        for (Long militarId : militarIds) {
            Militar militarCompleto = militarRepository.findById(militarId)
                    .orElseThrow(() -> new NoSuchElementException("Militar não encontrado (id " + militarId + ")"));
            Afastamento novo = Afastamento.builder()
                    .militar(militarCompleto)
                    .tipo(tipo)
                    .descricao(descricao)
                    .dataInicio(dataInicio)
                    .dataFim(dataFim)
                    .usuarioRegistro(usuario)
                    .loteMissao(loteMissao)
                    .build();
            Afastamento salvo = afastamentoRepository.save(novo);
            reconciliarServicosJaMarcados(salvo);
            criados.add(salvo);
        }
        return criados;
    }

    @Transactional
    public void cancelar(Long id) {
        if (!afastamentoRepository.existsById(id)) throw new NoSuchElementException("Afastamento nao encontrado");
        afastamentoRepository.deleteById(id);
        // Nao desfazemos automaticamente as trocas ja feitas ao cancelar um
        // afastamento - reatribuir de volta poderia colidir com outra coisa
        // que a pessoa passou a fazer nesse meio tempo. Fica como ajuste
        // manual (RN11) se for o caso.
    }

    private void reconciliarServicosJaMarcados(Afastamento afastamento) {
        Militar afastado = afastamento.getMilitar();
        List<ServicoEscalado> conflitantes = servicoEscaladoRepository
                .findByMilitar_IdAndDataBetween(afastado.getId(), afastamento.getDataInicio(), afastamento.getDataFim())
                .stream()
                .filter(s -> !s.isTravado()) // RN04 - dia travado nem o afastamento mexe
                .filter(s -> !s.isJaComecou()) // dia que ja comecou tambem nao muda mais
                .toList();

        if (conflitantes.isEmpty()) return;

        List<Militar> ativos = militarRepository.findAll().stream()
                .filter(m -> m.getSituacao() == br.com.milscale.core.domain.SituacaoPessoa.ATIVO)
                .filter(m -> !m.getId().equals(afastado.getId()))
                .toList();

        for (ServicoEscalado servico : conflitantes) {
            Militar substituto = escolherSubstituto(servico, ativos).orElse(null);
            servico.setMilitar(substituto);
            servico.setObservacao(substituto != null
                    ? "Realocado automaticamente — " + afastado.getNomeExibicao() + " entrou de afastamento"
                    : "Vaga em aberto — " + afastado.getNomeExibicao() + " entrou de afastamento e não havia substituto elegível");
            servicoEscaladoRepository.save(servico);
            if (substituto != null && (substituto.getDataUltimoServico() == null || servico.getData().isAfter(substituto.getDataUltimoServico()))) {
                substituto.setDataUltimoServico(servico.getData());
                militarRepository.save(substituto);
            }
        }

        // O afastado nao "gasta" mais a posicao na fila pelos servicos que
        // foram tirados dele - recalcula o ultimo servico dele considerando
        // só o que sobrou de fato atribuido a ele.
        recalcularUltimoServico(afastado);
    }

    private Optional<Militar> escolherSubstituto(ServicoEscalado servico, List<Militar> candidatos) {
        TipoServico tipo = servico.getTipoServico();
        LocalDate dia = servico.getData();
        List<RequisitoServico> requisitos = requisitoServicoRepository.findByTipoServico_Id(tipo.getId());
        RegraEscala regra = regraEscalaRepository.findByTipoServico_Id(tipo.getId()).orElse(null);
        int intervaloMinimo = regra != null ? regra.getIntervaloMinimo() : 7;

        return candidatos.stream()
                .filter(m -> elegibilidadeService.elegivel(m, requisitos))
                .filter(m -> !servicoEscaladoRepository.existsByMilitar_IdAndData(m.getId(), dia)) // RN05
                .filter(m -> !temImpedimentoNoDia(m, dia))
                .filter(m -> respeitaIntervalo(m, dia, intervaloMinimo))
                .max(Comparator.comparingLong(m -> diasSemServico(m, dia)));
    }


    private boolean temImpedimentoNoDia(Militar m, LocalDate dia) {
        return afastamentoRepository.findByDataFimGreaterThanEqual(dia).stream()
                .anyMatch(a -> a.getMilitar().getId().equals(m.getId()) && a.cobre(dia));
    }

    private boolean respeitaIntervalo(Militar m, LocalDate dia, int intervaloMinimo) {
        LocalDate janelaInicio = dia.minusDays(intervaloMinimo);
        LocalDate janelaFim = dia.plusDays(intervaloMinimo);
        return servicoEscaladoRepository.findByMilitar_IdAndDataBetween(m.getId(), janelaInicio, janelaFim).isEmpty();
    }

    private long diasSemServico(Militar m, LocalDate referencia) {
        if (m.getDataUltimoServico() == null) return Long.MAX_VALUE;
        return Math.abs(ChronoUnit.DAYS.between(m.getDataUltimoServico(), referencia));
    }

    private void recalcularUltimoServico(Militar militar) {
        LocalDate maisRecente = servicoEscaladoRepository
                .findByMilitar_IdAndDataBetweenOrderByDataDesc(militar.getId(), LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1))
                .stream()
                .map(ServicoEscalado::getData)
                .max(LocalDate::compareTo)
                .orElse(null);
        militar.setDataUltimoServico(maisRecente);
        militarRepository.save(militar);
    }
}
