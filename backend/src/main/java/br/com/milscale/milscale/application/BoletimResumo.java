package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Boletim;
import java.time.LocalDateTime;

public record BoletimResumo(Long id, String numero, String titulo, String autor, LocalDateTime dataPublicacao,
                            LocalDateTime dataAtualizacao, String avisoRelacionado, String avisoRelacionadoDescricao) {

    public static BoletimResumo de(Boletim b) {
        return new BoletimResumo(b.getId(), b.getNumero(), b.getTitulo(), b.getAutor().getNomeExibicao(),
                b.getDataPublicacao(), b.getDataAtualizacao(), b.getAvisoRelacionado(), b.getAvisoRelacionadoDescricao());
    }
}
