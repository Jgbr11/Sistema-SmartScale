package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** RF26 - impede a escalacao do militar no periodo informado (RN15). */
@Entity
@Table(name = "afastamento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Afastamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_afastamento")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_militar")
    private Militar militar;

    @Column(nullable = false, length = 15)
    private String tipo; // MISSAO, DISPENSA, FERIAS, LICENCA, CURSO, OUTRO

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario_registro")
    private Usuario usuarioRegistro;

    @Column(name = "data_registro", nullable = false)
    @Builder.Default
    private LocalDateTime dataRegistro = LocalDateTime.now();

    /** Agrupa varios Afastamento (um por militar) que nasceram do mesmo
     *  cadastro de missao com multiplas pessoas - null pra um afastamento
     *  individual (dispensa/ferias/licenca de uma pessoa so). */
    @Column(name = "lote_missao", length = 40)
    private String loteMissao;

    public boolean cobre(LocalDate data) {
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }
}
