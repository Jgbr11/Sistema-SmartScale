package br.com.milscale.milscale.adapters.persistence;

import br.com.smartscale.core.SituacaoPessoa;
import br.com.milscale.milscale.domain.Militar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MilitarRepository extends JpaRepository<Militar, Long> {
    Optional<Militar> findByCpf(String cpf);

    /** RN - nome de guerra nao pode repetir dentro do mesmo posto/graduacao. */
    List<Militar> findByPosto_IdAndNomeGuerraIgnoreCase(Long postoId, String nomeGuerra);

    /** Efetivo numa situacao (ex.: so ATIVO) - filtra no banco em vez de findAll() + filter. */
    List<Militar> findBySituacao(SituacaoPessoa situacao);
}
