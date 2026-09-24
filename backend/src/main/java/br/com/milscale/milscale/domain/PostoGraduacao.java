package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

/** Especializacao MilScale de "funcao" (RF04) - define tambem a antiguidade (RN, elegibilidade). */
@Entity
@Table(name = "posto_graduacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostoGraduacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_posto")
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String sigla;

    @Column(nullable = false, length = 60)
    private String descricao;

    @Column(name = "nivel_hierarquico", nullable = false, unique = true)
    private Integer nivelHierarquico;
}
