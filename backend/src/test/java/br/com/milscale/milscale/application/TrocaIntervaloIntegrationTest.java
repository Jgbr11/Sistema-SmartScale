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
    private ServicoEscalado servicoOrigem;
    private TipoServico caboDaGuarda;
    private Escala escalaTeste;
    private LocalDate dia;

    @BeforeEach
    void montarCenario() {
        Usuario usuarioGeracao = usuarioRepository.findByLogin("00000000001").orElseThrow();
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
                .situacao(SituacaoEscala.PUBLICADA).usuarioGeracao(usuarioGeracao).build());

        dia = LocalDate.now().plusMonths(2).withDayOfMonth(5);

        servicoOrigem = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(dia).tipoServico(caboDaGuarda).militar(militarA).build());

        servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escalaTeste).data(dia.plusDays(2)).tipoServico(caboDaGuarda).militar(militarB).build());

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
        assertThat(s.getSituacao()).isEqualTo(SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO);
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
        ServicoEscalado servicoD = criarServico(militarD, dia.plusDays(7));

        Solicitacao s = solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoD.getId(), "trocar de dia", loginA);

        assertThat(s.getTipoTroca()).isEqualTo(TipoTroca.TROCA_MUTUA);
        assertThat(s.getSubstituto().getId()).isEqualTo(militarD.getId());
        assertThat(s.getServicoDestino().getId()).isEqualTo(servicoD.getId());
        assertThat(s.getSituacao()).isEqualTo(SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO);
    }

    @Test
    void criarTrocaMutua_bloqueiaQuandoOSOLICITANTEFicariaEm1x1() {

        criarServico(militarA, dia.plusDays(15));
        Militar militarE = buscarOutroCabo(1);
        ServicoEscalado servicoE = criarServico(militarE, dia.plusDays(17));

        assertThatThrownBy(() ->
                solicitacaoService.criarTrocaMutua(servicoOrigem.getId(), servicoE.getId(), "teste", loginA)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Você ficaria");
    }

    @Test
    void criarTrocaMutua_bloqueiaQuandoOOUTROMilitarFicariaEm1x1() {

        Militar militarF = buscarOutroCabo(2);
        ServicoEscalado servicoF = criarServico(militarF, dia.plusDays(10));
        criarServico(militarF, dia.plusDays(2));

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
        criarServico(militarA, dia.plusDays(15));
        Militar militarD = buscarOutroCabo(0);
        Militar militarE = buscarOutroCabo(1);
        ServicoEscalado servicoD = criarServico(militarD, dia.plusDays(7));
        criarServico(militarE, dia.plusDays(17));

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
