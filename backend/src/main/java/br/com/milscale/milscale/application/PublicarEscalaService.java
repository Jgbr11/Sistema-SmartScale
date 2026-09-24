package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.EscalaRepository;
import br.com.milscale.milscale.domain.Escala;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/** RF11 - publica a escala, tornando-a visivel para as pessoas escaladas. */
@Service
public class PublicarEscalaService {

    private final EscalaRepository escalaRepository;

    public PublicarEscalaService(EscalaRepository escalaRepository) {
        this.escalaRepository = escalaRepository;
    }

    @Transactional
    public Escala publicar(Long escalaId) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new NoSuchElementException("Escala nao encontrada"));
        escala.setSituacao("PUBLICADA");
        escala.setDataPublicacao(LocalDateTime.now());
        return escalaRepository.save(escala);
    }
}
