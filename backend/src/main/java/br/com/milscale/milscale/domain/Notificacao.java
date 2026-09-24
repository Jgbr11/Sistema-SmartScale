package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notificação dentro do sistema - o sininho. Diferente do log de
 * auditoria (que é histórico pro Sargenteante ver "quem fez o quê"),
 * isso é um aviso direcionado a UMA pessoa específica, sobre algo que
 * precisa da atenção dela (ex.: uma troca esperando decisão).
 */
@Entity
@Table(name = "notificacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario_destinatario")
    private Usuario destinatario;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String mensagem;

    /** Rota do front pra onde o clique leva (ex.: "/trocas"). */
    @Column(length = 60)
    private String link;

    @Column(nullable = false)
    @Builder.Default
    private boolean lida = false;

    @Column(name = "data_criacao", nullable = false)
    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
}
