# MilScale — primeira fatia implementada

Sistema de escala de serviço militar. Este pacote contém a primeira fatia
funcional, de ponta a ponta: login → cadastro de militares → tipos de
serviço → regras da escala → gerar e publicar a escala → cada pessoa
consulta a própria escala.

## Arquitetura

Monólito hexagonal (Ports & Adapters), conforme o documento **"SMART
SCALE - ATIVOS REUTILIZÁVEIS, REQUISITOS E ARQUITETURA v2"**:

```
backend/src/main/java/br/com/milscale/
  core/domain/        ← NÚCLEO REUTILIZÁVEL (Linha de Produto de Software)
    PessoaEscalada.java        interface genérica (RF04)
    TipoTurno.java              interface genérica (RF06)
    CriterioDeOrdenacao.java    ponto de variação (RN01)
    MotorDeRodizio.java         algoritmo de fila/rodízio (RN01/RN05/RN06/RN15/RN20)

  milscale/domain/     ← ESPECIALIZAÇÃO MILSCALE
    Militar.java                implements PessoaEscalada
    TipoServico.java             implements TipoTurno
    CriterioOrdenacaoMilitar.java "maior nº de dias sem serviço primeiro"
    RegraEscala.java, RequisitoServico.java, Escala.java, ServicoEscalado.java,
    Afastamento.java, PostoGraduacao.java, Subunidade.java, Qualificacao.java,
    Usuario.java, PerfilAcesso.java

  milscale/application/    casos de uso (services)
  milscale/adapters/web/   controllers REST
  milscale/adapters/persistence/   repositórios Spring Data JPA
  milscale/adapters/config/        segurança, seed de dados
```

**Por que isso importa para reuso futuro:** o `MotorDeRodizio` não sabe o
que é um "militar" — ele só enxerga `PessoaEscalada` e `TipoTurno`. Um
futuro produto da linha (ex.: escala hospitalar) reaproveita esse motor
inteiro, criando apenas as suas próprias implementações dessas duas
interfaces e o seu próprio `CriterioDeOrdenacao` — sem tocar em uma linha
do núcleo.

Toda essa separação está comentada diretamente no código-fonte, marcando
com `NÚCLEO REUTILIZÁVEL (LPS)` o que é genérico e `Especialização
MilScale` o que é específico deste produto.

## Como rodar

### Backend
Requer Java 21+ e Maven.

```bash
cd backend
mvn spring-boot:run
```

Sobe em `http://localhost:8080`. Usa H2 em modo de compatibilidade MySQL,
gravado em `backend/data/` — não precisa de Docker nem MySQL instalado.
Na primeira execução, popula automaticamente o banco (postos, subunidades,
tipos de serviço, regras, 4 contas de demonstração + efetivo extra).

Para usar MySQL de verdade (ex.: em produção), rode com
`--spring.profiles.active=mysql` e configure as variáveis de ambiente
`DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (ver
`application-mysql.properties`). O script de schema já enviado por vocês
(`SMART SCALE - DB Modelo Lógico v2.sql`) é compatível.

**Testes automatizados** (H2 em memória, separado do banco de dev):
```bash
cd backend
mvn test
```
Roda sozinho também toda vez que você faz `mvn package` — não precisa
lembrar de rodar à parte antes de entregar uma versão.

**Lembrete de serviço por email** vem desligado por padrão (sem
credenciais de SMTP reais configuradas neste projeto). Pra ligar de
verdade:
```bash
export MAIL_HOST=smtp.exemplo.com
export MAIL_USER=seu-usuario
export MAIL_PASSWORD=sua-senha
export MILSCALE_EMAIL_HABILITADO=true
```
Sem isso, o sistema só registra no log o que teria enviado — não
quebra, só não envia de verdade.

### Frontend
Requer Node 18+.

```bash
cd frontend
npm install
npm run dev
```

Sobe em `http://localhost:5173` e já aponta para o backend em `:8080`.

### Docker Compose (MySQL de verdade, sem instalar nada além do Docker)
Requer Docker e Docker Compose.

```bash
docker compose up --build
```

Sobe os 3 serviços juntos: MySQL 8 (`:3306`, dados persistidos num
volume — sobrevive a `docker compose down` sem `-v`), backend Spring
Boot (`:8080`, perfil `mysql`, espera o MySQL responder de verdade
antes de subir) e frontend servido por nginx (`:5173`). Primeira
subida popula o banco automaticamente igual ao H2 do dev. Pra recomeçar
do zero: `docker compose down -v`.

**Testado de verdade** (não só escrito e torcido pra funcionar): rodei
o `docker compose up` completo com os 3 serviços, confirmei login,
geração de escala (zero vaga em aberto) e o roteamento do React
funcionando através das portas publicadas — ver a seção "Docker
Compose com MySQL real" mais abaixo pra detalhes de como validei isso
e um bug real que só apareceu nesse teste.

### Contas de demonstração
Senha igual para todas: `milscale123`. **Login agora é por CPF** (RF01,
mudou nesta rodada — antes era nome de guerra). Todo militar cadastrado
tem conta própria, login = CPF puro (11 dígitos, aceita com ou sem
pontuação no campo de login). As 4 contas abaixo são as de referência
dos perfis especiais — dá pra logar com o CPF de qualquer um dos ~200
militares gerados.

| CPF (login) | Perfil |
|---|---|
| `000.000.000-01` | Sargenteante — acesso completo |
| `000.000.000-02` | Cabo da Sargenteação — cadastros, gera escala |
| `000.000.000-03` | Sd EP da Sargenteação — só consulta |
| `000.000.000-04` | Militar Escalado — só a própria escala |
| *(qualquer CPF gerado)* | Militar Escalado por padrão |

## O que já funciona

- Login com sessão real (BCrypt + Spring Security), 401 sem sessão
- Controle de acesso por perfil no **servidor** (não só escondido na
  tela) — testado: Cabo tenta publicar escala → 403; Sargenteante → 200
- CRUD de militares (RF04)
- CRUD de tipos de serviço, privativo do Sargenteante (RF06, RN11) —
  efetivo necessário editável por tela
- Regras da escala editáveis, privativo do Sargenteante (RF07, RN11)
- Geração automática de escala (RF08) respeitando:
  - RN05 — ninguém ocupa duas vagas no mesmo dia
  - RN06 — intervalo mínimo de 3 dias entre serviços (rodízio 3-por-1:
    folga, folga, folga, serviço)
  - RN15 — quem está afastado não é escalado
  - RN01 — critério de fila (maior nº de dias sem servir primeiro)
- Publicação da escala, privativo do Sargenteante (RF11, RN14)
- Cada pessoa consulta a própria escala em formato de calendário (RF13)
- **Escala do mês em formato de calendário de verdade** — clique num
  dia pra ver só os serviços daquele dia (sem lista dos 40 primeiros)
- **Catálogo de qualificações (RF05)** — Sargenteante mantém os cursos
  (CFC, Motorista...); Cabo ou Sargenteante vinculam um curso a uma
  pessoa direto na tela de Militares. Isso já entra de verdade no
  filtro de elegibilidade da geração (não é só cadastro solto)
- **Missões e dispensas (RF26)** — Cabo ou Sargenteante registram
  afastamento (missão, dispensa, férias, licença); testei de ponta a
  ponta: quem está afastado não aparece na geração de escala nesse
  período, mesmo que fosse o único elegível pra função naquele dia
- **Bloqueio de dias (RF12/RN04)** — integrado no calendário de Escala
  do Mês: Sargenteante trava/destrava um dia inteiro (todos os
  serviços daquele dia de uma vez). Cabo tenta travar → 403
- **Trocas de serviço (RF15/RF17/RF18/RF19)** — fluxo completo: pedir
  → triagem do Cabo → autorização do Sargenteante → a troca é
  efetivada de verdade no serviço escalado. Testei de ponta a ponta,
  incluindo tentar travar o dia DEPOIS que o Cabo já aprovou mas ANTES
  do Sargenteante autorizar — a autorização é bloqueada corretamente
  (RN04). Também testei: pedir troca de serviço de outra pessoa (400),
  Sd EP tentando fazer triagem (403), autorizar duas vezes a mesma
  solicitação (400), cancelar depois de já decidida (400)
- **Feriados** — CRUD simples (Sargenteante mantém, todo mundo
  consulta), usado pelo peso de feriado nas regras da escala
- **"Último serviço" na tela de Militares** — mostra sempre "último
  serviço há X dias", sem misturar com "próximo serviço" — pensado
  pra sargenteação bater o olho e conferir rápido se a escala bate com
  o que o sistema calculou
- **Tratamento de erro centralizado** — data inválida, CPF duplicado,
  id inexistente e corpo mal formado agora voltam com status HTTP e
  mensagem claros (400/404/409) em vez de erro 500 cru. Testei os 4
  casos de propósito pra forçar quebra
- **Validação de datas invertidas** — gerar escala ou registrar
  afastamento com data final antes da inicial agora é rejeitado com
  mensagem clara (antes criava um registro vazio/inválido em silêncio)

## Base de dados (efetivo semeado)

Ajustada para bater com o Boletim Interno real do 5º Batalhão de
Suprimento que vocês me mandaram (BI nº 107, 12/06/2026):

- **Hierarquia**: Sd EV < Sd EP < Cabo < 3º Sgt < 2º Sgt < Tenente
  ("Recruta" e "Soldado EV" viraram um posto só, `Sd EV`)
- **Subunidades**: só CCAp, 1ª Cia e **Aprov** (Aprovisionamento) — não
  usamos 2ª Cia. Aprov não é um curso, é a seção onde a pessoa está
  lotada.
- **Elegibilidade por tipo de serviço**, conforme o boletim:
  - Oficial de Dia → Tenente
  - Graduado de Dia / Comandante da Guarda → 2º/3º Sgt (qualquer lotação)
  - Cabo da Guarda → Cabo (privativo)
  - Cabo de Dia → Sd EP com curso CFC
  - Motorista de Dia → Sd EP com curso Motorista
  - Monitoramento → Sd EP
  - **Graduado do Rancho** → 2º/3º Sgt **lotado no Aprov**
  - **Cozinheiro de Dia** → Sd EP **ou** Cabo, **lotados no Aprov**
  - **Rancheiro de Dia** → Sd EV **lotado no Aprov**
  - Plantão ao Alojamento, Guardas ao Quartel → Sd EV (qualquer lotação)
- **Guardas ao Quartel** tem efetivo fixo de 9 (conforme o boletim)
- **Plantão ao Alojamento** tem efetivo padrão de 3, editável na tela
  Tipos de Serviço entre 3 e 6 (padrão da OM)
- **Efetivo cadastrado**: ~116 militares (5 Tenentes, 19 Sargentos, 14
  Cabos, 41 Sd EP, 64 Sd EV — incluindo os subgrupos lotados no Aprov)
  — testado com geração de mês inteiro: **zero vaga em aberto, zero
  violação de RN05/RN06**
- **Login**: cada militar tem uma conta própria, login = nome de guerra
  em minúsculas (sem acento/espaço), senha padrão `milscale123`. Nomes
  repetidos ganham sufixo numérico automático.

## Redesign visual ("farda, não gradiente")

O visual anterior era funcional mas genérico. Reconstruí o sistema de
design em cima do mesmo esqueleto (nenhuma tela mudou de URL ou perdeu
funcionalidade), com um conceito específico pra esse produto:

- **Paleta**: oliva escuro `#2E3B23` / musgo `#47592f` (farda) + bege
  campanha `#F4F1E8` de fundo (não branco/cinza de SaaS) + um único
  acento de latão `#A8823D` (insígnia), usado com moderação — só em
  bordas de destaque, não decoração solta.
- **Tipografia**: IBM Plex Sans Condensed nos títulos/menu (cara de
  documento oficial) + Inter no corpo/tabelas (legibilidade em dado
  denso).
- **Superfícies**: bordas menos arredondadas, sombra quase nula —
  farda não tem gradiente nem glow de SaaS.
- **Painel reconstruído do zero** com dado real (não mockup): cards de
  estatística (militares ativos, vagas em aberto, trocas pendentes,
  afastamentos do dia), tabela "Serviço de hoje", ações rápidas, e um
  painel de alertas que só aparece quando há algo de verdade pra
  resolver.
- Tirei screenshots reais (Playwright + Chromium) de cada tela pra
  autocriticar antes de fechar — Login, Painel, Militares, Escala do
  Mês, Minha Escala, Trocas, Missões, Feriados, Regras da Escala,
  Tipos de Serviço e Qualificações foram todas revisadas visualmente.
- No processo, achei e corrigi dois bugs cosméticos que só apareceram
  nos screenshots: "SEPTEMBER" em inglês no nome da escala (o backend
  usava o enum do Java direto) e "Setembro **De** 2026" com D
  maiúsculo indevido (um `text-transform: capitalize` no CSS estava
  capitalizando cada palavra à toa).
- **Responsividade mobile fica pra depois** — combinado explicitamente
  com o usuário, é uma pergunta em aberto pra quando o sistema estiver
  mais evoluído.

## Correções de regra de negócio (pontuais)

- **Comandante da Guarda agora é privativo do 3º Sgt.** O 2º Sgt é
  mais antigo e só tira Graduado de Dia — antes os dois postos podiam
  ser escalados pros dois cargos. Testado: gerei um mês inteiro depois
  da mudança e confirmei que só aparece "3 Sgt" em Comandante da
  Guarda, sem criar vaga em aberto nova nem violar RN06.
- **Nome de guerra duplicado no mesmo posto agora é bloqueado (item 5
  do backlog).** Duas pessoas do mesmo posto/graduação não podem mais
  ter o mesmo nome de guerra — validado tanto no cadastro quanto na
  edição, com mensagem clara. Nomes repetidos entre postos
  *diferentes* continuam permitidos (ex.: um "Ribas" Tenente e um
  "Ribas" Cabo podem coexistir). Também corrigi a base semeada: ela
  tinha esses casos de verdade (Andrade, Duarte, Farias, Ribas,
  Salles, Teixeira, Vaz e Prado repetidos dentro do próprio Sd EV) —
  agora o gerador de dados controla nome único por posto, com uma
  lista de sobrenomes maior (124, antes 82) pra nunca faltar opção
  mesmo no maior grupo (Sd EV, com quase 80 pessoas). Testei os dois
  lados: cadastrar duplicata no mesmo posto → 400 com mensagem clara;
  mesmo nome em posto diferente → aceita normalmente.
- **Trocas de serviço — item 1 do backlog, duas mudanças:**
  - Nova etapa no fluxo: **o substituto sugerido precisa aceitar**
    antes de qualquer triagem. Fluxo agora é
    `AGUARDANDO_SUBSTITUTO → EM_TRIAGEM → AGUARDANDO_AUTORIZACAO →
    AUTORIZADA/NEGADA`. Nova aba "Pedem pra eu assumir" na tela de
    Trocas, com Aceitar/Recusar. Testei: pessoa errada tentando
    confirmar → bloqueado; Cabo tentando fazer triagem antes da
    confirmação → bloqueado; fluxo completo (aceitar → triagem →
    autorização → troca efetivada) → funcionou; e o caminho de recusa
    (fecha como NEGADA, serviço não muda de dono) → funcionou.
  - A lista de substitutos sugeridos agora **exclui quem violaria o
    intervalo mínimo de descanso da regra** daquele tipo de serviço
    (não só o dia imediatamente antes/depois — a janela completa da
    regra, igual ao motor de geração usa). Essa exclusão vale só pra
    sugestão automática: se dois militares combinarem espontaneamente
    uma troca 1-pra-1 fora da lista sugerida, o cadastro não bloqueia
    — quem decide quem assume é sempre quem pede. **Nota de
    transparência**: na primeira versão eu só filtrei o dia
    imediatamente antes/depois (como foi pedido ao pé da letra), e
    meu próprio teste completo do fluxo revelou uma violação real de
    RN06 (o substituto ficou com só 2 dias de intervalo entre dois
    serviços) — troquei pra usar a janela completa da regra e retestei
    até zerar a violação.
  - **Correção fina depois disso**: a regra normal do motor exige
    3x1 (3 dias de folga entre dois serviços), mas numa troca — por
    ser escolha própria de quem assume — o mínimo aceitável é mais
    frouxo: **2x1 é permitido, só o 1x1 (1 dia de folga ou menos) é
    proibido mesmo em troca**. Isso não é só sugestão — é travado de
    verdade no cadastro (`criar`), então mesmo uma troca combinada
    manualmente fora da lista sugerida não consegue forçar um 1x1.
- **Efetivo extra (item 4 do backlog).** Reforcei em +5 todas as
  categorias que ainda não tinham recebido reforço nas rodadas
  anteriores: 3º Sgt geral, 2º Sgt geral, Sgt do Aprov, Cabo geral,
  Cabo do Aprov, Sd EP-CFC, Sd EP-Motorista, Sd EP do Aprov e Sd EV
  do Aprov (Tenente e Sd EV geral já tinham sido reforçados numa
  correção anterior). Total foi de 143 para 188 militares. O sintoma
  relatado — "Cabo de Dia" com vaga em aberto no dia 4 — sumiu:
  testei a geração de 4 meses seguidos (setembro a dezembro) e todos
  saíram com **zero vaga em aberto**. Também confirmei que o efetivo
  maior não introduziu violação de RN05/RN06 nem duplicata de nome de
  guerra dentro do mesmo posto (item 5 continua protegido).
- **Popup de detalhes do militar + campos da carteira de identidade.**
  Baseado na carteira de identidade militar oficial que vocês mandaram:
  - Novos campos no cadastro: NR Registro, data de nascimento, FUSEX e
    **foto** (upload no cadastro, guardada como base64 — simples de
    propósito pra essa fatia; armazenamento de arquivo de verdade tipo
    S3 é o próximo passo natural se crescer).
  - Clicar no nome de guerra de qualquer militar escalado (Escala do
    Mês, Escala do Dia, Painel) abre uma tela sobreposta estilo
    carteira de identidade: foto, nome completo, nome de guerra,
    posto/subunidade, CPF, NR Registro, data de nascimento, FUSEX,
    cursos, e **em que funções ele pode servir hoje** (calculado de
    verdade via `GET /api/militares/{id}/funcoes-elegiveis`, não uma
    lista estática).
  - `GET /api/militares/{id}` (visão individual) foi aberto pra
    qualquer autenticado — antes só Cabo/Sd EP/Sargenteante
    conseguiam, mas o popup precisa funcionar pro Militar Escalado
    também quando ele vê colegas na Escala do dia. A listagem
    completa (`GET /api/militares`) continua restrita.
  - **Refatoração de reuso**: a lógica de elegibilidade (posto +
    subunidade + qualificação exigida) estava duplicada em três
    lugares (`GerarEscalaService`, `AfastamentoService`,
    `SolicitacaoService`). Extraí pra um `ElegibilidadeService` único
    e os três passaram a usar a mesma fonte — bom momento pra isso já
    que o popup precisava da quarta cópia dessa regra.
  - **Regra nova**: Sd EP com curso CFC ou Motorista não tiram mais
    Monitoramento (já têm função específica). Implementei um
    mecanismo de "qualificações excluídas" no `RequisitoServico` pra
    isso — testei com checagem por ID (não só por nome, que pode se
    repetir entre postos): zero pessoa com CFC/Motorista escalada em
    Monitoramento, e zero vaga em aberto nova no mês inteiro depois da
    mudança.
  - Testei o popup de ponta a ponta com screenshot real: abre no
    estilo carteira de identidade, mostra os dados certos, e a seção
    "Pode servir em" reflete a elegibilidade de verdade (um Tenente
    mostrou só "Oficial de Dia", por exemplo).
- **Ordenação de nomes (item 2 do backlog).** Duas telas:
  - **Militares**: ordenado por hierarquia de posto primeiro (Tenente
    no topo, Sd EV embaixo), e só dentro do mesmo posto por ordem
    alfabética do nome de guerra.
  - **Escala do dia / Escala do mês**: mantém a ordem das funções do
    Boletim Interno (Oficial de Dia → ... → Guardas ao Quartel), mas
    agora dentro de cada função com mais de uma vaga (Monitoramento,
    Plantão ao Alojamento, Guardas ao Quartel) os nomes aparecem em
    ordem alfabética. Conferido com screenshot real: "Guardas ao
    Quartel" saiu Andrade, Bueno, Cordeiro, Duarte, Farias, Prado,
    Ribas, Salles, Teixeira — alfabético de ponta a ponta.

- **Feriados, Missões e Avisos (item 3 do backlog, o maior — e o
  último que faltava)**:
  - **Feriado agora é um período** (data início/fim), não mais um dia
    único — testei criando um feriado de 2 dias, funcionou.
  - **Missão com múltiplos militares num cadastro só**. Antes cada
    afastamento só linkava 1 pessoa; agora o formulário tem uma lista
    com busca e checkbox pra selecionar quantos militares quiser.
    Internamente, cada pessoa ainda vira sua própria linha de
    Afastamento (a reconciliação automática de vaga continua
    funcionando por pessoa), mas todas compartilham um "lote" que
    agrupa a exibição. Testei cadastrando uma missão pra 3 pessoas de
    uma vez: os 3 registros saíram ligados, a tela de Missões e
    Dispensas mostra como 1 linha só, e nenhum dos 3 apareceu
    escalado durante o período da missão (reconciliação automática
    funcionando igual antes).
  - **Tela de Avisos nova**, pra Militar Escalado e Sd EP (que não têm
    Painel): um calendário igual ao de Minha Escala, mas em vez de
    serviço mostra dias com feriado (verde) ou missão/afastamento
    (âmbar) destacados. Clicar num dia mostra o detalhe embaixo —
    tipo, descrição, e quais militares estão envolvidos; clicar de
    novo no mesmo dia desmarca. Embaixo do calendário, uma lista geral
    de todos os avisos do mês. Endpoint `GET /api/avisos?mes=` aberto
    a qualquer autenticado, testado logado como Militar Escalado.
  - Esse mesmo comportamento de "clicar de novo desmarca" também foi
    aplicado à Escala do Mês (antes só marcava, nunca desmarcava).
  - Testei tudo com screenshot real: calendário com as cores certas,
    clique selecionando/mostrando detalhe, lista geral batendo com o
    que foi cadastrado.

## Correções pontuais adicionais (pós-backlog)

- **Rancheiro de Dia agora são 2 por dia** (era 1).
- **Aprovisionamento é exclusivo do rancho de verdade.** Achei uma
  brecha real: quem está lotado no Aprov (Sd EV, Sd EP, Cabo, Sgt)
  conseguia ser escalado em funções "gerais" do mesmo posto
  (Monitoramento, Guardas ao Quartel, Cabo da Guarda etc.), porque
  essas funções não excluíam por subunidade. Estendi o modelo de
  elegibilidade com uma exclusão por subunidade (igual à exclusão por
  qualificação que já existia) e apliquei em todas as funções que não
  são do rancho. Testei cruzando por ID (não por nome): zero dos 45
  militares do Aprov apareceu fora de Rancheiro/Cozinheiro/Graduado do
  Rancho no mês inteiro.
  - **Efeito colateral que corrigi**: como todo Sd EP já pertencia a um
    subgrupo (CFC, Motorista ou Aprov), excluir o Aprov do
    Monitoramento deixou essa função **sem ninguém elegível** (82
    vagas em aberto no teste). Criei um quarto grupo de Sd EP "geral"
    (sem curso, sem Aprov) especificamente pra cobrir o Monitoramento.
- **Perfil dos militares preenchido** (menos a foto, como pedido).
  Pesquisei faixas etárias reais de carreira no Exército Brasileiro
  (ESA, AMAN/EsPCEx, progressão de posto) pra gerar datas de
  nascimento plausíveis por posto — não são pessoas reais (os nomes
  são fictícios), mas as idades batem com o tempo de carreira típico
  de cada graduação: Sd EV 18-19, Sd EP 18-26, Cabo 20-30, 3º Sgt
  19-28, 2º Sgt 25-38, Tenente 24-30. NR Registro e FUSEX também
  preenchidos (formato plausível, sem fonte real por trás). Isso já
  foi direto pro gerador de dados (`DataSeeder`), não foi um ajuste
  manual avulso — vale pra sempre que o banco for resemeado.

## "Dias sólidos" — travamento automático por horário (sem botão de salvar)

Duas coisas importantes nessa rodada:

- **Provei que "salvar alterações" já não precisa existir.** O usuário
  achou que cadastrar uma missão exigiria resetar a escala inteira.
  Fiz um teste explícito: gerei um mês (775 serviços), cadastrei uma
  missão pra 1 militar, comparei o mês inteiro linha por linha —
  **exatamente 1 linha mudou** (a vaga dele no período da missão), as
  outras 774 continuaram idênticas. A realocação automática por
  afastamento (`reconciliarServicosJaMarcados`) já era cirúrgica desde
  antes; não existe "regerar tudo" escondido em lugar nenhum.
- **"Dia sólido" — travamento automático por horário, novo de
  verdade.** Diferente do travamento manual do Sargenteante
  (`BloqueioDiaService`, que é uma escolha pra dias futuros), um dia
  agora vira imutável sozinho assim que o horário de início do serviço
  passa (08h00, `TipoServico.horaInicio`) — sem ninguém precisar
  travar manualmente. Implementado como `ServicoEscalado.isJaComecou()`
  (calculado, não salvo no banco) e aplicado em três lugares:
  regenerar escala (bloqueia se o período tem dia já começado),
  realocação automática por afastamento (não mexe mais em serviço que
  já passou, só nos futuros dentro do mesmo período), e pedido de
  troca (bloqueia com mensagem clara). Exposto no JSON como
  `jaComecou`, com um ícone diferente (✅) do cadeado manual (🔒) na
  tela de Escala do Mês. Testei os três bloqueios de ponta a ponta com
  dados reais: regenerar um período com dia já passado → bloqueado;
  afastamento cobrindo um dia passado + um futuro do mesmo militar →
  só o futuro foi realocado, o passado ficou intacto; pedido de troca
  pro próprio serviço já começado → bloqueado. Gerar um período
  totalmente futuro continua funcionando normal.

## Regenerar período, Minha conta, Perfis e permissões

- **Botão "Regenerar este período".** Depois de cancelar um
  afastamento na tela de Missões e Dispensas, aparece um banner
  oferecendo regenerar exatamente aquele período (mesmo endpoint de
  gerar escala, só que disparado com um clique, sem precisar ir na
  tela de Escala). Testado via API: cancelar → regenerar aceito (200)
  → zero vaga em aberto, zero violação de regra depois.
- **Minha conta** — nova, aberta a qualquer perfil (link no menu de
  todo mundo). Mostra os próprios dados (nome, posto, CPF, NR
  Registro, data de nascimento, FUSEX, perfil de acesso) e um
  formulário de trocar a própria senha (`POST /api/auth/senha`,
  exige confirmar a senha atual). Testado de ponta a ponta: senha
  antiga parou de logar, nova senha passou a funcionar.
- **Perfis e permissões** — nova, privativa do Sargenteante (era só
  um placeholder "em construção"). Lista todo mundo com login, perfil
  de acesso (editável por um select) e situação (ativo/inativo), com
  busca por nome/login. Ações: trocar perfil, ativar/desativar conta,
  resetar senha pro padrão. **Auto-proteção testada**: o próprio
  Sargenteante não consegue alterar o próprio perfil nem desativar a
  própria conta (400 com mensagem clara nos dois casos) — evita
  travar o próprio acesso sem querer. Confirmei também que Militar
  Escalado toma 403 tentando acessar essa lista.
- Achei e corrigi um bug no meio do caminho: o mapa de rótulos de
  perfil no frontend esperava nomes tipo "Sargenteante", mas o banco
  guarda o nome igual ao ROLE do Spring Security ("SARGENTEANTE") —
  corrigido e extraído pra um utilitário compartilhado
  (`utils/perfis.ts`) já que duas telas (Minha conta e Perfis e
  permissões) precisavam do mesmo mapeamento.
- Conferido com screenshot real depois da correção: "Minha conta"
  mostra "Sargenteante" (não mais o valor cru), e "Perfis e
  permissões" mostra "Militar Escalado" no select de cada linha.

## Ficha do Militar

Página própria (`/militares/:id`, clicável a partir do nome na lista
de Militares) — diferente do popup que já existe: o popup é uma
consulta rápida sem sair da tela (Escala, Painel), a Ficha é o
"prontuário" completo, com histórico e edição:

- **Dados pessoais com edição** — privativo de Cabo/Sargenteante.
  Campos administrativos (telefone, email, subunidade, posto, NR
  Registro, data de nascimento, FUSEX) editam livre. Campos de
  identidade (nome completo, nome de guerra, CPF) também editam, mas
  pedem uma confirmação extra ao salvar, já que só deveriam mudar
  pra corrigir erro de cadastro — nunca uma edição de rotina.
  Autoedição continua bloqueada (a mensagem em "Minha conta" já
  mandava falar com o Cabo/Sargenteante, e a Ficha é onde isso
  acontece de fato).
- **CPF agora entra na validação de duplicidade** também na edição
  (antes só entrava no cadastro novo) — testei tentando salvar o CPF
  de outra pessoa e bloqueou com mensagem clara.
- **Três históricos**: serviços (todos, não só "há X dias"),
  missões/dispensas, e trocas (pedidas ou recebidas, com o papel de
  cada uma). Todos com endpoint próprio, privativo de
  Cabo/Sd EP/Sargenteante — testei que Militar Escalado toma 403
  tentando acessar tanto o histórico quanto a edição.
- Cursos/qualificações ficam de fora de propósito — continuam só no
  fluxo próprio (botão "Cursos" na lista de Militares), pra não
  duplicar caminho.
- Testado de ponta a ponta com screenshot real: visão de leitura e
  modo de edição, os dois batendo com os dados reais (inclusive uma
  edição e um afastamento que cadastrei via API apareceram certinho
  na tela).

## Log de auditoria e Boletim Interno

- **Log de auditoria** — nova tela, privativa do Sargenteante, com
  busca por pessoa/ação/detalhe. Fiz uma entidade e serviço genéricos
  (`AuditoriaService.registrar`) e pluguei em **todo** controller que
  muda estado: militar (cadastro/edição/desligamento), escala
  (gerar/publicar/travar/destravar), afastamento
  (cadastrar/cancelar), troca (todo o fluxo — pedir, confirmar
  substituto, triagem, autorização, cancelar), feriado
  (cadastrar/remover), usuário (perfil/ativo/reset de senha) e a
  troca de senha própria. O registro nunca lança exceção (uma falha
  ao gravar o log não pode derrubar a ação que estava sendo
  auditada). Testado: gerar escala e trocar senha apareceram no log
  na hora, com quem fez e quando; Militar Escalado toma 403 tentando
  acessar.
- **Boletim Interno** — nova tela, leitura aberta a todo mundo,
  manutenção privativa de Cabo/Sargenteante. A peça central é um
  editor de texto rico (`RichEditor.tsx`, reutilizável) que aceita
  **colar uma imagem direto (Ctrl+V) no meio do texto**, ou inserir
  por um botão de arquivo — misturado livremente com parágrafos,
  negrito, itálico, lista e título. O conteúdo salva como HTML com a
  imagem embutida em base64 (mesma lógica simples da foto do
  militar). Lista com "clique pra expandir e ler" em vez de navegar
  pra outra tela. Testei de ponta a ponta: inseri uma imagem via
  arquivo, conferi que a tag `<img>` com os dados em base64 apareceu
  de verdade no HTML do editor (não só visualmente), publiquei, e
  confirmei que o banco guardou a imagem junto do texto. Confirmei
  também que Militar Escalado consegue ler mas toma 403 tentando
  criar, e que editar/publicar geram entrada no log de auditoria.

## Responsividade mobile

Testado de verdade com viewport de iPhone 13 (Playwright + Chromium),
não só CSS "no escuro":

- **Menu lateral vira um painel deslizante** (off-canvas), acionado
  por um botão hambúrguer numa barra superior nova — fecha sozinho ao
  navegar, com fundo escurecido atrás.
- **Tabelas** ganharam scroll horizontal dentro do próprio card (não
  quebram o layout da página) — confirmei isso inspecionando o DOM
  (`scrollWidth` vs `clientWidth`), não só visualmente.
- **Calendários** (Escala do Mês, Minha Escala, Avisos) encolhem de
  verdade em vez de vazar — usam `minmax(0, 1fr)` nas colunas, e o
  texto "X serviços" dentro de cada dia some em telas bem pequenas,
  deixando só o número.
- **Formulários** (`.form-grid`) empilham em coluna única.
- Achei e corrigi três vazamentos reais que só apareceram no teste,
  não eram óbvios só lendo o código:
  1. Os números de estatística da Escala do Mês (775 vagas, 0 em
     aberto...) vazavam pra fora da tela — não tinham `flexWrap`.
  2. O formulário de "Novo militar" (foto + campos lado a lado)
     vazava inteiro pra direita — corrigido empilhando foto em cima
     dos campos no mobile.
  3. O popup de detalhes do militar tinha o mesmo risco (foto + texto
     lado a lado) — corrigido com `minWidth: 0` no flex e o grid de
     3 colunas (CPF/NR/data nascimento...) virando 2 no mobile.
- Testei visualmente: login, painel, tabela de Militares (com scroll
  confirmado), Escala do Mês + detalhe do dia, popup do militar,
  cadastro com foto, Avisos, editor do Boletim Interno (a barra de
  ferramentas quebra em duas linhas direitinho), e Minha Escala.
- Rodei a regressão da escala depois de tudo: zero vaga em aberto.

## Testes automatizados

Focados exatamente nas regras que já causaram bug de verdade nesta
sessão — não uma tentativa de cobrir tudo, mas de blindar o que mais
provavelmente quebra de novo no futuro. Rodam com `mvn test` (e
automaticamente toda vez que você roda `mvn package`, sem precisar
lembrar). Banco H2 em memória (`application-test.properties`),
separado do banco de desenvolvimento — nunca toca em `./data/milscale`.

- **`ElegibilidadeServiceTest`** (unidade, sem Spring, 0.02s): 8 casos
  cobrindo a regra central (RF06) — inclusive as duas exclusões que já
  causaram bug real: qualificação excluída (CFC/Motorista fora do
  Monitoramento) e subunidade excluída (Aprov fora de funções gerais).
- **`EscalaGeracaoIntegrationTest`** (`@SpringBootTest`, ~19s): gera um
  mês inteiro contra os ~200 militares semeados e confere: zero vaga
  em aberto, zero violação do intervalo mínimo (RN06, 3x1), ninguém do
  Aprovisionamento fora do rancho, ninguém com CFC/Motorista em
  Monitoramento.
- **`TrocaIntervaloIntegrationTest`**: a regressão do 1x1/2x1/3x1 nas
  trocas — construo o cenário direto (não dependo do gerador
  coincidentemente produzir os gaps certos), confirmando que 1x1 é
  sempre bloqueado (na lista de sugestão E no cadastro direto) e 2x1 é
  aceito.
- **Validei que os testes pegam os bugs de verdade**: reintroduzi
  cada um dos três bugs reais desta sessão (a exclusão de subunidade,
  o filtro de 1 dia em vez de 2 nas trocas) de propósito, confirmei
  que o teste correspondente falha, e desfiz — não é só "passou",
  é "passou porque a regra está certa".
- Total: 15 testes, todos verdes, ~23s.

## Docker Compose com MySQL real

A última pendência da lista. Em vez de escrever `docker-compose.yml`
sem poder testar, fiz três coisas em sequência:

1. **Instalei MySQL de verdade neste ambiente** (não Docker ainda) e
   subi o backend contra ele com `--spring.profiles.active=mysql`.
   Achei um bug real: os campos de texto grande (foto do militar em
   base64, conteúdo do Boletim) usavam `columnDefinition = "CLOB"` —
   sintaxe do H2 que **não existe no MySQL** e quebrava a criação da
   tabela. Corrigido removendo essa definição e deixando o Hibernate
   escolher o tipo certo por dialeto (`LONGTEXT` no MySQL, `CLOB` no
   H2, automático). Retestei: login, geração de escala (zero vaga
   aberta) e publicação de um Boletim com conteúdo grande — os três
   funcionando contra MySQL de verdade.
2. **Consegui instalar o Docker de verdade neste ambiente também** e
   testei o `docker build` de cada imagem (backend e frontend)
   isoladamente. O build do backend rodou os 15 testes automatizados
   **dentro do container** como parte do `mvn package` — se algum
   quebrar, a imagem nem termina de ser gerada.
3. **Subi os 3 serviços juntos com `docker compose up`** — MySQL,
   backend e frontend — e testei através das portas publicadas
   exatamente como um usuário real faria: login por CPF, geração de
   escala completa (zero vaga aberta), o Boletim salvando com
   conteúdo grande, e o roteamento do React funcionando em rotas
   profundas tipo `/militares` (confirma que o `nginx.conf` com
   `try_files` está certo, senão um recarregar de página nessas rotas
   daria 404). Também confirmei visualmente com screenshot: login
   funcionando e o Painel carregando dados reais (201 militares, zero
   vaga aberta) inteiramente através da pilha em containers.

Detalhe técnico: o healthcheck do MySQL no `docker-compose.yml` é
essencial — sem ele, o backend tentaria conectar assim que o
container do MySQL *existisse*, não quando ele *já aceita conexões*
de verdade, e cairia de cara na primeira subida. Testei isso
funcionando: o log mostrou o MySQL virando "Healthy" antes do backend
sequer começar a subir.

## "Meu histórico" — última tela de placeholder eliminada

O item "Histórico" no menu (todo mundo tem, com rótulos diferentes:
"Histórico", "Meu histórico", "Histórico completo") ainda apontava
pra uma tela "em construção". Implementei de verdade: cada pessoa vê
o **próprio** histórico de serviços, missões/dispensas e trocas — os
mesmos três blocos que já existiam na Ficha do Militar, só que sem
edição e sem os dados pessoais.

- Isso exigiu mexer em permissão no backend: os três endpoints de
  histórico eram privativos de Cabo/Sd EP/Sargenteante (fazia sentido
  pra Ficha do Militar, onde essas pessoas veem o histórico de
  QUALQUER UM). Adicionei uma checagem extra
  (`@militarService.ehOProprio`) que libera a própria pessoa a ver o
  próprio histórico mesmo sem perfil elevado, sem abrir pra ver o de
  outra pessoa — testei os dois lados: Militar Escalado vendo o
  próprio histórico (200) e tentando ver o de outra pessoa (403,
  continua bloqueado).
- Não sobrou nenhuma tela `EmConstrucaoPage` no sistema — essa era a
  última.
- 15 testes automatizados continuam verdes.

## Escala nunca fica com vaga em aberto ("aperta sozinha")

Bug real reportado pelo usuário (com log de auditoria anexado):
colocar vários militares de férias/missão ao mesmo tempo deixava vaga
em aberto na escala gerada — no quartel de verdade isso nunca
acontece, o efetivo simplesmente "aperta" (quem sobra serve mais
vezes). Achei a causa exata: o gerador cortava do pool quem não
respeitava o intervalo mínimo de 3 dias de folga e, se sobrasse gente
de menos depois desse corte, simplesmente desistia e deixava a vaga
aberta.

- **Corrigido**: quando o pool "rigoroso" (respeitando o intervalo)
  não tem gente suficiente, um segundo pool "relaxado" (que ignora só
  o intervalo mínimo, mantendo elegibilidade de posto/qualificação e
  afastamentos) preenche o resto — sempre priorizando quem está há
  mais tempo sem servir, nunca escolha arbitrária.
- Testei no pior caso possível: só 1 pessoa elegível no batalhão
  inteiro pro mês inteiro (Oficial de Dia só aceita Tenente, e coloquei
  7 dos 8 de férias). Resultado: ela serviu os 30 dias do mês, **zero
  vaga aberta**.
- Escrevi um teste automatizado permanente pra essa regra
  (`efetivoInsuficiente_apertaAEscalaEmVezDeDeixarVagaAberta`) e
  **provei que ele pega a regressão**: desliguei a correção de
  propósito e confirmei que 22 vagas ficariam abertas nesse cenário
  sem ela.

## Dois tipos de troca de serviço (substituição e troca mútua)

O usuário explicou que no quartel existem dois padrões de troca bem
diferentes, e o sistema só tinha um:

- **"Passar meu serviço"** (o que já existia) — alguém assume o seu
  serviço, e você fica de folga até o seu próximo serviço normal, sem
  pegar o dia de ninguém em troca.
- **"Trocar de dia com alguém"** (novo) — vocês dois trocam de dia
  entre si: você assume o serviço dele, e ele assume o seu. Só entre
  serviços do **mesmo tipo** (ex.: Cabo da Guarda por Cabo da Guarda).

Nomes escolhidos de propósito bem diretos (a pedido do usuário,
pensando em quem vai usar o sistema no dia a dia), com uma explicação
curta em cada opção na hora de pedir a troca.

- **A troca mútua exige checar a regra de 1x1 dos DOIS lados** — você
  assumindo o dia dele não pode te deixar em 1x1 contra seus PRÓPRIOS
  outros serviços, e ele assumindo o seu dia não pode deixar ELE em
  1x1 contra os dele. Os militares disponíveis pra troca só aparecem
  DEPOIS de escolher o tipo, exatamente como pedido — a lista muda
  conforme a regra de cada tipo.
- A autorização final agora efetiva os **dois lados** da troca mútua
  de uma vez (os dois `ServicoEscalado` trocam de dono), não só um.
- **6 testes automatizados novos**, cobrindo cada regra separadamente:
  permite quando os dois ficam em 2x1+, bloqueia quando o solicitante
  ficaria em 1x1, bloqueia quando o OUTRO militar ficaria em 1x1,
  bloqueia tipos de serviço diferentes, confirma que autorizar
  efetiva os dois lados, e confirma que a lista de candidatos exclui
  corretamente quem violaria qualquer um dos dois lados. Testei cada
  uma das duas checagens de 1x1 desligando de propósito e confirmando
  que o teste correspondente falha — provando que pegam a regressão
  de verdade.
- Testado de ponta a ponta também na interface: escolhi o serviço,
  vi as duas opções de tipo com a explicação, escolhi "Trocar de dia
  com alguém", a lista de candidatos carregou certinho, enviei o
  pedido, e a tela de "Minhas solicitações" mostrou a troca com a
  mensagem clara "você assume o dia X dele".
- Total: **22 testes automatizados, todos verdes**.

### Nota sobre esta entrega: reconstrução após reset de ambiente

No meio desta entrega, o ambiente de trabalho remoto resetou por
completo (não tem relação com nada do lado do usuário — Docker,
notebook ou celular; é uma característica de como esses ambientes
temporários funcionam). Isso apagou o progresso não salvo. Reconstruí
tudo a partir do último zip entregue, reaplicando cada mudança desta
seção com precisão, e confirmei que o resultado final compila e
builda **byte a byte idêntico** ao que já havia sido validado antes
do reset — nenhuma mudança foi perdida.

## Rodada de melhorias solicitadas (organização, notificações, PDF, integrações)

Pedido grande, dividido e testado em partes:

- **Boletim logo abaixo do Painel** no menu (Sargenteante e Cabo), e
  **"Minha conta" agora é o próprio cartão de perfil** no rodapé do
  menu (clicável), não mais um item dentro de Administração.
- **Tag de afastamento** no popup do militar e na Ficha — mostra só o
  tipo (ex.: "Missão", "Dispensa"), nunca a descrição completa, com
  endpoint novo (`GET /api/militares/{id}/afastamento-atual`).
- **Sistema de notificações (o sininho)** — entidade `Notificacao`
  nova, com um serviço que notifica por perfil (`registrarParaPerfis`)
  ou por usuário específico. Disparado em 3 pontos do fluxo de troca:
  substituto avisado quando é convidado, Cabo+Sargenteante avisados
  quando a troca chega pra triagem, só Sargenteante avisado quando
  falta a autorização final. Testei o fluxo inteiro via API (criar →
  aceitar → triagem → contagem de não-lidas em cada conta) e depois
  visualmente. **Achei e corrigi um bug real nessa etapa**: o painel
  do sininho estava sendo cortado pelo `overflow-y: auto` do menu
  lateral — resolvido trocando pra posição calculada a partir da
  posição real do botão (`position: fixed` + `getBoundingClientRect`),
  confirmado com screenshot antes/depois da correção.
- **Boletim ↔ Avisos conectados** — cada Aviso (feriado ou
  missão/afastamento, já agrupado por lote) ganhou uma chave estável;
  ao criar um Boletim, dá pra relacionar a um evento do mês atual ou
  do próximo, e a ligação aparece nos dois sentidos: o Boletim mostra
  uma tag "Relacionado: X", e a tela de Avisos mostra um botão "Ver
  Boletim relacionado" no dia do evento. Testado criando uma missão
  de teste, ligando um Boletim a ela, e conferindo visualmente os
  dois lados.
- **Botão de gerar PDF da Escala do Dia** — página de impressão nova
  (`/escala/pdf/:data`, sem menu lateral), acessível tanto direto na
  Escala do Dia quanto clicando num dia específico na Escala do Mês.
  Sem biblioteca de PDF no backend: a página é limpa, com CSS de
  impressão (`@media print` esconde a barra de ação), e dispara
  `window.print()` sozinha — o "Salvar como PDF" do navegador vira o
  PDF de verdade. Testado com screenshot real mostrando a tabela
  completa do dia.
- **Email de lembrete 1 dia antes do serviço** — `EmailService` +
  `LembreteServicoScheduler` (roda todo dia às 18h, configurável).
  **Importante**: fica desligado por padrão
  (`milscale.email.habilitado=false`) porque este projeto não tem
  credenciais de SMTP reais — com o interruptor desligado, o sistema
  registra no log exatamente o que teria sido enviado, sem tentar
  conectar em servidor nenhum, então a tarefa nunca quebra por falta
  de configuração. Testei isso de verdade: publiquei uma escala,
  cadastrei um email de teste, disparei o lembrete manualmente (novo
  endpoint `POST /api/lembretes/disparar`, Sargenteante) e confirmei
  no log a linha exata que seria enviada. Também testei os dois
  filtros importantes: pula quem não tem email cadastrado (confirmei
  contando quantos foram processados vs. quantos serviços existiam no
  dia), e pula escalas ainda em **rascunho** (só avisa sobre escala já
  publicada, testei gerando uma escala rascunho de propósito e
  confirmando `processados: 0`). Pra ligar de verdade em produção:
  configurar `MAIL_HOST`, `MAIL_USER`, `MAIL_PASSWORD` e
  `MILSCALE_EMAIL_HABILITADO=true` nas variáveis de ambiente.
- 15 testes automatizados continuam todos verdes depois de toda essa
  rodada.

## Login por CPF

Mudança de RF01: em vez de logar pelo nome de guerra, agora é pelo
CPF — a direção que ficou combinada há um tempo, finalmente
implementada.

- `Usuario.login` passou a guardar o CPF puro (11 dígitos) em vez do
  nome de guerra normalizado. Isso **simplificou o gerador de dados**
  de verdade: como CPF já é garantidamente único (validado no
  cadastro), não precisa mais da lógica de resolver colisão de nome
  com sufixo numérico (`loginUnico()`) — removi esse método inteiro.
- O campo de login aceita **com ou sem formatação** (`000.000.000-01`
  ou só os números) — tanto no formulário quanto direto na API, já
  que o backend normaliza tirando tudo que não é dígito antes de
  comparar.
- **Editar o CPF de alguém agora também atualiza o login da conta
  dela.** Pensei nisso porque a Ficha do Militar já permite corrigir
  CPF (com a confirmação extra de "isso deveria ser raro") — sem
  sincronizar, a pessoa ficaria com uma conta que não bate mais com o
  CPF novo dela, e não conseguiria mais entrar.
- Atualizei as telas que mostravam o login antigo: tela de login
  (rótulo e exemplo trocados pra CPF), Perfis e Permissões (coluna
  "CPF (login)", formatado), e tirei o campo "Login" duplicado da
  Minha Conta (já que agora é literalmente o mesmo valor do campo CPF
  logo acima, mostrar os dois ficaria redundante).

## Máscaras de campo (CPF, telefone, FUSEX)

Enquanto a pessoa digita, os símbolos (pontos, traço, parênteses)
encaixam sozinhos — sem precisar digitar `000.000.000-00`, só os
números, na ordem certa. Utilitário compartilhado
(`utils/mascaras.ts`) com `mascararCpf`, `mascararTelefone`,
`mascararFusex` e `mascararCep` (esse último pronto pra quando/se um
campo de endereço aparecer — hoje não existe nenhum no sistema).

- Aplicado em: login, cadastro de militar e edição na Ficha do
  Militar. Aproveitei e **adicionei o campo de telefone no cadastro**
  também — antes só dava pra informar editando depois.
- **Padrão de armazenamento**: CPF e telefone guardam só dígitos no
  banco (formatação é só visual, na hora de mostrar) — mesma lógica
  de sempre pro CPF, que eu estendi pro telefone também.
- Testei digitando caractere por caractere de verdade (não só
  simulando o valor final): `00000000001` vira `000.000.000-01`
  digitando, `41999998888` vira `(41) 99999-8888` digitando.
- **Dois bugs reais que achei nesse teste, não só a máscara em si**:
  1. A comparação que decide se você mudou a identidade (nome/CPF) na
     Ficha do Militar ia disparar o aviso de confirmação **toda vez**
     que você editasse qualquer coisa — porque comparava o CPF já
     formatado contra o CPF cru do banco, que nunca batiam. Corrigido
     comparando dígito com dígito.
  2. O telefone estava salvando **com a formatação dentro do banco**,
     inconsistente com o CPF. Corrigido pra salvar só números e
     formatar na exibição — testei editando, checando o valor cru no
     banco via API, e conferindo que a tela volta a mostrar formatado.

## Tudo entregue

**Backlog combinado com o usuário: completo** (os 5 itens — nome de
guerra duplicado, trocas de serviço, ordenação de nomes, efetivo
extra, e feriados/avisos — foram todos implementados e testados; ver
seções acima). Log de auditoria, Boletim Interno, testes automatizados,
login por CPF e Docker Compose com MySQL real também já saíram (ver
seções acima). Não há pendência sem prazo aberta no momento — o que
vier daqui pra frente é o que vocês decidirem pedir.

## Bugs críticos corrigidos numa rodada de revisão

Vocês reportaram (com prints) que um militar aleatório aparecia escalado
mais de 2/3 do mês, que dava pra ver a escala completa logado como
Militar Escalado (deveria ver só um dia por vez), e que não dava pra
gerar um período menor depois de já ter gerado o mês inteiro. Todos
tinham a mesma causa raiz, mais três problemas que apareceram na
investigação:

1. **Escala não era uma linha do tempo contínua.** Cada "gerar" criava
   registros novos sem apagar os antigos que se sobrepunham no
   período — por isso "Minha escala" contava tudo duplicado, e gerar
   de novo um período menor não tinha efeito. Corrigido: gerar agora
   **substitui** o que já existia no período (apaga o antigo e
   recria), bloqueando a substituição se houver dia travado ou troca
   pendente naquele intervalo. Testei sobreposição total, parcial (em
   duas escalas ao mesmo tempo) e nenhuma sobreposição — todos os
   casos encolhem/apagam/criam exatamente como esperado.
2. **Vazamento de dados real.** `GET /api/escalas` e `GET
   /api/escalas/{id}` não tinham nenhuma restrição de perfil — Militar
   Escalado conseguia baixar a escala do mês inteiro do batalhão pela
   API, mesmo com a tela escondendo isso. Agora esses dois endpoints
   são privativos de Cabo/Sd EP/Sargenteante; Militar Escalado usa um
   endpoint novo (`GET /api/escalas/dia?data=...`) que devolve só um
   dia por vez — testei os dois lados (403 pro mês, 200 pro dia).
3. **Intervalo mínimo dava só 2 dias de folga, não 3.** Um erro de
   contagem de calendário (`<` em vez de `<=`) deixava o motor
   escalar alguém de 3 em 3 dias corridos, quando o pedido era "no
   mínimo 3 dias de folga" (ou seja, de 4 em 4). Corrigido e validado.
4. **Afastamento cadastrado depois da escala já pronta não
   realocava a vaga.** Se alguém já estava escalado pro dia 9 e
   entrasse de missão do dia 8 ao 12, a vaga ficava "presa" com uma
   pessoa que não ia mais cumprir o serviço. Agora, ao cadastrar um
   afastamento, o sistema tenta achar automaticamente outra pessoa
   elegível e disponível pra cobrir cada vaga conflitante (mesmo
   critério de justiça do motor de geração), e corrige o contador de
   rodízio de quem foi afastado. Se não houver ninguém disponível sem
   violar o intervalo mínimo de descanso, a vaga fica honestamente em
   aberto em vez de forçar uma escolha errada. Testei os dois casos:
   com efetivo justo (ninguém disponível, vaga some com aviso) e com
   efetivo confortável (realocação automática de verdade).
5. **Efeito colateral da correção nº 3**: como o intervalo mínimo
   ficou mais rígido (de fato 4 dias, não 3), alguns postos com pouca
   gente (Tenentes, Sd EV geral) ficaram exatamente no limite
   matemático, sem nenhuma folga de reserva — o que fazia a
   realocação automática do item 4 falhar sempre por falta de
   substituto. Aumentei o efetivo de Tenentes (5→8) e Sd EV geral
   (48→72) pra sobrar margem de verdade. Achei também, no meio do
   caminho, um bug de sequência de CPF (`cpfSeq += 48` ficou
   desatualizado quando mudei a quantidade pra 72) — troquei todos os
   incrementos manuais por `.size()` pra essa classe de erro não
   voltar nunca mais.
6. Bônus: a coluna "Dias sem serviço" agora sempre mostra "último
   serviço há X dias" (nunca mais "escalado em X dias" — vocês pediram
   pra tirar essa distinção, já que o que importa pra conferir a
   escala é só a distância até o último serviço, positiva ou não).

Ao ligar o filtro de qualificação de verdade (curso exigido além do
posto), a geração de escala parou de preencher completamente. Dois
bugs de verdade, não só do ambiente de teste:

1. **Vaga parcialmente preenchida perdia o resto.** Se um tipo de
   serviço precisava de 3 pessoas e só 1 estava disponível, o sistema
   registrava só 1 vaga (a preenchida) e "esquecia" de registrar as
   outras 2 como vaga em aberto — corrigido em `GerarEscalaService`.
2. **Comparação de qualificação sempre dava falso.** A entidade
   `Qualificacao` não tinha `equals()`/`hashCode()` customizado, então
   `Set<Qualificacao>.contains(...)` comparava os objetos por
   identidade de memória em vez de pelo registro do banco — mesmo com
   a pessoa tendo o curso certo salvo corretamente, a checagem sempre
   falhava. Corrigido com `@EqualsAndHashCode(of = "id")`.

Isso é uma pegadinha clássica de JPA/Hibernate (comparar entidades por
igualdade sem `equals()` explícito) — vale revisar as outras entidades
se algum dia entrarem em um `Set`/`contains()` parecido.
