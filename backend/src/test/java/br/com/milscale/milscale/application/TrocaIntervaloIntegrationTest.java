package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regressão real desta sessão: minha primeira versão do filtro de troca
 * só olhava o dia imediatamente antes/depois, e um teste manual revelou
 * que isso deixava passar um 1x1 (só 1 dia de folga) - proibido mesmo
 * numa troca combinada espontaneamente. A regra certa, confirmada com o
 * usuário: 1x1 sempre proibido, 2x1 permitido só em troca (a regra
 * normal do motor exige 3x1). Construo o cenário direto (não dependo de
 * o gerador coincidentemente produzir esses gaps) pra o teste ser
 * determinístico.
 *
 * Também cobre a TROCA MÚTUA (os dois assumem o dia um do outro) - regra
 * bidirecional: precisa checar 1x1 dos DOIS lados, não só de quem assume.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TrocaIntervaloIntegrationTest {

    @Autowired private SolicitacaoService solicitacaoService;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Militar militarA, militarB, militarC;
    private String loginA;
    private ServicoEscalado servicoOrigem; // do militarA, no "dia D"
    private TipoServico caboDaGuarda;
    private Escala escalaTeste;
    private LocalDate dia;

    @BeforeEach
    void montarCenario() {
        Usuario usuarioGeracao = usuarioRepository.findByLogin("00000000001").orElseThrow(); // Zeni, Sargenteante
        caboDaGuarda = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Cabo da Guarda")).findFirst().orElseThrow();

        List<Militar> cabos = militarRepository.findAll().stream()
                .filter(m -> "Cb".equals(m.getPosto().getSigla()))
                .filter(m -> !"Aprov".equals(m.getSubunidade().getSigla()))
                .limit(3)
                .toList();
        militarA = cabos.get(0);
        militarB = cabos.get(1);
        militarC = cabos.get(2);
        loginA = usuarioRepository.findByMilitar_Id(militarA.getId()).orElseThrow().getLogin();

        escalaTeste = escalaRepository.save(Escala.builder()
                .descricao("Escala de teste").dataInicio(LocalDate.now().plusMonths(2).withDayOfMonth(1))
                .dataFim(LocalDate.now().plusMonths(2).withDayOfMonth(28))
                .usuarioGeracao(usuarioGeracao).build());

        dia = LocalDate.now().plusMonths(2).withDayOfMonth(5);

        // servico que o militarA vai oferecer pra troca, no "dia D"
        servicoOrigem = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(dia).tipoServico(caboDaGuarda).militar(militarA).build());

        // militarB ja tem servico em D+2 -> se assumir o dia D, fica em 1x1 (so 1 dia de folga)
        servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(dia.plusDays(2)).tipoServico(caboDaGuarda).militar(militarB).build());

        // militarC ja tem servico em D+3 -> se assumir o dia D, fica em 2x1 (2 dias de folga) - permitido em troca
        servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(dia.plusDays(3)).tipoServico(caboDaGuarda).militar(militarC).build());
    }

    @Test
    void criar_bloqueiaSubstitutoQueFicariaEm1x1() {
        assertThatThrownBy(() ->
                solicitacaoService.criar(servicoOrigem.getId(), militarB.getId(), "teste 1x1", loginA)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("1x1");
    }

    @Test
    void criar_permiteSubstitutoQueFicaEm2x1() {
        Solicitacao s = solicitacaoService.criar(servicoOrigem.getId(), militarC.getId(), "teste 2x1", loginA);

        assertThat(s).isNotNull();
        assertThat(s.getSubstituto().getId()).isEqualTo(militarC.getId());
        assertThat(s.getSituacao()).isEqualTo("AGUARDANDO_SUBSTITUTO");
    }

    @Test
    void listarElegiveis_excluiCandidato1x1MasIncluiCandidato2x1() {
        List<Militar> elegiveis = solicitacaoService.listarElegiveisParaTroca(servicoOrigem.getId(), militarA.getId());

        assertThat(elegiveis).extracting(Militar::getId)
                .as("candidato que ficaria em 1x1 não deveria ser sugerido")
                .doesNotContain(militarB.getId());
        assertThat(elegiveis).extracting(Militar::getId)
                .as("candidato que ficaria em 2x1 deveria ser sugerido")
                .contains(militarC.getId());
    }

    // ===================== TROCA MÚTUA =====================
    // Cenário à parte: além do servicoOrigem (militarA, dia 5), monto mais
    // militares e mais serviços pra testar as DUAS pontas da regra de 1x1 -
    // o solicitante assumindo o dia do outro, E o outro assumindo o dia do
    // solicitante - cada lado contra os PRÓPRIOS outros serviços.

    private Militar buscarOutroCabo(int indice) {
        return militarRepository.findAll().stream()
                .filter(m -> "Cb".equals(m.getPosto().getSigla()))
                .filter(m -> !"Aprov".equals(m.getSubunidade().getSigla()))
                .filter(m -> !List.of(militarA.getId(), militarB.getId(), militarC.getId()).contains(m.getId()))
                .toList().get(indice);
    }

    private ServicoEscalado criarServico(Militar m, LocalDate data) {
        return servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(data).tipoServico(caboDaGuarda).militar(m).build());
    }

    @Test
    void criarTrocaMutua_permiteQuandoOsDoisLadosFicamEm2x1OuMais() {
        Militar militarD = buscarOutroCabo(0);
        ServicoEscalado servicoD = criarServico(militarD, dia.plusDays(7)); // folga confortável dos dois lados

        Solicitacao s = solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoD.getId(), "trocar de dia", loginA);

        assertThat(s.getTipoTroca()).isEqualTo("TROCA_MUTUA");
        assertThat(s.getSubstituto().getId()).isEqualTo(militarD.getId());
        assertThat(s.getServicoDestino().getId()).isEqualTo(servicoD.getId());
        assertThat(s.getSituacao()).isEqualTo("AGUARDANDO_SUBSTITUTO");
    }

    @Test
    void criarTrocaMutua_bloqueiaQuandoOSOLICITANTEFicariaEm1x1() {
        // militarA ganha um SEGUNDO servico fixo no dia 20 - se ele assumir
        // o dia 22 de militarE, fica a só 2 dias de calendario (1x1) do
        // proprio outro servico dele.
        criarServico(militarA, dia.plusDays(15)); // dia 20
        Militar militarE = buscarOutroCabo(1);
        ServicoEscalado servicoE = criarServico(militarE, dia.plusDays(17)); // dia 22

        assertThatThrownBy(() ->
                solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoE.getId(), "teste", loginA)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Você ficaria");
    }

    @Test
    void criarTrocaMutua_bloqueiaQuandoOOUTROMilitarFicariaEm1x1() {
        // militarF tem o servico candidato a troca (dia 15) E um segundo
        // servico fixo bem perto do dia 5 que militarA esta oferecendo -
        // se F assumir o dia 5, fica em 1x1 contra o PROPRIO outro servico dele.
        Militar militarF = buscarOutroCabo(2);
        ServicoEscalado servicoF = criarServico(militarF, dia.plusDays(10)); // dia 15 - o que seria trocado
        criarServico(militarF, dia.plusDays(2)); // dia 7 - fixo, perto do dia 5

        assertThatThrownBy(() ->
                solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoF.getId(), "teste", loginA)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("outro militar ficaria");
    }

    @Test
    void criarTrocaMutua_bloqueiaTipoDeServicoDiferente() {
        TipoServico oficialDeDia = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Oficial de Dia")).findFirst().orElseThrow();
        Militar tenente = militarRepository.findAll().stream()
                .filter(m -> "Ten".equals(m.getPosto().getSigla())).findFirst().orElseThrow();
        ServicoEscalado servicoOutroTipo = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(dia.plusDays(7)).tipoServico(oficialDeDia).militar(tenente).build());

        assertThatThrownBy(() ->
                solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoOutroTipo.getId(), "teste", loginA)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("mesmo tipo");
    }

    @Test
    void autorizar_trocaMutuaEfetivaOsDoisLados() {
        Militar militarD = buscarOutroCabo(0);
        ServicoEscalado servicoD = criarServico(militarD, dia.plusDays(7));
        String loginD = usuarioRepository.findByMilitar_Id(militarD.getId()).orElseThrow().getLogin();

        Solicitacao s = solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoD.getId(), "trocar", loginA);
        solicitacaoService.confirmarSubstituto(s.getId(), true, null, loginD);
        solicitacaoService.triagem(s.getId(), true, "ok");
        solicitacaoService.autorizar(s.getId(), true, "ok");

        ServicoEscalado origemAtualizado = servicoEscaladoRepository.findById(servicoOrigem.getId()).orElseThrow();
        ServicoEscalado destinoAtualizado = servicoEscaladoRepository.findById(servicoD.getId()).orElseThrow();

        assertThat(origemAtualizado.getMilitar().getId()).as("o dia de A agora é do D").isEqualTo(militarD.getId());
        assertThat(destinoAtualizado.getMilitar().getId()).as("o dia de D agora é do A").isEqualTo(militarA.getId());
    }

    @Test
    void listarElegiveisParaTrocaMutua_excluiQuemViolariaQualquerDosDoisLados() {
        criarServico(militarA, dia.plusDays(15)); // segundo servico fixo de A, dia 20
        Militar militarD = buscarOutroCabo(0); // folga confortavel - deve aparecer
        Militar militarE = buscarOutroCabo(1); // perto do 2o servico de A - nao deve aparecer
        ServicoEscalado servicoD = criarServico(militarD, dia.plusDays(7));
        criarServico(militarE, dia.plusDays(17)); // dia 22, perto do dia 20 fixo de A

        List<SolicitacaoService.CandidatoTrocaMutua> candidatos =
                solicitacaoService.listarElegiveisParaTrocaMutua(servicoOrigem.getId(), militarA.getId());

        assertThat(candidatos).extracting(c -> c.militar().getId())
                .as("candidato com folga confortável dos dois lados deveria aparecer")
                .contains(militarD.getId());
        assertThat(candidatos).extracting(c -> c.militar().getId())
                .as("candidato que faria o solicitante ficar em 1x1 não deveria aparecer")
                .doesNotContain(militarE.getId());
    }
}
