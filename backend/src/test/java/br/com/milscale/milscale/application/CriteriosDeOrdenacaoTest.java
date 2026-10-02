package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.CriterioOrdenacaoMilitar;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.smartscale.core.CriterioDeOrdenacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CriteriosDeOrdenacaoTest {

    private final LocalDate hoje = LocalDate.now();

    private static MilitarEmGeracao militar(long id, int nivel, LocalDate ultimoServico, int servicosNaGeracao) {
        PostoGraduacao posto = PostoGraduacao.builder().id((long) nivel).sigla("P" + nivel).descricao("Posto " + nivel).nivelHierarquico(nivel).build();
        Militar m = Militar.builder().id(id).nomeGuerra("m" + id).posto(posto).dataUltimoServico(ultimoServico).build();
        MilitarEmGeracao emGeracao = new MilitarEmGeracao(m);
        for (int i = 0; i < servicosNaGeracao; i++) {
            emGeracao.marcarServico(ultimoServico);
        }
        return emGeracao;
    }

    private static List<Long> ordenar(List<MilitarEmGeracao> fila, CriterioDeOrdenacao<MilitarEmGeracao> criterio) {
        List<MilitarEmGeracao> copia = new ArrayList<>(fila);
        copia.sort(criterio.comparator());
        return copia.stream().map(MilitarEmGeracao::getId).toList();
    }

    @Test
    void maiorFolga_quemServiuHaMaisTempoPrimeiro() {
        var fila = List.of(militar(1, 1, hoje.minusDays(2), 0), militar(2, 1, hoje.minusDays(9), 0), militar(3, 1, hoje.minusDays(5), 0));
        assertThat(ordenar(fila, new CriterioOrdenacaoMilitar<>())).containsExactly(2L, 3L, 1L);
    }

    @Test
    void menorCarga_quemServiuMenosNaGeracaoPrimeiro_empateVaiPelaFolga() {
        var fila = List.of(militar(1, 1, hoje.minusDays(9), 2), militar(2, 1, hoje.minusDays(3), 0), militar(3, 1, hoje.minusDays(8), 0));
        assertThat(ordenar(fila, new CriterioMenorCargaNaGeracao())).containsExactly(3L, 2L, 1L);
    }

    @Test
    void maisModerno_menorPostoPrimeiro_empateVaiPelaFolga() {
        var fila = List.of(militar(1, 3, hoje.minusDays(9), 0), militar(2, 1, hoje.minusDays(2), 0), militar(3, 1, hoje.minusDays(7), 0));
        assertThat(ordenar(fila, new CriterioMaisModernoPrimeiro())).containsExactly(3L, 2L, 1L);
    }

    @Test
    void marcarServico_contaOsServicosDaGeracaoEMarcaComoAtualizado() {
        MilitarEmGeracao emGeracao = militar(1, 1, null, 0);
        assertThat(emGeracao.foiAtualizado()).isFalse();
        emGeracao.marcarServico(hoje);
        emGeracao.marcarServico(hoje.plusDays(4));
        assertThat(emGeracao.getServicosNaGeracao()).isEqualTo(2);
        assertThat(emGeracao.getUltimoServico()).isEqualTo(hoje.plusDays(4));
        assertThat(emGeracao.foiAtualizado()).isTrue();
    }
}
