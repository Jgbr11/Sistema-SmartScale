package br.com.milscale.milscale;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FusoHorarioTest {

    @Test
    void aplicacaoRodaNoHorarioDeBrasiliaMesmoComJvmEmUtc() {
        assertThat(TimeZone.getDefault().getID()).isEqualTo("America/Sao_Paulo");
    }
}
