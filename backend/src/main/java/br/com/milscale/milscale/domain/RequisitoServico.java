package br.com.milscale.milscale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * RF06 (ponto de adaptacao) - elegibilidade: quais postos podem assumir
 * cada tipo de servico e qual qualificacao e exigida (opcional).
 */
@Entity
@Table(name = "requisito_servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RequisitoServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_requisito")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tipo_servico")
    @JsonIgnore
    private TipoServico tipoServico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_posto")
    private PostoGraduacao posto;

    /** Opcional - alem do posto, algumas funcoes exigem lotacao numa
     *  subunidade especifica (ex.: Rancheiro de Dia exige estar no Aprov,
     *  nao um curso - e a secao onde a pessoa esta lotada). Nulo = qualquer
     *  subunidade serve, contanto que o posto bata. */
    @ManyToOne
    @JoinColumn(name = "id_subunidade")
    private Subunidade subunidade;

    @ManyToOne
    @JoinColumn(name = "id_qualificacao")
    private Qualificacao qualificacao;

    /** Opcional - postos que TEM essa(s) qualificacao(oes) ficam de fora,
     *  mesmo cumprindo posto/subunidade (ex.: Monitoramento e do Sd EP em
     *  geral, mas quem tem CFC ou Motorista nao tira - ja tem outra funcao
     *  especifica). Vazio = ninguem e excluido por qualificacao. */
    @ManyToMany
    @JoinTable(name = "requisito_qualificacao_excluida",
            joinColumns = @JoinColumn(name = "id_requisito"),
            inverseJoinColumns = @JoinColumn(name = "id_qualificacao"))
    @Builder.Default
    private java.util.Set<Qualificacao> qualificacoesExcluidas = new java.util.HashSet<>();

    /** Opcional - quem esta lotado NESSA subunidade fica de fora, mesmo
     *  cumprindo posto (ex.: ninguem do Aprovisionamento tira serviço que
     *  não seja do rancho - Aprov so serve Rancheiro/Cozinheiro/Graduado
     *  do Rancho, nunca Guardas ao Quartel, Monitoramento etc.). Nulo =
     *  ninguem e excluido por subunidade. */
    @ManyToOne
    @JoinColumn(name = "id_subunidade_excluida")
    private Subunidade subunidadeExcluida;
}
