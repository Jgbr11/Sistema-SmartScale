package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.PerfilAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilAcessoRepository extends JpaRepository<PerfilAcesso, Long> {
    Optional<PerfilAcesso> findByNome(String nome);
}
