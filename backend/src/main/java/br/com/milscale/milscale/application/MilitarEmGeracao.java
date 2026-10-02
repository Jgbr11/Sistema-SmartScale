package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Militar;
import br.com.smartscale.core.PessoaEscalada;
import br.com.smartscale.core.SituacaoPessoa;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MilitarEmGeracao implements PessoaEscalada {

    private final Militar militar;
    private LocalDate ultimoServico;
    private int servicosNaGeracao;
    private boolean atualizado;

    public MilitarEmGeracao(Militar militar) {
        this.militar = militar;
        this.ultimoServico = militar.getDataUltimoServico();
    }

    public Militar militar() { return militar; }
    public LocalDate getUltimoServico() { return ultimoServico; }
    public int getServicosNaGeracao() { return servicosNaGeracao; }
    public int getNivelHierarquico() { return militar.getPosto().getNivelHierarquico(); }
    public boolean foiAtualizado() { return atualizado; }

    public void marcarServico(LocalDate data) {
        ultimoServico = data;
        servicosNaGeracao++;
        atualizado = true;
    }

    @Override public Long getId() { return militar.getId(); }
    @Override public String getNomeExibicao() { return militar.getNomeExibicao(); }
    @Override public SituacaoPessoa getSituacao() { return militar.getSituacao(); }

    @Override
    public long getContadorRodizio() {
        return ultimoServico == null ? Integer.MAX_VALUE : ChronoUnit.DAYS.between(ultimoServico, LocalDate.now());
    }
}
