package br.com.milscale.milscale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "servico_escalado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServicoEscalado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico_escalado")
    private Long id;

    @Version
    private Long versao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_escala")
    @JsonIgnore
    private Escala escala;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tipo_servico")
    private TipoServico tipoServico;

    @ManyToOne
    @JoinColumn(name = "id_militar")
    private Militar militar;

    @Column(length = 40)
    private String posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SituacaoServico situacao = SituacaoServico.PREVISTO;

    @Column(nullable = false)
    @Builder.Default
    private boolean travado = false;

    @Column(length = 150)
    private String observacao;

    public boolean isJaComecou() {
        LocalDateTime inicio = LocalDateTime.of(data, tipoServico.getHoraInicio());
        return !LocalDateTime.now().isBefore(inicio);
    }
}
