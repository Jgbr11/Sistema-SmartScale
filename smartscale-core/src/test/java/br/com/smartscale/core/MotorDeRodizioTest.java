package br.com.smartscale.core;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MotorDeRodizioTest {

    record Pessoa(Long id, long contador) implements PessoaEscalada {
        public Long getId() { return id; }
        public String getNomeExibicao() { return "p" + id; }
        public SituacaoPessoa getSituacao() { return SituacaoPessoa.ATIVO; }
        public long getContadorRodizio() { return contador; }
    }

    record Turno(int efetivo) implements TipoTurno {
        public Long getId() { return 1L; }
        public String getNome() { return "Plantão"; }
        public int getEfetivoNecessario() { return efetivo; }
        public boolean isAtivo() { return true; }
    }

    private final CriterioDeOrdenacao<Pessoa> maiorContadorPrimeiro =
            () -> Comparator.comparingLong(Pessoa::contador).reversed();

    private final List<Pessoa> pool = List.of(new Pessoa(1L, 3), new Pessoa(2L, 9), new Pessoa(3L, 5));

    @Test
    void preencheAsVagasComOsMaisBemPosicionadosNaFila() {
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>().preencherVagas(pool, new Turno(2), maiorContadorPrimeiro);
        assertThat(escalados).extracting(Pessoa::id).containsExactly(2L, 3L);
    }

    @Test
    void comMenosGenteQueVagas_devolveQuemTem() {
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>()
                .preencherVagas(List.of(new Pessoa(1L, 1)), new Turno(3), maiorContadorPrimeiro);
        assertThat(escalados).hasSize(1);
    }

    @Test
    void filtroExtra_eAplicadoAntesDeOrdenar() {
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>()
                .preencherVagas(pool, p -> p.id() != 2L, new Turno(1), maiorContadorPrimeiro);
        assertThat(escalados).extracting(Pessoa::id).containsExactly(3L);
    }

    @Test
    void outroProdutoDaLinha_usaOutroCriterioSemMudarOMotor() {
        CriterioDeOrdenacao<Pessoa> menorContadorPrimeiro = () -> Comparator.comparingLong(Pessoa::contador);
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>().preencherVagas(pool, new Turno(1), menorContadorPrimeiro);
        assertThat(escalados).extracting(Pessoa::id).containsExactly(1L);
    }
}
