package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RF15/RF17/RF18/RF19 - pedido de troca (permuta) de um servico escalado.
 * Fluxo: AGUARDANDO_SUBSTITUTO (a pessoa sugerida precisa aceitar) ->
 * EM_TRIAGEM (Cabo analisa) -> AGUARDANDO_AUTORIZACAO (Sargenteante
 * decide) -> AUTORIZADA (troca efetivada no ServicoEscalado) ou NEGADA em
 * qualquer uma das duas etapas. O solicitante pode CANCELAR enquanto ainda
 * estiver em triagem.
 *
 * Nota de escopo desta fatia: o parecer de cada etapa fica guardado direto
 * nesta linha (comentarioCabo/comentarioSargenteante), sem uma tabela de
 * parecer separada - suficiente para o fluxo funcionar de ponta a ponta;
 * uma trilha de auditoria completa por etapa e um refinamento futuro.
 */
@Entity
@Table(name = "solicitacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitacao")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_servico_escalado")
    private ServicoEscalado servicoOrigem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_solicitante")
    private Militar solicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_substituto")
    private Militar substituto;

    /** SUBSTITUICAO (o substituto assume e o solicitante fica sem nada até o
     *  próximo serviço normal) ou TROCA_MUTUA (os dois trocam de dia entre si). */
    @Column(name = "tipo_troca", nullable = false, length = 20)
    @Builder.Default
    private String tipoTroca = "SUBSTITUICAO";

    /** Só preenchido em TROCA_MUTUA - o serviço do substituto que vai virar
     *  do solicitante quando a troca for autorizada. Em SUBSTITUICAO fica
     *  null, já que só o servicoOrigem muda de dono. */
    @ManyToOne
    @JoinColumn(name = "id_servico_destino")
    private ServicoEscalado servicoDestino;

    @Column(nullable = false, length = 250)
    private String justificativa;

    @Column(nullable = false, length = 25)
    @Builder.Default
    private String situacao = "AGUARDANDO_SUBSTITUTO"; // AGUARDANDO_SUBSTITUTO, EM_TRIAGEM, AGUARDANDO_AUTORIZACAO, AUTORIZADA, NEGADA, CANCELADA

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
