package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** RF08/RF11 - o periodo de escala gerado (rascunho) e publicado. */
@Entity
@Table(name = "escala")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Escala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escala")
    private Long id;

    @Column(nullable = false, length = 80)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_subunidade")
    private Subunidade subunidade; // null = escala da OM inteira

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false, length = 15)
    @Builder.Default
    private String situacao = "RASCUNHO"; // RASCUNHO, PUBLICADA, ENCERRADA

    @Column(name = "data_geracao", nullable = false)
    @Builder.Default
    private LocalDateTime dataGeracao = LocalDateTime.now();

    @Column(name = "data_publicacao")
    private LocalDateTime dataPublicacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario_geracao")
    private Usuario usuarioGeracao;

    @OneToMany(mappedBy = "escala", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServicoEscalado> servicos = new ArrayList<>();
}
