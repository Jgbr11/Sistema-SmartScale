package br.com.milscale.milscale.domain;

// Especialização MilScale (LPS)

import br.com.smartscale.core.PessoaEscalada;
import br.com.smartscale.core.SituacaoPessoa;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.time.temporal.ChronoUnit;

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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String cpf;

    @Column(name = "numero_registro", length = 20)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String numeroRegistro;

    @Column(name = "data_nascimento")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDate dataNascimento;

    @Column(length = 20)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String fusex;

    @Lob
    @Column(name = "foto_base64")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String fotoBase64;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_posto")
    private PostoGraduacao posto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_subunidade")
    private Subunidade subunidade;

    @Column(length = 120)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String email;

    @Column(length = 20)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SituacaoPessoa situacao = SituacaoPessoa.ATIVO;

    @Column(name = "data_ultimo_servico")
    private LocalDate dataUltimoServico;

    @ManyToMany
    @JoinTable(name = "militar_qualificacao",
            joinColumns = @JoinColumn(name = "id_militar"),
            inverseJoinColumns = @JoinColumn(name = "id_qualificacao"))
    @Builder.Default
    private Set<Qualificacao> qualificacoes = new HashSet<>();

    public boolean isTemFoto() {
        return fotoBase64 != null && !fotoBase64.isBlank();
    }

    @Override
    public String getNomeExibicao() {
        return posto != null ? posto.getSigla() + " " + nomeGuerra : nomeGuerra;
    }

    @Override
    public long getContadorRodizio() {
        if (dataUltimoServico == null) {
            return Integer.MAX_VALUE;
        }

        return ChronoUnit.DAYS.between(dataUltimoServico, LocalDate.now());
    }
}
