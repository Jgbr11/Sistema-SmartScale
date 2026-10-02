package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.PostoGraduacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostoGraduacaoRepository extends JpaRepository<PostoGraduacao, Long> {

    List<PostoGraduacao> findAllByOrderByNivelHierarquicoAsc();

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);

    boolean existsByNivelHierarquicoAndIdNot(Integer nivelHierarquico, Long id);
}
