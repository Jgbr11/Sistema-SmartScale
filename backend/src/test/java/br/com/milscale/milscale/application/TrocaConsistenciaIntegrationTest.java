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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TrocaConsistenciaIntegrationTest {

    @Autowired private SolicitacaoService solicitacaoService;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Militar a, b, c;
    private String loginA, loginB;
    private ServicoEscalado servicoDeA;

    @BeforeEach
    void montar() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        TipoServico caboDaGuarda = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Cabo da Guarda")).findFirst().orElseThrow();
        List<Militar> cabos = militarRepository.findAll().stream()
                .filter(m -> "Cb".equals(m.getPosto().getSigla()) && !"Aprov".equals(m.getSubunidade().getSigla()))
                .limit(3).toList();
        a = cabos.get(0); b = cabos.get(1); c = cabos.get(2);
        loginA = usuarioRepository.findByMilitar_Id(a.getId()).orElseThrow().getLogin();
        loginB = usuarioRepository.findByMilitar_Id(b.getId()).orElseThrow().getLogin();
        LocalDate dia = LocalDate.now().plusMonths(2).withDayOfMonth(10);
        Escala escala = escalaRepository.save(Escala.builder().descricao("t").dataInicio(dia.withDayOfMonth(1))
                .dataFim(dia.withDayOfMonth(28)).situacao(SituacaoEscala.PUBLICADA).usuarioGeracao(sargenteante).build());
        servicoDeA = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia).tipoServico(caboDaGuarda).militar(a).build());
    }

    @Test
    void segundoPedidoParaOMesmoServico_eRecusado() {
        solicitacaoService.criar(servicoDeA.getId(), b.getId(), "primeiro", loginA);
        assertThatThrownBy(() -> solicitacaoService.criar(servicoDeA.getId(), c.getId(), "segundo", loginA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("em andamento");
    }

    @Test
    void autorizar_servicoQueMudouDeDono_eRecusado() {
        Solicitacao s = solicitacaoService.criar(servicoDeA.getId(), b.getId(), "t", loginA);
        solicitacaoService.confirmarSubstituto(s.getId(), true, null, loginB);
        solicitacaoService.triagem(s.getId(), true, "ok");
        servicoDeA.setMilitar(c);
        servicoEscaladoRepository.save(servicoDeA);

        assertThatThrownBy(() -> solicitacaoService.autorizar(s.getId(), true, "ok"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mudou de dono");
    }

    @Test
    void autorizar_servicoQueJaComecou_eRecusado() {
        Solicitacao s = solicitacaoService.criar(servicoDeA.getId(), b.getId(), "t", loginA);
        solicitacaoService.confirmarSubstituto(s.getId(), true, null, loginB);
        solicitacaoService.triagem(s.getId(), true, "ok");
        servicoDeA.setData(LocalDate.now().minusDays(1));
        servicoEscaladoRepository.save(servicoDeA);

        assertThatThrownBy(() -> solicitacaoService.autorizar(s.getId(), true, "ok"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já começou");
    }
}
