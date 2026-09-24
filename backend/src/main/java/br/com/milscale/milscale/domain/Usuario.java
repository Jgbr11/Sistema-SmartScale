package br.com.milscale.milscale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** RF01/RF25 - conta de acesso vinculada a um militar. */
@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_militar", unique = true)
    private Militar militar;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_perfil")
    private PerfilAcesso perfil;

    @Column(nullable = false, unique = true, length = 20)
    private String login;

    /** RNF01 - senha sempre armazenada com hash (BCrypt), nunca em texto puro. */
    @Column(name = "senha_hash", nullable = false, length = 255)
    @JsonIgnore
    private String senhaHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;
}
