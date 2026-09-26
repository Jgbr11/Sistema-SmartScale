package br.com.milscale.milscale.domain;

import br.com.smartscale.core.CriterioDeOrdenacao;
import br.com.smartscale.core.PessoaEscalada;

import java.util.Comparator;

/**
 * Especializacao MilScale do ponto de variacao RN01: aqui o criterio e
 * "maior numero de dias sem servico primeiro" (ver Tabela 2 do modelo de
 * regras de negocio, coluna MilScale/batalhao). Um produto hospitalar da
 * mesma linha usaria "menor carga horaria acumulada primeiro" - outra
 * classe, implementando o mesmo contrato do nucleo.
 *
 * Generico sobre qualquer {@link PessoaEscalada} (nao só {@code Militar})
 * porque, durante a geracao, o motor trabalha com um wrapper mutavel
 * (ver GerarEscalaService.MilitarEmGeracao) que tambem implementa essa
 * interface - o criterio nao precisa saber disso.
 */
public class CriterioOrdenacaoMilitar<P extends PessoaEscalada> implements CriterioDeOrdenacao<P> {
    @Override
    public Comparator<P> comparator() {
        // maior contador primeiro -> ordem decrescente
        return Comparator.comparingLong(P::getContadorRodizio).reversed();
    }
}
