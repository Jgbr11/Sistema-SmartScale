# Plano 1 — Melhoria da Estrutura do Código (MilScale)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deixar o código do MilScale mais seguro de evoluir — tipos fortes no lugar de strings mágicas, contratos de entrada validados, erros HTTP previsíveis, regras de negócio num lugar só, geração de escala sem consultas repetidas, frontend sem funções duplicadas, configuração por ambiente e schema de banco versionado — **sem mudar o comportamento visível** para quem usa o sistema.

**Architecture:** Refatoração incremental protegida pelos 22 testes que já existem, mais testes novos escritos *antes* de cada mudança (TDD / testes de caracterização). A arquitetura hexagonal é mantida: `core/domain` (núcleo reutilizável da LPS) não é tocado; as mudanças ficam em `milscale/domain`, `milscale/application` e `milscale/adapters`. Cada tarefa entrega algo testável sozinho.

**Tech Stack:** Java 21, Spring Boot 3.3.4 (Web, Data JPA, Security, Validation), Hibernate 6.5, H2 (dev/teste) e MySQL 8 (Docker), Flyway, JUnit 5 + AssertJ + MockMvc + spring-security-test; React 19 + Vite 8 + TypeScript 6 + Vitest.

## Global Constraints

- **Quem commita é o usuário, tarefa a tarefa.** Nenhum implementador (humano, agente ou subagente) roda `git commit` / `git push`. Onde um plano normal teria "Commit", este plano tem **"Checkpoint"**: mostrar `git status` + `git diff --stat` + o diff relevante, sugerir a mensagem de commit e **parar** até o usuário confirmar que commitou. Só então começa a próxima tarefa. A revisão é feita sobre `git diff` da árvore de trabalho.
- Todo o trabalho acontece no branch `melhoria/estrutura`, criado pelo usuário ou com a autorização dele.
- Comportamento visível não muda: mesmas rotas, mesmos formatos JSON de resposta, mesmas mensagens de regra de negócio. Exceções explícitas: respostas de erro ganham corpo `{"erro": ...}` onde antes vinham vazias (403) ou como 500.
- O pacote `br.com.milscale.core.domain` **não pode ser alterado** (é o ativo reutilizável da LPS).
- Código novo segue o idioma do código existente: identificadores e mensagens em português, injeção por construtor, comentários curtos explicando o *porquê*.
- Backend: `cd backend && mvn test` precisa terminar verde ao fim de toda tarefa. Frontend: `cd frontend && npm run build` (e, a partir da Tarefa 7, `npm test`) também.
- Mensagens de erro de negócio continuam saindo como `{"erro": "<mensagem>"}` — o frontend (`src/api/client.ts`) lê exatamente a chave `erro`.

---

## Mapa de arquivos

**Backend — criar**
- `backend/src/main/java/br/com/milscale/milscale/adapters/web/ErroResposta.java` — corpo padrão de erro.
- `backend/src/main/java/br/com/milscale/milscale/domain/{SituacaoEscala,SituacaoServico,SituacaoSolicitacao,TipoTroca,TipoAfastamento}.java` — enums que substituem strings.
- `backend/src/main/java/br/com/milscale/milscale/domain/PoliticaDeDescanso.java` — regra única de intervalo (RN06 e 1x1 em troca).
- `backend/src/main/java/br/com/milscale/milscale/application/UsuarioLogadoService.java` — "quem é o usuário logado".
- `backend/src/main/java/br/com/milscale/milscale/application/ConsultaEscalaService.java` — leituras de escala (tira repositório do controller).
- `backend/src/main/java/br/com/milscale/milscale/application/ContaService.java` — `/me` e troca de senha (tira repositório do controller).
- `backend/src/main/java/br/com/milscale/milscale/adapters/web/dto/*.java` — records de request com Bean Validation.
- `backend/src/main/resources/db/migration/V1__schema_inicial.sql` — schema versionado.
- Testes em `backend/src/test/java/br/com/milscale/milscale/...` (listados em cada tarefa).

**Backend — modificar**: `pom.xml`, entidades de `domain/`, services de `application/`, controllers de `adapters/web/`, repositórios de `adapters/persistence/`, `SecurityConfig`, `application*.properties`, testes existentes.

**Frontend — criar**: `frontend/src/utils/formatadores.ts`, `frontend/src/utils/*.test.ts`, `frontend/.env.example`.
**Frontend — modificar**: `package.json`, `vite.config.ts`, `index.html`, `src/api/client.ts`, `src/components/Shell.tsx`, páginas com funções duplicadas, `nginx.conf`.

**Raiz**: `.gitignore`, `.env.example`, `docker-compose.yml`, `README.md`, `docs/HISTORICO.md`.

---

### Task 0: Preparação — branch, `.gitignore`, dependência de teste e linha de base

**Files:**
- Create: `.gitignore`
- Modify: `backend/pom.xml`

**Interfaces:**
- Produces: dependência `spring-security-test` disponível para as tarefas seguintes (anotação `@WithUserDetails`).

- [ ] **Step 1: Criar o branch de trabalho**

```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
git checkout -b melhoria/estrutura   # só com o ok do usuário
```

- [ ] **Step 2: Criar `.gitignore` na raiz**

```gitignore
# Backend
backend/target/
backend/data/
*.log

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.env
frontend/.env.local

# Docker / segredos
.env

# IDEs / SO
.idea/
.vscode/
*.iml
.DS_Store
Thumbs.db
```

- [ ] **Step 3: Adicionar `spring-security-test` no `backend/pom.xml`**, logo depois da dependência `spring-boot-starter-test`:

```xml
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-test</artifactId>
      <scope>test</scope>
    </dependency>
```

- [ ] **Step 4: Rodar a linha de base**

```bash
cd backend && mvn -q test
cd ../frontend && npm install && npm run build
```

Expected: backend com `Tests run: 22, Failures: 0, Errors: 0`; frontend com build sem erros. Se algo já falhar aqui, **pare e reporte** — não é regressão deste plano.

- [ ] **Step 5: Checkpoint** — mostrar `git status` e `git diff`, sugerir a mensagem `chore: gitignore e spring-security-test` e aguardar o usuário commitar.

---

### Task 1: Tratamento de erros padronizado

Hoje um 403 sai sem corpo, `MethodArgumentTypeMismatchException` (ex.: `/api/militares/abc`) vira 500 e não existe rede de segurança para exceções inesperadas. Esta tarefa adiciona handlers — **sem** transformar erros do próprio Spring MVC (404 de rota inexistente, 405, parâmetro obrigatório faltando) em 500.

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/adapters/web/ErroResposta.java`
- Modify: `backend/src/main/java/br/com/milscale/milscale/adapters/web/TratadorDeErros.java`
- Test: `backend/src/test/java/br/com/milscale/milscale/adapters/web/TratadorDeErrosIntegrationTest.java`

**Interfaces:**
- Produces: `record ErroResposta(String erro)`; handler de `MethodArgumentNotValidException` que responde 400 com a `defaultMessage` do primeiro erro de campo (usado pela Tarefa 4).

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.adapters.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que todo erro da API volta com status previsivel e corpo
 * {"erro": ...} - o frontend (api/client.ts) depende dessa chave pra
 * mostrar a mensagem certa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TratadorDeErrosIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000004") // Militar Escalado
    void acessoNegado_volta403ComCorpo() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    @WithUserDetails("00000000001") // Sargenteante
    void idInexistente_volta404() throws Exception {
        mvc.perform(get("/api/militares/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void idComTipoErrado_volta400EmVezDe500() throws Exception {
        mvc.perform(get("/api/militares/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void rotaInexistente_continua404EmVezDe500() throws Exception {
        mvc.perform(get("/api/rota-que-nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("00000000001")
    void parametroObrigatorioFaltando_continua400() throws Exception {
        mvc.perform(get("/api/avisos")) // exige ?mes=
                .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && mvn -q test -Dtest=TratadorDeErrosIntegrationTest`
Expected: FAIL em `acessoNegado_volta403ComCorpo` (sem `$.erro`) e em `idComTipoErrado_volta400EmVezDe500` (500).

- [ ] **Step 3: Criar `ErroResposta.java`**

```java
package br.com.milscale.milscale.adapters.web;

/** Corpo padrao de toda resposta de erro da API - o front le a chave "erro". */
public record ErroResposta(String erro) {}
```

- [ ] **Step 4: Reescrever `TratadorDeErros.java`**

```java
package br.com.milscale.milscale.adapters.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * Tratamento de erro centralizado - toda falha vira um status HTTP
 * previsivel com corpo {"erro": ...}, nunca um 500 cru nem um 403 vazio.
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErroResposta> dataInvalida(DateTimeParseException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta("Data invalida: " + ex.getParsedString()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(NoSuchElementException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Registro nao encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta(ex.getMessage() != null ? ex.getMessage() : "Requisicao invalida"));
    }

    /** Bean Validation nos DTOs de entrada (@Valid) - devolve a mensagem do primeiro campo invalido. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Requisicao invalida");
        return ResponseEntity.badRequest().body(new ErroResposta(msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoErrado(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta("Valor invalido para '" + ex.getName() + "'"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> violacaoDeIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResposta("Dado duplicado ou invalido (ex.: CPF/login ja cadastrado)"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta("Corpo da requisicao invalido ou faltando campos"));
    }

    /** @PreAuthorize negado. Sem este handler o Spring devolveria 403 sem corpo. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErroResposta("Acesso negado"));
    }

    /**
     * Rede de seguranca. Excecoes do proprio Spring MVC (rota inexistente,
     * metodo nao suportado, parametro faltando...) ja carregam o status
     * certo via ErrorResponse - so o que sobrar vira 500, sempre logado.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperado(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            String detalhe = er.getBody().getDetail();
            return ResponseEntity.status(er.getStatusCode())
                    .body(new ErroResposta(detalhe != null ? detalhe : "Requisicao invalida"));
        }
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResposta("Erro interno. Tente de novo ou avise o suporte."));
    }
}
```

- [ ] **Step 5: Rodar os testes**

Run: `cd backend && mvn -q test`
Expected: PASS — 22 antigos + 5 novos.

- [ ] **Step 6: Checkpoint** — diff, sugerir a mensagem `refactor: tratamento de erros padronizado` e aguardar o usuário commitar.

---

### Task 2: Enums de domínio no lugar de strings mágicas

Situações de escala, serviço e solicitação, tipo de troca e tipo de afastamento são `String` com os valores em comentário. Um erro de digitação (`"AUTORIZDA"`) hoje compila. Os enums mantêm **os mesmos nomes**, então o JSON e as colunas do banco continuam iguais (`@Enumerated(EnumType.STRING)`).

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/domain/SituacaoEscala.java`, `SituacaoServico.java`, `SituacaoSolicitacao.java`, `TipoTroca.java`, `TipoAfastamento.java`
- Modify: `domain/Escala.java`, `domain/ServicoEscalado.java`, `domain/Solicitacao.java`, `domain/Afastamento.java`
- Modify: `adapters/persistence/ServicoEscaladoRepository.java:30`, `adapters/persistence/SolicitacaoRepository.java:10,12`
- Modify: `application/GerarEscalaService.java`, `PublicarEscalaService.java`, `LembreteServicoScheduler.java`, `SolicitacaoService.java`, `AfastamentoService.java`, `AvisoService.java`, `adapters/web/AfastamentoController.java`
- Test: `backend/src/test/java/br/com/milscale/milscale/domain/EnumsContratoTest.java`; modify `EscalaGeracaoIntegrationTest.java:141`, `TrocaIntervaloIntegrationTest.java:100,141,144`

**Interfaces:**
- Produces (todos em `br.com.milscale.milscale.domain`):
  - `enum SituacaoEscala { RASCUNHO, PUBLICADA, ENCERRADA }`
  - `enum SituacaoServico { PREVISTO, CUMPRIDO, SUBSTITUIDO }`
  - `enum SituacaoSolicitacao { AGUARDANDO_SUBSTITUTO, EM_TRIAGEM, AGUARDANDO_AUTORIZACAO, AUTORIZADA, NEGADA, CANCELADA }`
  - `enum TipoTroca { SUBSTITUICAO, TROCA_MUTUA }`
  - `enum TipoAfastamento { MISSAO, DISPENSA, FERIAS, LICENCA, CURSO, OUTRO }`
  - `AfastamentoService.cadastrarMissao(List<Long>, TipoAfastamento, String, LocalDate, LocalDate, String)`

- [ ] **Step 1: Escrever o teste de contrato (falha por não compilar)**

`backend/src/test/java/br/com/milscale/milscale/domain/EnumsContratoTest.java`:

```java
package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os nomes destes enums sao contrato com o frontend (src/api/types.ts) e
 * com o que ja esta gravado no banco. Renomear qualquer valor quebra os
 * dois - este teste acusa isso na hora.
 */
class EnumsContratoTest {

    private static String[] nomes(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).toArray(String[]::new);
    }

    @Test
    void situacaoEscala() {
        assertThat(nomes(SituacaoEscala.values())).containsExactly("RASCUNHO", "PUBLICADA", "ENCERRADA");
    }

    @Test
    void situacaoServico() {
        assertThat(nomes(SituacaoServico.values())).containsExactly("PREVISTO", "CUMPRIDO", "SUBSTITUIDO");
    }

    @Test
    void situacaoSolicitacao() {
        assertThat(nomes(SituacaoSolicitacao.values())).containsExactly(
                "AGUARDANDO_SUBSTITUTO", "EM_TRIAGEM", "AGUARDANDO_AUTORIZACAO", "AUTORIZADA", "NEGADA", "CANCELADA");
    }

    @Test
    void tipoTroca() {
        assertThat(nomes(TipoTroca.values())).containsExactly("SUBSTITUICAO", "TROCA_MUTUA");
    }

    @Test
    void tipoAfastamento() {
        assertThat(nomes(TipoAfastamento.values())).containsExactly("MISSAO", "DISPENSA", "FERIAS", "LICENCA", "CURSO", "OUTRO");
    }
}
```

E ajustar os testes existentes para os tipos novos:
- `EscalaGeracaoIntegrationTest.java:141`: `.tipo("FERIAS")` → `.tipo(TipoAfastamento.FERIAS)` (e `import br.com.milscale.milscale.domain.TipoAfastamento;`).
- `TrocaIntervaloIntegrationTest.java:100` e `:144`: `.isEqualTo("AGUARDANDO_SUBSTITUTO")` → `.isEqualTo(SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO)`.
- `TrocaIntervaloIntegrationTest.java:141`: `.isEqualTo("TROCA_MUTUA")` → `.isEqualTo(TipoTroca.TROCA_MUTUA)`.
  (o arquivo já faz `import br.com.milscale.milscale.domain.*;`)

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && mvn -q test`
Expected: FAIL de compilação — `SituacaoEscala` etc. não existem.

- [ ] **Step 3: Criar os 5 enums**, um arquivo cada, no padrão:

```java
package br.com.milscale.milscale.domain;

/** RF08/RF11 - ciclo de vida de uma escala gerada. Nomes gravados no banco: nao renomear. */
public enum SituacaoEscala { RASCUNHO, PUBLICADA, ENCERRADA }
```

```java
package br.com.milscale.milscale.domain;

/** Situacao de uma vaga escalada. Nomes gravados no banco: nao renomear. */
public enum SituacaoServico { PREVISTO, CUMPRIDO, SUBSTITUIDO }
```

```java
package br.com.milscale.milscale.domain;

/** RF15-RF19 - etapas do fluxo de troca. Nomes gravados no banco: nao renomear. */
public enum SituacaoSolicitacao {
    AGUARDANDO_SUBSTITUTO, EM_TRIAGEM, AGUARDANDO_AUTORIZACAO, AUTORIZADA, NEGADA, CANCELADA;

    /** Texto legivel pra mensagens de erro ("em triagem", "aguardando autorizacao"...). */
    public String legivel() {
        return name().toLowerCase().replace('_', ' ');
    }
}
```

```java
package br.com.milscale.milscale.domain;

/** SUBSTITUICAO: o substituto assume e o solicitante fica sem nada. TROCA_MUTUA: os dois trocam de dia. */
public enum TipoTroca { SUBSTITUICAO, TROCA_MUTUA }
```

```java
package br.com.milscale.milscale.domain;

/** RF26 - motivos de afastamento. Nomes gravados no banco: nao renomear. */
public enum TipoAfastamento { MISSAO, DISPENSA, FERIAS, LICENCA, CURSO, OUTRO }
```

- [ ] **Step 4: Trocar os campos das entidades**

`Escala.java` (campo `situacao`):
```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SituacaoEscala situacao = SituacaoEscala.RASCUNHO;
```

`ServicoEscalado.java` (campo `situacao`):
```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SituacaoServico situacao = SituacaoServico.PREVISTO;
```

`Solicitacao.java` (campos `tipoTroca` e `situacao`):
```java
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_troca", nullable = false, length = 20)
    @Builder.Default
    private TipoTroca tipoTroca = TipoTroca.SUBSTITUICAO;
```
```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private SituacaoSolicitacao situacao = SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO;
```

`Afastamento.java` (campo `tipo`):
```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoAfastamento tipo;
```

- [ ] **Step 5: Trocar as assinaturas dos repositórios**

`ServicoEscaladoRepository.java`:
```java
    List<ServicoEscalado> findByTipoServico_IdAndSituacao(Long tipoServicoId, SituacaoServico situacao);
```
`SolicitacaoRepository.java`:
```java
    List<Solicitacao> findBySituacaoOrderByDataSolicitacaoAsc(SituacaoSolicitacao situacao);
    List<Solicitacao> findBySubstituto_IdAndSituacaoOrderByDataSolicitacaoAsc(Long substitutoId, SituacaoSolicitacao situacao);
```
(adicionar os imports de `br.com.milscale.milscale.domain.SituacaoServico` / `SituacaoSolicitacao`).

- [ ] **Step 6: Atualizar os usos nos services e controller**

- `GerarEscalaService`: `.situacao("RASCUNHO")` → `.situacao(SituacaoEscala.RASCUNHO)`; as duas ocorrências `.situacao("PREVISTO")` → `.situacao(SituacaoServico.PREVISTO)`.
- `PublicarEscalaService`: `escala.setSituacao("PUBLICADA")` → `escala.setSituacao(SituacaoEscala.PUBLICADA)`.
- `LembreteServicoScheduler`: `if (!"PUBLICADA".equals(s.getEscala().getSituacao())) continue;` → `if (s.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) continue;`
- `AvisoService` (montagem do aviso de afastamento): `primeiro.getTipo()` → `primeiro.getTipo().name()` (o record `Aviso.tipo` continua `String`, porque também carrega `"FERIADO"`).
- `AfastamentoService.cadastrarMissao`: parâmetro `String tipo` → `TipoAfastamento tipo`.
- `AfastamentoController.cadastrar`: `String tipo = String.valueOf(body.get("tipo"));` → `TipoAfastamento tipo = TipoAfastamento.valueOf(String.valueOf(body.get("tipo")));` (valor desconhecido lança `IllegalArgumentException` → 400; a Tarefa 4 substitui este `Map` por DTO).
- `SolicitacaoService` — substituir cada literal pelo enum:
  - `"AGUARDANDO_SUBSTITUTO"` → `SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO` (linhas do `aguardandoMinhaConfirmacao`, dois builders, `confirmarSubstituto`);
  - `"EM_TRIAGEM"`, `"AGUARDANDO_AUTORIZACAO"`, `"AUTORIZADA"`, `"NEGADA"`, `"CANCELADA"` → constantes correspondentes;
  - `.tipoTroca("SUBSTITUICAO")` / `.tipoTroca("TROCA_MUTUA")` → `TipoTroca.SUBSTITUICAO` / `TipoTroca.TROCA_MUTUA`;
  - `if ("TROCA_MUTUA".equals(s.getTipoTroca()))` → `if (s.getTipoTroca() == TipoTroca.TROCA_MUTUA)`;
  - `cancelar`: `if (!s.getSituacao().equals("AGUARDANDO_SUBSTITUTO") && !s.getSituacao().equals("EM_TRIAGEM"))` → `if (s.getSituacao() != SituacaoSolicitacao.AGUARDANDO_SUBSTITUTO && s.getSituacao() != SituacaoSolicitacao.EM_TRIAGEM)`;
  - `findByTipoServico_IdAndSituacao(..., "PREVISTO")` → `SituacaoServico.PREVISTO`;
  - `exigirSituacao` passa a ser:

```java
    private void exigirSituacao(Solicitacao s, SituacaoSolicitacao esperada) {
        if (s.getSituacao() != esperada) {
            throw new IllegalArgumentException("Esta solicitação já não está mais em " + esperada.legivel());
        }
    }
```

- [ ] **Step 7: Confirmar que não sobrou literal**

Run: `cd backend && grep -rnE '"(RASCUNHO|PUBLICADA|PREVISTO|AGUARDANDO_SUBSTITUTO|EM_TRIAGEM|AGUARDANDO_AUTORIZACAO|AUTORIZADA|NEGADA|CANCELADA|TROCA_MUTUA|SUBSTITUICAO)"' src/main`
Expected: nenhuma linha (só aparecem ações de auditoria como `"TROCA_AUTORIZADA"`, que não casam com o padrão exato — se aparecerem, conferir que são nomes de ação de auditoria e não situação).

- [ ] **Step 8: Rodar todos os testes**

Run: `cd backend && mvn -q test`
Expected: PASS (22 + 5 da Tarefa 1 + 5 novos).

- [ ] **Step 9: Checkpoint** — diff, sugerir a mensagem `refactor: enums de dominio no lugar de strings` e aguardar o usuário commitar.

---

### Task 3: Usuário logado e controllers sem repositório

A linha `usuarioRepository.findByLogin(login).orElseThrow()` aparece 15 vezes. Além disso `EscalaController`, `AuthController` e `MinhaEscalaController` usam repositórios direto, furando a camada de aplicação da arquitetura hexagonal.

**Files:**
- Create: `application/UsuarioLogadoService.java`, `application/ConsultaEscalaService.java`, `application/ContaService.java`
- Modify: `application/SolicitacaoService.java`, `AfastamentoService.java`, `BoletimService.java`, `NotificacaoService.java`
- Modify: `adapters/web/EscalaController.java`, `AuthController.java`, `MinhaEscalaController.java`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/ContaServiceIntegrationTest.java`

**Interfaces:**
- Produces:
  - `UsuarioLogadoService.usuario(String login): Usuario` — lança `NoSuchElementException("Usuário não encontrado")`
  - `UsuarioLogadoService.militar(String login): Militar`
  - `ConsultaEscalaService.listar(): List<Escala>`, `.buscar(Long id): Escala`, `.doDia(LocalDate data): List<ServicoEscalado>`
  - `ContaService.registrarAcesso(String login): Usuario`, `.trocarSenha(String login, String senhaAtual, String senhaNova): void`

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ContaServiceIntegrationTest {

    @Autowired private ContaService contaService;
    @Autowired private UsuarioLogadoService usuarioLogadoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void usuarioLogado_resolveMilitarPeloLogin() {
        assertThat(usuarioLogadoService.militar("00000000001").getNomeGuerra()).isEqualTo("Zeni");
    }

    @Test
    void usuarioLogado_loginDesconhecido_lancaNaoEncontrado() {
        assertThatThrownBy(() -> usuarioLogadoService.usuario("99999999999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void registrarAcesso_gravaUltimoAcesso() {
        assertThat(contaService.registrarAcesso("00000000004").getUltimoAcesso()).isNotNull();
    }

    @Test
    void trocarSenha_senhaAtualErrada_recusa() {
        assertThatThrownBy(() -> contaService.trocarSenha("00000000004", "errada", "novaSenha1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha atual incorreta");
    }

    @Test
    void trocarSenha_novaCurta_recusa() {
        assertThatThrownBy(() -> contaService.trocarSenha("00000000004", "milscale123", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A nova senha precisa ter pelo menos 6 caracteres");
    }

    @Test
    void trocarSenha_valida_gravaHashNovo() {
        contaService.trocarSenha("00000000004", "milscale123", "novaSenha1");
        String hash = usuarioRepository.findByLogin("00000000004").orElseThrow().getSenhaHash();
        assertThat(passwordEncoder.matches("novaSenha1", hash)).isTrue();
    }
}
```

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && mvn -q test -Dtest=ContaServiceIntegrationTest`
Expected: FAIL de compilação (`ContaService`, `UsuarioLogadoService` não existem).

- [ ] **Step 3: Criar `UsuarioLogadoService.java`**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/** Resolve o login da sessao (Authentication.getName(), o CPF) para o Usuario/Militar - um lugar so. */
@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuario(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    public Militar militar(String login) {
        return usuario(login).getMilitar();
    }
}
```

- [ ] **Step 4: Criar `ContaService.java`** (mensagens idênticas às do `AuthController` atual)

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** "Minha conta" - dados da sessao e troca da propria senha. */
@Service
public class ContaService {

    private final UsuarioLogadoService usuarioLogadoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(UsuarioLogadoService usuarioLogadoService, UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder) {
        this.usuarioLogadoService = usuarioLogadoService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarAcesso(String login) {
        Usuario usuario = usuarioLogadoService.usuario(login);
        usuario.setUltimoAcesso(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void trocarSenha(String login, String senhaAtual, String senhaNova) {
        Usuario usuario = usuarioLogadoService.usuario(login);
        if (senhaAtual == null || senhaNova == null || senhaNova.isBlank()) {
            throw new IllegalArgumentException("Informe a senha atual e a nova senha");
        }
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        if (senhaNova.length() < 6) {
            throw new IllegalArgumentException("A nova senha precisa ter pelo menos 6 caracteres");
        }
        usuario.setSenhaHash(passwordEncoder.encode(senhaNova));
        usuarioRepository.save(usuario);
    }
}
```

- [ ] **Step 5: Criar `ConsultaEscalaService.java`**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.EscalaRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.Escala;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** RF13/RF14 - leituras da escala (mes completo e um dia). */
@Service
public class ConsultaEscalaService {

    private final EscalaRepository escalaRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;

    public ConsultaEscalaService(EscalaRepository escalaRepository, ServicoEscaladoRepository servicoEscaladoRepository) {
        this.escalaRepository = escalaRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
    }

    public List<Escala> listar() {
        return escalaRepository.findAllByOrderByDataInicioDesc();
    }

    public Escala buscar(Long id) {
        return escalaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Escala não encontrada"));
    }

    public List<ServicoEscalado> doDia(LocalDate data) {
        return servicoEscaladoRepository.findByData(data);
    }
}
```

- [ ] **Step 6: Reescrever os controllers para usar os services**

`AuthController.java` (corpo da classe; imports: remover `UsuarioRepository`, `PasswordEncoder`, `LocalDateTime`, adicionar `ContaService`):

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ContaService contaService;
    private final AuditoriaService auditoriaService;

    public AuthController(ContaService contaService, AuditoriaService auditoriaService) {
        this.contaService = contaService;
        this.auditoriaService = auditoriaService;
    }

    /** RF13/RF25 - devolve o usuario logado e o perfil, para o front montar o menu certo. */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = contaService.registrarAcesso(auth.getName());
        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "login", usuario.getLogin(),
                "perfil", usuario.getPerfil().getNome(),
                "militarId", usuario.getMilitar().getId(),
                "nomeExibicao", usuario.getMilitar().getNomeExibicao()
        ));
    }

    /** "Minha conta" - qualquer usuario troca a propria senha, precisa confirmar a atual. */
    @PostMapping("/senha")
    public Map<String, String> trocarSenha(@RequestBody Map<String, String> body, Authentication auth) {
        contaService.trocarSenha(auth.getName(), body.get("senhaAtual"), body.get("senhaNova"));
        auditoriaService.registrar(auth.getName(), "SENHA_TROCADA_PELO_PROPRIO", auth.getName());
        return Map.of("mensagem", "Senha alterada com sucesso");
    }
}
```

`EscalaController.java`: remover `EscalaRepository`, `ServicoEscaladoRepository` e `UsuarioRepository` do construtor; injetar `ConsultaEscalaService consultaEscalaService` e `UsuarioLogadoService usuarioLogadoService`. Trocar:
- `escalaRepository.findAllByOrderByDataInicioDesc()` → `consultaEscalaService.listar()`
- `escalaRepository.findById(id).orElseThrow()` → `consultaEscalaService.buscar(id)`
- `servicoEscaladoRepository.findByData(LocalDate.parse(data))` → `consultaEscalaService.doDia(LocalDate.parse(data))`
- `usuarioRepository.findByLogin(auth.getName()).orElseThrow()` → `usuarioLogadoService.usuario(auth.getName())`

`MinhaEscalaController.java`: trocar `UsuarioRepository` por `UsuarioLogadoService`; `usuarioRepository.findByLogin(auth.getName()).orElseThrow().getMilitar().getId()` → `usuarioLogadoService.militar(auth.getName()).getId()`.

- [ ] **Step 7: Trocar as ocorrências nos services**

Injetar `UsuarioLogadoService` pelo construtor e substituir:
- `SolicitacaoService` (`minhas`, `aguardandoMinhaConfirmacao`, `criar`, `criarTrocaMutua`, `confirmarSubstituto`, `cancelar`): `usuarioRepository.findByLogin(X).orElseThrow().getMilitar()` → `usuarioLogadoService.militar(X)`. `usuarioRepository` continua sendo usado em `findByMilitar_Id` (notificações) — manter.
- `AfastamentoService.cadastrarMissao`: `usuarioRepository.findByLogin(loginUsuarioRegistro).orElseThrow()` → `usuarioLogadoService.usuario(loginUsuarioRegistro)`; remover `UsuarioRepository` do construtor se não sobrar uso.
- `BoletimService.criar`: `usuarioRepository.findByLogin(loginAutor).orElseThrow().getMilitar()` → `usuarioLogadoService.militar(loginAutor)`; remover `UsuarioRepository`.
- `NotificacaoService` (`listarRecentes`, `contarNaoLidas`, `marcarTodasComoLidas`): `usuarioRepository.findByLogin(loginUsuario).orElseThrow().getId()` → `usuarioLogadoService.usuario(loginUsuario).getId()` (`usuarioRepository` continua em `registrarParaPerfis`).

Conferir: `cd backend && grep -rn "findByLogin(.*).orElseThrow()" src/main` → só pode sobrar dentro de `UsuarioLogadoService` (que usa `orElseThrow(() -> ...)`, então o grep retorna vazio).

- [ ] **Step 8: Rodar todos os testes**

Run: `cd backend && mvn -q test`
Expected: PASS.

- [ ] **Step 9: Checkpoint** — diff, sugerir a mensagem `refactor: servicos de conta, usuario logado e consulta de escala` e aguardar o usuário commitar.

---

### Task 4: DTOs de entrada com Bean Validation

Os controllers recebem `Map<String, Object>` e convertem na mão (`Long.valueOf(String.valueOf(body.get(...)))`) — campo faltando vira `NumberFormatException`/`NullPointerException` (500). Os DTOs mantêm **exatamente os mesmos nomes de campo JSON** que o frontend já envia.

**Files:**
- Create em `backend/src/main/java/br/com/milscale/milscale/adapters/web/dto/`: `GerarEscalaRequest.java`, `CadastrarAfastamentoRequest.java`, `CriarSolicitacaoRequest.java`, `CriarTrocaMutuaRequest.java`, `ConfirmacaoSubstitutoRequest.java`, `DecisaoRequest.java`, `BoletimRequest.java`, `AlterarPerfilRequest.java`, `AlterarAtivoRequest.java`, `TrocarSenhaRequest.java`
- Modify: `EscalaController`, `AfastamentoController`, `SolicitacaoController`, `BoletimController`, `UsuarioController`, `AuthController`
- Test: `backend/src/test/java/br/com/milscale/milscale/adapters/web/ValidacaoRequestIntegrationTest.java`

**Interfaces:**
- Consumes: handler `MethodArgumentNotValidException` da Tarefa 1; `TipoAfastamento` da Tarefa 2.
- Produces: os records abaixo (usados também pelo Plano 2).

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.adapters.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ValidacaoRequestIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithUserDetails("00000000001")
    void gerarEscala_semDataFim_volta400ComMensagem() throws Exception {
        mvc.perform(post("/api/escalas/gerar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataInicio\":\"2030-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Informe a data final"));
    }

    @Test
    @WithUserDetails("00000000001")
    void afastamento_semMilitares_volta400() throws Exception {
        mvc.perform(post("/api/afastamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"militarIds\":[],\"tipo\":\"FERIAS\",\"descricao\":\"x\",\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-02\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Selecione ao menos um militar"));
    }

    @Test
    @WithUserDetails("00000000001")
    void afastamento_tipoDesconhecido_volta400() throws Exception {
        mvc.perform(post("/api/afastamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"militarIds\":[5],\"tipo\":\"PASSEIO\",\"descricao\":\"x\",\"dataInicio\":\"2030-01-01\",\"dataFim\":\"2030-01-02\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithUserDetails("00000000004")
    void pedirTroca_semSubstituto_volta400EmVezDe500() throws Exception {
        mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoOrigemId\":1,\"justificativa\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Escolha o substituto"));
    }

    @Test
    @WithUserDetails("00000000004")
    void trocarSenha_curta_volta400ComMensagemAntiga() throws Exception {
        mvc.perform(post("/api/auth/senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"milscale123\",\"senhaNova\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("pelo menos 6")));
    }
}
```

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && mvn -q test -Dtest=ValidacaoRequestIntegrationTest`
Expected: FAIL — `gerarEscala_semDataFim` responde 500/400 sem a mensagem esperada; `pedirTroca_semSubstituto` responde 500.

- [ ] **Step 3: Criar os records** (pacote `br.com.milscale.milscale.adapters.web.dto`; mensagens explícitas em português)

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GerarEscalaRequest(
        @NotNull(message = "Informe a data inicial") LocalDate dataInicio,
        @NotNull(message = "Informe a data final") LocalDate dataFim) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import br.com.milscale.milscale.domain.TipoAfastamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CadastrarAfastamentoRequest(
        @NotEmpty(message = "Selecione ao menos um militar") List<Long> militarIds,
        @NotNull(message = "Informe o tipo") TipoAfastamento tipo,
        @NotBlank(message = "Informe a descrição") @Size(max = 150, message = "Descrição com no máximo 150 caracteres") String descricao,
        @NotNull(message = "Informe a data inicial") LocalDate dataInicio,
        @NotNull(message = "Informe a data final") LocalDate dataFim) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarSolicitacaoRequest(
        @NotNull(message = "Informe o serviço") Long servicoOrigemId,
        @NotNull(message = "Escolha o substituto") Long substitutoId,
        @NotBlank(message = "Informe uma justificativa") @Size(max = 250, message = "Justificativa com no máximo 250 caracteres") String justificativa) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarTrocaMutuaRequest(
        @NotNull(message = "Informe o serviço") Long servicoOrigemId,
        @NotNull(message = "Escolha o serviço do outro militar") Long servicoDestinoId,
        @NotBlank(message = "Informe uma justificativa") @Size(max = 250, message = "Justificativa com no máximo 250 caracteres") String justificativa) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.Size;

/** Resposta do substituto sugerido. Campo "aceito" ausente = false (mesmo comportamento anterior). */
public record ConfirmacaoSubstitutoRequest(
        boolean aceito,
        @Size(max = 250, message = "Comentário com no máximo 250 caracteres") String comentario) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.Size;

/** Triagem (Cabo) e autorizacao (Sargenteante). "aprovado" ausente = false. */
public record DecisaoRequest(
        boolean aprovado,
        @Size(max = 250, message = "Comentário com no máximo 250 caracteres") String comentario) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoletimRequest(
        @Size(max = 20, message = "Número com no máximo 20 caracteres") String numero,
        @NotBlank(message = "Informe um título") @Size(max = 150, message = "Título com no máximo 150 caracteres") String titulo,
        @NotBlank(message = "O boletim não pode ficar vazio") String conteudoHtml,
        @Size(max = 60) String avisoRelacionado,
        @Size(max = 150) String avisoRelacionadoDescricao) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarPerfilRequest(@NotNull(message = "Escolha o perfil") Long perfilId) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

public record AlterarAtivoRequest(boolean ativo) {}
```

```java
package br.com.milscale.milscale.adapters.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(
        @NotBlank(message = "Informe a senha atual e a nova senha") String senhaAtual,
        @NotBlank(message = "Informe a senha atual e a nova senha")
        @Size(min = 6, message = "A nova senha precisa ter pelo menos 6 caracteres") String senhaNova) {}
```

- [ ] **Step 4: Trocar as assinaturas dos controllers** (`import jakarta.validation.Valid;` e `import br.com.milscale.milscale.adapters.web.dto.*;`)

`EscalaController.gerar`:
```java
    public Escala gerar(@Valid @RequestBody GerarEscalaRequest req, Authentication auth) {
        var usuario = usuarioLogadoService.usuario(auth.getName());
        Escala escala = gerarEscalaService.gerar(req.dataInicio(), req.dataFim(), usuario);
        auditoriaService.registrar(auth.getName(), "ESCALA_GERADA", req.dataInicio() + " a " + req.dataFim());
        return escala;
    }
```

`AfastamentoController.cadastrar` (remover `@SuppressWarnings` e o parsing manual):
```java
    public List<Afastamento> cadastrar(@Valid @RequestBody CadastrarAfastamentoRequest req, Authentication auth) {
        List<Afastamento> criados = afastamentoService.cadastrarMissao(req.militarIds(), req.tipo(), req.descricao(),
                req.dataInicio(), req.dataFim(), auth.getName());
        auditoriaService.registrar(auth.getName(), "AFASTAMENTO_CADASTRADO",
                req.tipo() + " (" + criados.size() + " militar(es)) - " + req.dataInicio() + " a " + req.dataFim() + " - " + req.descricao());
        return criados;
    }
```

`SolicitacaoController`:
```java
    @PostMapping("/{id}/confirmar-substituto")
    public Solicitacao confirmarSubstituto(@PathVariable Long id, @Valid @RequestBody ConfirmacaoSubstitutoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.confirmarSubstituto(id, req.aceito(), req.comentario(), auth.getName());
        auditoriaService.registrar(auth.getName(), req.aceito() ? "TROCA_SUBSTITUTO_ACEITOU" : "TROCA_SUBSTITUTO_RECUSOU", "solicitação " + id);
        return s;
    }

    @PostMapping
    public Solicitacao criar(@Valid @RequestBody CriarSolicitacaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.criar(req.servicoOrigemId(), req.substitutoId(), req.justificativa(), auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_PEDIDA", "serviço " + req.servicoOrigemId());
        return s;
    }

    @PostMapping("/troca-mutua")
    public Solicitacao criarTrocaMutua(@Valid @RequestBody CriarTrocaMutuaRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.criarTrocaMutua(req.servicoOrigemId(), req.servicoDestinoId(), req.justificativa(), auth.getName());
        auditoriaService.registrar(auth.getName(), "TROCA_PEDIDA", "serviço " + req.servicoOrigemId() + " (mútua)");
        return s;
    }

    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping("/{id}/triagem")
    public Solicitacao triagem(@PathVariable Long id, @Valid @RequestBody DecisaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.triagem(id, req.aprovado(), req.comentario());
        auditoriaService.registrar(auth.getName(), req.aprovado() ? "TROCA_TRIAGEM_APROVADA" : "TROCA_TRIAGEM_NEGADA", "solicitação " + id);
        return s;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping("/{id}/autorizacao")
    public Solicitacao autorizar(@PathVariable Long id, @Valid @RequestBody DecisaoRequest req, Authentication auth) {
        Solicitacao s = solicitacaoService.autorizar(id, req.aprovado(), req.comentario());
        auditoriaService.registrar(auth.getName(), req.aprovado() ? "TROCA_AUTORIZADA" : "TROCA_NEGADA", "solicitação " + id);
        return s;
    }
```

`BoletimController.criar` / `atualizar`: `@Valid @RequestBody BoletimRequest req` e passar `req.numero(), req.titulo(), req.conteudoHtml(), req.avisoRelacionado(), req.avisoRelacionadoDescricao()` no lugar de `body.get(...)`.

`UsuarioController`: `alterarPerfil(@PathVariable Long id, @Valid @RequestBody AlterarPerfilRequest req, ...)` usando `req.perfilId()`; `alterarAtivo(..., @RequestBody AlterarAtivoRequest req, ...)` usando `req.ativo()`.

`AuthController.trocarSenha`: `@Valid @RequestBody TrocarSenhaRequest req` e `contaService.trocarSenha(auth.getName(), req.senhaAtual(), req.senhaNova())`.

- [ ] **Step 5: Conferir que nenhum `Map` de entrada sobrou**

Run: `cd backend && grep -rn "@RequestBody Map" src/main`
Expected: nenhuma linha.

- [ ] **Step 6: Rodar todos os testes e o build do frontend**

Run: `cd backend && mvn -q test` → PASS.
Run: `cd ../frontend && npm run build` → PASS (nada mudou no front, mas confirma que os nomes de campo continuam os mesmos lendo `git grep -n "api.post" frontend/src` contra os records acima).

- [ ] **Step 7: Teste manual rápido** — subir backend (`mvn spring-boot:run`) e frontend (`npm run dev`), logar como `000.000.000-01`, gerar uma escala de 3 dias, pedir e aprovar uma troca, cadastrar um afastamento. Tudo deve funcionar igual.

- [ ] **Step 8: Checkpoint** — diff, sugerir a mensagem `refactor: DTOs de entrada com Bean Validation` e aguardar o usuário commitar.

---

### Task 5: Regra de descanso num lugar só (`PoliticaDeDescanso`)

A regra de intervalo (RN06) e a do 1x1 em troca estão espalhadas em três services, cada um com sua janela de datas e o default mágico `7` repetido (`regra != null ? regra.getIntervaloMinimo() : 7` aparece duas vezes). Esta tarefa começa com um **teste de caracterização** da realocação por afastamento (hoje sem teste nenhum), para garantir que a refatoração não muda nada.

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/domain/PoliticaDeDescanso.java`
- Modify: `application/GerarEscalaService.java`, `application/AfastamentoService.java`, `application/SolicitacaoService.java`
- Test: `backend/src/test/java/br/com/milscale/milscale/domain/PoliticaDeDescansoTest.java`, `backend/src/test/java/br/com/milscale/milscale/application/AfastamentoReconciliacaoIntegrationTest.java`

**Interfaces:**
- Produces (classe utilitária final, métodos estáticos, sem Spring):
  - `int INTERVALO_MINIMO_PADRAO = 7`
  - `int DISTANCIA_MINIMA_EM_TROCA = 3`
  - `long distanciaEmDias(LocalDate a, LocalDate b)`
  - `boolean respeitaIntervalo(LocalDate servicoAnterior, LocalDate dia, int intervaloMinimo)`
  - `boolean ficariaEm1x1(LocalDate servicoExistente, LocalDate novaData)`
  - `int intervaloMinimo(RegraEscala regra)`

- [ ] **Step 1: Teste de caracterização da realocação (deve PASSAR já no código atual)**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rede de seguranca pra refatoracao da regra de descanso: afastamento
 * cadastrado DEPOIS da escala pronta realoca so a vaga da pessoa
 * afastada (dentro do periodo), pra alguem elegivel, sem violar o
 * intervalo minimo. Cenario montado a mao (como no
 * TrocaIntervaloIntegrationTest) pra ser deterministico.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AfastamentoReconciliacaoIntegrationTest {

    @Autowired private AfastamentoService afastamentoService;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    void afastamentoDepoisDaEscala_realocaSoAVagaDentroDoPeriodo() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        TipoServico caboDaGuarda = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Cabo da Guarda")).findFirst().orElseThrow();
        List<Militar> cabos = militarRepository.findAll().stream()
                .filter(m -> "Cb".equals(m.getPosto().getSigla()))
                .filter(m -> !"Aprov".equals(m.getSubunidade().getSigla()))
                .toList();
        Militar afastado = cabos.get(0);
        Militar vizinho = cabos.get(1); // tem servico a 2 dias do alvo: NAO pode ser o escolhido (RN06)

        LocalDate dia = LocalDate.now().plusMonths(2).withDayOfMonth(10);
        Escala escala = escalaRepository.save(Escala.builder().descricao("teste")
                .dataInicio(dia.withDayOfMonth(1)).dataFim(dia.withDayOfMonth(28)).usuarioGeracao(sargenteante).build());
        ServicoEscalado alvo = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia).tipoServico(caboDaGuarda).militar(afastado).build());
        ServicoEscalado foraDoPeriodo = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia.plusDays(10)).tipoServico(caboDaGuarda).militar(afastado).build());
        servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia.plusDays(2)).tipoServico(caboDaGuarda).militar(vizinho).build());

        afastamentoService.cadastrarMissao(List.of(afastado.getId()), TipoAfastamento.DISPENSA, "teste",
                dia, dia, "00000000001");

        ServicoEscalado depois = servicoEscaladoRepository.findById(alvo.getId()).orElseThrow();
        assertThat(depois.getMilitar()).as("vaga realocada pra alguem").isNotNull();
        assertThat(depois.getMilitar().getId()).isNotIn(afastado.getId(), vizinho.getId());
        assertThat(depois.getMilitar().getPosto().getSigla()).isEqualTo("Cb");
        assertThat(depois.getMilitar().getSubunidade().getSigla()).isNotEqualTo("Aprov");
        assertThat(depois.getObservacao()).startsWith("Realocado automaticamente");
        assertThat(servicoEscaladoRepository.findById(foraDoPeriodo.getId()).orElseThrow().getMilitar().getId())
                .as("servico fora do periodo do afastamento continua com ele")
                .isEqualTo(afastado.getId());
    }
}
```

Run: `cd backend && mvn -q test -Dtest=AfastamentoReconciliacaoIntegrationTest`
Expected: PASS (caracteriza o comportamento atual). Se falhar, **pare e reporte**: é um bug pré-existente, não algo para "consertar" nesta tarefa.

- [ ] **Step 2: Escrever o teste unitário da política (falha: classe não existe)**

```java
package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaDeDescansoTest {

    private static final LocalDate D = LocalDate.of(2030, 1, 10);

    @Test
    void semServicoAnterior_sempreRespeita() {
        assertThat(PoliticaDeDescanso.respeitaIntervalo(null, D, 3)).isTrue();
    }

    @Test
    void intervalo3_exigeVaoDe4DiasDeCalendario() {
        // 3x1 = folga, folga, folga, servico
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.minusDays(3), D, 3)).isFalse();
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.minusDays(4), D, 3)).isTrue();
    }

    @Test
    void intervaloValeTambemParaServicoFuturo() {
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.plusDays(3), D, 3)).isFalse();
        assertThat(PoliticaDeDescanso.respeitaIntervalo(D.plusDays(4), D, 3)).isTrue();
    }

    @Test
    void troca_1x1ProibidoE2x1Permitido() {
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.plusDays(1), D)).isTrue();
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.plusDays(2), D)).isTrue();  // 1 dia de folga
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.plusDays(3), D)).isFalse(); // 2 dias de folga
        assertThat(PoliticaDeDescanso.ficariaEm1x1(D.minusDays(2), D)).isTrue();
    }

    @Test
    void semRegraCadastrada_usaIntervaloPadrao() {
        assertThat(PoliticaDeDescanso.intervaloMinimo(null)).isEqualTo(7);
        assertThat(PoliticaDeDescanso.intervaloMinimo(RegraEscala.builder().intervaloMinimo(3).build())).isEqualTo(3);
    }
}
```

Run: `cd backend && mvn -q test -Dtest=PoliticaDeDescansoTest` → FAIL de compilação.

- [ ] **Step 3: Criar `PoliticaDeDescanso.java`**

```java
package br.com.milscale.milscale.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * RN06 num lugar so. Antes cada service (geracao, realocacao por
 * afastamento, troca) tinha a propria janela de datas e o proprio
 * default - qualquer ajuste de regra precisava ser replicado em tres.
 */
public final class PoliticaDeDescanso {

    /** Usado so quando o tipo de servico nao tem RegraEscala cadastrada. */
    public static final int INTERVALO_MINIMO_PADRAO = 7;

    /** Em troca o minimo aceitavel e 2x1: vao de calendario de 3 dias. 1x1 (vao <= 2) e proibido. */
    public static final int DISTANCIA_MINIMA_EM_TROCA = 3;

    private PoliticaDeDescanso() {}

    public static long distanciaEmDias(LocalDate a, LocalDate b) {
        return Math.abs(ChronoUnit.DAYS.between(a, b));
    }

    /**
     * "intervaloMinimo" e o numero de dias de FOLGA exigidos entre dois
     * servicos (3 = folga, folga, folga, servico). Um vao de calendario de
     * exatamente `intervaloMinimo` dias so da `intervaloMinimo - 1` folgas,
     * por isso a distancia precisa ser estritamente maior.
     */
    public static boolean respeitaIntervalo(LocalDate servicoAnterior, LocalDate dia, int intervaloMinimo) {
        return servicoAnterior == null || distanciaEmDias(servicoAnterior, dia) > intervaloMinimo;
    }

    public static boolean ficariaEm1x1(LocalDate servicoExistente, LocalDate novaData) {
        return distanciaEmDias(servicoExistente, novaData) < DISTANCIA_MINIMA_EM_TROCA;
    }

    public static int intervaloMinimo(RegraEscala regra) {
        return regra != null ? regra.getIntervaloMinimo() : INTERVALO_MINIMO_PADRAO;
    }
}
```

- [ ] **Step 4: Usar a política nos três services**

`GerarEscalaService`:
- `int intervaloMinimo = regra != null ? regra.getIntervaloMinimo() : 7;` → `int intervaloMinimo = PoliticaDeDescanso.intervaloMinimo(regra);`
- a linha `if (em.diasDesdeUltimoServico(diaFinal) <= intervaloMinimo && em.getUltimoServico() != null) continue; // RN06` → `if (!PoliticaDeDescanso.respeitaIntervalo(em.getUltimoServico(), diaFinal, intervaloMinimo)) continue; // RN06` (e apagar o comentário longo de 4 linhas acima dela, que agora mora na política).
- remover o método `diasDesdeUltimoServico` de `MilitarEmGeracao` (fica sem uso).

`AfastamentoService`:
- `int intervaloMinimo = regra != null ? regra.getIntervaloMinimo() : 7;` → `int intervaloMinimo = PoliticaDeDescanso.intervaloMinimo(regra);`
- `respeitaIntervalo` passa a ser:

```java
    private boolean respeitaIntervalo(Militar m, LocalDate dia, int intervaloMinimo) {
        return servicoEscaladoRepository
                .findByMilitar_IdAndDataBetween(m.getId(), dia.minusDays(intervaloMinimo), dia.plusDays(intervaloMinimo))
                .stream()
                .allMatch(s -> PoliticaDeDescanso.respeitaIntervalo(s.getData(), dia, intervaloMinimo));
    }
```

`SolicitacaoService.ficariaEm1x1` passa a ser:

```java
    private boolean ficariaEm1x1(Long militarId, LocalDate novaData, Long excluirServicoId) {
        int janela = PoliticaDeDescanso.DISTANCIA_MINIMA_EM_TROCA - 1;
        return servicoEscaladoRepository.findByMilitar_IdAndDataBetween(militarId, novaData.minusDays(janela), novaData.plusDays(janela)).stream()
                .filter(s -> !s.getId().equals(excluirServicoId))
                .anyMatch(s -> PoliticaDeDescanso.ficariaEm1x1(s.getData(), novaData));
    }
```

- [ ] **Step 5: Rodar todos os testes**

Run: `cd backend && mvn -q test`
Expected: PASS — inclusive `TrocaIntervaloIntegrationTest` (1x1/2x1), `EscalaGeracaoIntegrationTest` (3x1) e o novo teste de caracterização.

- [ ] **Step 6: Checkpoint** — diff, sugerir a mensagem `refactor: PoliticaDeDescanso concentra RN06 e regra de troca` e aguardar o usuário commitar.

---

### Task 6: Geração e realocação sem consultas repetidas

Hoje, para um mês de 12 tipos, `GerarEscalaService` faz ~720 consultas de requisito/regra (dentro do laço dia × tipo) e reavalia a elegibilidade de ~200 militares em cada iteração. `AfastamentoService.temImpedimentoNoDia` carrega **todos** os afastamentos vigentes por candidato. Esta tarefa não muda o resultado — os testes de geração, troca e realocação são a rede de segurança.

**Files:**
- Modify: `adapters/persistence/MilitarRepository.java`, `adapters/persistence/AfastamentoRepository.java`
- Modify: `application/GerarEscalaService.java`, `application/AfastamentoService.java`, `application/SolicitacaoService.java`

**Interfaces:**
- Produces:
  - `MilitarRepository.findBySituacao(SituacaoPessoa situacao): List<Militar>`
  - `AfastamentoRepository.existsByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(Long militarId, LocalDate dia, LocalDate mesmoDia): boolean`

- [ ] **Step 1: Registrar o tempo atual dos testes de geração (referência)**

Run: `cd backend && mvn test -Dtest=EscalaGeracaoIntegrationTest | grep "Tests run"`
Anotar o `Time elapsed` para comparar no Step 6.

- [ ] **Step 2: Adicionar os métodos de repositório**

`MilitarRepository.java`:
```java
    List<Militar> findBySituacao(br.com.milscale.core.domain.SituacaoPessoa situacao);
```
`AfastamentoRepository.java`:
```java
    boolean existsByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(Long militarId, LocalDate dia, LocalDate mesmoDia);
```

- [ ] **Step 3: Pré-carregar tudo em `GerarEscalaService.gerar`**

Substituir o trecho que vai de `List<TipoServico> tipos = ...` até o fim do laço `for (LocalDate dia ...)` por:

```java
        List<TipoServico> tipos = tipoServicoRepository.findByAtivoTrue();
        List<Militar> ativos = militarRepository.findBySituacao(SituacaoPessoa.ATIVO);

        // Tudo que nao muda durante a geracao e carregado UMA vez, fora do laco dia x tipo.
        Map<Long, List<Militar>> elegiveisPorTipo = new HashMap<>();
        Map<Long, Integer> intervaloPorTipo = new HashMap<>();
        for (TipoServico tipo : tipos) {
            List<RequisitoServico> requisitos = requisitoServicoRepository.findByTipoServico_Id(tipo.getId());
            elegiveisPorTipo.put(tipo.getId(), ativos.stream().filter(m -> elegibilidadeService.elegivel(m, requisitos)).toList()); // RF06
            intervaloPorTipo.put(tipo.getId(), PoliticaDeDescanso.intervaloMinimo(regraEscalaRepository.findByTipoServico_Id(tipo.getId()).orElse(null)));
        }
        Map<Long, List<Afastamento>> afastamentosPorMilitar = afastamentoRepository.findByDataFimGreaterThanEqual(dataInicio).stream()
                .collect(Collectors.groupingBy(a -> a.getMilitar().getId()));

        Map<Long, MilitarEmGeracao> estado = new HashMap<>();
        for (Militar m : ativos) {
            estado.put(m.getId(), new MilitarEmGeracao(m, m.getDataUltimoServico()));
        }

        Escala escala = Escala.builder()
                .descricao("Escala de " + nomeMesPtBr(dataInicio) + " de " + dataInicio.getYear())
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .situacao(SituacaoEscala.RASCUNHO)
                .usuarioGeracao(usuarioGeracao)
                .build();

        List<ServicoEscalado> gerados = new ArrayList<>();

        for (LocalDate dia = dataInicio; !dia.isAfter(dataFim); dia = dia.plusDays(1)) {
            Set<Long> escaladosHoje = new HashSet<>(); // RN05

            for (TipoServico tipo : tipos) {
                int intervaloMinimo = intervaloPorTipo.get(tipo.getId());
                final LocalDate diaFinal = dia;

                List<MilitarEmGeracao> pool = new ArrayList<>();
                List<MilitarEmGeracao> disponiveis = new ArrayList<>();
                for (Militar m : elegiveisPorTipo.get(tipo.getId())) {
                    if (escaladosHoje.contains(m.getId())) continue; // RN05
                    if (temImpedimento(m, diaFinal, afastamentosPorMilitar)) continue; // RN15
                    MilitarEmGeracao em = estado.get(m.getId());
                    disponiveis.add(em);
                    if (PoliticaDeDescanso.respeitaIntervalo(em.getUltimoServico(), diaFinal, intervaloMinimo)) pool.add(em); // RN06
                }

                List<MilitarEmGeracao> escolhidos = new ArrayList<>(motor.preencherVagas(pool, tipo, criterio));

                // "A escala aperta sozinha, nunca fica vaga aberta": sem gente suficiente
                // respeitando RN06, completa com quem esta disponivel (RN05/RN15/RF06
                // continuam valendo), sempre pelo criterio normal da fila.
                int faltantesAntesDoAperto = tipo.getEfetivoNecessario() - escolhidos.size();
                if (faltantesAntesDoAperto > 0) {
                    Set<Long> jaEscolhidos = escolhidos.stream().map(e -> e.militar.getId()).collect(Collectors.toSet());
                    List<MilitarEmGeracao> poolRelaxado = disponiveis.stream()
                            .filter(em -> !jaEscolhidos.contains(em.militar.getId()))
                            .toList();
                    escolhidos.addAll(motor.preencherVagas(poolRelaxado, new TipoTurnoComEfetivo(tipo, faltantesAntesDoAperto), criterio));
                }

                for (MilitarEmGeracao escolhido : escolhidos) {
                    escaladosHoje.add(escolhido.militar.getId());
                    escolhido.marcarServico(dia);
                    gerados.add(ServicoEscalado.builder()
                            .escala(escala).data(dia).tipoServico(tipo)
                            .militar(escolhido.militar).situacao(SituacaoServico.PREVISTO).build());
                }
                // Uma linha por vaga que ficou sem ninguem (so quando nao ha NINGUEM elegivel/disponivel).
                int faltantes = tipo.getEfetivoNecessario() - escolhidos.size();
                for (int i = 0; i < faltantes; i++) {
                    gerados.add(ServicoEscalado.builder()
                            .escala(escala).data(dia).tipoServico(tipo).militar(null)
                            .situacao(SituacaoServico.PREVISTO).build());
                }
            }
        }
```

E trocar `temImpedimento` por:

```java
    private boolean temImpedimento(Militar m, LocalDate dia, Map<Long, List<Afastamento>> afastamentosPorMilitar) {
        return afastamentosPorMilitar.getOrDefault(m.getId(), List.of()).stream().anyMatch(a -> a.cobre(dia));
    }
```

Adicionar `import java.util.stream.Collectors;` e remover imports que ficarem sem uso. **Atenção à ordem do pool relaxado:** antes ele era montado percorrendo `ativos` na ordem do banco; agora percorre `elegiveisPorTipo` — que é `ativos` filtrado, então a ordem relativa é a mesma e o `sort` estável do motor produz o mesmo resultado.

- [ ] **Step 4: Ajustar `AfastamentoService` e `SolicitacaoService`**

`AfastamentoService.reconciliarServicosJaMarcados`: 
```java
        List<Militar> ativos = militarRepository.findBySituacao(SituacaoPessoa.ATIVO).stream()
                .filter(m -> !m.getId().equals(afastado.getId()))
                .toList();
```
`AfastamentoService.temImpedimentoNoDia`:
```java
    private boolean temImpedimentoNoDia(Militar m, LocalDate dia) {
        return afastamentoRepository.existsByMilitar_IdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual(m.getId(), dia, dia);
    }
```
`SolicitacaoService.listarElegiveisParaTroca`: `militarRepository.findAll().stream().filter(m -> m.getSituacao() == ...ATIVO)` → `militarRepository.findBySituacao(SituacaoPessoa.ATIVO).stream()`.

- [ ] **Step 5: Rodar todos os testes**

Run: `cd backend && mvn -q test`
Expected: PASS — em especial `EscalaGeracaoIntegrationTest` (zero vaga aberta, 3x1, Aprov, CFC/Motorista, aperto), `TrocaIntervaloIntegrationTest` e `AfastamentoReconciliacaoIntegrationTest`.

- [ ] **Step 6: Comparar o tempo** — repetir o comando do Step 1 e registrar antes/depois no resumo da tarefa (esperado: queda perceptível; não é critério de aceite, só informação).

- [ ] **Step 7: Checkpoint** — diff, sugerir a mensagem `perf: geracao de escala sem consultas dentro do laco` e aguardar o usuário commitar.

---

### Task 7: Frontend — utilitários compartilhados e testes com Vitest

`formatarDataBR` está copiada em 11 arquivos, `formatarCpf` em 4, `capitalizar` em 4, `formatarDataHora` e `formatarPeriodo` em 2 cada; `Shell.tsx` tem um mapa de perfis paralelo ao `utils/perfis.ts`. O frontend não tem nenhum teste.

**Files:**
- Create: `frontend/src/utils/formatadores.ts`, `frontend/src/utils/formatadores.test.ts`, `frontend/src/utils/mascaras.test.ts`, `frontend/src/utils/ordemTipos.test.ts`
- Modify: `frontend/package.json`, `frontend/index.html`, `frontend/src/components/Shell.tsx`
- Modify (remover cópias locais e importar): `components/MilitarDetalheOverlay.tsx`, `pages/Auditoria.tsx`, `Avisos.tsx`, `Boletim.tsx`, `EscalaDoMes.tsx`, `Feriados.tsx`, `FichaMilitar.tsx`, `Historico.tsx`, `MinhaConta.tsx`, `MinhaEscala.tsx`, `MissoesDispensas.tsx`, `Painel.tsx`, `PerfisPermissoes.tsx`, `Trocas.tsx`

**Interfaces:**
- Produces (`src/utils/formatadores.ts`): `formatarDataBR(iso: string): string`, `formatarDataHora(iso: string): string`, `formatarPeriodo(inicio: string, fim: string): string`, `formatarCpf(cpf: string): string`, `capitalizar(s: string): string`.

- [ ] **Step 1: Instalar o Vitest e criar o script**

```bash
cd frontend && npm install -D vitest
npm ls vite vitest
```
Expected: `vitest` instalado sem erro de peer dependency com o `vite@8`. Se o npm acusar conflito de peer, instalar a versão de vitest que declara suporte ao vite 8 (`npm view vitest peerDependencies`) e registrar a versão escolhida no resumo.

Em `package.json`, dentro de `"scripts"`, adicionar: `"test": "vitest run"`.

- [ ] **Step 2: Escrever os testes (falham: `formatadores.ts` não existe)**

`src/utils/formatadores.test.ts`:
```ts
import { describe, expect, it } from "vitest";
import { capitalizar, formatarCpf, formatarDataBR, formatarPeriodo } from "./formatadores";

describe("formatadores", () => {
  it("formata data ISO como dd/mm/aaaa", () => {
    expect(formatarDataBR("2026-09-05")).toBe("05/09/2026");
  });

  it("período de um dia só mostra a data uma vez", () => {
    expect(formatarPeriodo("2026-09-05", "2026-09-05")).toBe("05/09/2026");
    expect(formatarPeriodo("2026-09-05", "2026-09-07")).toBe("05/09/2026 a 07/09/2026");
  });

  it("formata CPF de 11 dígitos e devolve o resto como veio", () => {
    expect(formatarCpf("00000000001")).toBe("000.000.000-01");
    expect(formatarCpf("123")).toBe("123");
  });

  it("capitaliza só a primeira letra", () => {
    expect(capitalizar("setembro de 2026")).toBe("Setembro de 2026");
  });
});
```

`src/utils/mascaras.test.ts`:
```ts
import { describe, expect, it } from "vitest";
import { mascararCpf, mascararFusex, mascararTelefone, somenteDigitos } from "./mascaras";

describe("máscaras", () => {
  it("CPF encaixa pontuação enquanto digita", () => {
    expect(mascararCpf("0000")).toBe("000.0");
    expect(mascararCpf("00000000001")).toBe("000.000.000-01");
    expect(mascararCpf("000000000019999")).toBe("000.000.000-01");
  });

  it("telefone celular e fixo", () => {
    expect(mascararTelefone("41999998888")).toBe("(41) 99999-8888");
    expect(mascararTelefone("4133334444")).toBe("(41) 3333-4444");
  });

  it("FUSEX", () => {
    expect(mascararFusex("12345")).toBe("123-45");
  });

  it("somenteDigitos tira a máscara", () => {
    expect(somenteDigitos("000.000.000-01")).toBe("00000000001");
  });
});
```

`src/utils/ordemTipos.test.ts`:
```ts
import { describe, expect, it } from "vitest";
import { ordenarPorTipo } from "./ordemTipos";

describe("ordenarPorTipo", () => {
  it("segue a ordem do Boletim Interno e, dentro da função, o nome de guerra; vaga aberta por último", () => {
    const lista = [
      { tipoServico: { nome: "Guardas ao Quartel" }, militar: { nomeGuerra: "Prado" } },
      { tipoServico: { nome: "Oficial de Dia" }, militar: { nomeGuerra: "Zeni" } },
      { tipoServico: { nome: "Guardas ao Quartel" }, militar: null },
      { tipoServico: { nome: "Guardas ao Quartel" }, militar: { nomeGuerra: "Andrade" } },
    ];
    expect(ordenarPorTipo(lista).map((s) => `${s.tipoServico.nome}:${s.militar?.nomeGuerra ?? "-"}`)).toEqual([
      "Oficial de Dia:Zeni",
      "Guardas ao Quartel:Andrade",
      "Guardas ao Quartel:Prado",
      "Guardas ao Quartel:-",
    ]);
  });
});
```

Run: `cd frontend && npm test`
Expected: FAIL em `formatadores.test.ts` (módulo não encontrado); `mascaras` e `ordemTipos` PASS.

- [ ] **Step 3: Criar `src/utils/formatadores.ts`**

```ts
/** Formatadores de exibição usados em várias telas — um lugar só. */

/** "2026-09-05" → "05/09/2026". Recebe a data ISO do backend (LocalDate). */
export function formatarDataBR(iso: string): string {
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

/** Data e hora local (LocalDateTime do backend) no padrão brasileiro. */
export function formatarDataHora(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit",
  });
}

export function formatarPeriodo(inicio: string, fim: string): string {
  if (inicio === fim) return formatarDataBR(inicio);
  return `${formatarDataBR(inicio)} a ${formatarDataBR(fim)}`;
}

/** CPF guardado só com dígitos → "000.000.000-00". Qualquer outro tamanho volta como veio. */
export function formatarCpf(cpf: string): string {
  if (cpf.length !== 11) return cpf;
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`;
}

export function capitalizar(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1);
}
```

- [ ] **Step 4: Remover as cópias locais e importar**

Em cada arquivo abaixo, **apagar** a função local listada (no fim do arquivo) e adicionar o import `import { ... } from "../utils/formatadores";` com exatamente as funções que o arquivo usa:

| Arquivo | Funções locais a remover |
|---|---|
| `components/MilitarDetalheOverlay.tsx` | `formatarCpf`, `formatarDataBR` |
| `pages/Auditoria.tsx` | `formatarDataHora` |
| `pages/Avisos.tsx` | `capitalizar`, `formatarDataBR`, `formatarPeriodo` |
| `pages/Boletim.tsx` | `formatarDataHora`, `formatarDataBR` |
| `pages/EscalaDoMes.tsx` | `formatarDataBR`, `capitalizar` |
| `pages/Feriados.tsx` | `formatarPeriodo`, `formatarDataBR` |
| `pages/FichaMilitar.tsx` | `formatarCpf`, `formatarDataBR` |
| `pages/Historico.tsx` | `formatarDataBR` |
| `pages/MinhaConta.tsx` | `formatarCpf`, `formatarDataBR` |
| `pages/MinhaEscala.tsx` | `capitalizar` |
| `pages/MissoesDispensas.tsx` | `formatarDataBR` |
| `pages/Painel.tsx` | `capitalizar`, `formatarDataBR` |
| `pages/PerfisPermissoes.tsx` | `formatarCpf` |
| `pages/Trocas.tsx` | `formatarDataBR` |

(`formatarContador` fica onde está: as duas versões têm textos diferentes de propósito — "há X dias" no popup, "último serviço há X dias" na lista.)

Conferir: `cd frontend && grep -rnE "^function (formatarDataBR|formatarCpf|capitalizar|formatarDataHora|formatarPeriodo)" src` → nenhuma linha.

- [ ] **Step 5: `Shell.tsx` usa o mapa único de perfis**

Remover a função `formatarPerfil` do fim de `Shell.tsx`, adicionar `import { PERFIL_LABEL } from "../utils/perfis";` e trocar `{formatarPerfil(usuario.perfil)}` por `{PERFIL_LABEL[usuario.perfil] ?? usuario.perfil}`. (Efeito visível mínimo e desejado: o rodapé do menu passa a mostrar "Cabo da Sargenteação" com a mesma grafia das outras telas.)

- [ ] **Step 6: `index.html`** — `<html lang="en">` → `<html lang="pt-BR">`; `<title>frontend</title>` → `<title>MilScale</title>`.

- [ ] **Step 7: Rodar testes, lint e build**

Run: `cd frontend && npm test && npm run lint && npm run build`
Expected: 3 arquivos de teste PASS; lint sem erros novos; build OK.

- [ ] **Step 8: Teste manual** — `npm run dev`, abrir Escala do mês, Avisos, Feriados, Boletim, Minha conta, Perfis e permissões e o popup do militar: datas e CPF aparecem formatados como antes.

- [ ] **Step 9: Checkpoint** — diff, sugerir a mensagem `refactor(front): formatadores compartilhados e testes com vitest` e aguardar o usuário commitar.

---

### Task 8: Configuração por ambiente (URL da API, proxy, CORS, segredos do Compose)

`BASE_URL = "http://localhost:8080"` está fixo no código, o CORS só aceita `localhost:*` e as senhas do MySQL estão no `docker-compose.yml`. Solução: o frontend chama a API **no mesmo domínio** (`/api/...`) — em desenvolvimento via proxy do Vite, no Docker via proxy do nginx —, o CORS vira propriedade configurável e os segredos vão para `.env`.

**Files:**
- Modify: `frontend/src/api/client.ts`, `frontend/vite.config.ts`, `frontend/nginx.conf`
- Create: `frontend/.env.example`, `.env.example` (raiz)
- Modify: `backend/src/main/java/br/com/milscale/milscale/adapters/config/SecurityConfig.java`, `backend/src/main/resources/application.properties`, `docker-compose.yml`
- Test: `backend/src/test/java/br/com/milscale/milscale/adapters/config/CorsIntegrationTest.java`

**Interfaces:**
- Produces: propriedade `milscale.cors.origens` (lista separada por vírgula, padrão `http://localhost:*`); variável de build `VITE_API_URL` (padrão vazio = mesmo domínio).

- [ ] **Step 1: Teste de CORS (deve passar antes e depois — protege a mudança)**

```java
package br.com.milscale.milscale.adapters.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    void origemLocalPermitida() throws Exception {
        mvc.perform(options("/api/auth/me")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void origemDesconhecidaBloqueada() throws Exception {
        mvc.perform(options("/api/auth/me")
                        .header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
```

Run: `cd backend && mvn -q test -Dtest=CorsIntegrationTest` → PASS (caracteriza o atual).

- [ ] **Step 2: CORS configurável no `SecurityConfig`**

Adicionar o campo e o construtor:
```java
    private final List<String> origensPermitidas;

    public SecurityConfig(@Value("${milscale.cors.origens:http://localhost:*}") List<String> origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }
```
(`import org.springframework.beans.factory.annotation.Value;`) e em `corsConfigurationSource()`:
```java
        config.setAllowedOriginPatterns(origensPermitidas);
```

Em `application.properties`, no fim:
```properties
# Origens aceitas pelo CORS (separadas por virgula). Com o front servido
# pelo proprio nginx/proxy do Vite as chamadas sao same-origin e isto nem
# entra em jogo - so importa se o front morar em outro dominio.
milscale.cors.origens=${MILSCALE_CORS_ORIGENS:http://localhost:*}
```

Run: `mvn -q test -Dtest=CorsIntegrationTest` → PASS.

- [ ] **Step 3: Frontend chama a API no mesmo domínio**

`src/api/client.ts`, primeira linha:
```ts
// Vazio = mesmo domínio (proxy do Vite em dev, proxy do nginx no Docker).
// Defina VITE_API_URL só se o backend morar em outro endereço.
const BASE_URL = import.meta.env.VITE_API_URL ?? "";
```

`vite.config.ts`:
```ts
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Em dev o front chama /api no próprio :5173 e o Vite repassa pro backend -
    // mesmo domínio, então o cookie de sessão funciona sem depender de CORS.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
```

`frontend/.env.example`:
```bash
# Deixe vazio pra usar o mesmo domínio (recomendado).
# Só preencha se o backend estiver em outro endereço, ex.: https://api.exemplo.mil.br
VITE_API_URL=
```

`frontend/nginx.conf` — adicionar antes do `location /`:
```nginx
    # API: repassa pro container do backend - o navegador fala só com o
    # nginx (mesmo domínio), sem CORS e sem URL fixa no JS compilado.
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # fotos e boletins trafegam imagem em base64 dentro do JSON
        client_max_body_size 15m;
    }
```

- [ ] **Step 4: Segredos do Compose em `.env`**

`.env.example` (raiz):
```bash
# Copie para .env e troque as senhas antes de subir em qualquer lugar que não seja sua máquina.
MYSQL_DATABASE=milscale
MYSQL_USER=milscale
MYSQL_PASSWORD=troque-esta-senha
MYSQL_ROOT_PASSWORD=troque-esta-senha-root
```

`docker-compose.yml` — trocar os valores fixos por variáveis:
```yaml
  mysql:
    image: mysql:8.0
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-milscale}
      MYSQL_USER: ${MYSQL_USER:-milscale}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:?defina MYSQL_PASSWORD no .env}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:?defina MYSQL_ROOT_PASSWORD no .env}
    ports:
      - "3306:3306"
    volumes:
      - milscale_mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u $$MYSQL_USER -p$$MYSQL_PASSWORD"]
      interval: 5s
      timeout: 5s
      retries: 10

  backend:
    build:
      context: ./backend
    restart: unless-stopped
    environment:
      DB_HOST: mysql
      DB_PORT: "3306"
      DB_NAME: ${MYSQL_DATABASE:-milscale}
      DB_USER: ${MYSQL_USER:-milscale}
      DB_PASSWORD: ${MYSQL_PASSWORD:?defina MYSQL_PASSWORD no .env}
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
```
(o bloco `frontend` e `volumes` ficam iguais; manter o comentário explicativo do healthcheck.)

- [ ] **Step 5: Verificar**

- `cd backend && mvn -q test` → PASS.
- `cd frontend && npm run build` → PASS.
- Dev: `mvn spring-boot:run` + `npm run dev`, abrir `http://localhost:5173`, logar com `000.000.000-01` e navegar (DevTools → Network: chamadas vão para `localhost:5173/api/...`).
- Docker (se disponível): `cp .env.example .env`, ajustar senhas, `docker compose up --build`, abrir `http://localhost:5173`, logar e gerar uma escala. Sem Docker disponível, registrar no resumo que este passo não foi executado.

- [ ] **Step 6: Checkpoint** — diff, sugerir a mensagem `chore: configuracao por ambiente (proxy, CORS, .env)` e aguardar o usuário commitar.

---

### Task 9: Schema versionado com Flyway

Hoje o banco é criado/alterado por `spring.jpa.hibernate.ddl-auto=update`, que nunca remove nem renomeia nada e não deixa histórico. A partir daqui, toda mudança de schema é um arquivo `V<n>__descricao.sql`. Bancos que já existem (H2 local e volume MySQL) recebem **baseline** na versão 1, sem perder dados.

**Files:**
- Modify: `backend/pom.xml`, `backend/src/main/resources/application.properties`, `backend/src/main/resources/application-mysql.properties`, `backend/src/test/resources/application-test.properties`
- Create: `backend/src/main/resources/db/migration/V1__schema_inicial.sql`

**Interfaces:**
- Produces: convenção `backend/src/main/resources/db/migration/V<n>__<descricao>.sql` — o Plano 2 começa em `V2`.

- [ ] **Step 1: Gerar o DDL a partir das entidades atuais (com os enums da Tarefa 2)**

```bash
cd backend
mvn -q spring-boot:run -Dspring-boot.run.arguments="\
--spring.datasource.url=jdbc:h2:mem:gerar_ddl;MODE=MySQL;DATABASE_TO_LOWER=TRUE \
--spring.jpa.hibernate.ddl-auto=create-drop \
--spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect \
--spring.jpa.properties.hibernate.hbm2ddl.delimiter=; \
--spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create \
--spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=target/ddl-mysql.sql \
--server.port=0"
```
Quando aparecer `Started MilScaleApplication`, encerrar com Ctrl+C. Conferir que `target/ddl-mysql.sql` existe e contém `create table` para as 19 tabelas: `afastamento, boletim, escala, feriado, log_auditoria, militar, militar_qualificacao, notificacao, perfil_acesso, posto_graduacao, qualificacao, regra_escala, requisito_qualificacao_excluida, requisito_servico, servico_escalado, solicitacao, subunidade, tipo_servico, usuario`.

- [ ] **Step 2: Normalizar o script para rodar igual em H2 (modo MySQL) e MySQL**

Copiar `target/ddl-mysql.sql` para `src/main/resources/db/migration/V1__schema_inicial.sql` e aplicar **exatamente** estas edições:
1. Toda coluna `enum ('A','B',...)` vira `varchar(N)` com o `length` da anotação `@Column` correspondente: `escala.situacao` → `varchar(15)`; `servico_escalado.situacao` → `varchar(15)`; `solicitacao.situacao` → `varchar(25)`; `solicitacao.tipo_troca` → `varchar(20)`; `afastamento.tipo` → `varchar(15)`; `militar.situacao` → `varchar(15)`. (Motivo: bancos existentes já têm `varchar` nessas colunas; manter um tipo só nos dois caminhos.)
2. Remover o sufixo ` engine=InnoDB` de cada `create table`.
3. Colocar no topo o comentário:
```sql
-- V1 - schema inicial do MilScale, gerado a partir das entidades JPA
-- (Hibernate, dialeto MySQL) e normalizado pra rodar tambem no H2 em
-- MODE=MySQL. Bancos criados antes do Flyway recebem baseline nesta
-- versao (spring.flyway.baseline-on-migrate) e nao executam este script.
```

- [ ] **Step 3: Adicionar as dependências** em `pom.xml` (versões gerenciadas pelo Spring Boot):

```xml
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-mysql</artifactId>
    </dependency>
```

- [ ] **Step 4: Configurar o Flyway e desligar o `ddl-auto`**

`application.properties` — trocar `spring.jpa.hibernate.ddl-auto=update` por:
```properties
# Schema versionado pelo Flyway (src/main/resources/db/migration). O
# Hibernate nao cria nem altera tabela - so le e grava.
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
# Bancos que ja existiam antes do Flyway: marca como versao 1 sem rodar o V1.
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```
`application-mysql.properties` — trocar `spring.jpa.hibernate.ddl-auto=update` por `spring.jpa.hibernate.ddl-auto=none`.
`application-test.properties` — trocar `spring.jpa.hibernate.ddl-auto=create-drop` por:
```properties
# Mesmo caminho da producao: o schema do teste nasce das migrations do Flyway.
spring.jpa.hibernate.ddl-auto=none
```

- [ ] **Step 5: Rodar os testes (banco de teste agora nasce do V1)**

Run: `cd backend && mvn -q test`
Expected: PASS. Se falhar com erro de SQL do H2 ao aplicar o V1, a mensagem aponta a linha; corrigir **só a sintaxe** no V1 (sem mudar tipos/colunas) e registrar o ajuste no resumo.

- [ ] **Step 6: Verificar os três cenários de banco**

1. **Banco novo H2:** `rm -rf backend/data && cd backend && mvn spring-boot:run` → log mostra `Migrating schema ... to version "1 - schema inicial"`; logar e gerar escala.
2. **Banco H2 existente:** restaurar uma cópia de `backend/data/` feita **antes** desta tarefa (fazer a cópia no Step 1: `cp -r backend/data /tmp/milscale-data-backup` se existir), subir → log mostra `Creating baseline` e nenhuma migration aplicada; dados intactos.
3. **MySQL (Docker, se disponível):** `docker compose down -v && docker compose up --build` → V1 aplicado no MySQL; logar e gerar escala. Sem Docker, registrar que não foi executado.

- [ ] **Step 7: Checkpoint** — diff, sugerir a mensagem `build: schema versionado com Flyway` e aguardar o usuário commitar.

---

### Task 10: Documentação e comentários desatualizados

O README tem 940 linhas misturando manual de uso com diário de desenvolvimento, e há comentários que descrevem comportamentos antigos.

**Files:**
- Modify: `README.md`
- Create: `docs/HISTORICO.md`
- Modify: `backend/.../adapters/config/DataSeeder.java` (javadoc da classe), `backend/.../domain/Militar.java` (javadoc), `backend/.../domain/Solicitacao.java` (javadoc)

- [ ] **Step 1: Mover o diário para `docs/HISTORICO.md`**

Criar `docs/HISTORICO.md` com o título `# MilScale — histórico de entregas` e, abaixo, **todas** as seções do README a partir de `## Base de dados (efetivo semeado)` até o fim, sem alterar o texto.

- [ ] **Step 2: Reescrever o `README.md` como manual** com estas seções, nesta ordem (reaproveitando o texto que já existe):
1. `# MilScale` — um parágrafo: o que é (escala de serviço do 5º B Sup, primeiro produto da linha SmartScale, projeto de Dev. Orientado a Reuso — PUCPR).
2. `## Arquitetura` — o bloco atual (núcleo reutilizável × especialização MilScale), sem mudanças.
3. `## Como rodar` — Backend, Frontend (agora: `npm run dev` usa o proxy do Vite, não precisa configurar URL), Docker Compose (agora: `cp .env.example .env` antes do `docker compose up --build`), Testes (`mvn test` e `npm test`).
4. `## Banco de dados e migrations` — H2 em dev, MySQL via perfil `mysql`, Flyway em `backend/src/main/resources/db/migration`, regra "toda mudança de schema é um novo `V<n>__descricao.sql`, nunca editar migration já aplicada".
5. `## Contas de demonstração` — a tabela atual.
6. `## Perfis e regras de negócio principais` — lista curta: 4 perfis; geração (RN01, RN05, RN06 3x1, RN15, aperto); trocas (substituição/mútua, 2x1 permitido, 1x1 proibido); dia travado × dia sólido.
7. `## Histórico` — uma linha: "O registro detalhado de cada entrega está em [docs/HISTORICO.md](docs/HISTORICO.md)."

- [ ] **Step 3: Corrigir comentários**

- `DataSeeder.java`, javadoc da classe: substituir o parágrafo que começa em `Login: todo militar cadastrado ganha uma conta com login = nome de guerra...` por:
```java
 * Login: todo militar semeado ganha uma conta com login = CPF (11
 * digitos, RF01) e senha padrao "milscale123".
```
  e o comentário `// Contas de demonstração - login pelo nome de guerra, igual a todo mundo` por `// Contas de demonstração (uma por perfil) - login pelo CPF, igual a todo mundo`.
- `Militar.java`, javadoc da classe: trocar a referência a `ConsultarContadorRodizioService` (classe que não existe) por `{@link #getContadorRodizio()}`.
- `Solicitacao.java`, javadoc: `O solicitante pode CANCELAR enquanto ainda estiver em triagem.` → `O solicitante pode CANCELAR enquanto aguarda o substituto ou esta em triagem.`

- [ ] **Step 4: Verificar**

Run: `cd backend && mvn -q test` → PASS (só comentários mudaram). Ler o README renderizado (preview do editor ou GitHub) e conferir que todos os comandos citados existem.

- [ ] **Step 5: Checkpoint** — diff, sugerir a mensagem `docs: README como manual e historico separado` e aguardar o usuário commitar.

---

## Fechamento do Plano 1

- [ ] Rodar tudo uma última vez: `cd backend && mvn test` e `cd ../frontend && npm test && npm run lint && npm run build`.
- [ ] Rodar `/code-review` sobre o branch `melhoria/estrutura` e tratar os achados confirmados.
- [ ] Apresentar ao usuário: resumo por tarefa, números de testes antes/depois, tempo da geração antes/depois (Tarefa 6), qualquer passo manual não executado (ex.: Docker indisponível). O push e o PR ficam com o usuário.
