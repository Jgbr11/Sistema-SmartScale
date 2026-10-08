package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.LogAuditoriaRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuditoriaServiceTest {

    @Test
    void falhaAoGravar_naoPropagaMasFicaNoLog() {
        LogAuditoriaRepository logs = Mockito.mock(LogAuditoriaRepository.class);
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        when(usuarios.findByLogin(any())).thenReturn(Optional.empty());
        when(logs.save(any())).thenThrow(new IllegalStateException("banco fora"));
        ListAppender<ILoggingEvent> capturado = new ListAppender<>();
        capturado.start();
        ((Logger) LoggerFactory.getLogger(AuditoriaService.class)).addAppender(capturado);

        new AuditoriaService(logs, usuarios).registrar("00000000001", "ESCALA_GERADA", "x");

        assertThat(capturado.list).anyMatch(e -> e.getFormattedMessage().contains("ESCALA_GERADA"));
    }
}
