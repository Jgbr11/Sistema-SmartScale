package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

/** Especializacao MilScale de "qualificacao" (RF05) - cursos e habilitacoes. */
@Entity
@Table(name = "qualificacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class Qualificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_qualificacao")
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(length = 120)
    private String descricao;
}
