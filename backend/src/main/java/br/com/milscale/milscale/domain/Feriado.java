package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "feriado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Feriado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feriado")
    private Long id;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoFeriado tipo;

    public boolean cobre(LocalDate dia) {
        return !dia.isBefore(dataInicio) && !dia.isAfter(dataFim);
    }
}
