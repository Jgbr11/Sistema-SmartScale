package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/** Resolve o login da sessao (Authentication.getName(), o CPF) para o Usuario/Militar - um lugar so. */
@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuario(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    public Militar militar(String login) {
        return usuario(login).getMilitar();
    }
}
