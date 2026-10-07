package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.milscale.milscale.domain.Qualificacao;
import br.com.milscale.milscale.domain.RequisitoServico;
import br.com.milscale.milscale.domain.Subunidade;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ElegibilidadeServiceTest {

    private final ElegibilidadeService service = new ElegibilidadeService();

    private PostoGraduacao posto(long id) {
        return PostoGraduacao.builder().id(id).sigla("Sd EP").descricao("Soldado").nivelHierarquico(1).build();
    }

    private Subunidade subunidade(long id, String sigla) {
        return Subunidade.builder().id(id).sigla(sigla).nome(sigla).build();
    }

    private Qualificacao qualificacao(long id, String nome) {
        return Qualificacao.builder().id(id).nome(nome).build();
    }

    private Militar militar(PostoGraduacao posto, Subunidade sub, Qualificacao... quals) {
        Set<Qualificacao> set = new HashSet<>(List.of(quals));
        return Militar.builder().id(1L).nomeCompleto("Fulano").nomeGuerra("Fulano")
                .cpf("00000000000").posto(posto).subunidade(sub).qualificacoes(set).build();
    }

    @Test
    void elegivel_quandoSoExigePosto() {
        PostoGraduacao sdEp = posto(1);
        RequisitoServico req = RequisitoServico.builder().posto(sdEp).build();
        Militar m = militar(sdEp, subunidade(1, "CCAp"));

        assertThat(service.elegivel(m, List.of(req))).isTrue();
    }

    @Test
    void naoElegivel_quandoPostoDiferente() {
        PostoGraduacao sdEp = posto(1);
        PostoGraduacao cabo = posto(2);
        RequisitoServico req = RequisitoServico.builder().posto(sdEp).build();
        Militar m = militar(cabo, subunidade(1, "CCAp"));

        assertThat(service.elegivel(m, List.of(req))).isFalse();
    }

    @Test
    void naoElegivel_quandoFaltaQualificacaoExigida() {
        PostoGraduacao sdEp = posto(1);
        Qualificacao cfc = qualificacao(1, "CFC");
        RequisitoServico req = RequisitoServico.builder().posto(sdEp).qualificacao(cfc).build();
        Militar semCurso = militar(sdEp, subunidade(1, "CCAp"));

        assertThat(service.elegivel(semCurso, List.of(req))).isFalse();
    }

    @Test
    void elegivel_quandoTemQualificacaoExigida() {
        PostoGraduacao sdEp = posto(1);
        Qualificacao cfc = qualificacao(1, "CFC");
        RequisitoServico req = RequisitoServico.builder().posto(sdEp).qualificacao(cfc).build();
        Militar comCfc = militar(sdEp, subunidade(1, "CCAp"), cfc);

        assertThat(service.elegivel(comCfc, List.of(req))).isTrue();
    }

    @Test
    void naoElegivel_quandoTemQualificacaoExcluida_regressaoMonitoramento() {

        PostoGraduacao sdEp = posto(1);
        Qualificacao cfc = qualificacao(1, "CFC");
        Qualificacao motorista = qualificacao(2, "Motorista");
        RequisitoServico req = RequisitoServico.builder().posto(sdEp)
                .qualificacoesExcluidas(new HashSet<>(Set.of(cfc, motorista))).build();
        Militar comCfc = militar(sdEp, subunidade(1, "CCAp"), cfc);
        Militar semCurso = militar(sdEp, subunidade(1, "CCAp"));

        assertThat(service.elegivel(comCfc, List.of(req))).isFalse();
        assertThat(service.elegivel(semCurso, List.of(req))).isTrue();
    }

    @Test
    void naoElegivel_quandoSubunidadeExcluida_regressaoAprovisionamento() {

        PostoGraduacao sdEp = posto(1);
        Subunidade aprov = subunidade(2, "Aprov");
        Subunidade ccap = subunidade(1, "CCAp");
        RequisitoServico req = RequisitoServico.builder().posto(sdEp).subunidadeExcluida(aprov).build();
        Militar doAprov = militar(sdEp, aprov);
        Militar deFora = militar(sdEp, ccap);

        assertThat(service.elegivel(doAprov, List.of(req))).isFalse();
        assertThat(service.elegivel(deFora, List.of(req))).isTrue();
    }

    @Test
    void naoElegivel_quandoSubunidadeExigidaDiferente() {
        PostoGraduacao sdEp = posto(1);
        Subunidade aprov = subunidade(2, "Aprov");
        Subunidade ccap = subunidade(1, "CCAp");

        RequisitoServico req = RequisitoServico.builder().posto(sdEp).subunidade(aprov).build();
        Militar deFora = militar(sdEp, ccap);
        Militar doAprov = militar(sdEp, aprov);

        assertThat(service.elegivel(deFora, List.of(req))).isFalse();
        assertThat(service.elegivel(doAprov, List.of(req))).isTrue();
    }

    @Test
    void naoElegivel_quandoListaDeRequisitosVazia() {
        Militar m = militar(posto(1), subunidade(1, "CCAp"));
        assertThat(service.elegivel(m, List.of())).isFalse();
    }
}
