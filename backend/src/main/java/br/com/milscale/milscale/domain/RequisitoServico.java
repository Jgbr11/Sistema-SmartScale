package br.com.milscale.milscale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToOne
    @JoinColumn(name = "id_subunidade")
    private Subunidade subunidade;

    @ManyToOne
    @JoinColumn(name = "id_qualificacao")
    private Qualificacao qualificacao;

    @ManyToMany
    @JoinTable(name = "requisito_qualificacao_excluida",
            joinColumns = @JoinColumn(name = "id_requisito"),
            inverseJoinColumns = @JoinColumn(name = "id_qualificacao"))
    @Builder.Default
    private Set<Qualificacao> qualificacoesExcluidas = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_subunidade_excluida")
    private Subunidade subunidadeExcluida;
}
