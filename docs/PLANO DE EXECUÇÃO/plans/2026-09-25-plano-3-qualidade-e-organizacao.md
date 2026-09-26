# Plano 3 — Qualidade de Código, Organização e Limpeza de Comentários (MilScale)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corrigir o que a revisão de código de 25/09/2026 apontou de má prática, duplicação, organização e peso de resposta, **sem mudar o visual nem as regras de negócio**. De quebra, deixar o código só com comentários que delimitam seções.

**Architecture:** Refatoração protegida por testes, como no Plano 1:
- **Backend:** a arquitetura hexagonal ganha uma porta de e-mail e **testes de arquitetura (ArchUnit)** que impedem regressões de camada. Isso também é material para a disciplina.
- **Frontend:** ganha hooks e componentes compartilhados (carregamento, permissões, datas, diálogos, calendário) e rotas protegidas por perfil. O visual novo fica para o Plano 4.

**Tech Stack:** Java 21, Spring Boot 3.3, ArchUnit 1.3 (novo); React 19, TypeScript, Vitest, jsdom e @testing-library/react (novos, só para testar componentes).

## Global Constraints

- **Quem commita é o usuário, tarefa a tarefa.** Cada tarefa termina num **Checkpoint**: diff, mensagem sugerida e **parar** até o usuário confirmar o commit.
- **Branch:** `melhoria/qualidade`, criado a partir da `main` atualizada. A criação depende do ok do usuário.
- **JDK:** o `JAVA_HOME` da máquina é o JDK 11. Rodar o Maven com `export JAVA_HOME="/c/Program Files/Java/jdk-23"` só no comando.
- **Comportamento:** nenhuma mudança visível de regra de negócio. As mensagens de erro continuam as mesmas, exceto as que o plano cita explicitamente.
- **Núcleo protegido:** o pacote `br.com.smartscale.core` só pode ter comentários removidos. Nenhuma linha de código muda.
- **Migrations aplicadas não são tocadas**, nem para tirar comentário: o Flyway recusa subir se o checksum mudar.
- **Regra de comentários** (vale para todo código novo daqui para frente, inclusive nos Planos 2, 4 e 5):
  - **Permitidos:**
    - delimitadores de seção: `// ---- Nome ----` em Java/TS, `{/* Nome */}` em JSX, `/* ---- Nome ---- */` em CSS, `# ---- Nome ----` em `.properties`/YAML/Dockerfile/nginx e `-- ---- Nome ----` em SQL novo;
    - a marcação de uma linha da linha de produto: `// Núcleo reutilizável (LPS)` no topo dos arquivos do `core` e `// Especialização MilScale (LPS)` no topo das classes que implementam interfaces do núcleo.
  - **Proibidos:** javadoc explicativo, comentário que narra o código ou conta a história da mudança, código comentado e `eslint-disable`.
  - **Onde fica a explicação:**
    - o **porquê** vai no nome (de classe, método, teste ou variável) e nos testes;
    - a rastreabilidade RF/RN vai para `docs/RASTREABILIDADE.md` (Task 1);
    - o histórico fica em `docs/HISTORICO.md`.
- **Verificação ao fim de cada tarefa:**
  - `cd backend && mvn test` verde;
  - `cd frontend && npm test && npm run lint && npm run build` verdes;
  - o número de avisos do lint não pode aumentar.

## Registro de execução

| Ordem | Task | Status | Commit | Testes | Resumo |
|---|---|---|---|---|---|
| 1 | 0 — Preparação | ⬜ Pendente | — | — | — |
| 2 | 1 — Matriz de rastreabilidade RF/RN | ⬜ Pendente | — | — | — |
| 3 | 2 — Limpeza de comentários (backend e configuração) | ⬜ Pendente | — | — | — |
| 4 | 3 — Limpeza de comentários e de códigos RF/RN (frontend) | ⬜ Pendente | — | — | — |
| 5 | 4 — Higiene do backend | ⬜ Pendente | — | — | — |
| 6 | 5 — Testes de arquitetura (ArchUnit) | ⬜ Pendente | — | — | — |
| 7 | 6 — Porta de e-mail e agendador como adaptadores | ⬜ Pendente | — | — | — |
| 8 | 7 — Respostas leves (Boletim e Painel) | ⬜ Pendente | — | — | — |
| 9 | 8 — Frontend: hooks, datas e organização de pastas | ⬜ Pendente | — | — | — |
| 10 | 9 — Frontend: rotas protegidas por perfil | ⬜ Pendente | — | — | — |
| 11 | 10 — Frontend: diálogos, avisos e tratamento de erro | ⬜ Pendente | — | — | — |
| 12 | 11 — Frontend: calendário e tabela de trocas compartilhados | ⬜ Pendente | — | — | — |

---

## Achados da revisão cobertos por este plano

**Backend:**
- **Comentários:** 165 blocos de javadoc e cerca de 430 linhas de comentário narrativo, com RF/RN espalhados. A história das mudanças está dentro do código.
- **Nomes completos no meio do código:** vários usos inline de `br.com.milscale...` e `java.util...` em vez de import. `@SuppressWarnings` desnecessário.
- **`CadastroApoioController`:** devolve `Object` e acessa repositório direto.
- **`AuditoriaService.registrar`:** engole qualquer exceção sem log (`catch (Exception e) {}`).
- **`EmailService`:** fica na camada de aplicação, mas depende do `JavaMailSender` (infraestrutura) e grava o e-mail do militar no log.
- **`LembreteServicoScheduler`:** é um adaptador de entrada (agendamento) que mora em `application/`.
- **Respostas pesadas:**
  - `GET /api/boletins` devolve o HTML de todos os boletins, com as imagens em base64;
  - o Painel baixa a lista inteira de militares e de afastamentos só para contar.

**Frontend:**
- **Checagens de perfil espalhadas:** 20+ comparações `usuario?.perfil === "..."` repetidas página a página.
- **Carregamento sem tratamento de erro:** `carregar()` sem `try/catch` em quase todas as páginas (Promise rejeitada sem tratamento e tela presa em "Carregando…"), e 7 `eslint-disable` para contornar dependências de `useEffect`.
- **Bug de fuso horário:** `new Date().toISOString().slice(0, 10)` aparece em 9 lugares. Depois das 21h, no horário de Brasília, "hoje" vira o dia seguinte e o filtro de "serviços futuros" e o de "afastados hoje" erram.
- **Diálogos nativos:** 12 usos de `confirm`/`alert`/`prompt`, que não seguem o visual e travam a aba.
- **Código duplicado:**
  - o calendário mensal está copiado em 3 páginas;
  - a tabela de solicitações está copiada 4 vezes em `Trocas.tsx`.
- **Rotas sem proteção:** qualquer perfil abre `/perfis`, `/auditoria` e as outras pela URL e só recebe erro da API. A rota `/bloqueio` duplica `/escala`, e não existe página 404.
- **Código morto:** `pages/EmConstrucao.tsx` não é usada.
- **Códigos na interface:** RF/RN aparecem em 20 textos de tela ("RF08 — o motor escolhe…"), vocabulário interno que o usuário não precisa ver.

---

### Task 0: Preparação

- [ ] **Step 1:** confirmar `git status` limpo na `main` e, com o ok do usuário, rodar `git checkout -b melhoria/qualidade`.
- [ ] **Step 2: Linha de base.** Anotar os números que o plano não pode piorar:
  - **Testes:** o total de testes do backend (`cd backend && mvn -q test`) e do frontend (`cd frontend && npm test`).
  - **Avisos do lint:** a contagem de avisos (`npm run lint 2>&1 | grep -c warning`).
  - **Comentários:** os números para comparar no fim, com os comandos abaixo.
```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
grep -rhE '^\s*(//|/\*|\*)' backend/src | wc -l
grep -rhE '^\s*(//|/\*|\*)|\{/\*' frontend/src | wc -l
grep -rhoE '\b(RF|RN)[0-9]{2}\b' frontend/src/pages | wc -l
```

---

### Task 1: Matriz de rastreabilidade RF/RN

Hoje a ligação "requisito → código" só existe nos comentários, e a Task 2 vai apagá-los. Antes disso, ela passa para um documento próprio. Isso também é um artefato útil para a disciplina: rastreabilidade de requisitos reutilizáveis.

**Files:**
- Create: `docs/RASTREABILIDADE.md`
- Modify: `README.md` (link na seção "Histórico")

- [ ] **Step 1: Criar `docs/RASTREABILIDADE.md`** com o conteúdo abaixo. Ele foi levantado com `grep -rlE '\bRF04\b' backend/src/main/java` para cada código, na versão de 25/09/2026.

```markdown
# MilScale — rastreabilidade de requisitos

Onde cada requisito funcional (RF) e regra de negócio (RN) do catálogo do SmartScale está implementado.
Os códigos seguem os documentos "SMART SCALE - Ativos reutilizáveis, requisitos e arquitetura v2" e
"Modelo de regras de negócio v1". Núcleo = `br.com.smartscale.core` (reutilizável pela linha de produto).

## Requisitos funcionais

| Código | Requisito | Backend | Tela | Testes |
|---|---|---|---|---|
| RF01 | Autenticar por CPF e senha | `MilScaleUserDetailsService`, `Usuario`, `ContaService`, `MilitarService` (login acompanha o CPF) | Login | `ContaServiceIntegrationTest` |
| RF04 | Manter pessoas escaladas | Núcleo: `PessoaEscalada`, `SituacaoPessoa`. MilScale: `Militar`, `PostoGraduacao`, `Subunidade`, `MilitarService`, `MilitarController` | Militares, Ficha do militar | `MassAssignmentIntegrationTest`, `MilitarCadastroIntegrationTest` |
| RF05 | Manter qualificações | `Qualificacao`, `QualificacaoService`, `QualificacaoController` | Qualificações, Militares (cursos) | `ElegibilidadeServiceTest` |
| RF06 | Manter tipos de turno e elegibilidade | Núcleo: `TipoTurno`, `MotorDeRodizio`. MilScale: `TipoServico`, `RequisitoServico`, `ElegibilidadeService`, `TipoServicoService` | Tipos de serviço | `ElegibilidadeServiceTest` |
| RF07 | Manter regras da escala | `RegraEscala`, `RegraEscalaService`, `RegraEscalaController` | Regras da escala | `MaxServicosMesIntegrationTest` |
| RF08 | Gerar a escala automaticamente | `GerarEscalaService`, `Escala`, `ServicoEscalado`, `EscalaController` | Escala do mês | `EscalaGeracaoIntegrationTest` |
| RF11 | Publicar a escala | `PublicarEscalaService`, `EscalaController` | Escala do mês | `VisibilidadeRascunhoIntegrationTest` |
| RF12 | Travar dias da escala | `BloqueioDiaService`, `ServicoEscalado.isJaComecou` | Escala do mês | `BloqueioDiaIntegrationTest` |
| RF13 | Consultar a própria escala | `MinhaEscalaService`, `MinhaEscalaController` | Minha escala, Escala do dia | `VisibilidadeRascunhoIntegrationTest` |
| RF14 | Consultar a escala completa e do dia | `ConsultaEscalaService`, `EscalaController` | Escala do mês, Escala do dia, PDF | `EscalaDoMesIntegrationTest` |
| RF15 | Pedir troca de serviço | `SolicitacaoService`, `SolicitacaoController` | Trocas | `TrocaIntervaloIntegrationTest`, `TrocaConsistenciaIntegrationTest` |
| RF17 | Triagem da troca (Cabo) | `SolicitacaoService.triagem` | Trocas, Painel | `TrocaIntervaloIntegrationTest` |
| RF18 | Autorizar a troca (Sargenteante) | `SolicitacaoService.autorizar` | Trocas, Painel | `TrocaConsistenciaIntegrationTest` |
| RF19 | Consultar solicitações | `SolicitacaoService.minhas`, `MilitarController` (histórico) | Trocas, Meu histórico, Ficha | — |
| RF25 | Perfis e permissões | `PerfilAcesso`, `Usuario`, `UsuarioService`, `UsuarioController` | Perfis e permissões | `TratadorDeErrosIntegrationTest` |
| RF26 | Missões, dispensas, férias e licenças | `Afastamento`, `AfastamentoService`, `AfastamentoController`, `AvisoService` | Missões e dispensas, Avisos | `AfastamentoReconciliacaoIntegrationTest` |

## Regras de negócio

| Código | Regra | Onde |
|---|---|---|
| RN01 | Escala a pessoa mais bem posicionada na fila (ponto de variação) | Núcleo: `CriterioDeOrdenacao`, `MotorDeRodizio`. MilScale: `CriterioOrdenacaoMilitar`, `GerarEscalaService`, `AfastamentoService` |
| RN04 | Dia travado não aceita troca nem alteração | `BloqueioDiaService`, `GerarEscalaService`, `SolicitacaoService`, `AfastamentoService` |
| RN05 | Uma pessoa não ocupa duas vagas no mesmo dia | `MotorDeRodizio`, `GerarEscalaService`, `AfastamentoService` |
| RN06 | Intervalo mínimo de folga entre serviços | `PoliticaDeDescanso`, `RegraEscala`, `GerarEscalaService`, `SolicitacaoService` |
| RN11 | Manutenção de catálogo e regras é privativa do Sargenteante | `@PreAuthorize` em `TipoServicoController`, `RegraEscalaController`, `QualificacaoController`, `FeriadoController`, `EscalaController` |
| RN14 | Publicar escala e autorizar troca são privativos do Sargenteante | `EscalaController.publicar`, `SolicitacaoController.autorizar` |
| RN15 | Quem está afastado não é escalado | `Afastamento`, `GerarEscalaService`, `AfastamentoService` |
| RN20 | O ciclo da escala é decidido por quem gera (o motor só preenche vagas) | `MotorDeRodizio`, `RegraEscala` |
```

Os testes citados que ainda não existem são criados pelo Plano 2. Se o Plano 3 rodar antes, mantenha a coluna com os testes que existem no momento e acrescente os outros quando forem criados.

- [ ] **Step 2:** no `README.md`, seção "Histórico", acrescentar a linha: `A ligação entre requisitos (RF/RN) e código está em [docs/RASTREABILIDADE.md](docs/RASTREABILIDADE.md).`

- [ ] **Step 3: Checkpoint** — diff, sugerir `docs: matriz de rastreabilidade RF/RN` e aguardar o usuário commitar.

---

### Task 2: Limpeza de comentários — backend e arquivos de configuração

**Files:**
- Modify: todos os `.java` em `backend/src/main/java` e `backend/src/test/java`
- Modify: `backend/src/main/resources/application*.properties`, `backend/src/test/resources/application-test.properties`, `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`, `docker-compose.yml`, `.gitignore` (este só com delimitadores)
- **Não tocar:** `backend/src/main/resources/db/migration/V1__schema_inicial.sql` e as outras migrations já aplicadas.

**Como aplicar, arquivo por arquivo:**
1. Apagar todo javadoc (`/** ... */`) de classe, método e campo.
2. Apagar os comentários de linha (`// ...`) e os comentários no fim de linha (`codigo; // RN05`).
3. Quando uma classe tiver mais de ~100 linhas com grupos claros de métodos, separar os grupos com `// ---- Nome ----`.
4. No topo de cada arquivo do núcleo (`smartscale-core/src/main/java/br/com/smartscale/core`), logo depois do `package`, deixar `// Núcleo reutilizável (LPS)`. No topo de `Militar`, `TipoServico` e `CriterioOrdenacaoMilitar`, deixar `// Especialização MilScale (LPS)`.
5. Se a remoção de um comentário deixar uma decisão incompreensível, **renomeie** o método ou a variável para o nome contar o porquê. Não mantenha o comentário.

**Seções sugeridas nas classes grandes:**

| Classe | Seções |
|---|---|
| `SolicitacaoService` | `Consultas`, `Pedidos`, `Decisões`, `Candidatos`, `Regras internas` |
| `GerarEscalaService` | `Geração`, `Apoio` |
| `AfastamentoService` | `Cadastro`, `Realocação` |
| `MilitarService` | `Consultas`, `Cadastro`, `Validações` |
| `DemoSeeder` (Plano 2) ou `DataSeeder` | `Contas de demonstração`, `Efetivo`, `Dados gerados` |
| `TratadorDeErros` | `Entrada inválida`, `Não encontrado e conflito`, `Segurança`, `Inesperado` |

**Exemplo** (`PoliticaDeDescanso`):

Antes:
```java
/**
 * RN06 num lugar so. Antes cada service (geracao, realocacao por
 * afastamento, troca) tinha a propria janela de datas e o proprio
 * default - qualquer ajuste de regra precisava ser replicado em tres.
 */
public final class PoliticaDeDescanso {

    /** Usado so quando o tipo de servico nao tem RegraEscala cadastrada. */
    public static final int INTERVALO_MINIMO_PADRAO = 7;
```
Depois:
```java
public final class PoliticaDeDescanso {

    public static final int INTERVALO_MINIMO_SEM_REGRA_CADASTRADA = 7;
```
(O renome da constante tira a necessidade do comentário. Atualizar os usos: `grep -rn INTERVALO_MINIMO_PADRAO backend/src`.)

**Exemplo** (`application.properties`), depois:
```properties
# ---- Banco (desenvolvimento) ----
spring.datasource.url=jdbc:h2:file:./data/milscale;MODE=MySQL;DATABASE_TO_LOWER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# ---- Schema ----
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

- [ ] **Step 1: Contagem inicial** — rodar e anotar:
```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
grep -rnE '^\s*(//|/\*|\*)' backend/src/main/java backend/src/test/java | wc -l
```

- [ ] **Step 2: Limpar o núcleo** (os 5 arquivos de `smartscale-core/src/main/java/br/com/smartscale/core`, mais o `CatalogoDeCriterios` do Plano 5), só comentários. Conferir com `git diff --stat smartscale-core/src` que só há linhas removidas, mais a marcação LPS.

- [ ] **Step 3: Limpar `milscale/domain`** (entidades, enums, `PoliticaDeDescanso`, `CriterioOrdenacaoMilitar`).

- [ ] **Step 4: Limpar `milscale/application`** (services), aplicando as seções da tabela.

- [ ] **Step 5: Limpar `milscale/adapters`** (web, dto, persistence, config).

- [ ] **Step 6: Limpar os testes.** A intenção de cada teste já está no nome do método. Tire os javadocs de classe e os comentários de cenário. Se algum cenário não se explicar sozinho, extraia um método com nome descritivo (ex.: `servicoDoMilitarBDoisDiasDepois()`).

- [ ] **Step 7: Limpar configuração** — `.properties`, `Dockerfile`s, `nginx.conf`, `docker-compose.yml`: só `# ---- Nome ----`.

- [ ] **Step 8: Verificar que não sobrou comentário fora da regra:**
```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
grep -rnE '^\s*(//|/\*|\*)' backend/src/main/java backend/src/test/java \
  | grep -vE ':\s*// (---- [^-]+ ----|Núcleo reutilizável \(LPS\)|Especialização MilScale \(LPS\))\s*$'
grep -rnE '[;{})]\s*//' backend/src/main/java backend/src/test/java
grep -nE '^\s*#' backend/src/main/resources/*.properties backend/src/test/resources/*.properties \
  backend/Dockerfile frontend/Dockerfile frontend/nginx.conf docker-compose.yml | grep -vE '# ---- [^-]+ ----\s*$'
```
Expected: as três saídas vazias.

- [ ] **Step 9: Rodar tudo** — `cd backend && mvn -q test` → PASS, com o mesmo número de testes da linha de base.

- [ ] **Step 10: Checkpoint** — diff (grande, mas só remoções, delimitadores e renomes), sugerir `refactor: comentarios reduzidos a delimitadores de secao (backend e configuracao)` e aguardar o usuário commitar.

---

### Task 3: Limpeza de comentários e de códigos RF/RN — frontend

**Files:**
- Modify: todos os `.ts`, `.tsx` e `.css` em `frontend/src`, e `frontend/vite.config.ts`
- Delete: `frontend/src/pages/EmConstrucao.tsx` (sem uso: `grep -rn EmConstrucao frontend/src` só acha o próprio arquivo)

**Regra:** a mesma da Task 2. Em JSX, blocos grandes podem ter `{/* Nome */}`, com até 3 palavras. No CSS, `/* ---- Nome ---- */`.

**Textos de tela sem RF/RN:** trocar as 20 menções por texto simples, dizendo o que a pessoa faz ali:

| Arquivo | Antes | Depois |
|---|---|---|
| `EscalaDoMes.tsx` | `RF08 — o motor escolhe quem está há mais tempo sem tirar serviço` | `Quem está há mais tempo sem tirar serviço entra primeiro` |
| `EscalaDoMes.tsx` | `Só o Sargenteante publica a escala (RN14).` | `Só o Sargenteante publica a escala.` |
| `EscalaDoMes.tsx` | `… nem pelo Sargenteante (RN04).` | `… nem pelo Sargenteante.` |
| `Militares.tsx` | `Dados da carteira de identidade militar — RF04` | `Dados da carteira de identidade militar` |
| `Militares.tsx` | `Seu perfil só consulta o efetivo (RF04 / RN11). …` | `Seu perfil só consulta o efetivo. …` |
| `Qualificacoes.tsx` | `… por um tipo de serviço (RF05)` | `… por um tipo de serviço` |
| `TiposServico.tsx` | `<p className="sub">RF06</p>` | remover a linha |
| `TiposServico.tsx` | `… tipos de serviço (RN11). …` | `… tipos de serviço. …` |
| `RegrasEscala.tsx` | `… alimenta o motor de geração (RF07)` | `… vale para a próxima escala gerada` |
| `RegrasEscala.tsx` | `… (RN11). …` | remover o parêntese |
| `Trocas.tsx` | `RF15 — escolha um serviço seu, …` | `Escolha um serviço seu, …` |
| `PerfisPermissoes.tsx` | `… no sistema — RF25` | `Quem tem acesso ao quê no sistema` |
| `MissoesDispensas.tsx` | `… nesse período (RF26 / RN15)` | `… nesse período` |
| `MissoesDispensas.tsx` | `RF26 — pode selecionar mais de uma pessoa …` | `Pode selecionar mais de uma pessoa …` |

Para os que sobrarem, usar `grep -rnE '\b(RF|RN)[0-9]{2}\b' frontend/src`. O critério é o mesmo: remover o código e manter a frase.

- [ ] **Step 1:** apagar `pages/EmConstrucao.tsx`.
- [ ] **Step 2:** limpar os comentários de `api/`, `context/`, `components/`, `utils/`, `pages/`, `styles.css` e `vite.config.ts`.
- [ ] **Step 3:** trocar os textos com RF/RN da tabela.
- [ ] **Step 4: Verificar:**
```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale/frontend
grep -rnE '^\s*(//|/\*|\*)' src vite.config.ts | grep -vE ':\s*(// ---- [^-]+ ----|/\* ---- [^-]+ ---- \*/)\s*$'
grep -rnE '[;{})]\s*//' src
grep -rnE '\b(RF|RN)[0-9]{2}\b' src
```
Expected: as três saídas vazias. Os `eslint-disable` saem na Task 8, e aqui podem continuar.
- [ ] **Step 5:** `npm test && npm run lint && npm run build` → PASS.
- [ ] **Step 6: Checkpoint** — diff, sugerir `refactor(front): comentarios reduzidos a delimitadores e textos sem codigos RF/RN` e aguardar o usuário commitar.

---

### Task 4: Higiene do backend

**Files:**
> **Se o Plano 5 já tiver sido executado:** a Task 6 dele substituiu o `CadastroApoioController` por `PostoGraduacaoController` e `SubunidadeController`, que já têm services próprios. Nesse caso, pular aqui tudo o que for do `CadastroApoioService` e fazer só o restante da task.

- Create: `application/CadastroApoioService.java`
- Modify: `adapters/web/CadastroApoioController.java`, `application/AuditoriaService.java`, e todo arquivo com nome totalmente qualificado inline
- Test: `backend/src/test/java/br/com/milscale/milscale/application/AuditoriaServiceTest.java`

- [ ] **Step 1: Teste que falha para a auditoria** (hoje a falha some sem deixar rastro)

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.LogAuditoriaRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuditoriaServiceTest {

    @Test
    void falhaAoGravar_naoPropagaMasFicaNoLog() {
        LogAuditoriaRepository logs = Mockito.mock(LogAuditoriaRepository.class);
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        when(usuarios.findByLogin(any())).thenReturn(Optional.empty());
        when(logs.save(any())).thenThrow(new IllegalStateException("banco fora"));
        ListAppender<ILoggingEvent> capturado = new ListAppender<>();
        capturado.start();
        ((Logger) LoggerFactory.getLogger(AuditoriaService.class)).addAppender(capturado);

        new AuditoriaService(logs, usuarios).registrar("00000000001", "ESCALA_GERADA", "x");

        assertThat(capturado.list).anyMatch(e -> e.getFormattedMessage().contains("ESCALA_GERADA"));
    }
}
```
Run → FAIL (nada no log).

- [ ] **Step 2: `AuditoriaService` loga a falha**

```java
    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
```
```java
        } catch (Exception e) {
            log.warn("Falha ao gravar auditoria {} de {}: {}", acao, login, e.getMessage());
        }
```

- [ ] **Step 3: `CadastroApoioService` com retorno tipado e ordenado**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.PostoGraduacaoRepository;
import br.com.milscale.milscale.adapters.persistence.SubunidadeRepository;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.milscale.milscale.domain.Subunidade;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CadastroApoioService {

    private final PostoGraduacaoRepository postoGraduacaoRepository;
    private final SubunidadeRepository subunidadeRepository;

    public CadastroApoioService(PostoGraduacaoRepository postoGraduacaoRepository, SubunidadeRepository subunidadeRepository) {
        this.postoGraduacaoRepository = postoGraduacaoRepository;
        this.subunidadeRepository = subunidadeRepository;
    }

    public List<PostoGraduacao> postos() {
        return postoGraduacaoRepository.findAll(Sort.by(Sort.Direction.DESC, "nivelHierarquico"));
    }

    public List<Subunidade> subunidades() {
        return subunidadeRepository.findAll(Sort.by("sigla"));
    }
}
```
No `CadastroApoioController`: injetar o service e devolver `List<PostoGraduacao>` / `List<Subunidade>`.

- [ ] **Step 4: Imports no lugar de nomes completos.** Listar:
```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale/backend
grep -rnE '[^.a-zA-Z](br\.com\.milscale|java\.(util|time))\.[a-z.]*[A-Z][A-Za-z]+' src/main/java src/test/java | grep -v '^[^:]*:[0-9]*:import '
```
Em cada ocorrência, adicionar o `import` e usar o nome simples. Casos conhecidos:
- `br.com.smartscale.core.SituacaoPessoa` em `AfastamentoService`, `SolicitacaoService` e `MilitarRepository`;
- `java.util.UUID`, `java.util.ArrayList` e `java.util.Comparator` em `AfastamentoService`;
- `java.time.temporal.ChronoUnit` em `Militar` e `GerarEscalaService`;
- os tipos `TipoServico`, `ServicoEscalado`, `Afastamento`, `Solicitacao` e `Militar` qualificados em `MilitarController`/`SolicitacaoController`.

Conferir: o comando acima não devolve mais nada.

- [ ] **Step 5:** `mvn -q test` → PASS.
- [ ] **Step 6: Checkpoint** — diff, sugerir `refactor: imports, auditoria com log de falha e cadastro de apoio tipado` e aguardar o usuário commitar.

---

### Task 5: Testes de arquitetura com ArchUnit

As regras da arquitetura hexagonal viram teste. Se alguém fizer o núcleo depender do MilScale, ou a aplicação depender de web ou de infraestrutura de e-mail, o build quebra. Uma das regras **falha de propósito agora** (e-mail na aplicação) e é resolvida na Task 6.

**Files:**
- Modify: `backend/pom.xml`
- Test: `backend/src/test/java/br/com/milscale/milscale/ArquiteturaTest.java`

- [ ] **Step 1: Dependência**
```xml
    <dependency>
      <groupId>com.tngtech.archunit</groupId>
      <artifactId>archunit-junit5</artifactId>
      <version>1.3.0</version>
      <scope>test</scope>
    </dependency>
```

- [ ] **Step 2: Regras.** O isolamento do núcleo não precisa de regra aqui: desde o Plano 5, Task 1, ele é o módulo `smartscale-core`, cujo `pom.xml` não tem dependências. Se o núcleo tentar importar algo do produto, o build dele já quebra.
```java
package br.com.milscale.milscale;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.milscale", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioNaoConheceAplicacaoNemAdaptadores = noClasses()
            .that().resideInAPackage("br.com.milscale.milscale.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "br.com.milscale.milscale.application..", "br.com.milscale.milscale.adapters..", "org.springframework..");

    @ArchTest
    static final ArchRule aplicacaoNaoConheceWebNemServlet = noClasses()
            .that().resideInAPackage("br.com.milscale.milscale.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "br.com.milscale.milscale.adapters.web..", "br.com.milscale.milscale.adapters.config..",
                    "org.springframework.web..", "jakarta.servlet..");

    @ArchTest
    static final ArchRule aplicacaoNaoConheceInfraestruturaDeEmail = noClasses()
            .that().resideInAPackage("br.com.milscale.milscale.application..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.mail..");

    @ArchTest
    static final ArchRule controllersSoNoAdaptadorWeb = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("br.com.milscale.milscale.adapters.web..");
}
```

- [ ] **Step 3: Rodar** — `mvn -q test -Dtest=ArquiteturaTest`
Expected: **só** `aplicacaoNaoConheceInfraestruturaDeEmail` falha, por causa do `EmailService`. Se outra regra falhar, registre a violação na nota de execução e corrija nesta tarefa. As prováveis:
- entidade usando classe do Spring: mover para um service;
- service da aplicação importando `adapters.web.dto`: usar o record da aplicação.

- [ ] **Step 4:** marcar a regra de e-mail como pendente, **só até a Task 6**: comentar a anotação `@ArchTest` dela com `// ---- Pendente: Task 6 ----` e registrar isso na nota de execução. É a única exceção temporária à regra de comentários, e sai na próxima tarefa.

- [ ] **Step 5:** `mvn -q test` → PASS.
- [ ] **Step 6: Checkpoint** — diff, sugerir `test: regras de arquitetura hexagonal com ArchUnit` e aguardar o usuário commitar.

---

### Task 6: Porta de e-mail e agendador como adaptadores

**Files:**
- Create: `application/EnvioDeEmail.java` (porta), `application/LembreteServicoService.java`, `adapters/email/EnvioDeEmailSmtp.java`, `adapters/agendamento/LembreteServicoAgendador.java`
- Delete: `application/EmailService.java`, `application/LembreteServicoScheduler.java`
- Modify: `adapters/web/LembreteController.java`, `ArquiteturaTest.java` (reativar a regra)
- Test: `backend/src/test/java/br/com/milscale/milscale/application/LembreteServicoIntegrationTest.java`

**Interfaces:**
- Produces: `interface EnvioDeEmail { void enviar(String destinatario, String assunto, String corpo); }`, `LembreteServicoService.enviarPara(LocalDate dia): int`.

- [ ] **Step 1: Teste que falha** (o lembrete hoje não tem teste nenhum)

```java
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
```
Run → FAIL de compilação.

- [ ] **Step 2: Porta e serviço de aplicação**

```java
package br.com.milscale.milscale.application;

public interface EnvioDeEmail {
    void enviar(String destinatario, String assunto, String corpo);
}
```
```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.SituacaoEscala;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class LembreteServicoService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final EnvioDeEmail envioDeEmail;

    public LembreteServicoService(ServicoEscaladoRepository servicoEscaladoRepository, EnvioDeEmail envioDeEmail) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.envioDeEmail = envioDeEmail;
    }

    public int enviarPara(LocalDate dia) {
        int processados = 0;
        for (ServicoEscalado s : servicoEscaladoRepository.findByData(dia)) {
            if (s.getMilitar() == null || s.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) continue;
            String email = s.getMilitar().getEmail();
            if (email == null || email.isBlank()) continue;
            envioDeEmail.enviar(email,
                    "MilScale — você está escalado amanhã (" + dia.format(DATA_BR) + ")",
                    "Olá, " + s.getMilitar().getNomeExibicao() + ".\n\n"
                            + "Você está escalado para \"" + s.getTipoServico().getNome() + "\" no dia " + dia.format(DATA_BR) + ".\n\n"
                            + "Qualquer imprevisto, procure o Cabo ou o Sargenteante com antecedência.\n\n"
                            + "— MilScale, 5º Batalhão de Suprimento");
            processados++;
        }
        return processados;
    }
}
```

- [ ] **Step 3: Adaptadores**

```java
package br.com.milscale.milscale.adapters.email;

import br.com.milscale.milscale.application.EnvioDeEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EnvioDeEmailSmtp implements EnvioDeEmail {

    private static final Logger log = LoggerFactory.getLogger(EnvioDeEmailSmtp.class);

    private final JavaMailSender mailSender;
    private final boolean habilitado;
    private final String remetente;

    public EnvioDeEmailSmtp(JavaMailSender mailSender,
                            @Value("${milscale.email.habilitado:false}") boolean habilitado,
                            @Value("${spring.mail.username:}") String remetente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remetente = remetente.isBlank() ? "milscale@exemplo.mil.br" : remetente;
    }

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        if (destinatario == null || destinatario.isBlank()) return;
        if (!habilitado) {
            log.info("[email desabilitado] para {} | {}", mascarar(destinatario), assunto);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.info("Email enviado para {}: {}", mascarar(destinatario), assunto);
        } catch (Exception e) {
            log.error("Falha ao enviar email para {}: {}", mascarar(destinatario), e.getMessage());
        }
    }

    static String mascarar(String email) {
        int arroba = email.indexOf('@');
        return arroba <= 1 ? "***" : email.charAt(0) + "***" + email.substring(arroba);
    }
}
```
```java
package br.com.milscale.milscale.adapters.agendamento;

import br.com.milscale.milscale.application.LembreteServicoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LembreteServicoAgendador {

    private static final Logger log = LoggerFactory.getLogger(LembreteServicoAgendador.class);

    private final LembreteServicoService lembreteServicoService;

    public LembreteServicoAgendador(LembreteServicoService lembreteServicoService) {
        this.lembreteServicoService = lembreteServicoService;
    }

    @Scheduled(cron = "${milscale.lembrete.cron:0 0 18 * * *}")
    public void enviarLembretesDeAmanha() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        log.info("Lembrete de serviço de {}: {} email(s) processado(s)", amanha, lembreteServicoService.enviarPara(amanha));
    }
}
```
Apagar `EmailService.java` e `LembreteServicoScheduler.java`. No `LembreteController`, trocar a dependência por `LembreteServicoService` e a chamada por `enviarPara(dia)`.

- [ ] **Step 4: Reativar a regra** do `ArquiteturaTest` (tirar o comentário da Task 5).
- [ ] **Step 5:** `mvn -q test` → PASS, com `ArquiteturaTest` inteiro verde.
- [ ] **Step 6: Checkpoint** — diff, sugerir `refactor: porta de email e agendador como adaptadores (hexagonal)` e aguardar o usuário commitar.

---

### Task 7: Respostas leves — Boletim e Painel

**Files:**
- Create: `application/BoletimResumo.java`, `application/PainelResumo.java`, `application/PainelService.java`, `adapters/web/PainelController.java`
- Modify: `application/BoletimService.java`, `adapters/web/BoletimController.java`, repositórios `MilitarRepository`, `ServicoEscaladoRepository`, `SolicitacaoRepository`, `AfastamentoRepository`
- Modify (front): `src/api/types.ts`, `pages/Boletim.tsx`, `pages/Avisos.tsx`, `pages/Painel.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/adapters/web/RespostasLevesIntegrationTest.java`

**Interfaces:**
- Produces:
  - `GET /api/boletins` → `List<BoletimResumo>`, sem `conteudoHtml`;
  - `GET /api/boletins/{id}` → `Boletim` completo, sem mudança;
  - `GET /api/painel/resumo` → `PainelResumo(long militaresAtivos, long vagasAbertasNoMes, long trocasEmTriagem, long trocasAguardandoAutorizacao, long afastadosHoje)`, só para Cabo e Sargenteante.

- [ ] **Step 1: Testes que falham**

```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.BoletimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RespostasLevesIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private BoletimService boletimService;

    @Test
    @WithUserDetails("00000000004")
    void listaDeBoletins_naoTrazOConteudo() throws Exception {
        boletimService.criar("1", "Com imagem", "<p>" + "x".repeat(5000) + "</p>", null, null, "00000000001");
        mvc.perform(get("/api/boletins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Com imagem"))
                .andExpect(content().string(not(containsString("conteudoHtml"))));
    }

    @Test
    @WithUserDetails("00000000001")
    void resumoDoPainel_contaSemBaixarListas() throws Exception {
        mvc.perform(get("/api/painel/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.militaresAtivos", greaterThan(100)));
    }

    @Test
    @WithUserDetails("00000000004")
    void resumoDoPainel_eSoDaSargenteacao() throws Exception {
        mvc.perform(get("/api/painel/resumo")).andExpect(status().isForbidden());
    }
}
```
Run → FAIL.

- [ ] **Step 2: Boletim resumido**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Boletim;
import java.time.LocalDateTime;

public record BoletimResumo(Long id, String numero, String titulo, String autor, LocalDateTime dataPublicacao,
                            LocalDateTime dataAtualizacao, String avisoRelacionado, String avisoRelacionadoDescricao) {

    public static BoletimResumo de(Boletim b) {
        return new BoletimResumo(b.getId(), b.getNumero(), b.getTitulo(), b.getAutor().getNomeExibicao(),
                b.getDataPublicacao(), b.getDataAtualizacao(), b.getAvisoRelacionado(), b.getAvisoRelacionadoDescricao());
    }
}
```
`BoletimService.listar()` passa a devolver `List<BoletimResumo>` com `findAllByOrderByDataPublicacaoDesc().stream().map(BoletimResumo::de).toList()`. O controller segue o tipo.

- [ ] **Step 3: Resumo do Painel**
  - **Repositórios:**
    - `MilitarRepository`: `long countBySituacao(SituacaoPessoa situacao);`
    - `ServicoEscaladoRepository`: `long countByDataBetweenAndMilitarIsNull(LocalDate inicio, LocalDate fim);`
    - `SolicitacaoRepository`: `long countBySituacao(SituacaoSolicitacao situacao);`
    - `AfastamentoRepository`:
```java
    @Query("select count(distinct a.militar.id) from Afastamento a where a.dataInicio <= :dia and a.dataFim >= :dia")
    long contarMilitaresAfastadosEm(@Param("dia") LocalDate dia);
```
  - **`PainelResumo` e `PainelService`:**
```java
package br.com.milscale.milscale.application;

public record PainelResumo(long militaresAtivos, long vagasAbertasNoMes, long trocasEmTriagem,
                           long trocasAguardandoAutorizacao, long afastadosHoje) {}
```
```java
package br.com.milscale.milscale.application;

import br.com.smartscale.core.SituacaoPessoa;
import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.SituacaoSolicitacao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class PainelService {

    private final MilitarRepository militarRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final AfastamentoRepository afastamentoRepository;

    public PainelService(MilitarRepository militarRepository, ServicoEscaladoRepository servicoEscaladoRepository,
                         SolicitacaoRepository solicitacaoRepository, AfastamentoRepository afastamentoRepository) {
        this.militarRepository = militarRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.afastamentoRepository = afastamentoRepository;
    }

    public PainelResumo resumo() {
        YearMonth mes = YearMonth.now();
        return new PainelResumo(
                militarRepository.countBySituacao(SituacaoPessoa.ATIVO),
                servicoEscaladoRepository.countByDataBetweenAndMilitarIsNull(mes.atDay(1), mes.atEndOfMonth()),
                solicitacaoRepository.countBySituacao(SituacaoSolicitacao.EM_TRIAGEM),
                solicitacaoRepository.countBySituacao(SituacaoSolicitacao.AGUARDANDO_AUTORIZACAO),
                afastamentoRepository.contarMilitaresAfastadosEm(LocalDate.now()));
    }
}
```
  - **`PainelController`:**
```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.PainelResumo;
import br.com.milscale.milscale.application.PainelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
@PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping("/resumo")
    public PainelResumo resumo() {
        return painelService.resumo();
    }
}
```

- [ ] **Step 4: Frontend**
  - **`types.ts`:**
    - `Boletim` segue igual, usada no detalhe;
    - nova `BoletimResumo` (`id, numero?, titulo, autor, dataPublicacao, dataAtualizacao?, avisoRelacionado?, avisoRelacionadoDescricao?`);
    - nova `PainelResumo` com os 5 números.
  - **`Boletim.tsx`:**
    - a lista vira `BoletimResumo[]`, e o autor é exibido como `b.autor` (string);
    - ao expandir um boletim, buscar `/api/boletins/${id}` e guardar num `Map<number, string>` de conteúdos já carregados;
    - "Editar" busca o detalhe antes de abrir o formulário.
  - **`Avisos.tsx`:** a lista de boletins vira `BoletimResumo[]`. Continua funcionando, porque só usa `avisoRelacionado` e `titulo`.
  - **`Painel.tsx`:**
    - trocar as buscas de `/api/militares` e `/api/afastamentos`, e o cálculo de vagas, por uma única `/api/painel/resumo`;
    - `trocasPendentes` = `trocasEmTriagem + trocasAguardandoAutorizacao`.

    As listas de triagem/autorização e o "Serviço de hoje" continuam como estão.

- [ ] **Step 5:** `mvn -q test` → PASS; `npm test && npm run build` → PASS. Teste manual: abrir e editar um boletim com imagem; o Painel mostra os mesmos números de antes.
- [ ] **Step 6: Checkpoint** — diff, sugerir `perf: boletins sem conteudo na lista e resumo do painel no servidor` e aguardar o usuário commitar.

---

### Task 8: Frontend — hooks, datas e organização de pastas

**Files:**
- Create: `src/hooks/useCarregamento.ts`, `src/hooks/usePermissoes.ts`, `src/utils/datas.ts`, `src/utils/datas.test.ts`
- Move:
  - `src/components/Shell.tsx` → `src/components/layout/Shell.tsx`, com `PageHeader` extraído para `src/components/layout/PageHeader.tsx`;
  - `src/components/NotificacaoSino.tsx` → `src/components/layout/NotificacaoSino.tsx`;
  - `src/components/MilitarDetalheOverlay.tsx` → `src/components/militar/MilitarDetalheOverlay.tsx`;
  - `src/components/RichEditor.tsx` → `src/components/boletim/RichEditor.tsx`.
- Modify: todas as páginas (imports, permissões, datas e carregamento)

**Estrutura final de `src/`:**
```
src/
  api/          client.ts, types.ts
  components/
    layout/     Shell, PageHeader, NotificacaoSino
    ui/         (Tasks 10 e 11, e Plano 4)
    militar/    MilitarDetalheOverlay
    boletim/    RichEditor
  context/      AuthContext
  hooks/        useCarregamento, usePermissoes
  pages/        (uma por rota)
  utils/        datas, formatadores, mascaras, ordemTipos, perfis, afastamentoTipos
```

- [ ] **Step 1: Teste das datas (falha)** — o caso das 23h30 prova o bug do `toISOString`.

```ts
import { describe, expect, it } from "vitest";
import { celulasDoMes, dataDoMes, deslocarMes, hojeISO, mesISO } from "./datas";

describe("datas", () => {
  it("hoje é o dia local, mesmo tarde da noite", () => {
    expect(hojeISO(new Date(2026, 8, 25, 23, 30))).toBe("2026-09-25");
  });

  it("mês no formato da API", () => {
    expect(mesISO(2026, 0)).toBe("2026-01");
  });

  it("desloca mês atravessando o ano", () => {
    expect(deslocarMes({ ano: 2026, mes: 11 }, 1)).toEqual({ ano: 2027, mes: 0 });
    expect(deslocarMes({ ano: 2026, mes: 0 }, -1)).toEqual({ ano: 2025, mes: 11 });
  });

  it("células do mês começam no dia da semana certo", () => {
    const celulas = celulasDoMes(2026, 8);
    expect(celulas.slice(0, 3)).toEqual([null, null, 1]);
    expect(celulas.filter((c) => c !== null)).toHaveLength(30);
    expect(dataDoMes(2026, 8, 5)).toBe("2026-09-05");
  });
});
```
(setembro de 2026 começa numa terça: duas células vazias.)

- [ ] **Step 2: `src/utils/datas.ts`**

```ts
export interface MesAno {
  ano: number;
  mes: number;
}

const doisDigitos = (n: number) => String(n).padStart(2, "0");

export function hojeISO(agora: Date = new Date()): string {
  return `${agora.getFullYear()}-${doisDigitos(agora.getMonth() + 1)}-${doisDigitos(agora.getDate())}`;
}

export function mesISO(ano: number, mes: number): string {
  return `${ano}-${doisDigitos(mes + 1)}`;
}

export function dataDoMes(ano: number, mes: number, dia: number): string {
  return `${mesISO(ano, mes)}-${doisDigitos(dia)}`;
}

export function deslocarMes({ ano, mes }: MesAno, delta: number): MesAno {
  const d = new Date(ano, mes + delta, 1);
  return { ano: d.getFullYear(), mes: d.getMonth() };
}

export function celulasDoMes(ano: number, mes: number): (number | null)[] {
  const deslocamento = new Date(ano, mes, 1).getDay();
  const dias = new Date(ano, mes + 1, 0).getDate();
  return [...Array(deslocamento).fill(null), ...Array.from({ length: dias }, (_, i) => i + 1)];
}

export function nomeDoMes(ano: number, mes: number): string {
  const texto = new Date(ano, mes, 1).toLocaleDateString("pt-BR", { month: "long", year: "numeric" });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
```

- [ ] **Step 3: `src/hooks/useCarregamento.ts`**

```ts
import { useCallback, useEffect, useState } from "react";
import { ApiError } from "../api/client";

export function useCarregamento<T>(buscar: () => Promise<T>, dependencias: unknown[]) {
  const [dados, setDados] = useState<T | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [versao, setVersao] = useState(0);

  useEffect(() => {
    let ativo = true;
    buscar()
      .then((resultado) => {
        if (!ativo) return;
        setDados(resultado);
        setErro(null);
      })
      .catch((e) => {
        if (ativo) setErro(e instanceof ApiError ? e.message : "Não foi possível carregar os dados.");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [...dependencias, versao]);

  const recarregar = useCallback(() => {
    setCarregando(true);
    setVersao((v) => v + 1);
  }, []);

  return { dados, carregando, erro, recarregar };
}
```
Uso típico, trocando `useState` + `carregar()` + `useEffect` + `eslint-disable`:
```tsx
  const { dados: tipos, carregando, erro, recarregar } = useCarregamento(
    () => api.get<TipoServico[]>("/api/tipos-servico"), []);
```
Mostrar `erro` num `<div className="error-box">` onde hoje a página só mostra "Carregando…".

- [ ] **Step 4: `src/hooks/usePermissoes.ts`**

```ts
import { useAuth } from "../context/AuthContext";

export function usePermissoes() {
  const perfil = useAuth().usuario?.perfil;
  const sargenteante = perfil === "SARGENTEANTE";
  const cabo = perfil === "CABO_SARGENTEACAO";
  return {
    perfil,
    gerenciaCadastros: sargenteante || cabo,
    geraEscala: sargenteante || cabo,
    fazTriagem: sargenteante || cabo,
    publicaEscala: sargenteante,
    autorizaTrocas: sargenteante,
    mantemConfiguracoes: sargenteante,
    daSargenteacao: sargenteante || cabo || perfil === "SD_EP_SARGENTEACAO",
  };
}
```
Substituir cada `usuario?.perfil === ...` das páginas pela permissão equivalente:

| Hoje | Depois |
|---|---|
| `CABO \|\| SARGENTEANTE` em Militares, Ficha, Missões, Boletim | `gerenciaCadastros` |
| `CABO \|\| SARGENTEANTE` em Escala do mês (gerar) | `geraEscala` |
| `CABO \|\| SARGENTEANTE` em Trocas e Painel (triagem) | `fazTriagem` |
| `SARGENTEANTE` em Escala do mês (publicar e travar) | `publicaEscala` |
| `SARGENTEANTE` em Trocas e Painel (autorizar) | `autorizaTrocas` |
| `SARGENTEANTE` em Tipos, Regras, Qualificações, Feriados | `mantemConfiguracoes` |

Conferir com `grep -rn 'perfil ===' src/pages src/components`: só pode restar dentro de `App.tsx`/`Shell.tsx` (roteamento e menu).

- [ ] **Step 5: Datas locais** — trocar os 9 `new Date().toISOString().slice(0, 10)` por `hojeISO()`. Nos calendários, trocar a montagem manual de células e datas e o `mudarMes` por `celulasDoMes`, `dataDoMes` e `deslocarMes`. Conferir: `grep -rn 'toISOString().slice' src` → vazio.

- [ ] **Step 6: Mover os arquivos** para as pastas da estrutura acima, ajustando os imports (o `tsc -b` do build aponta cada um). O `PageHeader` sai do `Shell.tsx` para o seu próprio arquivo.

- [ ] **Step 7: Sem `eslint-disable`.** Os 7 que existem desaparecem com o `useCarregamento`. No `RichEditor`, o efeito de valor inicial vira `useLayoutEffect` com um `ref` que já guarda o valor inicial, sem dependência externa. Conferir: `grep -rn eslint-disable src` → vazio.

- [ ] **Step 8:** `npm test && npm run lint && npm run build` → PASS, com o número de avisos do lint menor que a linha de base.
  Teste manual:
  1. Navegar por todas as telas com os 4 perfis.
  2. Com o backend desligado, uma tela mostra mensagem de erro em vez de ficar em "Carregando…".

- [ ] **Step 9: Checkpoint** — diff, sugerir `refactor(front): hooks de carregamento e permissao, datas locais e pastas organizadas` e aguardar o usuário commitar.

---

### Task 9: Frontend — rotas protegidas por perfil e página 404

**Files:**
- Create: `src/pages/NaoEncontrada.tsx`
- Modify: `src/App.tsx`, `src/components/layout/Shell.tsx` (o item "Bloqueio de dias" passa a apontar para `/escala`)

- [ ] **Step 1: `RotaProtegida` aceita a lista de perfis**

```tsx
type Perfil = Usuario["perfil"];

function RotaProtegida({ children, perfis }: { children: React.ReactNode; perfis?: Perfil[] }) {
  const { usuario, carregando } = useAuth();
  if (carregando) return <div style={{ padding: 40 }}>Carregando…</div>;
  if (!usuario) return <Navigate to="/login" replace />;
  if (perfis && !perfis.includes(usuario.perfil)) return <Navigate to="/" replace />;
  return <Shell>{children}</Shell>;
}
```
(Se a Task 4 do Plano 2 já tiver sido feita, manter o redirecionamento de `trocarSenha` antes da checagem de perfil.)

- [ ] **Step 2: Perfis por rota** (constantes no topo do `App.tsx`):

```tsx
const SARGENTEACAO: Perfil[] = ["SARGENTEANTE", "CABO_SARGENTEACAO", "SD_EP_SARGENTEACAO"];
const GESTAO: Perfil[] = ["SARGENTEANTE", "CABO_SARGENTEACAO"];
const SARGENTEANTE: Perfil[] = ["SARGENTEANTE"];
```

| Rota | Perfis |
|---|---|
| `/painel`, `/qualificacoes`, `/missoes` | `GESTAO` |
| `/militares`, `/militares/:id` | `SARGENTEACAO` |
| `/tipos-servico`, `/regras`, `/feriados`, `/perfis`, `/auditoria` | `SARGENTEANTE` |
| `/escala`, `/minha-escala`, `/avisos`, `/trocas`, `/boletim`, `/historico`, `/minha-conta`, `/escala/pdf/:data` | todos (sem `perfis`) |

- [ ] **Step 3:** remover a rota `/bloqueio` e apontar o item "Bloqueio de dias" do menu para `/escala`.

- [ ] **Step 4: Página 404**

```tsx
import { Link } from "react-router-dom";
import { PageHeader } from "../components/layout/PageHeader";

export function NaoEncontradaPage() {
  return (
    <>
      <PageHeader title="Página não encontrada" subtitle="O endereço digitado não existe no MilScale" />
      <div className="body">
        <Link to="/" className="btn btn-primary">Ir para o início</Link>
      </div>
    </>
  );
}
```
No `App.tsx`: `<Route path="*" element={<RotaProtegida><NaoEncontradaPage /></RotaProtegida>} />`.

- [ ] **Step 5:** `npm run build` → PASS. Teste manual:
  1. Como `000.000.000-04`, abrir `/perfis`, `/auditoria` e `/painel` pela URL: volta para a tela inicial.
  2. Abrir `/qualquer-coisa`: aparece a página 404.
- [ ] **Step 6: Checkpoint** — diff, sugerir `feat(front): rotas protegidas por perfil e pagina 404` e aguardar o usuário commitar.

---

### Task 10: Frontend — diálogos, avisos e tratamento de erro em toda ação

**Files:**
- Create: `src/components/ui/Feedback.tsx`, `src/components/ui/Feedback.test.tsx`
- Modify: `package.json` (dev: `jsdom`, `@testing-library/react`), `vite.config.ts` (bloco `test`), `src/main.tsx` (provider), `src/styles.css` (diálogo e avisos), páginas com `confirm`/`alert`/`prompt` e ações sem `try/catch`

**Interfaces:**
- Produces: `useFeedback()` → `{ avisar(mensagem, tipo?), confirmar(mensagem): Promise<boolean>, perguntar(mensagem, { obrigatorio? }): Promise<string | null> }`.

- [ ] **Step 1: Ambiente de teste de componente**
```bash
cd frontend && npm install -D jsdom @testing-library/react
```
Em `vite.config.ts`, no objeto do `defineConfig`: `test: { environment: "jsdom" },`. Em cima do arquivo, acrescentar `/// <reference types="vitest/config" />` para o TypeScript aceitar a chave `test`.

- [ ] **Step 2: Teste que falha**

```tsx
import { act, fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { FeedbackProvider, useFeedback } from "./Feedback";

function Botao({ onResultado }: { onResultado: (v: boolean) => void }) {
  const { confirmar } = useFeedback();
  return <button onClick={async () => onResultado(await confirmar("Remover este boletim?"))}>abrir</button>;
}

describe("Feedback", () => {
  it("confirmar resolve true ao confirmar e false ao voltar", async () => {
    const resultados: boolean[] = [];
    render(<FeedbackProvider><Botao onResultado={(v) => resultados.push(v)} /></FeedbackProvider>);

    fireEvent.click(screen.getByText("abrir"));
    expect(screen.getByText("Remover este boletim?")).toBeTruthy();
    await act(async () => fireEvent.click(screen.getByText("Confirmar")));

    fireEvent.click(screen.getByText("abrir"));
    await act(async () => fireEvent.click(screen.getByText("Voltar")));

    expect(resultados).toEqual([true, false]);
  });
});
```

- [ ] **Step 3: `src/components/ui/Feedback.tsx`**

```tsx
import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from "react";

type TipoAviso = "sucesso" | "erro" | "info";

interface Aviso {
  id: number;
  tipo: TipoAviso;
  mensagem: string;
}

interface Dialogo {
  tipo: "confirmar" | "perguntar";
  mensagem: string;
  obrigatorio: boolean;
  resolver: (valor: boolean | string | null) => void;
}

interface Feedback {
  avisar: (mensagem: string, tipo?: TipoAviso) => void;
  confirmar: (mensagem: string) => Promise<boolean>;
  perguntar: (mensagem: string, opcoes?: { obrigatorio?: boolean }) => Promise<string | null>;
}

const FeedbackContext = createContext<Feedback | undefined>(undefined);

export function FeedbackProvider({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const [dialogo, setDialogo] = useState<Dialogo | null>(null);
  const [resposta, setResposta] = useState("");
  const proximoId = useRef(1);

  const avisar = useCallback((mensagem: string, tipo: TipoAviso = "info") => {
    const id = proximoId.current++;
    setAvisos((atual) => [...atual, { id, tipo, mensagem }]);
    setTimeout(() => setAvisos((atual) => atual.filter((a) => a.id !== id)), 5000);
  }, []);

  const confirmar = useCallback(
    (mensagem: string) =>
      new Promise<boolean>((resolve) =>
        setDialogo({ tipo: "confirmar", mensagem, obrigatorio: false, resolver: (v) => resolve(v === true) })),
    []);

  const perguntar = useCallback(
    (mensagem: string, opcoes?: { obrigatorio?: boolean }) =>
      new Promise<string | null>((resolve) => {
        setResposta("");
        setDialogo({ tipo: "perguntar", mensagem, obrigatorio: opcoes?.obrigatorio ?? false,
          resolver: (v) => resolve(typeof v === "string" ? v : null) });
      }),
    []);

  const fechar = useCallback((valor: boolean | string | null) => {
    setDialogo((atual) => {
      atual?.resolver(valor);
      return null;
    });
  }, []);

  useEffect(() => {
    if (!dialogo) return;
    const aoTeclar = (e: KeyboardEvent) => {
      if (e.key === "Escape") fechar(dialogo.tipo === "confirmar" ? false : null);
    };
    document.addEventListener("keydown", aoTeclar);
    return () => document.removeEventListener("keydown", aoTeclar);
  }, [dialogo, fechar]);

  const podeConfirmar = dialogo?.tipo !== "perguntar" || !dialogo.obrigatorio || resposta.trim() !== "";

  return (
    <FeedbackContext.Provider value={{ avisar, confirmar, perguntar }}>
      {children}
      {dialogo && (
        <div className="dialogo-fundo" onClick={() => fechar(dialogo.tipo === "confirmar" ? false : null)}>
          <div className="dialogo" role="dialog" aria-modal="true" aria-label={dialogo.mensagem} onClick={(e) => e.stopPropagation()}>
            <p>{dialogo.mensagem}</p>
            {dialogo.tipo === "perguntar" && (
              <textarea autoFocus rows={3} value={resposta} onChange={(e) => setResposta(e.target.value)} />
            )}
            <div className="dialogo-acoes">
              <button className="btn btn-outline" onClick={() => fechar(dialogo.tipo === "confirmar" ? false : null)}>
                Voltar
              </button>
              <button className="btn btn-primary" autoFocus={dialogo.tipo === "confirmar"} disabled={!podeConfirmar}
                onClick={() => fechar(dialogo.tipo === "confirmar" ? true : resposta.trim())}>
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
      <div className="avisos" aria-live="polite">
        {avisos.map((a) => (
          <div key={a.id} className={`aviso aviso-${a.tipo}`}>{a.mensagem}</div>
        ))}
      </div>
    </FeedbackContext.Provider>
  );
}

export function useFeedback() {
  const ctx = useContext(FeedbackContext);
  if (!ctx) throw new Error("useFeedback deve ser usado dentro de um FeedbackProvider");
  return ctx;
}
```
CSS (`styles.css`, seção `/* ---- Diálogos e avisos ---- */`):
```css
.dialogo-fundo { position: fixed; inset: 0; background: rgba(28, 33, 23, 0.55); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 16px; }
.dialogo { background: var(--white); border: 1px solid var(--border); border-radius: 8px; padding: 20px; width: 420px; max-width: 100%; display: flex; flex-direction: column; gap: 14px; }
.dialogo textarea { width: 100%; border: 1px solid var(--border); border-radius: 5px; padding: 8px; font: inherit; }
.dialogo-acoes { display: flex; justify-content: flex-end; gap: 8px; }
.avisos { position: fixed; bottom: 16px; right: 16px; display: flex; flex-direction: column; gap: 8px; z-index: 1100; }
.aviso { padding: 10px 14px; border-radius: 6px; font-size: 13px; background: var(--dark); color: var(--white); max-width: 360px; }
.aviso-sucesso { background: var(--green-pill-text); }
.aviso-erro { background: var(--red-text); }
```
Em `main.tsx`, envolver o `<App />` com `<FeedbackProvider>`.

- [ ] **Step 4: Trocar os 12 diálogos nativos.** Listar com `grep -rnE '\b(confirm|alert|prompt)\(' src`. Padrões de troca:
  - **Confirmação:** `if (!confirm("X")) return;` → `if (!(await confirmar("X"))) return;`
  - **Pergunta opcional:** `prompt("Motivo (opcional):") ?? ""` → `(await perguntar("Motivo (opcional):")) ?? ""`, e se a pessoa voltar, a ação é cancelada.
  - **Pergunta obrigatória:** `prompt("Motivo da recusa:")` com `if (!comentario) return;` → `const comentario = await perguntar("Motivo da recusa:", { obrigatorio: true }); if (comentario === null) return;`
  - **Alerta:** `alert(msg)` → `avisar(msg, "sucesso" | "erro")`. A senha temporária do Plano 2, Task 4, usa `confirmar` com o texto da senha, para a pessoa ter tempo de anotar.

- [ ] **Step 5: Toda ação com tratamento de erro.** Nas ações das páginas que hoje não têm `try/catch`, envolver no padrão abaixo:
  - **Trocas:** `cancelar`, `decidirConfirmacao`, `decidirTriagem`;
  - **Militares:** `GerenciarCursos.alternar`;
  - **Boletim:** `remover`;
  - **Missões:** `cancelarGrupo`;
  - **Qualificações:** `salvarEdicao`;
  - **Tipos de serviço:** `salvarEfetivo`;
  - **Regras:** `salvar`;
  - **Notificações:** `clicarNotificacao`, `marcarTodasLidas`.
```tsx
    try {
      await api.post(...);
      avisar("Pedido cancelado.", "sucesso");
      recarregar();
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.", "erro");
    }
```
O texto de sucesso usa o mesmo verbo do botão ("Cancelar pedido" → "Pedido cancelado.", "Publicar" → "Escala publicada.").

- [ ] **Step 6:** conferir `grep -rnE '\b(confirm|alert|prompt)\(' src` → só as funções do `useFeedback`. Rodar `npm test && npm run lint && npm run build` → PASS.
- [ ] **Step 7: Checkpoint** — diff, sugerir `feat(front): dialogos e avisos proprios, e erro tratado em toda acao` e aguardar o usuário commitar.

---

### Task 11: Frontend — calendário e tabela de solicitações compartilhados

**Files:**
- Create: `src/components/ui/CalendarioMensal.tsx`, `src/components/trocas/TabelaSolicitacoes.tsx`
- Modify: `pages/EscalaDoMes.tsx`, `pages/Avisos.tsx`, `pages/MinhaEscala.tsx`, `pages/Trocas.tsx`, `src/styles.css`

- [ ] **Step 1: `CalendarioMensal`**

```tsx
import { celulasDoMes, dataDoMes, hojeISO } from "../../utils/datas";

const DIAS_SEMANA = ["DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"];

export interface InfoDoDia {
  rotulo?: string;
  destaque?: "atencao" | "positivo";
  habilitado?: boolean;
}

interface Props {
  ano: number;
  mes: number;
  diaSelecionado: string | null;
  onSelecionar: (dataISO: string) => void;
  infoDoDia?: (dataISO: string) => InfoDoDia;
}

export function CalendarioMensal({ ano, mes, diaSelecionado, onSelecionar, infoDoDia }: Props) {
  const hoje = hojeISO();
  return (
    <div className="calendar-grid">
      {DIAS_SEMANA.map((d) => <div key={d} className="dow">{d}</div>)}
      {celulasDoMes(ano, mes).map((dia, i) => {
        if (dia === null) return <div key={`vazio-${i}`} className="calendar-cell empty" />;
        const dataISO = dataDoMes(ano, mes, dia);
        const info = infoDoDia?.(dataISO) ?? {};
        const classes = [
          "calendar-cell",
          info.destaque && `destaque-${info.destaque}`,
          diaSelecionado === dataISO && "selecionado",
          dataISO === hoje && "hoje",
        ].filter(Boolean).join(" ");
        return (
          <button key={dataISO} className={classes} disabled={info.habilitado === false}
            aria-pressed={diaSelecionado === dataISO} onClick={() => onSelecionar(dataISO)}>
            {dia}
            {info.rotulo && <span className="tipo">{info.rotulo}</span>}
          </button>
        );
      })}
    </div>
  );
}
```
CSS (`/* ---- Calendário ---- */`): as cores dos estados, que hoje são inline em cada página, viram classes:
```css
.calendar-cell { background: #fbfcfa; border: 1px solid var(--border-2); text-align: left; color: var(--dark); }
.calendar-cell:disabled { background: transparent; border-color: transparent; cursor: default; }
.calendar-cell.destaque-atencao { background: var(--amber-bg); }
.calendar-cell.destaque-atencao .tipo { color: var(--amber-text); }
.calendar-cell.destaque-positivo { background: var(--green-pill-bg); }
.calendar-cell.destaque-positivo .tipo { color: var(--green-pill-text); }
.calendar-cell.hoje { box-shadow: inset 0 0 0 2px var(--accent); }
.calendar-cell.selecionado { background: var(--sidebar-active); color: #fff; }
.calendar-cell.selecionado .tipo { color: #d8e2cc; }
```

- [ ] **Step 2: Usar nas 3 páginas.** Cada uma passa a ter só a regra do próprio dia:
  - **Escala do mês:** `rotulo` = "N serviços" (+ " · travado"); `destaque: "atencao"` se houver vaga aberta; `habilitado` = o dia tem serviços.
  - **Avisos:** `rotulo` = o tipo do aviso ou "N avisos"; `destaque: "positivo"` para feriado e `"atencao"` para missão.
  - **Minha escala:** `rotulo` = o nome do serviço; `destaque: "positivo"` nos dias em que a pessoa está escalada.

  Apagar das páginas as constantes `DIAS_SEMANA`, a montagem de `celulas` e os estilos inline de célula.

- [ ] **Step 3: `TabelaSolicitacoes`** — uma tabela só para as 4 abas de Trocas. As colunas variam por aba:

```tsx
import type { ReactNode } from "react";
import type { Solicitacao } from "../../api/types";

export interface Coluna {
  titulo: string;
  valor: (s: Solicitacao) => ReactNode;
}

export function TabelaSolicitacoes({ itens, colunas, vazio, acoes }: {
  itens: Solicitacao[];
  colunas: Coluna[];
  vazio: string;
  acoes?: (s: Solicitacao) => ReactNode;
}) {
  if (itens.length === 0) return <div className="vazio">{vazio}</div>;
  return (
    <table>
      <thead>
        <tr>
          {colunas.map((c) => <th key={c.titulo}>{c.titulo}</th>)}
          {acoes && <th />}
        </tr>
      </thead>
      <tbody>
        {itens.map((s) => (
          <tr key={s.id}>
            {colunas.map((c) => <td key={c.titulo}>{c.valor(s)}</td>)}
            {acoes && <td className="acoes">{acoes(s)}</td>}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```
Em `Trocas.tsx`:
- declarar as colunas reutilizáveis uma vez: `quemPediu`, `servico`, `dia`, `tipo`, `assume`, `justificativa`, `parecerDoCabo`, `situacao`;
- montar cada aba com a lista delas;
- mover `SituacaoPill` e `TipoTrocaPill` para `src/components/trocas/`.

CSS: `.vazio { padding: 20px; color: var(--grey); font-size: 13px; } td.acoes { white-space: nowrap; }`.

- [ ] **Step 4:** `npm test && npm run lint && npm run build` → PASS. Teste manual: as três telas de calendário e as quatro abas de Trocas funcionam igual a antes, agora com o dia de hoje destacado no calendário.
- [ ] **Step 5: Checkpoint** — diff, sugerir `refactor(front): calendario mensal e tabela de solicitacoes compartilhados` e aguardar o usuário commitar.

---

## Fechamento do Plano 3

- [ ] Rodar tudo: `cd backend && mvn test` e `cd frontend && npm test && npm run lint && npm run build`.
- [ ] Repetir as contagens da Task 0 e registrar antes e depois, na nota de fechamento:
  - comentários no backend e no frontend;
  - RF/RN nas telas;
  - `eslint-disable`;
  - diálogos nativos;
  - avisos do lint.
- [ ] Rodar `/code-review` sobre o branch `melhoria/qualidade` e tratar os achados confirmados.
- [ ] O merge na `main` e o push ficam com o usuário.
