package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RF-adm - quem fez o que no sistema. Guardado como registro imutavel:
 * uma vez criado, nunca é editado, só consultado (e talvez um dia
 * arquivado, nunca corrigido - log de auditoria que pode ser alterado
 * não serve pra nada).
 */
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

    /** Login de quem fez a acao - guardado como texto solto (nao FK) de
     *  proposito, pra o log continuar legivel mesmo se a conta for
     *  desativada ou o usuario for removido no futuro. */
    @Column(name = "usuario_login", length = 20)
    private String usuarioLogin;

    @Column(name = "usuario_nome_exibicao", length = 60)
    private String usuarioNomeExibicao;

    @Column(nullable = false, length = 60)
    private String acao;

    @Column(length = 300)
    private String descricao;
}
