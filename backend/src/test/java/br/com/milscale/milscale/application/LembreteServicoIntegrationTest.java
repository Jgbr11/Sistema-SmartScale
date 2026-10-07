package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LembreteServicoIntegrationTest {

    static final List<String> enviados = new ArrayList<>();

    @TestConfiguration
    static class EmailFalso {
        @Bean @Primary
        EnvioDeEmail envioDeEmailFalso() {
            return (destinatario, assunto, corpo) -> enviados.add(destinatario);
        }
    }

    @Autowired private LembreteServicoService lembreteServicoService;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MilitarRepository militarRepository;

    @Test
    void avisaSoQuemTemEmailEmEscalaPublicada() {
        enviados.clear();
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        Militar comEmail = usuarioRepository.findByLogin("00000000004").orElseThrow().getMilitar();
        comEmail.setEmail("nogueira@exemplo.mil.br");
        militarRepository.save(comEmail);
        Militar semEmail = usuarioRepository.findByLogin("00000000003").orElseThrow().getMilitar();
        LocalDate amanha = LocalDate.now().plusDays(1);
        TipoServico tipo = tipoServicoRepository.findAll().get(0);
        Escala publicada = escalaRepository.save(Escala.builder().descricao("p").dataInicio(amanha).dataFim(amanha)
                .situacao(SituacaoEscala.PUBLICADA).usuarioGeracao(sargenteante).build());
        servicoEscaladoRepository.save(ServicoEscalado.builder().escala(publicada).data(amanha).tipoServico(tipo).militar(comEmail).build());
        servicoEscaladoRepository.save(ServicoEscalado.builder().escala(publicada).data(amanha).tipoServico(tipo).militar(semEmail).build());

        int processados = lembreteServicoService.enviarPara(amanha);

        assertThat(processados).isEqualTo(1);
        assertThat(enviados).containsExactly("nogueira@exemplo.mil.br");
    }
}
