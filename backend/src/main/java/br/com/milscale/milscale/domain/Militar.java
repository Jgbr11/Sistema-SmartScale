package br.com.milscale.milscale.domain;

import br.com.milscale.core.domain.PessoaEscalada;
import br.com.milscale.core.domain.SituacaoPessoa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Especializacao MilScale de {@link PessoaEscalada} (RF04).
 * "Pessoa escalada" do nucleo -> "militar" aqui.
 *
 * O contador de rodizio do MilScale e "dias sem tirar servico", calculado
 * a partir da data do ultimo servico cumprido (ver
 * br.com.milscale.milscale.application.ConsultarContadorRodizioService).
 * Por simplicidade nesta primeira fatia, guardamos a data do ultimo
 * servico diretamente no militar e calculamos o contador sob demanda.
 */
@Entity
@Table(name = "militar")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Militar implements PessoaEscalada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_militar")
    private Long id;

    @Column(name = "nome_completo", nullable = false, length = 120)
    private String nomeCompleto;

    @Column(name = "nome_guerra", nullable = false, length = 40)
    private String nomeGuerra;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    /** Numero de registro (NR REGISTRO na carteira de identidade militar). */
    @Column(name = "numero_registro", length = 20)
    private String numeroRegistro;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    /** Numero do FUSEX (sistema de saude do Exercito) - aparece na carteira de identidade. */
    @Column(length = 20)
    private String fusex;

    /** Foto 3x4 da carteira de identidade, guardada como data URL base64.
     *  Simples de propósito pra essa fatia - MySQL/H2 aguentam um TEXT/CLOB
     *  tranquilo pro volume de gente de um batalhão; um armazenamento de
     *  arquivo de verdade (S3 etc.) é o próximo passo natural se crescer. */
    @Lob
    @Column(name = "foto_base64")
    private String fotoBase64;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_posto")
    private PostoGraduacao posto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_subunidade")
    private Subunidade subunidade;

    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SituacaoPessoa situacao = SituacaoPessoa.ATIVO;

    /** Data do ultimo servico cumprido - usada para calcular o contador de rodizio. */
    @Column(name = "data_ultimo_servico")
    private LocalDate dataUltimoServico;

    /** RF05 - cursos/habilitacoes da pessoa (CFC, Motorista...). Usado como
     *  filtro real de elegibilidade em GerarEscalaService (RF06 - requisito_servico).
     *  Exposto no JSON (sem @JsonIgnore) para a tela de Qualificacoes gerenciar. */
    @ManyToMany
    @JoinTable(name = "militar_qualificacao",
            joinColumns = @JoinColumn(name = "id_militar"),
            inverseJoinColumns = @JoinColumn(name = "id_qualificacao"))
    @Builder.Default
    private Set<Qualificacao> qualificacoes = new HashSet<>();

    @Override
    public String getNomeExibicao() {
        return posto != null ? posto.getSigla() + " " + nomeGuerra : nomeGuerra;
    }

    @Override
    public long getContadorRodizio() {
        if (dataUltimoServico == null) {
            return Integer.MAX_VALUE; // nunca serviu: prioridade maxima na fila
        }
        // Valor pode ser negativo (servico ja marcado pra uma data futura) -
        // o front decide como rotular isso; aqui so devolvemos o fato.
        return java.time.temporal.ChronoUnit.DAYS.between(dataUltimoServico, LocalDate.now());
    }
}
