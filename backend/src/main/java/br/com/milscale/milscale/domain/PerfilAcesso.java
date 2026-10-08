package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfil_acesso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PerfilAcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nome;

    @Column(length = 150)
    private String descricao;
}
