package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Boletim Interno (BI) do batalhao - pode ter texto e imagens coladas
 * direto no editor, misturados livremente (nao é so anexo separado).
 * O conteudo vem como HTML gerado pelo editor rich-text do front, com
 * imagens embutidas como data URI base64 - mesma logica simples ja
 * usada pra foto do Militar, aqui reaproveitada pra imagem colada.
 */
@Entity
@Table(name = "boletim")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Boletim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boletim")
    private Long id;

    @Column(length = 20)
    private String numero;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Lob
    @Column(name = "conteudo_html", nullable = false)
    private String conteudoHtml;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_autor")
    private Militar autor;

    @Column(name = "data_publicacao", nullable = false)
    @Builder.Default
    private LocalDateTime dataPublicacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    /** Opcional - liga o boletim a um feriado ou missão/afastamento cadastrado
     *  (chave que a tela de Avisos usa, ex.: "afastamento-<lote>" ou "feriado-<id>").
     *  A descrição fica duplicada aqui de propósito, pra não precisar resolver
     *  a chave de novo só pra mostrar "relacionado a: X" na listagem do Boletim. */
    @Column(name = "aviso_relacionado", length = 60)
    private String avisoRelacionado;

    @Column(name = "aviso_relacionado_descricao", length = 150)
    private String avisoRelacionadoDescricao;
}
