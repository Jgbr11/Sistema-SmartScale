package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitacao")
    private Long id;

    @Version
    private Long versao;

    @ManyToOne
    @JoinColumn(name = "id_servico_escalado")
    private ServicoEscalado servicoOrigem;

    @Column(name = "servico_origem_data")
    private LocalDate servicoOrigemData;

    @Column(name = "servico_origem_tipo", length = 60)
    private String servicoOrigemTipo;

    @Column(name = "servico_destino_data")
    private LocalDate servicoDestinoData;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_solicitante")
    private Militar solicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_substituto")
    private Militar substituto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_troca", nullable = false, length = 20)
    @Builder.Default
    private TipoTroca tipoTroca = TipoTroca.SUBSTITUICAO;

    @ManyToOne
    @JoinColumn(name = "id_servico_destino")
    private ServicoEscalado servicoDestino;

    @Column(nullable = false, length = 250)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private SituacaoSolicitacao situacao = SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO;

    @Column(length = 250)
    private String comentarioCabo;

    @Column(length = 250)
    private String comentarioSargenteante;

    @Column(name = "data_solicitacao", nullable = false)
    @Builder.Default
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_decisao_final")
    private LocalDateTime dataDecisaoFinal;
}
