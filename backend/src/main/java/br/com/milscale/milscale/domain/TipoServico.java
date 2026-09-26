package br.com.milscale.milscale.domain;

import br.com.smartscale.core.TipoTurno;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Especializacao MilScale de {@link TipoTurno} (RF06).
 * "Tipo de turno" do nucleo -> "tipo de servico" aqui, sempre de 24h.
 */
@Entity
@Table(name = "tipo_servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoServico implements TipoTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_servico")
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(length = 150)
    private String descricao;

    @Column(name = "efetivo_necessario", nullable = false)
    private int efetivoNecessario;

    @Column(name = "hora_inicio", nullable = false)
    @Builder.Default
    private LocalTime horaInicio = LocalTime.of(8, 0);

    @Column(name = "duracao_horas", nullable = false)
    @Builder.Default
    private int duracaoHoras = 24;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @OneToMany(mappedBy = "tipoServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequisitoServico> requisitos = new ArrayList<>();
}
