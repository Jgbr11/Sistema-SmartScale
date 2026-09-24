package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
    Optional<Usuario> findByMilitar_Id(Long militarId);
}
