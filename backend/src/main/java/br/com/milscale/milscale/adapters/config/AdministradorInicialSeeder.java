package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.PerfilAcessoRepository;
import br.com.milscale.milscale.adapters.persistence.PostoGraduacaoRepository;
import br.com.milscale.milscale.adapters.persistence.SubunidadeRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
public class AdministradorInicialSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicialSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final MilitarRepository militarRepository;
    private final PostoGraduacaoRepository postoRepository;
    private final SubunidadeRepository subunidadeRepository;
    private final PerfilAcessoRepository perfilAcessoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String cpf;
    private final String senha;

    public AdministradorInicialSeeder(UsuarioRepository usuarioRepository, MilitarRepository militarRepository,
                                      PostoGraduacaoRepository postoRepository, SubunidadeRepository subunidadeRepository,
                                      PerfilAcessoRepository perfilAcessoRepository, PasswordEncoder passwordEncoder,
                                      @Value("${milscale.admin.cpf:}") String cpf,
                                      @Value("${milscale.admin.senha:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.militarRepository = militarRepository;
        this.postoRepository = postoRepository;
        this.subunidadeRepository = subunidadeRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
        this.passwordEncoder = passwordEncoder;
        this.cpf = cpf.replaceAll("\\D", "");
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;
        if (cpf.length() != 11 || senha.length() < 6) {
            log.warn("Nenhum usuario cadastrado. Defina MILSCALE_ADMIN_CPF e MILSCALE_ADMIN_SENHA para criar o primeiro Sargenteante.");
            return;
        }
        Militar admin = militarRepository.save(Militar.builder()
                .nomeCompleto("Administrador do Sistema").nomeGuerra("Admin").cpf(cpf)
                .posto(postoRepository.findBySigla("2 Sgt").orElseThrow())
                .subunidade(subunidadeRepository.findBySigla("CCAp").orElseThrow())
                .build());
        usuarioRepository.save(Usuario.builder()
                .militar(admin).login(cpf)
                .perfil(perfilAcessoRepository.findByNome("SARGENTEANTE").orElseThrow())
                .senhaHash(passwordEncoder.encode(senha)).senhaTemporaria(true)
                .build());
        log.info("Primeiro Sargenteante criado para o CPF informado em MILSCALE_ADMIN_CPF.");
    }
}
