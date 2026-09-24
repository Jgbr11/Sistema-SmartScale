package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

/** Especializacao MilScale de "unidade organizacional" (RF04, ponto de adaptacao). */
@Entity
@Table(name = "subunidade")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subunidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subunidade")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
