package br.com.milscale.milscale.domain;

// Especialização MilScale (LPS)

import br.com.smartscale.core.TipoTurno;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnore
    @OneToMany(mappedBy = "tipoServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequisitoServico> requisitos = new ArrayList<>();

    @JsonProperty("quantidadeRequisitos")
    public int getQuantidadeRequisitos() {
        return requisitos == null ? 0 : requisitos.size();
    }
}
