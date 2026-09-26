package br.com.milscale.milscale.application;

import br.com.smartscale.core.MotorDeRodizio;
import br.com.smartscale.core.PessoaEscalada;
import br.com.smartscale.core.SituacaoPessoa;
import br.com.smartscale.core.TipoTurno;
import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Caso de uso RF08: "Gerar automaticamente a escala de um periodo a
 * partir das regras vigentes e do contador de rodizio de cada pessoa."
 *
 * Esta classe e a "cola" MilScale: ela consulta o banco (postos,
 * requisitos, afastamentos, regras), monta os pools elegiveis dia a dia
 * e delega a escolha de quem preenche cada vaga para o
 * {@link MotorDeRodizio} do nucleo reutilizavel. As regras verificadas
 * aqui fora do motor sao as que dependem de tabelas exclusivas do
 * MilScale (requisito_servico, afastamento, regra_escala) - por isso nao
 * podiam morar no nucleo.
 */
@Service
public class GerarEscalaService {

    private final TipoServicoRepository tipoServicoRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final RegraEscalaRepository regraEscalaRepository;
    private final MilitarRepository militarRepository;
    private final AfastamentoRepository afastamentoRepository;
    private final EscalaRepository escalaRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ElegibilidadeService elegibilidadeService;
    private final MotorDeRodizio<MilitarEmGeracao, TipoTurno> motor = new MotorDeRodizio<>();
    private final CriterioOrdenacaoMilitar<MilitarEmGeracao> criterio = new CriterioOrdenacaoMilitar<>();

    public GerarEscalaService(TipoServicoRepository tipoServicoRepository,
                               RequisitoServicoRepository requisitoServicoRepository,
                               RegraEscalaRepository regraEscalaRepository,
                               MilitarRepository militarRepository,
                               AfastamentoRepository afastamentoRepository,
                               EscalaRepository escalaRepository,
                               ServicoEscaladoRepository servicoEscaladoRepository,
                               SolicitacaoRepository solicitacaoRepository,
                               ElegibilidadeService elegibilidadeService) {
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.regraEscalaRepository = regraEscalaRepository;
        this.militarRepository = militarRepository;
        this.afastamentoRepository = afastamentoRepository;
        this.escalaRepository = escalaRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.elegibilidadeService = elegibilidadeService;
    }

    @Transactional
    public Escala gerar(LocalDate dataInicio, LocalDate dataFim, Usuario usuarioGeracao) {
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim) > 366) {
            throw new IllegalArgumentException("O período não pode passar de um ano");
        }

        // A escala e uma UNICA linha do tempo continua, nao um cofrinho por mes:
        // gerar de novo um periodo que ja tem servico marcado SUBSTITUI o que
        // havia ali, em vez de empilhar registros duplicados por cima (o que
        // inflava a contagem em "Minha escala" e impedia gerar um periodo menor
        // depois de ja ter gerado o mes inteiro).
        List<ServicoEscalado> existentesNoPeriodo = servicoEscaladoRepository.findByDataBetween(dataInicio, dataFim);
        if (!existentesNoPeriodo.isEmpty()) {
            boolean temDiaTravado = existentesNoPeriodo.stream().anyMatch(ServicoEscalado::isTravado);
            if (temDiaTravado) {
                throw new IllegalArgumentException(
                        "Há dias travados nesse período — destrave antes de gerar de novo, ou escolha outro período (RN04)");
            }
            // RF12 (variante automática) - um dia cujo horário de início já
            // passou vira "sólido" sozinho, sem precisar de travamento manual.
            boolean temDiaJaComecado = existentesNoPeriodo.stream().anyMatch(ServicoEscalado::isJaComecou);
            if (temDiaJaComecado) {
                throw new IllegalArgumentException(
                        "Esse período inclui um dia cujo serviço já começou — não é possível gerar de novo pro passado. Ajuste a data de início.");
            }
            List<Solicitacao> solicitacoesNoPeriodo = solicitacaoRepository.findByServicoOrigem_DataBetween(dataInicio, dataFim);
            if (!solicitacoesNoPeriodo.isEmpty()) {
                throw new IllegalArgumentException(
                        "Há pedidos de troca vinculados a esse período — resolva-os antes de gerar de novo");
            }
            Set<Long> escalasAfetadas = new HashSet<>();
            for (ServicoEscalado s : existentesNoPeriodo) escalasAfetadas.add(s.getEscala().getId());
            servicoEscaladoRepository.deleteAll(existentesNoPeriodo);
            // limpa escalas que ficaram completamente vazias depois da substituicao
            for (Long escalaId : escalasAfetadas) {
                if (servicoEscaladoRepository.findByEscala_Id(escalaId).isEmpty()) {
                    escalaRepository.deleteById(escalaId);
                }
            }
        }

        List<TipoServico> tipos = tipoServicoRepository.findByAtivoTrue();
        List<Militar> ativos = militarRepository.findBySituacao(SituacaoPessoa.ATIVO);

        // Tudo que nao muda durante a geracao e carregado UMA vez, fora do laco dia x tipo.
        // RF06 - elegibilidade real: posto E (se exigido) a qualificação específica.
        // Uma mesma função pode ter mais de uma combinação posto+curso aceita
        // (ex.: Cozinheiro de Dia aceita Sd EP-Rancho OU Cb-Rancho) - por isso
        // guardamos um conjunto de combinações, não um único posto.
        Map<Long, List<Militar>> elegiveisPorTipo = new HashMap<>();
        Map<Long, Integer> intervaloPorTipo = new HashMap<>();
        for (TipoServico tipo : tipos) {
            List<RequisitoServico> requisitos = requisitoServicoRepository.findByTipoServico_Id(tipo.getId());
            elegiveisPorTipo.put(tipo.getId(), ativos.stream().filter(m -> elegibilidadeService.elegivel(m, requisitos)).toList());
            intervaloPorTipo.put(tipo.getId(), PoliticaDeDescanso.intervaloMinimo(regraEscalaRepository.findByTipoServico_Id(tipo.getId()).orElse(null)));
        }
        Map<Long, List<Afastamento>> afastamentosPorMilitar = afastamentoRepository.findByDataFimGreaterThanEqual(dataInicio).stream()
                .collect(Collectors.groupingBy(a -> a.getMilitar().getId()));

        // Estado mutavel do rodizio durante a geracao (RN01/RN06): comeca com o
        // ultimo servico conhecido no banco, e vai avancando conforme escalamos.
        Map<Long, MilitarEmGeracao> estado = new HashMap<>();
        for (Militar m : ativos) {
            estado.put(m.getId(), new MilitarEmGeracao(m, m.getDataUltimoServico()));
        }

        Escala escala = Escala.builder()
                .descricao("Escala de " + nomeMesPtBr(dataInicio) + " de " + dataInicio.getYear())
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .situacao(SituacaoEscala.RASCUNHO)
                .usuarioGeracao(usuarioGeracao)
                .build();

        List<ServicoEscalado> gerados = new ArrayList<>();

        for (LocalDate dia = dataInicio; !dia.isAfter(dataFim); dia = dia.plusDays(1)) {
            // RN05: quem ja foi escalado num tipo de servico hoje sai do pool dos demais tipos hoje.
            Set<Long> escaladosHoje = new HashSet<>();

            for (TipoServico tipo : tipos) {
                int intervaloMinimo = intervaloPorTipo.get(tipo.getId());

                final LocalDate diaFinal = dia;
                List<MilitarEmGeracao> pool = new ArrayList<>();
                List<MilitarEmGeracao> disponiveis = new ArrayList<>(); // RN05 + RF06 + RN15, sem RN06 (base do "aperto")
                for (Militar m : elegiveisPorTipo.get(tipo.getId())) {
                    if (escaladosHoje.contains(m.getId())) continue; // RN05
                    if (temImpedimento(m, diaFinal, afastamentosPorMilitar)) continue; // RN15
                    MilitarEmGeracao em = estado.get(m.getId());
                    disponiveis.add(em);
                    if (PoliticaDeDescanso.respeitaIntervalo(em.getUltimoServico(), diaFinal, intervaloMinimo)) pool.add(em); // RN06
                }

                List<MilitarEmGeracao> escolhidos = new ArrayList<>(motor.preencherVagas(pool, tipo, criterio));

                // "A escala aperta sozinha, nunca fica vaga aberta" - se o pool que
                // respeita o intervalo minimo (RN06) nao tiver gente suficiente pra
                // cobrir o efetivo necessario (ex.: muita gente de ferias/missao ao
                // mesmo tempo, deixando o efetivo elegivel pequeno demais pro 3x1
                // rigoroso), o sistema relaxa o intervalo minimo como ULTIMO recurso
                // - exatamente o que aconteceria de verdade no quartel. Mesmo assim
                // prioriza sempre quem esta ha MAIS tempo sem servir (o criterio de
                // ordenacao normal), entao quem "aperta" e sempre quem folgou menos
                // tempo dentre os poucos disponiveis, nunca escolha arbitraria.
                int faltantesAntesDoAperto = tipo.getEfetivoNecessario() - escolhidos.size();
                if (faltantesAntesDoAperto > 0) {
                    Set<Long> jaEscolhidosNesteTipo = escolhidos.stream().map(e -> e.militar.getId()).collect(Collectors.toSet());
                    // RN05, RF06 e RN15 continuam valendo - so o intervalo minimo e relaxado.
                    List<MilitarEmGeracao> poolRelaxado = disponiveis.stream()
                            .filter(em -> !jaEscolhidosNesteTipo.contains(em.militar.getId()))
                            .toList();
                    TipoTurnoComEfetivo turnoRestante = new TipoTurnoComEfetivo(tipo, faltantesAntesDoAperto);
                    List<MilitarEmGeracao> extras = motor.preencherVagas(poolRelaxado, turnoRestante, criterio);
                    escolhidos.addAll(extras);
                }

                for (MilitarEmGeracao escolhido : escolhidos) {
                    escaladosHoje.add(escolhido.militar.getId());
                    escolhido.marcarServico(dia);
                    gerados.add(ServicoEscalado.builder()
                            .escala(escala).data(dia).tipoServico(tipo)
                            .militar(escolhido.militar).situacao(SituacaoServico.PREVISTO).build());
                }
                // Vagas que sobraram sem gente elegível/disponível também viram
                // registro (id_militar NULL) - uma linha por vaga em aberto, não
                // uma só por tipo/dia, senão duas vagas faltando em Monitoramento
                // apareceriam como se faltasse só uma. So acontece agora quando
                // NINGUEM elegivel sobrou de jeito nenhum (nem afrouxando RN06) -
                // ex.: efetivo zerado pra aquele posto/curso especifico.
                int faltantes = tipo.getEfetivoNecessario() - escolhidos.size();
                for (int i = 0; i < faltantes; i++) {
                    gerados.add(ServicoEscalado.builder()
                            .escala(escala).data(dia).tipoServico(tipo).militar(null)
                            .situacao(SituacaoServico.PREVISTO).build());
                }
            }
        }

        escala.setServicos(gerados);
        Escala salva = escalaRepository.save(escala);

        // Persiste o novo "ultimo servico" de quem foi escalado, para as proximas geracoes.
        for (MilitarEmGeracao em : estado.values()) {
            if (em.foiAtualizado()) {
                em.militar.setDataUltimoServico(em.getUltimoServico());
                militarRepository.save(em.militar);
            }
        }

        return salva;
    }

    private boolean temImpedimento(Militar m, LocalDate dia, Map<Long, List<Afastamento>> afastamentosPorMilitar) {
        return afastamentosPorMilitar.getOrDefault(m.getId(), List.of()).stream().anyMatch(a -> a.cobre(dia));
    }

    /** Wrapper leve que reaproveita o mesmo TipoServico, só com o
     *  efetivo necessário trocado pelo número de vagas que faltam -
     *  usado só na segunda chamada ao motor (o "aperto" da escala). */
    private record TipoTurnoComEfetivo(TipoServico original, int efetivoRestante) implements TipoTurno {
        @Override public Long getId() { return original.getId(); }
        @Override public String getNome() { return original.getNome(); }
        @Override public int getEfetivoNecessario() { return efetivoRestante; }
        @Override public boolean isAtivo() { return original.isAtivo(); }
    }

    /**
     * Wrapper que implementa {@link PessoaEscalada} do nucleo, mas com um
     * "ultimo servico" mutavel durante a geracao - assim o motor sempre
     * enxerga o contador de rodizio atualizado a cada vaga preenchida,
     * sem precisar tocar o Militar gerenciado pelo JPA a cada iteracao.
     */
    private static class MilitarEmGeracao implements PessoaEscalada {
        final Militar militar;
        private LocalDate ultimoServico;
        private boolean atualizado = false;

        MilitarEmGeracao(Militar militar, LocalDate ultimoServico) {
            this.militar = militar;
            this.ultimoServico = ultimoServico;
        }

        LocalDate getUltimoServico() { return ultimoServico; }
        boolean foiAtualizado() { return atualizado; }

        void marcarServico(LocalDate data) {
            this.ultimoServico = data;
            this.atualizado = true;
        }

        @Override public Long getId() { return militar.getId(); }
        @Override public String getNomeExibicao() { return militar.getNomeExibicao(); }
        @Override public SituacaoPessoa getSituacao() { return militar.getSituacao(); }
        @Override public long getContadorRodizio() {
            return ultimoServico == null ? Integer.MAX_VALUE : ChronoUnit.DAYS.between(ultimoServico, LocalDate.now());
        }
    }

    private static String nomeMesPtBr(LocalDate data) {
        return data.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "BR"));
    }
}
