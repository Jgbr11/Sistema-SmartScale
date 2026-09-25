package br.com.milscale.milscale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** RF08/RF13/RF14 - uma vaga preenchida (ou em aberto) num dia da escala. */
@Entity
@Table(name = "servico_escalado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServicoEscalado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico_escalado")
    private Long id;

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
    private Militar militar; // null = posto em aberto (nao coberto pelo motor)

    @Column(length = 40)
    private String posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SituacaoServico situacao = SituacaoServico.PREVISTO;

    @Column(nullable = false)
    @Builder.Default
    private boolean travado = false; // RF12 - dia bloqueado preserva a alocacao

    @Column(length = 150)
    private String observacao;

    /** Calculado, não guardado no banco — vira "sólido" sozinho quando o
     *  horário de início do serviço já passou, sem precisar de travamento
     *  manual (RF12, variante automática). Exposto no JSON pro front
     *  mostrar um ícone diferente do cadeado manual (`travado`). */
    public boolean isJaComecou() {
        java.time.LocalDateTime inicio = java.time.LocalDateTime.of(data, tipoServico.getHoraInicio());
        return !java.time.LocalDateTime.now().isBefore(inicio);
    }
}
