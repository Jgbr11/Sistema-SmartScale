# MilScale

Sistema de escala de serviço militar do 5º Batalhão de Suprimento — gera a
escala diária (Oficial de Dia, Graduado de Dia, Guardas ao Quartel etc.),
controla trocas de serviço, missões e dispensas, e cada militar consulta a
própria escala. É o primeiro produto da linha **SmartScale**, desenvolvido
na disciplina de Desenvolvimento Orientado a Reuso de Software (PUCPR).

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
    PoliticaDeDescanso.java      intervalo mínimo (RN06) e regra de troca (1x1/2x1)
    RegraEscala.java, RequisitoServico.java, Escala.java, ServicoEscalado.java,
    Afastamento.java, PostoGraduacao.java, Subunidade.java, Qualificacao.java,
    Usuario.java, PerfilAcesso.java, enums de situação/tipo

  milscale/application/    casos de uso (services)
  milscale/adapters/web/   controllers REST (+ dto/ com os contratos de entrada)
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

**Stack:** Java 21 + Spring Boot 3.3 (Web, Data JPA, Security, Validation),
Flyway, H2 (desenvolvimento/testes) e MySQL 8 (Docker); React 19 + Vite 8 +
TypeScript, testes com JUnit 5 e Vitest.

## Como rodar

### Backend
Requer Java 21 ou mais novo e Maven. (Com JDK 23+ o `pom.xml` já habilita
o processamento de anotações que o Lombok precisa.)

```bash
cd backend
mvn spring-boot:run
```

Sobe em `http://localhost:8080`. Usa H2 em modo de compatibilidade MySQL,
gravado em `backend/data/` — não precisa de Docker nem MySQL instalado.
Na primeira execução o Flyway cria as tabelas e o sistema popula o banco
(postos, subunidades, tipos de serviço, regras, 4 contas de demonstração e
~200 militares).

Para usar MySQL de verdade, rode com `--spring.profiles.active=mysql` e
configure `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (ver
`application-mysql.properties`).

**Lembrete de serviço por email** vem desligado por padrão. Pra ligar:
```bash
export MAIL_HOST=smtp.exemplo.com
export MAIL_USER=seu-usuario
export MAIL_PASSWORD=sua-senha
export MILSCALE_EMAIL_HABILITADO=true
```
Sem isso, o sistema só registra no log o que teria enviado.

### Frontend
Requer Node 18+.

```bash
cd frontend
npm install
npm run dev
```

Sobe em `http://localhost:5173`. As chamadas para `/api` são repassadas
pelo próprio Vite ao backend em `:8080` — não é preciso configurar URL.
Só se o backend morar em outro endereço, defina `VITE_API_URL` (ver
`frontend/.env.example`).

### Docker Compose (MySQL de verdade, sem instalar nada além do Docker)
Requer Docker e Docker Compose.

```bash
cp .env.example .env     # e troque as senhas
docker compose up --build
```

Sobe os 3 serviços: MySQL 8 (`:3306`, dados num volume — sobrevive a
`docker compose down` sem `-v`), backend (`:8080`, perfil `mysql`, espera o
MySQL responder antes de subir) e frontend servido por nginx (`:5173`), que
também repassa `/api` ao backend. Sem o `.env` o Compose se recusa a subir.
Pra recomeçar do zero: `docker compose down -v`.

### Configurações por variável de ambiente

| Variável | Onde | Para quê |
|---|---|---|
| `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` | `.env` (Compose) | banco do Docker |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | backend, perfil `mysql` | conexão com o MySQL |
| `MILSCALE_CORS_ORIGENS` | backend | origens aceitas pelo CORS (padrão `http://localhost:*`) — só importa se o front estiver em outro domínio |
| `VITE_API_URL` | frontend (build) | endereço do backend; vazio = mesmo domínio |
| `MAIL_*`, `MILSCALE_EMAIL_HABILITADO`, `MILSCALE_LEMBRETE_CRON` | backend | lembrete de serviço por email |

## Testes

```bash
cd backend && mvn test      # JUnit: regras de escala, trocas, validação, erros, segurança
cd frontend && npm test     # Vitest: formatadores, máscaras, ordenação
```

Os testes do backend usam H2 em memória (nunca tocam em `backend/data/`) e
rodam também a cada `mvn package` — inclusive dentro do build da imagem
Docker.

## Banco de dados e migrations

O schema é versionado com **Flyway**, em
`backend/src/main/resources/db/migration`. O Hibernate não cria nem altera
tabelas (`ddl-auto=none`).

- **Toda mudança de schema é um arquivo novo** `V<n>__descricao.sql`
  (`V2__...`, `V3__...`). **Nunca edite uma migration já aplicada** — o
  Flyway recusa subir se o conteúdo de uma versão já registrada mudar.
- Bancos criados antes do Flyway são marcados como versão 1 na primeira
  subida (`baseline-on-migrate`), sem rodar o `V1` e sem perder dados.

## Contas de demonstração

Senha igual para todas: `milscale123`. Login por **CPF** (aceita com ou sem
pontuação). Todo militar semeado tem conta própria — dá pra logar com o CPF
de qualquer um dos ~200 militares gerados.

| CPF (login) | Perfil |
|---|---|
| `000.000.000-01` | Sargenteante — acesso completo |
| `000.000.000-02` | Cabo da Sargenteação — cadastros, gera escala |
| `000.000.000-03` | Sd EP da Sargenteação — só consulta |
| `000.000.000-04` | Militar Escalado — só a própria escala |
| *(qualquer CPF gerado)* | Militar Escalado por padrão |

## Perfis e regras de negócio principais

- **Perfis:** Sargenteante (tudo, inclusive publicar escala, travar dias e
  autorizar trocas), Cabo da Sargenteação (cadastros, afastamentos, gerar
  escala, triagem de trocas), Sd EP da Sargenteação (consulta) e Militar
  Escalado (a própria escala, a escala do dia e os próprios pedidos). O
  controle é feito no servidor, não só escondido na tela.
- **Geração da escala (RF08):** ninguém tira duas vagas no mesmo dia (RN05);
  intervalo mínimo de 3 dias de folga entre serviços — 3x1 (RN06); quem
  está afastado não é escalado (RN15); a fila prioriza quem está há mais
  tempo sem servir (RN01). Se faltar gente, a escala "aperta" (relaxa só o
  intervalo mínimo) em vez de deixar vaga aberta.
- **Elegibilidade:** cada tipo de serviço aceita combinações de posto,
  subunidade e curso (ex.: Cabo de Dia = Sd EP com CFC; quem é do
  Aprovisionamento só serve no rancho).
- **Trocas de serviço:** substituição ("passar meu serviço") ou troca mútua
  ("trocar de dia"). Fluxo: o outro militar aceita → triagem do Cabo →
  autorização do Sargenteante. Em troca, 2x1 é permitido e 1x1 é sempre
  proibido.
- **Dia travado × dia sólido:** o Sargenteante trava dias manualmente; além
  disso, um dia cujo serviço já começou (08h) fica imutável sozinho.
- **Afastamento cadastrado depois da escala pronta** realoca
  automaticamente só as vagas afetadas.

## Histórico

O registro detalhado de cada entrega (decisões, bugs encontrados, como cada
coisa foi testada) está em [docs/HISTORICO.md](docs/HISTORICO.md). Os planos
de melhoria em execução estão em `docs/PLANO DE EXECUÇÃO/plans/`.
