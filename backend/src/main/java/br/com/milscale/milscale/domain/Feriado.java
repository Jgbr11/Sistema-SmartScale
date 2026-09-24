package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** Feriados usados pelo peso_feriado das regras da escala (RegraEscala.pesoFeriado). */
@Entity
@Table(name = "feriado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Feriado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feriado")
    private Long id;

    /** Um feriado pode cobrir mais de um dia (ex.: ponte, recesso) - por isso
     *  é um período, não uma data única. */
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false, length = 15)
    private String tipo; // NACIONAL, MILITAR, OM

    public boolean cobre(LocalDate dia) {
        return !dia.isBefore(dataInicio) && !dia.isAfter(dataFim);
    }
}
