package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"milscale.seed.demo=false",
        "spring.datasource.url=jdbc:h2:mem:seed_prod;MODE=MySQL;DATABASE_TO_LOWER=TRUE"})
@ActiveProfiles("test")
class SeedSemDemonstracaoIntegrationTest {

    @Autowired private MilitarRepository militarRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private PerfilAcessoRepository perfilAcessoRepository;

    @Test
    void criaSoDadosDeReferencia() {
        assertThat(tipoServicoRepository.count()).isEqualTo(12);
        assertThat(perfilAcessoRepository.count()).isEqualTo(4);
        assertThat(militarRepository.count()).isZero();
        assertThat(usuarioRepository.count()).isZero();
    }
}
