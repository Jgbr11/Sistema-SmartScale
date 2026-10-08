package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegeracaoComHistoricoIntegrationTest {

    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private SolicitacaoService solicitacaoService;
    @Autowired private SolicitacaoRepository solicitacaoRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Solicitacao pedidoNoPeriodo(LocalDate inicio, LocalDate fim) {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        Escala escala = gerarEscalaService.gerar(inicio, fim, sargenteante);
        escala.setSituacao(SituacaoEscala.PUBLICADA);
        escalaRepository.save(escala);
        ServicoEscalado servico = servicoEscaladoRepository.findByEscala_Id(escala.getId()).stream()
                .filter(s -> s.getTipoServico().getNome().equals("Guardas ao Quartel")).findFirst().orElseThrow();
        String login = usuarioRepository.findByMilitar_Id(servico.getMilitar().getId()).orElseThrow().getLogin();
        Long substituto = solicitacaoService.listarElegiveisParaTroca(servico.getId(), servico.getMilitar().getId()).get(0).getId();
        return solicitacaoService.criar(servico.getId(), substituto, "teste", login);
    }

    @Test
    void trocaCancelada_naoImpedeRegerar_eGuardaAFotografia() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        LocalDate fim = inicio.plusDays(4);
        Solicitacao s = pedidoNoPeriodo(inicio, fim);
        LocalDate dataOriginal = s.getServicoOrigem().getData();
        solicitacaoService.cancelar(s.getId(), usuarioRepository.findByMilitar_Id(s.getSolicitante().getId()).orElseThrow().getLogin());

        gerarEscalaService.gerar(inicio, fim, sargenteante);

        Solicitacao depois = solicitacaoRepository.findById(s.getId()).orElseThrow();
        assertThat(depois.getServicoOrigem()).isNull();
        assertThat(depois.getServicoOrigemData()).isEqualTo(dataOriginal);
        assertThat(depois.getServicoOrigemTipo()).isEqualTo("Guardas ao Quartel");
    }

    @Test
    void trocaEmAndamento_continuaImpedindoRegerar() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        LocalDate fim = inicio.plusDays(4);
        pedidoNoPeriodo(inicio, fim);

        assertThatThrownBy(() -> gerarEscalaService.gerar(inicio, fim, sargenteante))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("em andamento");
    }
}
