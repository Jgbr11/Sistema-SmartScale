package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_auditoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long id;

    @Column(name = "data_hora", nullable = false)
    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(name = "usuario_login", length = 20)
    private String usuarioLogin;

    @Column(name = "usuario_nome_exibicao", length = 60)
    private String usuarioNomeExibicao;

    @Column(nullable = false, length = 60)
    private String acao;

    @Column(length = 300)
    private String descricao;
}
