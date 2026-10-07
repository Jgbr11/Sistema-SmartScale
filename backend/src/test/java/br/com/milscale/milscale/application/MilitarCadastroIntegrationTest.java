package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MilitarCadastroIntegrationTest {

    @Autowired private MilitarService militarService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PostoGraduacaoRepository postoRepository;
    @Autowired private SubunidadeRepository subunidadeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private DadosMilitar novo(String cpf, String nomeGuerra) {
        return new DadosMilitar("Novo " + nomeGuerra, nomeGuerra, cpf, null, null, null, null, null, null,
                postoRepository.findAll().get(0).getId(), subunidadeRepository.findAll().get(0).getId());
    }

    @Test
    void cadastrar_criaContaComLoginCpfESenhaTemporaria() {
        MilitarService.MilitarCadastrado r = militarService.cadastrar(novo("123.456.789-09", "Recem"));

        assertThat(r.militar().getCpf()).isEqualTo("12345678909");
        Usuario u = usuarioRepository.findByMilitar_Id(r.militar().getId()).orElseThrow();
        assertThat(u.getLogin()).isEqualTo("12345678909");
        assertThat(u.getPerfil().getNome()).isEqualTo("MILITAR_ESCALADO");
        assertThat(u.isSenhaTemporaria()).isTrue();
        assertThat(passwordEncoder.matches(r.senhaTemporaria(), u.getSenhaHash())).isTrue();
    }

    @Test
    void cadastrar_cpfDuplicado_recusaComMensagemClara() {
        assertThatThrownBy(() -> militarService.cadastrar(novo("00000000001", "Outro")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe outro militar cadastrado com esse CPF");
    }

    @Test
    void cadastrar_cpfComTamanhoErrado_recusa() {
        assertThatThrownBy(() -> militarService.cadastrar(novo("123", "Curto")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CPF precisa ter 11 números");
    }

    @Test
    void desligar_desativaAConta() {
        Long id = usuarioRepository.findByLogin("00000000004").orElseThrow().getMilitar().getId();
        militarService.desligar(id);
        assertThat(usuarioRepository.findByLogin("00000000004").orElseThrow().isAtivo()).isFalse();
    }
}
