package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Qualificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificacaoRepository extends JpaRepository<Qualificacao, Long> {
}
