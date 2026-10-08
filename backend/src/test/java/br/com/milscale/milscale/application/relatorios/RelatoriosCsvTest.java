package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.TipoAfastamento;
import br.com.milscale.milscale.domain.TipoServico;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelatoriosCsvTest {

    private final PostoGraduacao cabo = PostoGraduacao.builder().id(1L).sigla("Cb").descricao("Cabo").nivelHierarquico(3).build();
    private final Militar silva = Militar.builder().id(1L).nomeGuerra("Silva").posto(cabo).build();
    private final TipoServico guarda = TipoServico.builder().id(1L).nome("Cabo da Guarda; noturno").build();
    private final LocalDate dia = LocalDate.of(2030, 1, 10);

    @Test
    void escalaDoDia_segueOEsqueletoEEscapaSeparador() {
        ServicoEscalado servico = ServicoEscalado.builder().data(dia).tipoServico(guarda).militar(silva).build();
        String csv = new RelatorioEscalaDoDia(dia, List.of(servico)).gerar();

        String[] linhas = csv.split("\r\n");
        assertThat(linhas[0]).isEqualTo("﻿Função;Posto;Nome de guerra;Situação");
        assertThat(linhas[1]).isEqualTo("\"Cabo da Guarda; noturno\";Cb;Silva;PREVISTO");
        assertThat(csv).endsWith("Gerado por MilScale, 5º Batalhão de Suprimento\r\n");
    }

    @Test
    void vagaSemMilitar_apareceComoVagaEmAberto() {
        ServicoEscalado vaga = ServicoEscalado.builder().data(dia).tipoServico(guarda).build();
        assertThat(new RelatorioEscalaDoDia(dia, List.of(vaga)).gerar()).contains(";;VAGA EM ABERTO;PREVISTO");
    }

    @Test
    void servicosDoMilitar_personalizaORodape() {
        ServicoEscalado servico = ServicoEscalado.builder().data(dia).tipoServico(guarda).militar(silva).build();
        String csv = new RelatorioServicosDoMilitar(silva, List.of(servico)).gerar();
        assertThat(csv).contains("2030-01-10;\"Cabo da Guarda; noturno\";PREVISTO");
        assertThat(csv).contains("Serviços de Cb Silva — Gerado por MilScale");
    }

    @Test
    void textoQueComecaComoFormula_eNeutralizado() {
        Afastamento a = Afastamento.builder().militar(silva).tipo(TipoAfastamento.MISSAO)
                .descricao("=HYPERLINK(\"http://x\")").dataInicio(dia).dataFim(dia.plusDays(2)).build();
        String csv = new RelatorioAfastamentosDoMes(YearMonth.of(2030, 1), List.of(a)).gerar();
        assertThat(csv).contains("\"'=HYPERLINK(\"\"http://x\"\")\"");
        assertThat(csv).doesNotContain(";=HYPERLINK");
    }

    @Test
    void cadaRelatorioTemSeuNomeDeArquivo() {
        assertThat(new RelatorioEscalaDoDia(dia, List.of()).nomeDoArquivo()).isEqualTo("escala-2030-01-10.csv");
        assertThat(new RelatorioServicosDoMilitar(silva, List.of()).nomeDoArquivo()).isEqualTo("servicos-silva.csv");
        assertThat(new RelatorioAfastamentosDoMes(YearMonth.of(2030, 1), List.of()).nomeDoArquivo()).isEqualTo("afastamentos-2030-01.csv");
    }
}
