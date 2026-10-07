package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BloqueioDiaIntegrationTest {

    @Autowired private BloqueioDiaService bloqueioDiaService;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Escala escala;
    private TipoServico tipo;

    @BeforeEach
    void montar() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        tipo = tipoServicoRepository.findAll().get(0); // horaInicio 08:00
        escala = escalaRepository.save(Escala.builder().descricao("teste")
                .dataInicio(LocalDate.now().minusDays(5)).dataFim(LocalDate.now().plusDays(5))
                .usuarioGeracao(sargenteante).build());
    }

    private void servicoEm(LocalDate dia) {
        servicoEscaladoRepository.save(ServicoEscalado.builder().escala(escala).data(dia).tipoServico(tipo).build());
    }

    @Test
    void travarDiaQueJaPassou_recusa() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        servicoEm(ontem);
        assertThatThrownBy(() -> bloqueioDiaService.travar(ontem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já começou");
    }

    @Test
    void destravarDiaQueJaPassou_recusa() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        servicoEm(ontem);
        assertThatThrownBy(() -> bloqueioDiaService.destravar(ontem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já começou");
    }

    @Test
    void travarDiaFuturo_continuaFuncionando() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        servicoEm(amanha);
        assertThat(bloqueioDiaService.travar(amanha)).allMatch(ServicoEscalado::isTravado);
        assertThat(bloqueioDiaService.destravar(amanha)).noneMatch(ServicoEscalado::isTravado);
    }
}
