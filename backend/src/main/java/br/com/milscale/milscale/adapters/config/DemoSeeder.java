package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
@Order(2)
@ConditionalOnProperty(name = "milscale.seed.demo", havingValue = "true")
public class DemoSeeder implements CommandLineRunner {

    private static final String SENHA_DEMONSTRACAO = "milscale123";

    private static final String[] SOBRENOMES = {
            "Almeida", "Barreto", "Cordeiro", "Bueno", "Prado", "Ribas", "Duarte", "Teixeira", "Salles",
            "Andrade", "Farias", "Vaz", "Comita", "Lima", "Sato", "Braga", "Perceu", "Adelar", "Carvalho",
            "Reis", "Guilherme", "Cauan", "Silveira", "Ferraz", "Madeira", "Weslley", "Cruz", "Simas",
            "Amaral", "Maia", "Cunha", "Araujo", "Raimundo", "Salomao", "Battistella", "Rhuan", "Paris",
            "Carlos", "Silvestre", "Leonardo", "Otavio", "Gomes", "Rocha", "Pires", "Castro", "Moraes",
            "Freitas", "Nunes", "Ramos", "Correia", "Dias", "Peixoto", "Sales", "Xavier", "Bastos",
            "Coutinho", "Rezende", "Lacerda", "Vieira", "Pinheiro", "Tavares", "Guedes", "Assis", "Brito",
            "Monteiro", "Cavalcanti", "Siqueira", "Marques", "Azevedo", "Cardoso", "Pacheco", "Esteves",
            "Nogueira", "Zeni", "Menezes", "Jonas", "Bittencourt", "Ferreira", "Martins", "Alves", "Souza",
            "Barros", "Campos", "Costa", "Cunha Filho", "Delgado", "Espindola", "Fagundes", "Galvao",
            "Henriques", "Ibraim", "Junqueira", "Kalil", "Loureiro", "Magalhaes", "Neves", "Oliveira",
            "Paiva", "Quintana", "Ramalho", "Sarmento", "Torquato", "Uchoa", "Valadares", "Wanderley",
            "Yamamoto", "Zampieri", "Abreu", "Bandeira", "Carneiro", "Domingues", "Estrela", "Falcao"
    };

    private final MilitarRepository militarRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostoGraduacaoRepository postoRepository;
    private final SubunidadeRepository subunidadeRepository;
    private final QualificacaoRepository qualificacaoRepository;
    private final PerfilAcessoRepository perfilAcessoRepository;

    private final Map<Long, Set<String>> nomesUsadosPorPosto = new HashMap<>();
    private final Random rnd = new Random(42);

    public DemoSeeder(MilitarRepository militarRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                      PostoGraduacaoRepository postoRepository, SubunidadeRepository subunidadeRepository,
                      QualificacaoRepository qualificacaoRepository, PerfilAcessoRepository perfilAcessoRepository) {
        this.militarRepository = militarRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.postoRepository = postoRepository;
        this.subunidadeRepository = subunidadeRepository;
        this.qualificacaoRepository = qualificacaoRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (militarRepository.count() > 0) return;

        PostoGraduacao sdEv = posto("Sd EV"), sdEp = posto("Sd EP"), cb = posto("Cb"),
                sgt3 = posto("3 Sgt"), sgt2 = posto("2 Sgt"), ten = posto("Ten");
        Subunidade ccap = subunidade("CCAp"), cia1 = subunidade("1 Cia"), aprov = subunidade("Aprov");
        Qualificacao cfc = curso("CFC"), motorista = curso("Motorista");
        PerfilAcesso perfilMilitar = perfil("MILITAR_ESCALADO"), perfilSdEp = perfil("SD_EP_SARGENTEACAO"),
                perfilCabo = perfil("CABO_SARGENTEACAO"), perfilSargenteante = perfil("SARGENTEANTE");

        String senha = passwordEncoder.encode(SENHA_DEMONSTRACAO);
        int cpfSeq = 1;

        // ---- Contas de demonstração, uma por perfil ----
        cpfSeq = criarConta("Rafael", "Zeni", cpfSeq, sgt2, ccap, perfilSargenteante, senha);
        cpfSeq = criarConta("Ricardo", "Menezes", cpfSeq, cb, ccap, perfilCabo, senha);
        cpfSeq = criarConta("Thiago", "Cardoso", cpfSeq, sdEp, cia1, perfilSdEp, senha);
        cpfSeq = criarConta("Fernando", "Nogueira", cpfSeq, cb, cia1, perfilMilitar, senha);

        // ---- Efetivo ----
        cpfSeq += gerarComLogin(8, ten, ccap, cia1, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLogin(11, sgt3, ccap, cia1, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLogin(11, sgt2, ccap, cia1, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLoginSubunidadeFixa(11, List.of(sgt3, sgt2), aprov, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLogin(11, cb, ccap, cia1, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLoginSubunidadeFixa(8, List.of(cb), aprov, perfilMilitar, senha, cpfSeq).size();

        List<Militar> sdEpCfc = gerarComLogin(13, sdEp, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEpCfc.size();
        List<Militar> sdEpMotorista = gerarComLogin(13, sdEp, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEpMotorista.size();
        for (Militar m : sdEpCfc) vincularQualificacao(m, cfc);
        for (Militar m : sdEpMotorista) vincularQualificacao(m, motorista);

        cpfSeq += gerarComLogin(13, sdEp, ccap, cia1, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLoginSubunidadeFixa(13, List.of(sdEp), aprov, perfilMilitar, senha, cpfSeq).size();
        cpfSeq += gerarComLogin(72, sdEv, ccap, cia1, perfilMilitar, senha, cpfSeq).size();
        gerarComLoginSubunidadeFixa(13, List.of(sdEv), aprov, perfilMilitar, senha, cpfSeq);
    }

    // ---- Referências ----
    private PostoGraduacao posto(String sigla) { return postoRepository.findBySigla(sigla).orElseThrow(); }
    private Subunidade subunidade(String sigla) { return subunidadeRepository.findBySigla(sigla).orElseThrow(); }
    private Qualificacao curso(String nome) { return qualificacaoRepository.findByNome(nome).orElseThrow(); }
    private PerfilAcesso perfil(String nome) { return perfilAcessoRepository.findByNome(nome).orElseThrow(); }

    // ---- Geração de militares ----
    private String sobrenomeUnicoParaPosto(PostoGraduacao posto, int indiceInicial) {
        Set<String> usados = nomesUsadosPorPosto.computeIfAbsent(posto.getId(), k -> new HashSet<>());
        for (int tentativa = 0; tentativa < SOBRENOMES.length; tentativa++) {
            String candidato = SOBRENOMES[(indiceInicial + tentativa) % SOBRENOMES.length];
            if (!usados.contains(candidato)) {
                usados.add(candidato);
                return candidato;
            }
        }
        String comSufixo = SOBRENOMES[indiceInicial % SOBRENOMES.length] + " " + (usados.size() + 1);
        usados.add(comSufixo);
        return comSufixo;
    }

    private void vincularQualificacao(Militar militar, Qualificacao q) {
        militar.getQualificacoes().add(q);
        militarRepository.save(militar);
    }

    private List<Militar> gerarComLogin(int quantidade, PostoGraduacao posto, Subunidade a, Subunidade b,
                                        PerfilAcesso perfil, String senhaHash, int cpfInicial) {
        List<Militar> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            String sobrenome = sobrenomeUnicoParaPosto(posto, cpfInicial + i);
            String cpf = String.format("%011d", cpfInicial + i + 1000);
            Subunidade sub = (i % 2 == 0) ? a : b;
            lista.add(criarMilitarComLogin(sobrenome, cpf, posto, sub, perfil, senhaHash));
        }
        return lista;
    }

    private List<Militar> gerarComLoginSubunidadeFixa(int quantidade, List<PostoGraduacao> postos, Subunidade sub,
                                                      PerfilAcesso perfil, String senhaHash, int cpfInicial) {
        List<Militar> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            PostoGraduacao posto = postos.get(i % postos.size());
            String sobrenome = sobrenomeUnicoParaPosto(posto, cpfInicial + i + 30);
            String cpf = String.format("%011d", cpfInicial + i + 1000);
            lista.add(criarMilitarComLogin(sobrenome, cpf, posto, sub, perfil, senhaHash));
        }
        return lista;
    }

    private Militar criarMilitarComLogin(String sobrenome, String cpf, PostoGraduacao posto, Subunidade sub,
                                         PerfilAcesso perfil, String senhaHash) {
        Militar m = militarRepository.save(Militar.builder()
                .nomeCompleto("Fulano " + sobrenome)
                .nomeGuerra(sobrenome)
                .cpf(cpf)
                .posto(posto)
                .subunidade(sub)
                .dataNascimento(gerarDataNascimento(posto))
                .numeroRegistro(gerarNumeroRegistro())
                .fusex(gerarFusex())
                .build());
        usuarioRepository.save(Usuario.builder().militar(m).perfil(perfil).login(cpf).senhaHash(senhaHash).build());
        return m;
    }

    private int criarConta(String nomeCompleto, String nomeGuerra, int cpfSeq, PostoGraduacao posto,
                           Subunidade subunidade, PerfilAcesso perfil, String senhaHash) {
        String cpf = String.format("%011d", cpfSeq);
        Militar militar = militarRepository.save(Militar.builder()
                .nomeCompleto(nomeCompleto + " " + nomeGuerra).nomeGuerra(nomeGuerra).cpf(cpf)
                .posto(posto).subunidade(subunidade)
                .dataNascimento(gerarDataNascimento(posto))
                .numeroRegistro(gerarNumeroRegistro())
                .fusex(gerarFusex())
                .build());
        usuarioRepository.save(Usuario.builder().militar(militar).perfil(perfil).login(cpf).senhaHash(senhaHash).build());
        nomesUsadosPorPosto.computeIfAbsent(posto.getId(), k -> new HashSet<>()).add(nomeGuerra);
        return cpfSeq + 1;
    }

    // ---- Dados pessoais plausíveis ----
    private LocalDate gerarDataNascimento(PostoGraduacao posto) {
        int idadeMin, idadeMax;
        switch (posto.getSigla()) {
            case "Sd EV" -> { idadeMin = 18; idadeMax = 19; }
            case "Sd EP" -> { idadeMin = 18; idadeMax = 26; }
            case "Cb" -> { idadeMin = 20; idadeMax = 30; }
            case "3 Sgt" -> { idadeMin = 19; idadeMax = 28; }
            case "2 Sgt" -> { idadeMin = 25; idadeMax = 38; }
            case "Ten" -> { idadeMin = 24; idadeMax = 30; }
            default -> { idadeMin = 20; idadeMax = 35; }
        }
        int idade = idadeMin + rnd.nextInt(idadeMax - idadeMin + 1);
        return LocalDate.now().minusYears(idade).minusDays(rnd.nextInt(365));
    }

    private String gerarNumeroRegistro() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String gerarFusex() {
        return String.format("%03d-%02d", rnd.nextInt(1000), rnd.nextInt(100));
    }
}
