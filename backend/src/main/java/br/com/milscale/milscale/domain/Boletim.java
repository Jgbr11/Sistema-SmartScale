package br.com.milscale.milscale.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "aviso_relacionado", length = 60)
    private String avisoRelacionado;

    @Column(name = "aviso_relacionado_descricao", length = 150)
    private String avisoRelacionadoDescricao;
}
