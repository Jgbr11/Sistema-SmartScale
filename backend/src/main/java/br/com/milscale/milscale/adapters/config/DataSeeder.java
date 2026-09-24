package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Carga inicial de dominio para desenvolvimento.
 *
 * Ajustada para bater com o Boletim Interno real do 5º Batalhão de
 * Suprimento (BI nº 107, 12/06/2026):
 *  - "Recruta" e "Soldado EV" são a mesma graduação — ficou só "Sd EV".
 *  - Hierarquia: Sd EV < Sd EP < Cabo < 3º Sgt < 2º Sgt < Tenente.
 *  - Plantão ao Alojamento, Rancheiro de Dia e Guardas ao Quartel são
 *    tirados por Sd EV — não por Sd EP.
 *  - Cabo de Dia é tirado por Sd EP com o curso CFC.
 *
 * Subunidades: só CCAp, 1ª Cia e Aprov (Aprovisionamento) - não usamos
 * 2ª Cia neste sistema. "Aprov" NÃO é um curso: é a seção onde a pessoa
 * está lotada. Quem está lotado no Aprov é quem pode tirar Rancheiro de
 * Dia, Cozinheiro de Dia e Graduado do Rancho - a elegibilidade dessas
 * três funções agora é posto + SUBUNIDADE (não mais qualificação).
 *
 * Login: todo militar cadastrado ganha uma conta com login = nome de
 * guerra em minúsculas (sem espaço/acento), senha padrão "milscale123".
 * Em caso de nome de guerra repetido, entra um sufixo numérico.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final SubunidadeRepository subunidadeRepository;
    private final PostoGraduacaoRepository postoRepository;
    private final QualificacaoRepository qualificacaoRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final RegraEscalaRepository regraEscalaRepository;
    private final PerfilAcessoRepository perfilAcessoRepository;
    private final MilitarRepository militarRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(SubunidadeRepository subunidadeRepository, PostoGraduacaoRepository postoRepository,
                       QualificacaoRepository qualificacaoRepository, TipoServicoRepository tipoServicoRepository,
                       RequisitoServicoRepository requisitoServicoRepository, RegraEscalaRepository regraEscalaRepository,
                       PerfilAcessoRepository perfilAcessoRepository, MilitarRepository militarRepository,
                       UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.subunidadeRepository = subunidadeRepository;
        this.postoRepository = postoRepository;
        this.qualificacaoRepository = qualificacaoRepository;
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.regraEscalaRepository = regraEscalaRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
        this.militarRepository = militarRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
            // Reforço pra sobrar sobrenome suficiente pro maior grupo (Sd EV) nunca repetir
            // dentro do mesmo posto (RN - nome de guerra nao pode repetir no mesmo posto).
            "Barros", "Campos", "Costa", "Cunha Filho", "Delgado", "Espindola", "Fagundes", "Galvao",
            "Henriques", "Ibraim", "Junqueira", "Kalil", "Loureiro", "Magalhaes", "Neves", "Oliveira",
            "Paiva", "Quintana", "Ramalho", "Sarmento", "Torquato", "Uchoa", "Valadares", "Wanderley",
            "Yamamoto", "Zampieri", "Abreu", "Bandeira", "Carneiro", "Domingues", "Estrela", "Falcao"
    };

    /** RN - nome de guerra nao pode repetir dentro do MESMO posto (postos diferentes podem repetir). */
    private final Map<Long, Set<String>> nomesUsadosPorPosto = new HashMap<>();
    private final Random rnd = new Random(42);

    private String sobrenomeUnicoParaPosto(PostoGraduacao posto, int indiceInicial) {
        Set<String> usados = nomesUsadosPorPosto.computeIfAbsent(posto.getId(), k -> new HashSet<>());
        for (int tentativa = 0; tentativa < SOBRENOMES.length; tentativa++) {
            String candidato = SOBRENOMES[(indiceInicial + tentativa) % SOBRENOMES.length];
            if (!usados.contains(candidato)) {
                usados.add(candidato);
                return candidato;
            }
        }
        // Lista esgotada pra esse posto (nao deveria acontecer com 124 sobrenomes
        // disponiveis) - degrada pra um sufixo numerico em vez de quebrar o seed.
        String base = SOBRENOMES[indiceInicial % SOBRENOMES.length];
        String comSufixo = base + " " + (usados.size() + 1);
        usados.add(comSufixo);
        return comSufixo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (subunidadeRepository.count() > 0) return; // já semeado

        Subunidade ccap = subunidadeRepository.save(Subunidade.builder().sigla("CCAp").nome("Companhia de Comando e Apoio").build());
        Subunidade cia1 = subunidadeRepository.save(Subunidade.builder().sigla("1 Cia").nome("Primeira Companhia").build());
        Subunidade aprov = subunidadeRepository.save(Subunidade.builder().sigla("Aprov").nome("Aprovisionamento").build());

        PostoGraduacao rct_sdEv = postoRepository.save(PostoGraduacao.builder().sigla("Sd EV").descricao("Soldado EV").nivelHierarquico(1).build());
        PostoGraduacao sdEp = postoRepository.save(PostoGraduacao.builder().sigla("Sd EP").descricao("Soldado EP").nivelHierarquico(2).build());
        PostoGraduacao cb = postoRepository.save(PostoGraduacao.builder().sigla("Cb").descricao("Cabo").nivelHierarquico(3).build());
        PostoGraduacao sgt3 = postoRepository.save(PostoGraduacao.builder().sigla("3 Sgt").descricao("Terceiro-Sargento").nivelHierarquico(4).build());
        PostoGraduacao sgt2 = postoRepository.save(PostoGraduacao.builder().sigla("2 Sgt").descricao("Segundo-Sargento").nivelHierarquico(5).build());
        PostoGraduacao ten = postoRepository.save(PostoGraduacao.builder().sigla("Ten").descricao("Tenente").nivelHierarquico(6).build());
        PostoGraduacao sdEv = rct_sdEv;

        // Só cursos de verdade sobram aqui - Rancho virou lotação (Aprov), não curso.
        Qualificacao cfc = qualificacaoRepository.save(Qualificacao.builder().nome("CFC").descricao("Curso de Formação de Cabos — preparação para Cabo da Guarda").build());
        Qualificacao motorista = qualificacaoRepository.save(Qualificacao.builder().nome("Motorista").descricao("Habilitação de motorista militar").build());

        record TipoDef(String nome, int efetivo) {}
        List<TipoDef> defs = List.of(
                new TipoDef("Oficial de Dia", 1),
                new TipoDef("Graduado de Dia", 1),
                new TipoDef("Comandante da Guarda", 1),
                new TipoDef("Cabo da Guarda", 1),
                new TipoDef("Cabo de Dia", 1),
                new TipoDef("Motorista de Dia", 1),
                new TipoDef("Monitoramento", 3),
                new TipoDef("Plantão ao Alojamento", 3),
                new TipoDef("Graduado do Rancho", 1),
                new TipoDef("Cozinheiro de Dia", 1),
                new TipoDef("Rancheiro de Dia", 2),
                new TipoDef("Guardas ao Quartel", 9)
        );
        Map<String, TipoServico> tipos = new LinkedHashMap<>();
        for (TipoDef d : defs) {
            TipoServico t = tipoServicoRepository.save(TipoServico.builder().nome(d.nome()).efetivoNecessario(d.efetivo()).build());
            tipos.put(d.nome(), t);
            regraEscalaRepository.save(RegraEscala.builder().tipoServico(t).intervaloMinimo(3).diasFolga(1).maxServicosMes(10).build());
        }

        // Elegibilidade (RF06)
        salvarRequisito(tipos.get("Oficial de Dia"), ten, null, null);
        // Graduado de Dia / Comandante da Guarda / Cabo da Guarda: quem esta
        // lotado no Aprovisionamento NUNCA entra nessas (so serve o rancho) -
        // por isso a exclusao vai direto no unico requisito de cada um, sem
        // deixar nenhuma versão "aberta" duplicada que anularia a exclusão.
        salvarRequisitoExcluindoAprov(tipos.get("Graduado de Dia"), sgt3, aprov);
        salvarRequisitoExcluindoAprov(tipos.get("Graduado de Dia"), sgt2, aprov);
        // Comandante da Guarda e privativo do 3o Sgt - o 2o Sgt e mais antigo
        // e so tira Graduado de Dia, nao Comandante da Guarda.
        salvarRequisitoExcluindoAprov(tipos.get("Comandante da Guarda"), sgt3, aprov);
        salvarRequisitoExcluindoAprov(tipos.get("Cabo da Guarda"), cb, aprov);
        salvarRequisito(tipos.get("Cabo de Dia"), sdEp, null, cfc);
        salvarRequisito(tipos.get("Motorista de Dia"), sdEp, null, motorista);
        // Monitoramento e do Sd EP em geral, mas quem tem CFC ou Motorista ja
        // tem funcao especifica, e quem esta no Aprov so serve o rancho -
        // as duas exclusoes juntas nesse requisito.
        requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipos.get("Monitoramento")).posto(sdEp)
                .qualificacoesExcluidas(new java.util.HashSet<>(java.util.List.of(cfc, motorista)))
                .subunidadeExcluida(aprov)
                .build());
        salvarRequisitoExcluindoAprov(tipos.get("Plantão ao Alojamento"), sdEv, aprov);
        // Estes três exigem lotação no Aprov (não é curso, é a seção da pessoa):
        salvarRequisito(tipos.get("Graduado do Rancho"), sgt3, aprov, null);
        salvarRequisito(tipos.get("Graduado do Rancho"), sgt2, aprov, null);
        salvarRequisito(tipos.get("Cozinheiro de Dia"), sdEp, aprov, null);
        salvarRequisito(tipos.get("Cozinheiro de Dia"), cb, aprov, null);
        salvarRequisito(tipos.get("Rancheiro de Dia"), sdEv, aprov, null);
        salvarRequisitoExcluindoAprov(tipos.get("Guardas ao Quartel"), sdEv, aprov);

        PerfilAcesso perfilMilitar = perfilAcessoRepository.save(PerfilAcesso.builder().nome("MILITAR_ESCALADO")
                .descricao("Consulta a própria escala e abre solicitações de permuta, dispensa e férias").build());
        PerfilAcesso perfilSdEp = perfilAcessoRepository.save(PerfilAcesso.builder().nome("SD_EP_SARGENTEACAO")
                .descricao("Consulta a escala completa, as solicitações e o histórico de serviços").build());
        PerfilAcesso perfilCabo = perfilAcessoRepository.save(PerfilAcesso.builder().nome("CABO_SARGENTEACAO")
                .descricao("Mantém cadastros e afastamentos, gera a escala e faz a triagem das solicitações").build());
        PerfilAcesso perfilSargenteante = perfilAcessoRepository.save(PerfilAcesso.builder().nome("SARGENTEANTE")
                .descricao("Edita a escala, mantém regras e tipos de serviço, trava serviços, publica a escala e autoriza as solicitações").build());

        String senha = passwordEncoder.encode("milscale123");
        int cpfSeq = 1;

        // Contas de demonstração - login pelo nome de guerra, igual a todo mundo
        cpfSeq = criarConta("Rafael", "Zeni", cpfSeq, sgt2, ccap, perfilSargenteante, senha);
        cpfSeq = criarConta("Ricardo", "Menezes", cpfSeq, cb, ccap, perfilCabo, senha);
        cpfSeq = criarConta("Thiago", "Cardoso", cpfSeq, sdEp, cia1, perfilSdEp, senha);
        cpfSeq = criarConta("Fernando", "Nogueira", cpfSeq, cb, cia1, perfilMilitar, senha);

        Random rnd = this.rnd;

        // Tenente — 1/dia (Oficial de Dia). 5 pessoas era exatamente o minimo
        // matematico pro intervalo de 3 dias de folga (4x a demanda) - sem
        // NENHUMA folga de reserva, entao um afastamento nao tinha pra quem
        // ir. Subiu pra 8 (8x a demanda) pra sobrar gente disponivel de verdade.
        List<Militar> tenentes = gerarComLogin(8, ten, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += tenentes.size();

        // Sargentos gerais (Graduado de Dia, Comandante da Guarda) — não são do Aprov
        List<Militar> sgt3Geral = gerarComLogin(11, sgt3, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sgt3Geral.size();
        List<Militar> sgt2Geral = gerarComLogin(11, sgt2, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sgt2Geral.size();

        // Sargentos do Aprov — só eles tiram Graduado do Rancho
        List<Militar> sgtAprov = gerarComLoginSubunidadeFixa(11, List.of(sgt3, sgt2), aprov, perfilMilitar, senha, cpfSeq);
        cpfSeq += sgtAprov.size();

        // Cabos gerais — 1/dia (Cabo da Guarda) → 6
        List<Militar> cabosGeral = gerarComLogin(11, cb, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += cabosGeral.size();

        // Cabos do Aprov — reforço opcional do Cozinheiro de Dia
        List<Militar> cabosAprov = gerarComLoginSubunidadeFixa(8, List.of(cb), aprov, perfilMilitar, senha, cpfSeq);
        cpfSeq += cabosAprov.size();

        // Sd EP gerais — Cabo de Dia(CFC) + Motorista de Dia(Motorista) + Monitoramento
        List<Militar> sdEpCfc = gerarComLogin(13, sdEp, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEpCfc.size();
        List<Militar> sdEpMotorista = gerarComLogin(13, sdEp, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEpMotorista.size();
        for (Militar m : sdEpCfc) vincularQualificacao(m, cfc);
        for (Militar m : sdEpMotorista) vincularQualificacao(m, motorista);

        // Sd EP "geral" — sem CFC, sem Motorista, sem Aprov. So esse grupo
        // fica elegivel pro Monitoramento (quem tem CFC/Motorista ja tem
        // funcao propria, e quem e do Aprov so serve o rancho).
        List<Militar> sdEpGeral = gerarComLogin(13, sdEp, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEpGeral.size();

        // Sd EP do Aprov — Cozinheiro de Dia
        List<Militar> sdEpAprov = gerarComLoginSubunidadeFixa(13, List.of(sdEp), aprov, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEpAprov.size();

        // Sd EV gerais — Plantão(3-6) + Guardas ao Quartel(9)
        // Sd EV geral — Plantão(3-6) + Guardas ao Quartel(9) = 12/dia. Com 48
        // pessoas era exatamente 4x a demanda (o minimo matematico pro
        // intervalo de 3 dias de folga), sem folga de reserva pra afastamento
        // nenhum. Subiu pra 72 (6x) pra sobrar gente disponivel de verdade.
        List<Militar> sdEvGeral = gerarComLogin(72, sdEv, ccap, cia1, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEvGeral.size();

        // Sd EV do Aprov — Rancheiro de Dia
        List<Militar> sdEvAprov = gerarComLoginSubunidadeFixa(13, List.of(sdEv), aprov, perfilMilitar, senha, cpfSeq);
        cpfSeq += sdEvAprov.size();
    }

    private void vincularQualificacao(Militar militar, Qualificacao q) {
        militar.getQualificacoes().add(q);
        militarRepository.save(militar);
    }

    /** Gera N militares alternando entre CCAp e 1ª Cia, com login proprio pelo nome de guerra. */
    private List<Militar> gerarComLogin(int quantidade, PostoGraduacao posto, Subunidade a, Subunidade b,
                                         PerfilAcesso perfil, String senhaHash, int cpfInicial) {
        List<Militar> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            String sobrenome = sobrenomeUnicoParaPosto(posto, cpfInicial + i);
            String cpf = String.format("%011d", cpfInicial + i + 1000);
            Subunidade sub = (i % 2 == 0) ? a : b;
            Militar m = criarMilitarComLogin(sobrenome, cpf, posto, sub, perfil, senhaHash);
            lista.add(m);
        }
        return lista;
    }

    /** Gera N militares (posto sorteado entre os informados) todos numa subunidade fixa (ex.: Aprov). */
    private List<Militar> gerarComLoginSubunidadeFixa(int quantidade, List<PostoGraduacao> postos, Subunidade sub,
                                                        PerfilAcesso perfil, String senhaHash, int cpfInicial) {
        List<Militar> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            PostoGraduacao posto = postos.get(i % postos.size());
            String sobrenome = sobrenomeUnicoParaPosto(posto, cpfInicial + i + 30);
            String cpf = String.format("%011d", cpfInicial + i + 1000);
            Militar m = criarMilitarComLogin(sobrenome, cpf, posto, sub, perfil, senhaHash);
            lista.add(m);
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
        String login = cpf; // RF01 - login por CPF
        usuarioRepository.save(Usuario.builder().militar(m).perfil(perfil).login(login).senhaHash(senhaHash).build());
        return m;
    }

    /**
     * Data de nascimento plausivel por posto, baseada em como a carreira
     * militar de verdade progride (pesquisado): Sd EV é o serviço militar
     * obrigatório (incorpora aos 18); Sd EP é carreira voluntária; Cabo
     * já passou pelo CFC; os Sgt vêm da ESA (entrada 17-24 + curso); o
     * Tenente já passou por EsPCEx+AMAN (5 anos) e um tempo como 2º Ten.
     */
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
        LocalDate hoje = LocalDate.now();
        LocalDate nascimentoBase = hoje.minusYears(idade);
        // varia o dia dentro do ano pra nao nascer todo mundo dia 1 de janeiro
        return nascimentoBase.minusDays(rnd.nextInt(365));
    }

    private String gerarNumeroRegistro() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String gerarFusex() {
        return String.format("%03d-%02d", rnd.nextInt(1000), rnd.nextInt(100));
    }

    private void salvarRequisito(TipoServico tipo, PostoGraduacao posto, Subunidade subunidade, Qualificacao qualificacao) {
        requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipo).posto(posto).subunidade(subunidade).qualificacao(qualificacao).build());
    }

    /** Variante que abre pro posto em geral, MENOS quem esta lotado na subunidade informada
     *  (ex.: qualquer 3 Sgt tira Comandante da Guarda, menos os do Aprovisionamento). */
    private void salvarRequisitoExcluindoAprov(TipoServico tipo, PostoGraduacao posto, Subunidade subunidadeExcluida) {
        requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipo).posto(posto).subunidadeExcluida(subunidadeExcluida).build());
    }

    private int criarConta(String nomeCompleto, String nomeGuerra, int cpfSeq, PostoGraduacao posto,
                            Subunidade subunidade, PerfilAcesso perfil, String senhaHash) {
        Militar militar = militarRepository.save(Militar.builder()
                .nomeCompleto(nomeCompleto + " " + nomeGuerra).nomeGuerra(nomeGuerra).cpf(String.format("%011d", cpfSeq))
                .posto(posto).subunidade(subunidade)
                .dataNascimento(gerarDataNascimento(posto))
                .numeroRegistro(gerarNumeroRegistro())
                .fusex(gerarFusex())
                .build());
        String login = String.format("%011d", cpfSeq); // RF01 - login por CPF
        usuarioRepository.save(Usuario.builder().militar(militar).perfil(perfil).login(login).senhaHash(senhaHash).build());
        // Registra o nome pra ninguem mais desse posto ser gerado com o mesmo depois.
        nomesUsadosPorPosto.computeIfAbsent(posto.getId(), k -> new HashSet<>()).add(nomeGuerra);
        return cpfSeq + 1;
    }
}
