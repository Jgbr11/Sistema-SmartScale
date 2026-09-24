package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * RF07 - regras da escala por tipo de servico. Corresponde a RN06
 * (intervalo minimo), RN20 (ciclo) e aos pesos de equidade usados numa
 * futura fase de "avaliador de justica".
 * Manutencao privativa do perfil Sargenteante (RN11).
 */
@Entity
@Table(name = "regra_escala")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegraEscala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regra")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_tipo_servico", unique = true)
    private TipoServico tipoServico;

    /** RN06 - dias minimos entre dois servicos do mesmo militar. */
    @Column(name = "intervalo_minimo", nullable = false)
    @Builder.Default
    private int intervaloMinimo = 7;

    @Column(name = "dias_folga", nullable = false)
    @Builder.Default
    private int diasFolga = 1;

    @Column(name = "max_servicos_mes")
    private Integer maxServicosMes;

    @Column(name = "peso_fim_semana", nullable = false, precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal pesoFimSemana = new BigDecimal("1.5");

    @Column(name = "peso_feriado", nullable = false, precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal pesoFeriado = new BigDecimal("2.0");
}
