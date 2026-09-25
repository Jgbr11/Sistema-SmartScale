# Plano 2 — Implementações Necessárias (MilScale)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corrigir as falhas de regra de negócio e de segurança encontradas na análise e completar o que o sistema promete mas não faz:
- escala em rascunho visível para todos;
- dados pessoais expostos a qualquer usuário logado;
- XSS no Boletim;
- senha padrão fixa;
- militar cadastrado pela tela que não consegue logar;
- regras de escala que o motor ignora;
- tipos de serviço novos que ninguém consegue tirar.

**Architecture:** Mesma arquitetura hexagonal. Cada tarefa é uma fatia vertical (backend + frontend + teste) entregável sozinha. **Pré-requisito: o Plano 1 concluído** — este plano usa `UsuarioLogadoService`, `ConsultaEscalaService`, `ContaService`, os enums (`SituacaoEscala`, `SituacaoServico`...), os DTOs em `adapters/web/dto`, `ErroResposta`, `PoliticaDeDescanso`, o `GerarEscalaService` pré-carregado, os formatadores do front (`utils/formatadores.ts`), o Vitest e o Flyway (migrations começam em `V2`).

**Tech Stack:** Java 21, Spring Boot 3.3.4, Spring Security 6, Hibernate 6.5, Flyway, jsoup 1.18.1 (novo), JUnit 5 + MockMvc + spring-security-test; React 19, TypeScript 6, Vitest, DOMPurify 3 (novo).

## Global Constraints

- **Quem commita é o usuário, tarefa a tarefa.** Nenhum implementador (humano, agente ou subagente) roda `git commit` / `git push`. Cada tarefa termina num **Checkpoint**: mostrar `git status` + `git diff --stat` + o diff relevante, sugerir a mensagem de commit e **parar** até o usuário confirmar que commitou.
- Branch de trabalho: `melhoria/implementacoes`, criado a partir de `melhoria/estrutura` depois do Plano 1 commitado (criação só com o ok do usuário).
- Decisões já tomadas com o usuário (não reabrir):
  - **Rascunho:** só a sargenteação (Cabo, Sd EP, Sargenteante) vê escala em RASCUNHO na Escala do mês/dia. "Minha escala", "Meu histórico" e a visão do Militar Escalado mostram só escala PUBLICADA.
  - **Regras:** implementar **máx. serviços/mês** no motor; **esconder** "dias de folga" e os pesos (fim de semana/feriado) da tela; os campos continuam no banco, reservados para um futuro avaliador de justiça.
  - **Flyway:** já adotado no Plano 1. Toda mudança de schema aqui é uma migration nova (`V2__...`, `V3__...`). Nunca editar o `V1`.
- O pacote `br.com.milscale.core.domain` não pode ser alterado.
- Erros continuam no formato `{"erro": "..."}` (`ErroResposta`). Mensagens em português.
- Ao fim de cada tarefa: `cd backend && mvn test` verde; `cd frontend && npm test && npm run build` verdes.
- **Senhas nunca aparecem em log de auditoria nem em log de aplicação.**

---

## Mapa de arquivos

**Backend — criar**
- `adapters/web/PerfisSargenteacao.java` — "esse usuário é da sargenteação?" a partir do `Authentication`.
- `adapters/web/dto/MilitarDetalheResponse.java`, `adapters/web/dto/MilitarCadastradoResponse.java`
- `application/SanitizadorHtml.java` — limpeza de HTML do Boletim (jsoup).
- `application/GeradorDeSenha.java` — senha temporária aleatória.
- `adapters/config/SenhaTemporariaFilter.java` — bloqueia a API até a pessoa trocar a senha temporária.
- `application/RequisitoServicoService.java`, `application/NovoRequisito.java`, `adapters/web/RequisitoServicoController.java`
- `resources/db/migration/V2__usuario_senha_temporaria.sql`
- Testes (listados em cada tarefa).

**Backend — modificar**: `pom.xml`, `domain/Militar.java`, `domain/Usuario.java`, `domain/TipoServico.java`, `domain/RegraEscala.java`, repositórios `ServicoEscaladoRepository`, services `ConsultaEscalaService`, `MinhaEscalaService`, `MilitarService`, `SolicitacaoService`, `BoletimService`, `UsuarioService`, `ContaService`, `GerarEscalaService`, `RegraEscalaService`, `TipoServicoService`, controllers `EscalaController`, `MilitarController`, `UsuarioController`, `AuthController`, `TipoServicoController`, `RegraEscalaController`, `QualificacaoController`, `SecurityConfig`, testes `TrocaIntervaloIntegrationTest`.

**Frontend — criar**: `src/components/RequisitosPainel.tsx`, `src/utils/requisitos.ts`, `src/utils/requisitos.test.ts`.
**Frontend — modificar**: `src/api/types.ts`, `src/App.tsx`, `src/context/AuthContext.tsx`, `src/components/Shell.tsx`, `src/components/MilitarDetalheOverlay.tsx`, `src/components/RichEditor.tsx`, páginas `Boletim`, `EscalaDoDia`, `MinhaEscala`, `MinhaConta`, `FichaMilitar`, `Militares`, `PerfisPermissoes`, `Login`, `RegrasEscala`, `TiposServico`, `Auditoria`; `package.json`.

---

### Task 0: Preparação

- [ ] **Step 1:** Confirmar que o Plano 1 está commitado (`git status` limpo em `melhoria/estrutura`) e, com o ok do usuário, `git checkout -b melhoria/implementacoes`.
- [ ] **Step 2:** Linha de base: `cd backend && mvn -q test` e `cd ../frontend && npm test && npm run build` — tudo verde. Anotar o número de testes.

---

### Task 1: Escala em rascunho só para a sargenteação

Hoje `GET /api/minha-escala`, `GET /api/escalas/dia` e o histórico de serviços devolvem serviços de escala em RASCUNHO. O Militar Escalado vê (e pode pedir troca de) uma escala que ainda vai mudar. A publicação só afeta o lembrete por e-mail.

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/adapters/web/PerfisSargenteacao.java`
- Modify: `adapters/persistence/ServicoEscaladoRepository.java`, `application/ConsultaEscalaService.java`, `application/MinhaEscalaService.java`, `application/MilitarService.java`, `application/SolicitacaoService.java`, `adapters/web/EscalaController.java`, `adapters/web/MilitarController.java`
- Modify: `backend/src/test/java/.../application/TrocaIntervaloIntegrationTest.java` (escala de teste passa a ser PUBLICADA)
- Modify: `frontend/src/pages/EscalaDoDia.tsx`, `frontend/src/pages/MinhaEscala.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/adapters/web/VisibilidadeRascunhoIntegrationTest.java`

**Interfaces:**
- Consumes: `ConsultaEscalaService` e `UsuarioLogadoService` (Plano 1, Tarefa 3); `SituacaoEscala` (Plano 1, Tarefa 2).
- Produces:
  - `PerfisSargenteacao.ehSargenteacao(Authentication auth): boolean` (estático; usado também na Tarefa 2)
  - `ConsultaEscalaService.doDia(LocalDate data, boolean incluirRascunho): List<ServicoEscalado>`
  - `MilitarService.historicoServicos(Long militarId, boolean incluirRascunho): List<ServicoEscalado>`
  - `ServicoEscaladoRepository.findByDataAndEscala_Situacao(LocalDate, SituacaoEscala)`
  - `ServicoEscaladoRepository.findByMilitar_IdAndDataBetweenAndEscala_Situacao(Long, LocalDate, LocalDate, SituacaoEscala)`
  - `ServicoEscaladoRepository.findByMilitar_IdAndEscala_SituacaoOrderByDataDesc(Long, SituacaoEscala)`

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.application.SolicitacaoService;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escala em RASCUNHO ainda pode mudar - so a sargenteacao enxerga.
 * Quem e escalado so ve (e so pede troca de) escala PUBLICADA.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VisibilidadeRascunhoIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SolicitacaoService solicitacaoService;

    private Escala escala;
    private ServicoEscalado servico;
    private Militar militarEscalado; // dono da conta 00000000004 (perfil MILITAR_ESCALADO)
    private LocalDate dia;

    @BeforeEach
    void montar() {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        militarEscalado = usuarioRepository.findByLogin("00000000004").orElseThrow().getMilitar();
        TipoServico caboDaGuarda = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Cabo da Guarda")).findFirst().orElseThrow();
        dia = LocalDate.now().plusMonths(2).withDayOfMonth(10);
        escala = escalaRepository.save(Escala.builder().descricao("teste")
                .dataInicio(dia.withDayOfMonth(1)).dataFim(dia.withDayOfMonth(28))
                .situacao(SituacaoEscala.RASCUNHO).usuarioGeracao(sargenteante).build());
        servico = servicoEscaladoRepository.save(ServicoEscalado.builder()
                .escala(escala).data(dia).tipoServico(caboDaGuarda).militar(militarEscalado).build());
    }

    private void publicar() {
        escala.setSituacao(SituacaoEscala.PUBLICADA);
        escalaRepository.save(escala);
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_naoVeRascunhoNaEscalaDoDia() throws Exception {
        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        publicar();
        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("00000000003") // Sd EP da Sargenteacao
    void sargenteacao_veRascunhoNaEscalaDoDia() throws Exception {
        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("00000000004")
    void minhaEscala_mostraSoPublicada() throws Exception {
        String mes = YearMonth.from(dia).toString();
        mvc.perform(get("/api/minha-escala").param("mes", mes))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        publicar();
        mvc.perform(get("/api/minha-escala").param("mes", mes))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("00000000004")
    void meuHistorico_naoMostraRascunho() throws Exception {
        mvc.perform(get("/api/militares/" + militarEscalado.getId() + "/historico-servicos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void pedirTrocaDeServicoEmRascunho_bloqueia() {
        Militar outro = usuarioRepository.findByLogin("00000000002").orElseThrow().getMilitar(); // Cabo
        assertThatThrownBy(() -> solicitacaoService.criar(servico.getId(), outro.getId(), "teste", "00000000004"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ainda não foi publicada");
    }
}
```

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && mvn -q test -Dtest=VisibilidadeRascunhoIntegrationTest`
Expected: FAIL — o Militar Escalado recebe 1 item com a escala em rascunho, e a troca não é bloqueada.

- [ ] **Step 3: Consultas no repositório** (`ServicoEscaladoRepository.java`, com `import br.com.milscale.milscale.domain.SituacaoEscala;`)

```java
    List<ServicoEscalado> findByDataAndEscala_Situacao(LocalDate data, SituacaoEscala situacao);
    List<ServicoEscalado> findByMilitar_IdAndDataBetweenAndEscala_Situacao(Long militarId, LocalDate inicio, LocalDate fim, SituacaoEscala situacao);
    List<ServicoEscalado> findByMilitar_IdAndEscala_SituacaoOrderByDataDesc(Long militarId, SituacaoEscala situacao);
```

- [ ] **Step 4: `PerfisSargenteacao.java`**

```java
package br.com.milscale.milscale.adapters.web;

import org.springframework.security.core.Authentication;

import java.util.Set;

/** Perfis que trabalham na sargenteacao (veem rascunho e dados completos do efetivo). */
public final class PerfisSargenteacao {

    private static final Set<String> ROLES = Set.of(
            "ROLE_CABO_SARGENTEACAO", "ROLE_SD_EP_SARGENTEACAO", "ROLE_SARGENTEANTE");

    private PerfisSargenteacao() {}

    public static boolean ehSargenteacao(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> ROLES.contains(a.getAuthority()));
    }
}
```

- [ ] **Step 5: Filtrar nos services**

`ConsultaEscalaService.doDia`:
```java
    /** RF14 - um dia da escala. Quem nao e da sargenteacao so ve escala PUBLICADA. */
    public List<ServicoEscalado> doDia(LocalDate data, boolean incluirRascunho) {
        return incluirRascunho
                ? servicoEscaladoRepository.findByData(data)
                : servicoEscaladoRepository.findByDataAndEscala_Situacao(data, SituacaoEscala.PUBLICADA);
    }
```

`MinhaEscalaService.doMes`:
```java
    /** RF13 - so escala PUBLICADA: rascunho ainda pode mudar e nao deve orientar ninguem. */
    public List<ServicoEscalado> doMes(Long militarId, YearMonth mes) {
        return servicoEscaladoRepository.findByMilitar_IdAndDataBetweenAndEscala_Situacao(
                militarId, mes.atDay(1), mes.atEndOfMonth(), SituacaoEscala.PUBLICADA);
    }
```

`MilitarService.historicoServicos`:
```java
    public List<ServicoEscalado> historicoServicos(Long militarId, boolean incluirRascunho) {
        return incluirRascunho
                ? servicoEscaladoRepository.findByMilitar_IdOrderByDataDesc(militarId)
                : servicoEscaladoRepository.findByMilitar_IdAndEscala_SituacaoOrderByDataDesc(militarId, SituacaoEscala.PUBLICADA);
    }
```

`SolicitacaoService` — em `criar`, logo depois da checagem "Esse serviço não é seu":
```java
        if (servico.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) {
            throw new IllegalArgumentException("Essa escala ainda não foi publicada — só dá pra pedir troca depois da publicação");
        }
```
Em `criarTrocaMutua`, depois da checagem equivalente:
```java
        if (servicoOrigem.getEscala().getSituacao() != SituacaoEscala.PUBLICADA
                || servicoDestino.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) {
            throw new IllegalArgumentException("Essa escala ainda não foi publicada — só dá pra pedir troca depois da publicação");
        }
```
Em `listarElegiveisParaTrocaMutua`, dentro do laço, junto dos outros `continue`:
```java
            if (candidato.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) continue;
```

- [ ] **Step 6: Controllers passam o perfil**

`EscalaController.escalaDoDia`:
```java
    @GetMapping("/dia")
    public List<ServicoEscalado> escalaDoDia(@RequestParam String data, Authentication auth) {
        return consultaEscalaService.doDia(LocalDate.parse(data), PerfisSargenteacao.ehSargenteacao(auth));
    }
```

`MilitarController.historicoServicos` (mesma anotação `@PreAuthorize` de hoje):
```java
    public List<ServicoEscalado> historicoServicos(@PathVariable Long id, Authentication auth) {
        return militarService.historicoServicos(id, PerfisSargenteacao.ehSargenteacao(auth));
    }
```

- [ ] **Step 7: Ajustar o teste de trocas existente**

Em `TrocaIntervaloIntegrationTest.montarCenario()`, no builder da `escalaTeste`, acrescentar `.situacao(SituacaoEscala.PUBLICADA)` antes do `.usuarioGeracao(...)`. (Trocas agora exigem escala publicada; o cenário simula uma escala publicada.)

- [ ] **Step 8: Rodar todos os testes**

Run: `cd backend && mvn -q test`
Expected: PASS.

- [ ] **Step 9: Mensagens no frontend**

`pages/EscalaDoDia.tsx`: trocar o texto `Nenhum serviço registrado para esse dia.` por `Nenhuma escala publicada para esse dia ainda.`

`pages/MinhaEscala.tsx`: no `<PageHeader ...>`, usar `subtitle="Seus serviços nas escalas já publicadas pelo Sargenteante"` (se o componente não tiver `subtitle` hoje, acrescentar).

Run: `cd frontend && npm run build` → PASS.

- [ ] **Step 10: Teste manual**
  1. Como `000.000.000-01`, gerar uma escala para o mês que vem, sem publicar.
  2. Como `000.000.000-04`, abrir "Minha escala" e "Escala do dia" num dia desse mês: vazio.
  3. Como `000.000.000-01`, publicar a escala.
  4. Como `000.000.000-04`, abrir de novo: os serviços aparecem.

- [ ] **Step 11: Checkpoint** — diff, sugerir `feat: escala em rascunho visivel so para a sargenteacao` e aguardar o usuário commitar.

---

### Task 2: Privacidade dos dados do militar e foto sob demanda

Toda resposta que inclui um `Militar` serializa a entidade inteira: CPF, data de nascimento, FUSEX, NR de registro, e-mail, telefone e **foto em base64**. Isso vale para `GET /api/militares/{id}` (aberto a qualquer logado), a escala do dia, trocas, avisos e usuários. Consequências: um Militar Escalado lê o CPF de todo o efetivo, e a escala de um mês carrega a foto repetida em centenas de serviços.

Correção:
- esses campos passam a ser **só de entrada** no JSON da entidade;
- o detalhe completo sai por um DTO, só para a sargenteação ou para a própria pessoa;
- a foto ganha um endpoint próprio.

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/adapters/web/dto/MilitarDetalheResponse.java`
- Modify: `domain/Militar.java`, `adapters/web/MilitarController.java`
- Modify: `frontend/src/api/types.ts`, `frontend/src/components/MilitarDetalheOverlay.tsx`, `frontend/src/pages/FichaMilitar.tsx`, `frontend/src/pages/MinhaConta.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/adapters/web/PrivacidadeMilitarIntegrationTest.java`

**Interfaces:**
- Consumes: `PerfisSargenteacao.ehSargenteacao` (Tarefa 1).
- Produces:
  - `MilitarDetalheResponse.completo(Militar)`, `MilitarDetalheResponse.publico(Militar)`
  - `Militar.isTemFoto(): boolean` → JSON `temFoto`
  - `GET /api/militares/{id}/foto` → `200 {"fotoBase64": "..."}` ou `204`
  - No front: `Militar.cpf`, `numeroRegistro`, `dataNascimento`, `fusex`, `email`, `telefone` passam a ser opcionais; `fotoBase64` sai do tipo; entra `temFoto: boolean`.

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrivacidadeMilitarIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;

    private Long idDe(String login) {
        return usuarioRepository.findByLogin(login).orElseThrow().getMilitar().getId();
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_naoVeDadosPessoaisDeColega() throws Exception {
        mvc.perform(get("/api/militares/" + idDe("00000000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeGuerra").value("Zeni"))
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.dataNascimento").doesNotExist())
                .andExpect(jsonPath("$.fusex").doesNotExist())
                .andExpect(jsonPath("$.fotoBase64").doesNotExist());
    }

    @Test
    @WithUserDetails("00000000004")
    void militarEscalado_veOsProprios() throws Exception {
        mvc.perform(get("/api/militares/" + idDe("00000000004")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("00000000004"));
    }

    @Test
    @WithUserDetails("00000000001")
    void sargenteante_veDadosCompletos() throws Exception {
        mvc.perform(get("/api/militares/" + idDe("00000000004")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("00000000004"))
                .andExpect(jsonPath("$.dataNascimento").exists());
    }

    @Test
    @WithUserDetails("00000000001")
    void escalaDoDiaENaListagem_naoCarregamCpfNemFoto() throws Exception {
        Usuario sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        Militar m = sargenteante.getMilitar();
        m.setFotoBase64("data:image/png;base64,AAAA");
        militarRepository.save(m);
        TipoServico tipo = tipoServicoRepository.findAll().get(0);
        LocalDate dia = LocalDate.now().plusMonths(2).withDayOfMonth(3);
        Escala escala = escalaRepository.save(Escala.builder().descricao("t").dataInicio(dia).dataFim(dia)
                .situacao(SituacaoEscala.PUBLICADA).usuarioGeracao(sargenteante).build());
        servicoEscaladoRepository.save(ServicoEscalado.builder().escala(escala).data(dia).tipoServico(tipo).militar(m).build());

        mvc.perform(get("/api/escalas/dia").param("data", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"cpf\""))))
                .andExpect(content().string(not(containsString("fotoBase64"))))
                .andExpect(jsonPath("$[0].militar.temFoto").value(true));
        mvc.perform(get("/api/militares"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("fotoBase64"))));
    }

    @Test
    @WithUserDetails("00000000004")
    void fotoTemEndpointProprio() throws Exception {
        Militar m = militarRepository.findById(idDe("00000000001")).orElseThrow();
        m.setFotoBase64("data:image/png;base64,AAAA");
        militarRepository.save(m);

        mvc.perform(get("/api/militares/" + m.getId() + "/foto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoBase64").value("data:image/png;base64,AAAA"));
        mvc.perform(get("/api/militares/" + idDe("00000000002") + "/foto"))
                .andExpect(status().isNoContent());
    }
}
```

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && mvn -q test -Dtest=PrivacidadeMilitarIntegrationTest`
Expected: FAIL — `$.cpf` aparece para o Militar Escalado, `fotoBase64` aparece na escala e o endpoint `/foto` não existe.

- [ ] **Step 3: Campos sensíveis só de entrada na entidade**

Em `Militar.java`, adicionar `import com.fasterxml.jackson.annotation.JsonProperty;` e anotar **cada um** dos campos `cpf`, `numeroRegistro`, `dataNascimento`, `fusex`, `fotoBase64`, `email`, `telefone` com:
```java
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
```
(continuam aceitos no cadastro e na edição, mas nunca são serializados a partir da entidade). Adicionar o método:
```java
    /** A foto em si sai por GET /api/militares/{id}/foto - aqui so o aviso de que existe,
     *  pra nao repetir base64 em cada servico da escala. */
    public boolean isTemFoto() {
        return fotoBase64 != null && !fotoBase64.isBlank();
    }
```

- [ ] **Step 4: Criar `MilitarDetalheResponse.java`**

```java
package br.com.milscale.milscale.adapters.web.dto;

import br.com.milscale.core.domain.SituacaoPessoa;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PostoGraduacao;
import br.com.milscale.milscale.domain.Qualificacao;
import br.com.milscale.milscale.domain.Subunidade;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.Set;

/**
 * Visao individual de um militar. "completo" (sargenteacao ou a propria
 * pessoa) inclui os dados pessoais da carteira de identidade; "publico"
 * (colega vendo colega) omite esses campos - eles nem aparecem no JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MilitarDetalheResponse(
        Long id, String nomeCompleto, String nomeGuerra, String nomeExibicao,
        PostoGraduacao posto, Subunidade subunidade, SituacaoPessoa situacao,
        long contadorRodizio, Set<Qualificacao> qualificacoes, boolean temFoto,
        String cpf, String numeroRegistro, LocalDate dataNascimento, String fusex,
        String email, String telefone) {

    public static MilitarDetalheResponse completo(Militar m) {
        return new MilitarDetalheResponse(m.getId(), m.getNomeCompleto(), m.getNomeGuerra(), m.getNomeExibicao(),
                m.getPosto(), m.getSubunidade(), m.getSituacao(), m.getContadorRodizio(), m.getQualificacoes(), m.isTemFoto(),
                m.getCpf(), m.getNumeroRegistro(), m.getDataNascimento(), m.getFusex(), m.getEmail(), m.getTelefone());
    }

    public static MilitarDetalheResponse publico(Militar m) {
        return new MilitarDetalheResponse(m.getId(), m.getNomeCompleto(), m.getNomeGuerra(), m.getNomeExibicao(),
                m.getPosto(), m.getSubunidade(), m.getSituacao(), m.getContadorRodizio(), m.getQualificacoes(), m.isTemFoto(),
                null, null, null, null, null, null);
    }
}
```

- [ ] **Step 5: `MilitarController` — detalhe por perfil e endpoint de foto**

```java
    /** Aberto a qualquer autenticado (popup da Escala do dia). Dados pessoais
     *  so para a sargenteacao ou para a propria pessoa. */
    @GetMapping("/{id}")
    public MilitarDetalheResponse buscar(@PathVariable Long id, Authentication auth) {
        Militar m = militarService.buscar(id);
        boolean completo = PerfisSargenteacao.ehSargenteacao(auth) || militarService.ehOProprio(id, auth.getName());
        return completo ? MilitarDetalheResponse.completo(m) : MilitarDetalheResponse.publico(m);
    }

    /** Foto 3x4 sob demanda - 204 quando a pessoa nao tem foto cadastrada. */
    @GetMapping("/{id}/foto")
    public ResponseEntity<Map<String, String>> foto(@PathVariable Long id) {
        Militar m = militarService.buscar(id);
        if (!m.isTemFoto()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(Map.of("fotoBase64", m.getFotoBase64()));
    }
```
(imports: `MilitarDetalheResponse`, `org.springframework.http.ResponseEntity`, `java.util.Map`.)

- [ ] **Step 6: Rodar os testes do backend**

Run: `cd backend && mvn -q test`
Expected: PASS.

- [ ] **Step 7: Frontend — tipos**

Em `src/api/types.ts`, na interface `Militar`: remover `fotoBase64?: string;`, tornar opcionais `cpf?: string;` (e manter opcionais os demais sensíveis) e adicionar `temFoto: boolean;`.

- [ ] **Step 8: Frontend — popup do militar busca a foto e esconde o que não veio**

Em `components/MilitarDetalheOverlay.tsx`:
- novo estado `const [foto, setFoto] = useState<string | null>(null);`;
- no `useEffect`, depois de `setMilitar(m)` (dentro do `.then`), acrescentar:
```tsx
      setFoto(null);
      if (m.temFoto) {
        api.get<{ fotoBase64: string } | undefined>(`/api/militares/${militarId}/foto`)
          .then((r) => setFoto(r?.fotoBase64 ?? null));
      }
```
- trocar `militar.fotoBase64 ? (<img src={militar.fotoBase64} .../>)` por `foto ? (<img src={foto} .../>)`;
- trocar as quatro linhas `CampoDado` de CPF / NR REGISTRO / DATA NASCIMENTO / FUSEX por:
```tsx
              {militar.cpf !== undefined && (
                <>
                  <CampoDado label="CPF" valor={formatarCpf(militar.cpf)} />
                  <CampoDado label="NR REGISTRO" valor={militar.numeroRegistro || "—"} />
                  <CampoDado label="DATA NASCIMENTO" valor={militar.dataNascimento ? formatarDataBR(militar.dataNascimento) : "—"} />
                  <CampoDado label="FUSEX" valor={militar.fusex || "—"} />
                </>
              )}
```

- [ ] **Step 9: Frontend — Ficha e Minha conta com CPF opcional**

- `pages/FichaMilitar.tsx`, visão de leitura: `formatarCpf(militar.cpf)` → `militar.cpf ? formatarCpf(militar.cpf) : "—"`. Formulário de edição: `cpf: mascararCpf(militar.cpf)` → `cpf: mascararCpf(militar.cpf ?? "")`. A Ficha é só da sargenteação, que recebe o detalhe completo, então o comportamento não muda.
- `pages/MinhaConta.tsx`: `formatarCpf(militar.cpf)` → `militar.cpf ? formatarCpf(militar.cpf) : "—"`.

Run: `cd frontend && npm test && npm run build` → PASS (o `tsc -b` acusa qualquer uso esquecido de `fotoBase64`/`cpf` obrigatório).

- [ ] **Step 10: Teste manual**
  1. Como `000.000.000-01`, cadastrar foto num militar escalado para hoje. Abrir o popup pela Escala do mês: a foto e o CPF aparecem.
  2. Como `000.000.000-04`, abrir o popup do mesmo colega pela Escala do dia: aparecem a foto, o nome, o posto e os cursos, mas não o bloco de CPF/NR/nascimento/FUSEX.
  3. Na aba Network do DevTools, a resposta de `/api/escalas/dia` não contém `fotoBase64` nem `cpf`.

- [ ] **Step 11: Checkpoint** — diff, sugerir `fix(seguranca): dados pessoais do militar so para sargenteacao ou o proprio; foto sob demanda` e aguardar o usuário commitar.

---

### Task 3: Boletim sem XSS

O conteúdo do Boletim é HTML livre, gravado sem limpeza e renderizado com `dangerouslySetInnerHTML`. Um Cabo (ou qualquer um com acesso à API) pode gravar `<img src=x onerror=...>` e executar script na sessão de quem lê, inclusive do Sargenteante. A correção limpa o HTML nas duas pontas: o **backend** limpa ao gravar (fonte da verdade) e o **frontend** limpa ao exibir (cobre também o que já está gravado).

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/java/br/com/milscale/milscale/application/SanitizadorHtml.java`
- Modify: `application/BoletimService.java`
- Modify: `frontend/package.json`, `frontend/src/pages/Boletim.tsx`, `frontend/src/components/RichEditor.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/SanitizadorHtmlTest.java`, `backend/src/test/java/br/com/milscale/milscale/application/BoletimSanitizacaoIntegrationTest.java`

**Interfaces:**
- Produces: `SanitizadorHtml.sanitizar(String html): String` (null → null).

- [ ] **Step 1: Escrever o teste unitário (falha: classe não existe)**

```java
package br.com.milscale.milscale.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizadorHtmlTest {

    private final SanitizadorHtml sanitizador = new SanitizadorHtml();

    @Test
    void removeScript() {
        assertThat(sanitizador.sanitizar("<p>oi</p><script>alert(1)</script>")).isEqualTo("<p>oi</p>");
    }

    @Test
    void removeAtributoDeEvento() {
        assertThat(sanitizador.sanitizar("<img src=\"https://x/a.png\" onerror=\"alert(1)\">"))
                .doesNotContain("onerror");
    }

    @Test
    void removeLinkJavascript() {
        assertThat(sanitizador.sanitizar("<a href=\"javascript:alert(1)\">x</a>")).doesNotContain("javascript");
    }

    @Test
    void mantemImagemColadaEmBase64ComEstilo() {
        String img = "<img src=\"data:image/png;base64,AAAA\" style=\"max-width:100%\">";
        assertThat(sanitizador.sanitizar(img))
                .contains("src=\"data:image/png;base64,AAAA\"")
                .contains("style=\"max-width:100%\"");
    }

    @Test
    void mantemFormatacaoDoEditor() {
        String html = "<h3>Titulo</h3><p><b>negrito</b> <i>italico</i></p><ul><li>item</li></ul>";
        assertThat(sanitizador.sanitizar(html)).isEqualTo(html);
    }

    @Test
    void nuloContinuaNulo() {
        assertThat(sanitizador.sanitizar(null)).isNull();
    }
}
```

Run: `cd backend && mvn -q test -Dtest=SanitizadorHtmlTest` → FAIL de compilação.

- [ ] **Step 2: Dependência jsoup** no `pom.xml` (não é gerenciada pelo Spring Boot, então a versão vai explícita):

```xml
    <dependency>
      <groupId>org.jsoup</groupId>
      <artifactId>jsoup</artifactId>
      <version>1.18.1</version>
    </dependency>
```

- [ ] **Step 3: Criar `SanitizadorHtml.java`**

```java
package br.com.milscale.milscale.application;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * Limpa o HTML do Boletim antes de gravar: fica so a formatacao que o
 * editor produz (paragrafo, titulo, negrito, lista, imagem...) e sai
 * tudo que executa codigo (script, on*=, javascript:). Imagem colada vem
 * como data URI - por isso "data" e liberado so no src de <img>.
 */
@Component
public class SanitizadorHtml {

    private static final Safelist PERMITIDO = Safelist.relaxed()
            .addAttributes("img", "style")
            .addProtocols("img", "src", "data");

    private static final Document.OutputSettings SAIDA = new Document.OutputSettings().prettyPrint(false);

    public String sanitizar(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, "", PERMITIDO, SAIDA);
    }
}
```

Run: `mvn -q test -Dtest=SanitizadorHtmlTest` → PASS. (Se `mantemFormatacaoDoEditor` falhar só por diferença de espaço ou aspas na saída do jsoup, ajustar a asserção para `contains` de cada tag. A regra de limpeza não muda.)

- [ ] **Step 4: Teste de integração do Boletim (falha)**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Boletim;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BoletimSanitizacaoIntegrationTest {

    @Autowired private BoletimService boletimService;

    @Test
    void criar_gravaHtmlLimpo() {
        Boletim b = boletimService.criar("1", "Teste", "<p>ok</p><img src=x onerror=alert(1)>", null, null, "00000000001");
        assertThat(b.getConteudoHtml()).contains("<p>ok</p>").doesNotContain("onerror");
    }

    @Test
    void atualizar_gravaHtmlLimpo() {
        Boletim b = boletimService.criar("1", "Teste", "<p>ok</p>", null, null, "00000000001");
        Boletim editado = boletimService.atualizar(b.getId(), "1", "Teste", "<p>novo</p><script>x()</script>", null, null);
        assertThat(editado.getConteudoHtml()).isEqualTo("<p>novo</p>");
    }

    @Test
    void conteudoQueSoTinhaScript_ficaVazioERecusado() {
        assertThatThrownBy(() -> boletimService.criar("1", "Teste", "<script>x()</script>", null, null, "00000000001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O boletim não pode ficar vazio");
    }
}
```

Run: `mvn -q test -Dtest=BoletimSanitizacaoIntegrationTest` → FAIL.

- [ ] **Step 5: `BoletimService` limpa antes de validar e gravar**

Injetar `SanitizadorHtml sanitizador` no construtor. Em `criar` e em `atualizar`, **antes** da checagem `if (conteudoHtml == null || conteudoHtml.isBlank())`, acrescentar:
```java
        conteudoHtml = sanitizador.sanitizar(conteudoHtml);
```

Run: `cd backend && mvn -q test` → PASS.

- [ ] **Step 6: Frontend — DOMPurify ao exibir e ao editar**

```bash
cd frontend && npm install dompurify
```
`pages/Boletim.tsx`: `import DOMPurify from "dompurify";` e trocar `dangerouslySetInnerHTML={{ __html: b.conteudoHtml }}` por `dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(b.conteudoHtml) }}`.

`components/RichEditor.tsx`: `import DOMPurify from "dompurify";` e trocar `ref.current.innerHTML = valorInicial || "";` por `ref.current.innerHTML = DOMPurify.sanitize(valorInicial || "");` (boletim antigo aberto para edição também é limpo).

Run: `npm test && npm run build` → PASS.

- [ ] **Step 7: Teste manual**
  1. Criar um boletim com texto, negrito, lista e uma imagem colada (Ctrl+V): tudo aparece igual.
  2. Via DevTools, mandar `POST /api/boletins` com `conteudoHtml` = `<img src=x onerror="alert(1)">texto`.
  3. Abrir esse boletim: não aparece alerta nenhum.

- [ ] **Step 8: Checkpoint** — diff, sugerir `fix(seguranca): sanitiza HTML do Boletim (jsoup no back, DOMPurify no front)` e aguardar o usuário commitar.

---

### Task 4: Senha temporária e troca obrigatória no primeiro acesso

Hoje o "resetar senha" volta todo mundo para `milscale123`, que aparece na tela de login e na tela de Perfis. Quem souber o CPF de alguém que teve a senha resetada entra na conta dessa pessoa.

Correção:
- o reset gera uma **senha temporária aleatória**, mostrada uma única vez ao Sargenteante;
- a conta fica marcada como `senhaTemporaria`;
- enquanto a marca existir, o **backend** recusa todas as chamadas da API, exceto `/api/auth/**`;
- o **frontend** leva a pessoa direto para "Minha conta" para criar a própria senha.

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__usuario_senha_temporaria.sql`
- Create: `backend/src/main/java/br/com/milscale/milscale/application/GeradorDeSenha.java`
- Create: `backend/src/main/java/br/com/milscale/milscale/adapters/config/SenhaTemporariaFilter.java`
- Modify: `domain/Usuario.java`, `application/UsuarioService.java`, `application/ContaService.java`, `adapters/web/UsuarioController.java`, `adapters/web/AuthController.java`, `adapters/config/SecurityConfig.java`
- Modify: `frontend/src/api/types.ts`, `src/context/AuthContext.tsx`, `src/App.tsx`, `src/components/Shell.tsx`, `src/pages/MinhaConta.tsx`, `src/pages/PerfisPermissoes.tsx`, `src/pages/Login.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/SenhaTemporariaIntegrationTest.java`

**Interfaces:**
- Produces:
  - coluna `usuario.senha_temporaria` / campo `Usuario.senhaTemporaria` (boolean, default false)
  - `GeradorDeSenha.gerar(): String` (10 caracteres, sem caracteres ambíguos)
  - `UsuarioService.resetarSenha(Long usuarioId): String` (devolve a senha temporária)
  - `POST /api/usuarios/{id}/resetar-senha` → `{"mensagem": ..., "senhaTemporaria": "..."}`
  - `GET /api/auth/me` ganha `"trocarSenha": boolean`
  - No front: `Usuario.trocarSenha: boolean`; `useAuth().recarregar(): Promise<void>`

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SenhaTemporariaIntegrationTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private ContaService contaService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MockMvc mvc;

    private Usuario usuario4() {
        return usuarioRepository.findByLogin("00000000004").orElseThrow();
    }

    @Test
    void resetar_geraSenhaAleatoriaEMarcaComoTemporaria() {
        String senha = usuarioService.resetarSenha(usuario4().getId());
        String outra = usuarioService.resetarSenha(usuario4().getId());

        assertThat(senha).hasSize(10).isNotEqualTo("milscale123").isNotEqualTo(outra);
        Usuario u = usuario4();
        assertThat(passwordEncoder.matches(outra, u.getSenhaHash())).isTrue();
        assertThat(u.isSenhaTemporaria()).isTrue();
    }

    @Test
    void trocarSenha_tiraAMarcaDeTemporaria() {
        String temp = usuarioService.resetarSenha(usuario4().getId());
        contaService.trocarSenha("00000000004", temp, "minhaSenhaNova");
        assertThat(usuario4().isSenhaTemporaria()).isFalse();
    }

    @Test
    @WithUserDetails("00000000004")
    void comSenhaTemporaria_apiBloqueadaMenosAuth() throws Exception {
        Usuario u = usuario4();
        u.setSenhaTemporaria(true);
        usuarioRepository.save(u);

        mvc.perform(get("/api/boletins"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Troque sua senha temporária antes de continuar"));
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocarSenha").value(true));
    }

    @Test
    @WithUserDetails("00000000004")
    void semSenhaTemporaria_apiLiberada() throws Exception {
        mvc.perform(get("/api/boletins")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me")).andExpect(jsonPath("$.trocarSenha").value(false));
    }
}
```

Run: `cd backend && mvn -q test -Dtest=SenhaTemporariaIntegrationTest` → FAIL de compilação (`isSenhaTemporaria` não existe; `resetarSenha` devolve `void`).

- [ ] **Step 2: Migration `V2__usuario_senha_temporaria.sql`**

```sql
-- Conta com senha gerada pelo sistema (reset ou cadastro novo): a pessoa
-- e obrigada a criar a propria senha antes de usar qualquer outra funcao.
alter table usuario add column senha_temporaria boolean not null default false;
```

- [ ] **Step 3: Campo na entidade** (`Usuario.java`, depois de `ativo`):

```java
    /** Senha gerada pelo sistema - enquanto true, so /api/auth/** responde (SenhaTemporariaFilter). */
    @Column(name = "senha_temporaria", nullable = false)
    @Builder.Default
    private boolean senhaTemporaria = false;
```

- [ ] **Step 4: `GeradorDeSenha.java`**

```java
package br.com.milscale.milscale.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Senha temporaria aleatoria - sem 0/O, 1/l/I pra poder ser ditada ou anotada sem confusao. */
@Component
public class GeradorDeSenha {

    private static final String ALFABETO = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 10;
    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        StringBuilder sb = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
```

- [ ] **Step 5: Reset devolve a senha temporária; troca limpa a marca**

`UsuarioService` (injetar `GeradorDeSenha geradorDeSenha` no construtor):
```java
    /** Gera uma senha temporaria (mostrada uma unica vez pra quem resetou) e obriga a troca no proximo acesso. */
    @Transactional
    public String resetarSenha(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        String senha = geradorDeSenha.gerar();
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setSenhaTemporaria(true);
        usuarioRepository.save(usuario);
        return senha;
    }
```

`ContaService.trocarSenha` — logo antes do `usuarioRepository.save(usuario);` final:
```java
        usuario.setSenhaTemporaria(false);
```

`UsuarioController.resetarSenha`:
```java
    @PostMapping("/{id}/resetar-senha")
    public Map<String, String> resetarSenha(@PathVariable Long id, Authentication auth) {
        String senhaTemporaria = usuarioService.resetarSenha(id);
        auditoriaService.registrar(auth.getName(), "SENHA_RESETADA", "usuário id " + id); // nunca a senha
        return Map.of("mensagem", "Senha temporária gerada", "senhaTemporaria", senhaTemporaria);
    }
```

`AuthController.me` — no `Map.of(...)`, acrescentar o par `"trocarSenha", usuario.isSenhaTemporaria()`.

- [ ] **Step 6: `SenhaTemporariaFilter.java`**

```java
package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Enquanto a conta estiver com senha temporaria, so /api/auth/** responde
 * (ver quem sou eu, trocar a senha, sair). A trava fica no servidor, nao
 * so na tela - senao bastaria chamar a API direto pra pular a troca.
 * Nao e @Component de proposito: e registrado so dentro da cadeia do
 * Spring Security (SecurityConfig), pra nao rodar duas vezes.
 */
public class SenhaTemporariaFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public SenhaTemporariaFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean logado = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);

        if (logado && uri.startsWith("/api/") && !uri.startsWith("/api/auth/")
                && usuarioRepository.findByLogin(auth.getName()).map(Usuario::isSenhaTemporaria).orElse(false)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\":\"Troque sua senha temporária antes de continuar\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
```

`SecurityConfig`:
- acrescentar `UsuarioRepository usuarioRepository` ao construtor (ao lado de `origensPermitidas`, do Plano 1) e guardar em campo;
- no `filterChain`, antes de `return http.build();`:
```java
        http.addFilterAfter(new SenhaTemporariaFilter(usuarioRepository),
                org.springframework.security.web.access.intercept.AuthorizationFilter.class);
```

Run: `cd backend && mvn -q test` → PASS.

- [ ] **Step 7: Frontend — sessão sabe da senha temporária**

`src/api/types.ts`, interface `Usuario`: acrescentar `trocarSenha: boolean;`.

`src/context/AuthContext.tsx`: acrescentar `recarregar: () => Promise<void>;` em `AuthContextValue` e expor `recarregar: carregarSessao` no `value` do Provider.

`src/App.tsx`, em `RotaProtegida` (importar `useLocation` de `react-router-dom`):
```tsx
function RotaProtegida({ children }: { children: React.ReactNode }) {
  const { usuario, carregando } = useAuth();
  const local = useLocation();
  if (carregando) return <div style={{ padding: 40 }}>Carregando…</div>;
  if (!usuario) return <Navigate to="/login" replace />;
  // Senha temporária: nada funciona antes de criar a própria (o backend também bloqueia).
  if (usuario.trocarSenha && local.pathname !== "/minha-conta") return <Navigate to="/minha-conta" replace />;
  return <Shell>{children}</Shell>;
}
```

`src/components/Shell.tsx`: com senha temporária, sem menu e sem sininho (as chamadas dariam 403):
- `const menu = usuario ? MENU_POR_PERFIL[usuario.perfil] : undefined;` → `const menu = usuario && !usuario.trocarSenha ? MENU_POR_PERFIL[usuario.perfil] : undefined;`
- as duas ocorrências `{usuario && <NotificacaoSino />}` → `{usuario && !usuario.trocarSenha && <NotificacaoSino />}`

- [ ] **Step 8: Frontend — Minha conta no modo "crie sua senha"**

Em `pages/MinhaConta.tsx`:
- `const { usuario } = useAuth();` → `const { usuario, recarregar } = useAuth();`;
- no `useEffect`: `if (!usuario) return;` → `if (!usuario || usuario.trocarSenha) return;`;
- no JSX, logo depois de `<div className="body">`:
```tsx
        {usuario?.trocarSenha && (
          <div className="card" style={{ background: "var(--amber-bg)", border: "none" }}>
            <p style={{ fontSize: 13, color: "var(--amber-text)", fontWeight: 600 }}>
              Sua senha é temporária. Crie uma senha nova para continuar usando o sistema.
            </p>
          </div>
        )}
```
- envolver o card "Meus dados" em `{!usuario?.trocarSenha && ( ... )}`;
- `<TrocarSenhaForm />` → `<TrocarSenhaForm onTrocou={recarregar} />`; assinatura `function TrocarSenhaForm({ onTrocou }: { onTrocou: () => Promise<void> })`; no `salvar`, depois de `setConfirmarSenha("");`, acrescentar `await onTrocou();`.

- [ ] **Step 9: Frontend — Perfis mostra a senha gerada; login sem senha exposta**

`pages/PerfisPermissoes.tsx`, função `resetarSenha`:
```tsx
  async function resetarSenha(usuarioId: number, nome: string) {
    if (!confirm(`Gerar uma senha temporária para ${nome}? A senha atual deixa de funcionar.`)) return;
    setErro(null);
    setProcessando(usuarioId);
    try {
      const r = await api.post<{ senhaTemporaria: string }>(`/api/usuarios/${usuarioId}/resetar-senha`, {});
      alert(`Senha temporária de ${nome}: ${r.senhaTemporaria}\n\nEla aparece só agora. Entregue pessoalmente — no primeiro acesso a pessoa vai ser obrigada a criar uma senha nova.`);
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível resetar a senha.");
    } finally {
      setProcessando(null);
    }
  }
```

`pages/Login.tsx`: envolver o parágrafo "Contas de demonstração (senha milscale123) ..." em `{import.meta.env.DEV && ( ... )}`. Ele continua aparecendo no `npm run dev` e some do build de produção. As contas de demonstração continuam documentadas no README.

Run: `cd frontend && npm test && npm run build` → PASS.

- [ ] **Step 10: Teste manual**
  1. Como Sargenteante, em Perfis e permissões, resetar a senha de `000.000.000-04` e anotar a senha exibida.
  2. Sair e entrar com o CPF e a senha temporária: a tela cai direto em "Minha conta", sem menu, com o aviso.
  3. Tentar abrir `/boletim` pela barra de endereço: volta para "Minha conta".
  4. Trocar a senha: o menu aparece e o sistema funciona.
  5. Conferir no Log de auditoria que a senha não aparece em lugar nenhum.

- [ ] **Step 11: Checkpoint** — diff, sugerir `feat(seguranca): senha temporaria aleatoria com troca obrigatoria` e aguardar o usuário commitar.

---

### Task 5: Conta de acesso criada junto com o cadastro do militar

`MilitarService.cadastrar` só grava o `Militar`, sem criar `Usuario`, então quem é cadastrado pela tela nunca consegue entrar no sistema (o README promete o contrário). Além disso:
- o cadastro não valida CPF duplicado com mensagem clara (depende do 409 genérico do banco) nem normaliza a máscara;
- desligar um militar não desativa a conta dele, que continua conseguindo entrar.

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/adapters/web/dto/MilitarCadastradoResponse.java`
- Modify: `application/MilitarService.java`, `adapters/web/MilitarController.java`
- Modify: `frontend/src/pages/Militares.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/MilitarCadastroIntegrationTest.java`

**Interfaces:**
- Consumes: `GeradorDeSenha.gerar()` e `Usuario.senhaTemporaria` (Tarefa 4); `MilitarDetalheResponse` (Tarefa 2).
- Produces:
  - `MilitarService.cadastrar(Militar): MilitarService.MilitarCadastrado` com `record MilitarCadastrado(Militar militar, String senhaTemporaria)`
  - `POST /api/militares` → `{"militar": MilitarDetalheResponse, "senhaTemporaria": "..."}`

- [ ] **Step 1: Escrever os testes que falham**

```java
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

    private Militar novo(String cpf, String nomeGuerra) {
        return Militar.builder().nomeCompleto("Novo " + nomeGuerra).nomeGuerra(nomeGuerra).cpf(cpf)
                .posto(postoRepository.findAll().get(0)).subunidade(subunidadeRepository.findAll().get(0)).build();
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
```

Run: `cd backend && mvn -q test -Dtest=MilitarCadastroIntegrationTest` → FAIL de compilação (`MilitarCadastrado` não existe).

- [ ] **Step 2: `MilitarService` — cadastro com conta, CPF normalizado, desligamento completo**

Injetar no construtor `PerfilAcessoRepository perfilAcessoRepository`, `PasswordEncoder passwordEncoder` e `GeradorDeSenha geradorDeSenha`. Então:

```java
    /** Resultado do cadastro: a senha temporaria aparece uma unica vez, pra quem cadastrou entregar a pessoa. */
    public record MilitarCadastrado(Militar militar, String senhaTemporaria) {}

    /** RF04 + RF01 - todo militar cadastrado ganha conta propria: login = CPF, perfil Militar Escalado,
     *  senha temporaria (troca obrigatoria no primeiro acesso). */
    @Transactional
    public MilitarCadastrado cadastrar(Militar militar) {
        militar.setId(null);
        militar.setSituacao(SituacaoPessoa.ATIVO);
        militar.setCpf(normalizarCpf(militar.getCpf()));
        validarCpfUnico(militar.getCpf(), null);
        validarNomeGuerraUnicoNoPosto(militar.getPosto().getId(), militar.getNomeGuerra(), null);
        Militar salvo = militarRepository.save(militar);

        PerfilAcesso perfil = perfilAcessoRepository.findByNome("MILITAR_ESCALADO")
                .orElseThrow(() -> new IllegalStateException("Perfil MILITAR_ESCALADO não cadastrado"));
        String senha = geradorDeSenha.gerar();
        usuarioRepository.save(Usuario.builder()
                .militar(salvo).perfil(perfil).login(salvo.getCpf())
                .senhaHash(passwordEncoder.encode(senha)).senhaTemporaria(true)
                .build());
        return new MilitarCadastrado(salvo, senha);
    }

    /** CPF e login sao guardados so com digitos (a mascara e visual). */
    private static String normalizarCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("O CPF precisa ter 11 números");
        }
        return digitos;
    }
```

Em `atualizar`, como primeira linha depois de `Militar existente = buscar(id);`:
```java
        dados.setCpf(normalizarCpf(dados.getCpf()));
```

`desligar`:
```java
    /** RF04 - inativar (nunca excluir: preserva o historico) e fechar o acesso ao sistema. */
    @Transactional
    public Militar desligar(Long id) {
        Militar existente = buscar(id);
        existente.setSituacao(SituacaoPessoa.DESLIGADO);
        usuarioRepository.findByMilitar_Id(id).ifPresent(u -> {
            u.setAtivo(false);
            usuarioRepository.save(u);
        });
        return militarRepository.save(existente);
    }
```
(imports: `PerfilAcesso`, `Usuario`, `PerfilAcessoRepository`, `PasswordEncoder`.)

- [ ] **Step 3: Resposta do cadastro**

`MilitarCadastradoResponse.java`:
```java
package br.com.milscale.milscale.adapters.web.dto;

/** A senha temporaria vai so nesta resposta - nao fica guardada em lugar nenhum em texto. */
public record MilitarCadastradoResponse(MilitarDetalheResponse militar, String senhaTemporaria) {}
```

`MilitarController.cadastrar`:
```java
    @PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SARGENTEANTE')")
    @PostMapping
    public MilitarCadastradoResponse cadastrar(@RequestBody Militar militar, Authentication auth) {
        MilitarService.MilitarCadastrado r = militarService.cadastrar(militar);
        auditoriaService.registrar(auth.getName(), "MILITAR_CADASTRADO",
                r.militar().getNomeExibicao() + " (id " + r.militar().getId() + ") - conta criada");
        return new MilitarCadastradoResponse(MilitarDetalheResponse.completo(r.militar()), r.senhaTemporaria());
    }
```

Run: `cd backend && mvn -q test` → PASS.

- [ ] **Step 4: Frontend — mostrar as credenciais uma vez**

Em `pages/Militares.tsx`, no `salvar` do `NovoMilitarForm`, trocar `await api.post("/api/militares", {...});` por:
```tsx
      const r = await api.post<{ militar: { nomeExibicao: string }; senhaTemporaria: string }>("/api/militares", {
        // ...mesmo corpo de hoje, sem mudanças...
      });
      alert(
        `Conta criada para ${r.militar.nomeExibicao}.\n\n` +
        `Login: ${mascararCpf(cpf)}\nSenha temporária: ${r.senhaTemporaria}\n\n` +
        `Ela aparece só agora. No primeiro acesso a pessoa vai criar a própria senha.`
      );
```
(`mascararCpf` já é importado de `../utils/mascaras` nesse arquivo; se não for, adicionar ao import.)

Run: `cd frontend && npm test && npm run build` → PASS.

- [ ] **Step 5: Teste manual**
  1. Cadastrar um militar novo pela tela e anotar a senha exibida.
  2. Sair e entrar com o CPF e essa senha: cai na troca obrigatória (Tarefa 4).
  3. Trocar a senha: a pessoa entra como Militar Escalado.
  4. Desligar esse militar: o login dele passa a falhar.

- [ ] **Step 6: Checkpoint** — diff, sugerir `feat: cadastro de militar cria conta de acesso; desligar fecha o acesso` e aguardar o usuário commitar.

---

### Task 6: Regras da escala — máximo de serviços por mês de verdade

A tela Regras da Escala deixa editar "dias de folga" e "máx. serviços/mês", e a entidade guarda também pesos de fim de semana e de feriado. O motor usa **só** o intervalo mínimo.

Decisão tomada com o usuário:
- **implementar** o máximo de serviços por mês, contado por tipo de serviço e por militar;
- **esconder** "dias de folga" (os pesos já não aparecem na tela);
- manter os dois no banco, reservados para o futuro avaliador de justiça.

Se faltar gente, a regra do "aperto" (a escala nunca fica com vaga aberta) continua valendo e pode passar do limite, do mesmo jeito que já relaxa o intervalo mínimo.

**Files:**
- Modify: `application/GerarEscalaService.java`, `application/RegraEscalaService.java`, `adapters/web/RegraEscalaController.java`, `domain/RegraEscala.java` (javadoc)
- Modify: `frontend/src/pages/RegrasEscala.tsx`, `frontend/src/pages/Auditoria.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/MaxServicosMesIntegrationTest.java`

**Interfaces:**
- Consumes: estrutura pré-carregada de `GerarEscalaService` (Plano 1, Tarefa 6).
- Produces: `RegraEscalaService.atualizar(Long id, RegraEscala dados)` passa a alterar **só** `intervaloMinimo` e `maxServicosMes`, e valida os dois.

- [ ] **Step 1: Escrever os testes que falham**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cenario montado pra o limite mensal ser o UNICO motivo da escolha:
 * 7 Tenentes com prioridade maxima na fila, mas que ja tiraram 1 Oficial
 * de Dia no mes; 1 Tenente com prioridade menor e nenhum servico no mes.
 * Com maxServicosMes=1, so o oitavo pode ser escalado.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MaxServicosMesIntegrationTest {

    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private RegraEscalaService regraEscalaService;
    @Autowired private RegraEscalaRepository regraEscalaRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private MilitarRepository militarRepository;
    @Autowired private EscalaRepository escalaRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private TipoServico oficialDeDia;
    private RegraEscala regra;
    private Militar oitavo;
    private LocalDate dia;
    private Usuario sargenteante;

    @BeforeEach
    void montar() {
        sargenteante = usuarioRepository.findByLogin("00000000001").orElseThrow();
        oficialDeDia = tipoServicoRepository.findAll().stream()
                .filter(t -> t.getNome().equals("Oficial de Dia")).findFirst().orElseThrow();
        regra = regraEscalaRepository.findByTipoServico_Id(oficialDeDia.getId()).orElseThrow();
        List<Militar> tenentes = militarRepository.findAll().stream()
                .filter(m -> "Ten".equals(m.getPosto().getSigla())).toList();
        assertThat(tenentes).hasSize(8);

        dia = LocalDate.now().plusMonths(2).withDayOfMonth(20);
        Escala anterior = escalaRepository.save(Escala.builder().descricao("anterior")
                .dataInicio(dia.withDayOfMonth(1)).dataFim(dia.withDayOfMonth(7)).usuarioGeracao(sargenteante).build());
        for (int i = 0; i < 7; i++) {
            Militar t = tenentes.get(i);
            t.setDataUltimoServico(LocalDate.of(2000, 1, 1)); // topo da fila
            militarRepository.save(t);
            servicoEscaladoRepository.save(ServicoEscalado.builder().escala(anterior)
                    .data(dia.withDayOfMonth(i + 1)).tipoServico(oficialDeDia).militar(t).build());
        }
        oitavo = tenentes.get(7);
        oitavo.setDataUltimoServico(dia.withDayOfMonth(5)); // fila mais baixa, mas sem servico no mes
        militarRepository.save(oitavo);
    }

    private Militar oficialDeDiaGerado() {
        Escala e = gerarEscalaService.gerar(dia, dia, sargenteante);
        return servicoEscaladoRepository.findByEscala_Id(e.getId()).stream()
                .filter(s -> s.getTipoServico().getId().equals(oficialDeDia.getId()))
                .findFirst().orElseThrow().getMilitar();
    }

    @Test
    void comLimite_escolheQuemAindaNaoAtingiu() {
        regra.setMaxServicosMes(1);
        regraEscalaRepository.save(regra);
        assertThat(oficialDeDiaGerado().getId()).isEqualTo(oitavo.getId());
    }

    @Test
    void semLimite_prioridadeDaFilaDecide_controleDoCenario() {
        regra.setMaxServicosMes(null);
        regraEscalaRepository.save(regra);
        assertThat(oficialDeDiaGerado().getId()).isNotEqualTo(oitavo.getId());
    }

    @Test
    void atualizarRegra_validaValores() {
        RegraEscala invalida = RegraEscala.builder().intervaloMinimo(-1).build();
        assertThatThrownBy(() -> regraEscalaService.atualizar(regra.getId(), invalida))
                .isInstanceOf(IllegalArgumentException.class);
        RegraEscala maxZero = RegraEscala.builder().intervaloMinimo(3).maxServicosMes(0).build();
        assertThatThrownBy(() -> regraEscalaService.atualizar(regra.getId(), maxZero))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

Run: `cd backend && mvn -q test -Dtest=MaxServicosMesIntegrationTest`
Expected: FAIL em `comLimite_escolheQuemAindaNaoAtingiu` e `atualizarRegra_validaValores`. O teste de controle passa, o que prova que o cenário realmente depende do limite.

- [ ] **Step 2: Motor respeita o limite mensal**

Em `GerarEscalaService`:

a) Acrescentar à classe:
```java
    /** Contagem de servicos de um tipo, por militar, num mes (RF07 - max_servicos_mes). */
    private record ChaveMes(Long militarId, Long tipoId, YearMonth mes) {}
```

b) No laço de pré-carga por tipo, buscar a regra uma vez só e guardar também o máximo:
```java
        Map<Long, Integer> maxPorTipo = new HashMap<>(); // valor null = sem limite
        for (TipoServico tipo : tipos) {
            List<RequisitoServico> requisitos = requisitoServicoRepository.findByTipoServico_Id(tipo.getId());
            elegiveisPorTipo.put(tipo.getId(), ativos.stream().filter(m -> elegibilidadeService.elegivel(m, requisitos)).toList()); // RF06
            RegraEscala regra = regraEscalaRepository.findByTipoServico_Id(tipo.getId()).orElse(null);
            intervaloPorTipo.put(tipo.getId(), PoliticaDeDescanso.intervaloMinimo(regra));
            maxPorTipo.put(tipo.getId(), regra != null ? regra.getMaxServicosMes() : null);
        }
```

c) Logo depois, contar o que já existe nos meses tocados pelo período. O que estava no período já foi apagado mais acima, e o Hibernate faz o flush da remoção antes desta consulta:
```java
        Map<ChaveMes, Integer> servicosNoMes = new HashMap<>();
        for (ServicoEscalado s : servicoEscaladoRepository.findByDataBetween(
                dataInicio.withDayOfMonth(1), dataFim.withDayOfMonth(dataFim.lengthOfMonth()))) {
            if (s.getMilitar() == null) continue;
            servicosNoMes.merge(new ChaveMes(s.getMilitar().getId(), s.getTipoServico().getId(), YearMonth.from(s.getData())), 1, Integer::sum);
        }
```

d) Na montagem do pool rigoroso, trocar
```java
                    if (PoliticaDeDescanso.respeitaIntervalo(em.getUltimoServico(), diaFinal, intervaloMinimo)) pool.add(em); // RN06
```
por
```java
                    boolean abaixoDoMaximo = max == null
                            || servicosNoMes.getOrDefault(new ChaveMes(m.getId(), tipo.getId(), YearMonth.from(diaFinal)), 0) < max;
                    // RN06 + RF07: o pool rigoroso respeita intervalo E limite mensal. O "aperto"
                    // abaixo (ultimo recurso contra vaga aberta) relaxa os dois.
                    if (abaixoDoMaximo && PoliticaDeDescanso.respeitaIntervalo(em.getUltimoServico(), diaFinal, intervaloMinimo)) pool.add(em);
```
com `Integer max = maxPorTipo.get(tipo.getId());` declarado ao lado de `int intervaloMinimo = ...`.

e) No laço `for (MilitarEmGeracao escolhido : escolhidos)`, depois de `escolhido.marcarServico(dia);`:
```java
                    servicosNoMes.merge(new ChaveMes(escolhido.militar.getId(), tipo.getId(), YearMonth.from(dia)), 1, Integer::sum);
```

(import `java.time.YearMonth`.)

- [ ] **Step 3: `RegraEscalaService.atualizar` valida e só mexe no que o motor usa**

```java
    @Transactional
    public RegraEscala atualizar(Long id, RegraEscala dados) {
        RegraEscala existente = regraEscalaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Regra nao encontrada"));
        if (dados.getIntervaloMinimo() < 0 || dados.getIntervaloMinimo() > 30) {
            throw new IllegalArgumentException("O intervalo mínimo precisa estar entre 0 e 30 dias");
        }
        if (dados.getMaxServicosMes() != null && dados.getMaxServicosMes() < 1) {
            throw new IllegalArgumentException("O máximo de serviços por mês precisa ser pelo menos 1 (ou vazio pra sem limite)");
        }
        // diasFolga e os pesos ficam como estao: reservados pro futuro avaliador de justica.
        existente.setIntervaloMinimo(dados.getIntervaloMinimo());
        existente.setMaxServicosMes(dados.getMaxServicosMes());
        return regraEscalaRepository.save(existente);
    }
```

`RegraEscalaController.atualizar`: acrescentar `Authentication auth` e registrar em auditoria:
```java
        RegraEscala salva = regraEscalaService.atualizar(id, regra);
        auditoriaService.registrar(auth.getName(), "REGRA_ESCALA_ALTERADA",
                salva.getTipoServico().getNome() + ": intervalo " + salva.getIntervaloMinimo()
                        + ", máx/mês " + (salva.getMaxServicosMes() == null ? "sem limite" : salva.getMaxServicosMes()));
        return salva;
```
(injetar `AuditoriaService` no construtor.)

`RegraEscala.java`, acima de `diasFolga`, `pesoFimSemana` e `pesoFeriado`, colocar o comentário: `/** Reservado pro futuro avaliador de justica - o motor ainda nao usa. */`.

Run: `cd backend && mvn -q test` → PASS. Os testes de geração continuam verdes: o seed usa `maxServicosMes=10`, que nunca chega a limitar num rodízio 3x1.

- [ ] **Step 4: Frontend — tirar "dias de folga" e explicar o máximo**

Em `pages/RegrasEscala.tsx`:
- remover a coluna `<th>Dias de folga</th>` e o `<td>` correspondente (o que tem o input de `diasFolga`);
- tirar `diasFolga` do estado `rascunho` (tipo e valores iniciais), de `iniciarEdicao` e de `salvar` (o `...r` do corpo continua mandando o valor antigo, que o backend ignora);
- trocar o cabeçalho `Máx. serviços/mês` por `Máx. no mês (por militar)`;
- no card âmbar do rodapé, acrescentar um segundo parágrafo:
```tsx
          <p style={{ fontSize: 12, color: "var(--amber-text)", marginTop: 6 }}>
            O máximo no mês conta só os serviços deste tipo, por militar. Se faltar gente
            (muitas férias/missões ao mesmo tempo), a escala aperta pra não deixar vaga aberta e
            pode passar do limite — igual acontece com o intervalo mínimo.
          </p>
```

`pages/Auditoria.tsx`: no mapa de rótulos de ações (o objeto onde está `ESCALA_PUBLICADA: "Escala publicada"`), acrescentar `REGRA_ESCALA_ALTERADA: "Regra da escala alterada",`.

Run: `cd frontend && npm test && npm run build` → PASS.

- [ ] **Step 5: Teste manual** — pôr "Máx. no mês" = 2 em Cabo de Dia e gerar um mês. Na Ficha de alguns Sd EP com CFC, conferir que cada um tem no máximo 2 Cabo de Dia no mês, exceto se aparecer vaga "apertada". Depois restaurar o valor 10.

- [ ] **Step 6: Checkpoint** — diff, sugerir `feat: maximo de servicos por mes aplicado na geracao; dias de folga fora da tela` e aguardar o usuário commitar.

---

### Task 7: Tipo de serviço novo — regra padrão e requisitos de elegibilidade (backend)

Um tipo de serviço criado pela tela nasce:
- **sem regra**, então o motor cai no intervalo padrão de 7 dias;
- **sem requisitos**, então ninguém é elegível e todo dia gera vaga em aberto.

Os requisitos só existem no `DataSeeder` e não há endpoint para mantê-los. Além disso:
- `TipoServico` serializa a lista de requisitos dentro de **cada** serviço da escala, o que pesa no JSON;
- tipos de serviço, qualificações e requisitos são alterados sem registro no log de auditoria.

**Files:**
- Create: `backend/src/main/java/br/com/milscale/milscale/application/NovoRequisito.java`, `application/RequisitoServicoService.java`, `adapters/web/RequisitoServicoController.java`
- Modify: `domain/TipoServico.java`, `application/TipoServicoService.java`, `adapters/web/TipoServicoController.java`, `adapters/web/QualificacaoController.java`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/TipoServicoRequisitoIntegrationTest.java`

**Interfaces:**
- Produces:
  - `record NovoRequisito(@NotNull Long postoId, Long subunidadeId, Long qualificacaoId, Set<Long> qualificacoesExcluidasIds, Long subunidadeExcluidaId)`
  - `RequisitoServicoService.listar(Long tipoId)`, `.adicionar(Long tipoId, NovoRequisito)`, `.remover(Long tipoId, Long requisitoId)`
  - `GET /api/tipos-servico/{tipoId}/requisitos` (qualquer autenticado), `POST` e `DELETE /{requisitoId}` (Sargenteante)
  - JSON de `TipoServico`: sai `requisitos`, entra `quantidadeRequisitos: number`

- [ ] **Step 1: Escrever os testes que falham**

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
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TipoServicoRequisitoIntegrationTest {

    @Autowired private TipoServicoService tipoServicoService;
    @Autowired private RequisitoServicoService requisitoServicoService;
    @Autowired private GerarEscalaService gerarEscalaService;
    @Autowired private RegraEscalaRepository regraEscalaRepository;
    @Autowired private PostoGraduacaoRepository postoRepository;
    @Autowired private SubunidadeRepository subunidadeRepository;
    @Autowired private TipoServicoRepository tipoServicoRepository;
    @Autowired private ServicoEscaladoRepository servicoEscaladoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private TipoServico criarTipo(String nome) {
        return tipoServicoService.cadastrar(TipoServico.builder().nome(nome).efetivoNecessario(1).build());
    }

    private Long id(String siglaPosto) {
        return postoRepository.findAll().stream().filter(p -> p.getSigla().equals(siglaPosto)).findFirst().orElseThrow().getId();
    }

    private Long subunidade(String sigla) {
        return subunidadeRepository.findAll().stream().filter(s -> s.getSigla().equals(sigla)).findFirst().orElseThrow().getId();
    }

    private List<ServicoEscalado> gerar3DiasDoTipo(TipoServico tipo) {
        LocalDate inicio = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        Escala e = gerarEscalaService.gerar(inicio, inicio.plusDays(2), usuarioRepository.findByLogin("00000000001").orElseThrow());
        return servicoEscaladoRepository.findByEscala_Id(e.getId()).stream()
                .filter(s -> s.getTipoServico().getId().equals(tipo.getId())).toList();
    }

    @Test
    void cadastrarTipo_criaRegraPadrao3x1() {
        TipoServico t = criarTipo("Sentinela Extra");
        RegraEscala regra = regraEscalaRepository.findByTipoServico_Id(t.getId()).orElseThrow();
        assertThat(regra.getIntervaloMinimo()).isEqualTo(3);
        assertThat(regra.getMaxServicosMes()).isNull();
    }

    @Test
    void tipoSemRequisito_geraVagaAberta_porIssoATelaAvisa() {
        TipoServico t = criarTipo("Sentinela Extra");
        assertThat(gerar3DiasDoTipo(t)).allMatch(s -> s.getMilitar() == null);
    }

    @Test
    void comRequisito_tipoNovoEPreenchidoPorQuemCumpre() {
        TipoServico t = criarTipo("Sentinela Extra");
        requisitoServicoService.adicionar(t.getId(),
                new NovoRequisito(id("Sd EV"), null, null, Set.of(), subunidade("Aprov")));

        List<ServicoEscalado> servicos = gerar3DiasDoTipo(t);
        assertThat(servicos).hasSize(3).allMatch(s -> s.getMilitar() != null);
        assertThat(servicos).allMatch(s -> s.getMilitar().getPosto().getSigla().equals("Sd EV")
                && !s.getMilitar().getSubunidade().getSigla().equals("Aprov"));
    }

    @Test
    void exigirEExcluirSubunidadeAoMesmoTempo_recusa() {
        TipoServico t = criarTipo("Sentinela Extra");
        assertThatThrownBy(() -> requisitoServicoService.adicionar(t.getId(),
                new NovoRequisito(id("Sd EV"), subunidade("CCAp"), null, Set.of(), subunidade("Aprov"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removerRequisitoDeOutroTipo_naoEncontra() {
        TipoServico t = criarTipo("Sentinela Extra");
        RequisitoServico r = requisitoServicoService.adicionar(t.getId(), new NovoRequisito(id("Sd EV"), null, null, Set.of(), null));
        TipoServico outro = tipoServicoRepository.findAll().stream().filter(x -> !x.getId().equals(t.getId())).findFirst().orElseThrow();
        assertThatThrownBy(() -> requisitoServicoService.remover(outro.getId(), r.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void removerRequisito_tiraDaLista() {
        TipoServico t = criarTipo("Sentinela Extra");
        RequisitoServico r = requisitoServicoService.adicionar(t.getId(), new NovoRequisito(id("Sd EV"), null, null, Set.of(), null));
        requisitoServicoService.remover(t.getId(), r.getId());
        assertThat(requisitoServicoService.listar(t.getId())).isEmpty();
    }
}
```

Run: `cd backend && mvn -q test -Dtest=TipoServicoRequisitoIntegrationTest` → FAIL de compilação.

- [ ] **Step 2: `NovoRequisito.java`**

```java
package br.com.milscale.milscale.application;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * RF06 - uma combinacao aceita pra tirar um tipo de servico: posto
 * obrigatorio; subunidade e curso exigidos, cursos e subunidade
 * excluidos sao opcionais (ver ElegibilidadeService).
 */
public record NovoRequisito(
        @NotNull(message = "Escolha o posto/graduação") Long postoId,
        Long subunidadeId,
        Long qualificacaoId,
        Set<Long> qualificacoesExcluidasIds,
        Long subunidadeExcluidaId) {}
```

- [ ] **Step 3: `RequisitoServicoService.java`**

```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/** RF06 - manutencao dos requisitos de elegibilidade de cada tipo de servico (antes so existiam no seed). */
@Service
public class RequisitoServicoService {

    private final TipoServicoRepository tipoServicoRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final PostoGraduacaoRepository postoRepository;
    private final SubunidadeRepository subunidadeRepository;
    private final QualificacaoRepository qualificacaoRepository;

    public RequisitoServicoService(TipoServicoRepository tipoServicoRepository, RequisitoServicoRepository requisitoServicoRepository,
                                   PostoGraduacaoRepository postoRepository, SubunidadeRepository subunidadeRepository,
                                   QualificacaoRepository qualificacaoRepository) {
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.postoRepository = postoRepository;
        this.subunidadeRepository = subunidadeRepository;
        this.qualificacaoRepository = qualificacaoRepository;
    }

    public List<RequisitoServico> listar(Long tipoId) {
        buscarTipo(tipoId);
        return requisitoServicoRepository.findByTipoServico_Id(tipoId);
    }

    @Transactional
    public RequisitoServico adicionar(Long tipoId, NovoRequisito novo) {
        TipoServico tipo = buscarTipo(tipoId);
        PostoGraduacao posto = postoRepository.findById(novo.postoId())
                .orElseThrow(() -> new NoSuchElementException("Posto/graduação não encontrado"));
        Subunidade exigida = subunidadeOuNulo(novo.subunidadeId());
        Subunidade excluida = subunidadeOuNulo(novo.subunidadeExcluidaId());
        if (exigida != null && excluida != null) {
            throw new IllegalArgumentException("Escolha exigir OU excluir uma subunidade, não os dois");
        }
        Qualificacao curso = novo.qualificacaoId() == null ? null : qualificacaoRepository.findById(novo.qualificacaoId())
                .orElseThrow(() -> new NoSuchElementException("Curso não encontrado"));
        Set<Long> idsExcluidos = novo.qualificacoesExcluidasIds() == null ? Set.of() : novo.qualificacoesExcluidasIds();
        Set<Qualificacao> cursosExcluidos = new HashSet<>(qualificacaoRepository.findAllById(idsExcluidos));
        if (curso != null && cursosExcluidos.contains(curso)) {
            throw new IllegalArgumentException("O curso exigido não pode estar também entre os excluídos");
        }
        return requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipo).posto(posto).subunidade(exigida).qualificacao(curso)
                .qualificacoesExcluidas(cursosExcluidos).subunidadeExcluida(excluida)
                .build());
    }

    @Transactional
    public void remover(Long tipoId, Long requisitoId) {
        RequisitoServico r = requisitoServicoRepository.findById(requisitoId)
                .filter(x -> x.getTipoServico().getId().equals(tipoId))
                .orElseThrow(() -> new NoSuchElementException("Requisito não encontrado neste tipo de serviço"));
        // Remove pela colecao do pai (orphanRemoval) pra o Hibernate nao "ressuscitar" o filho via cascade.
        r.getTipoServico().getRequisitos().remove(r);
        requisitoServicoRepository.delete(r);
    }

    private TipoServico buscarTipo(Long tipoId) {
        return tipoServicoRepository.findById(tipoId)
                .orElseThrow(() -> new NoSuchElementException("Tipo de servico nao encontrado"));
    }

    private Subunidade subunidadeOuNulo(Long id) {
        return id == null ? null : subunidadeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subunidade não encontrada"));
    }
}
```

- [ ] **Step 4: `TipoServico` — requisitos fora do JSON e contagem no lugar**

Em `TipoServico.java` (imports `com.fasterxml.jackson.annotation.JsonIgnore` e `JsonProperty`), anotar o campo `requisitos` com `@JsonIgnore` e acrescentar:
```java
    /** So a quantidade vai no JSON (a lista completa sai por /api/tipos-servico/{id}/requisitos) -
     *  sem isso cada servico da escala carregava a lista inteira de requisitos do seu tipo. */
    @JsonProperty("quantidadeRequisitos")
    public int getQuantidadeRequisitos() {
        return requisitos == null ? 0 : requisitos.size();
    }
```

- [ ] **Step 5: `TipoServicoService.cadastrar` valida e cria a regra padrão**

Injetar `RegraEscalaRepository regraEscalaRepository` no construtor.
```java
    /** Novo tipo nasce com o mesmo 3x1 dos servicos do BI e sem limite mensal. */
    private static final int INTERVALO_MINIMO_NOVO_TIPO = 3;

    @Transactional
    public TipoServico cadastrar(TipoServico tipo) {
        if (tipo.getNome() == null || tipo.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do serviço");
        }
        if (tipo.getEfetivoNecessario() < 1) {
            throw new IllegalArgumentException("O efetivo necessário precisa ser pelo menos 1");
        }
        tipo.setId(null);
        tipo.setAtivo(true);
        if (tipo.getHoraInicio() == null) tipo.setHoraInicio(java.time.LocalTime.of(8, 0));
        if (tipo.getDuracaoHoras() <= 0) tipo.setDuracaoHoras(24);
        if (tipo.getRequisitos() == null) tipo.setRequisitos(new java.util.ArrayList<>());
        TipoServico salvo = tipoServicoRepository.save(tipo);
        regraEscalaRepository.save(RegraEscala.builder()
                .tipoServico(salvo).intervaloMinimo(INTERVALO_MINIMO_NOVO_TIPO).build());
        return salvo;
    }
```
(`RegraEscala.builder()` já traz `diasFolga=1` e os pesos padrão; `maxServicosMes` fica nulo.)

- [ ] **Step 6: `RequisitoServicoController.java`**

```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.AuditoriaService;
import br.com.milscale.milscale.application.NovoRequisito;
import br.com.milscale.milscale.application.RequisitoServicoService;
import br.com.milscale.milscale.domain.RequisitoServico;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF06 - quem pode tirar cada tipo de servico. Consulta aberta; manutencao privativa do Sargenteante (RN11). */
@RestController
@RequestMapping("/api/tipos-servico/{tipoId}/requisitos")
public class RequisitoServicoController {

    private final RequisitoServicoService requisitoServicoService;
    private final AuditoriaService auditoriaService;

    public RequisitoServicoController(RequisitoServicoService requisitoServicoService, AuditoriaService auditoriaService) {
        this.requisitoServicoService = requisitoServicoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<RequisitoServico> listar(@PathVariable Long tipoId) {
        return requisitoServicoService.listar(tipoId);
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public RequisitoServico adicionar(@PathVariable Long tipoId, @Valid @RequestBody NovoRequisito novo, Authentication auth) {
        RequisitoServico r = requisitoServicoService.adicionar(tipoId, novo);
        auditoriaService.registrar(auth.getName(), "REQUISITO_ADICIONADO",
                r.getTipoServico().getNome() + ": " + r.getPosto().getSigla() + " (requisito id " + r.getId() + ")");
        return r;
    }

    @PreAuthorize("hasRole('SARGENTEANTE')")
    @DeleteMapping("/{requisitoId}")
    public void remover(@PathVariable Long tipoId, @PathVariable Long requisitoId, Authentication auth) {
        requisitoServicoService.remover(tipoId, requisitoId);
        auditoriaService.registrar(auth.getName(), "REQUISITO_REMOVIDO", "tipo " + tipoId + ", requisito id " + requisitoId);
    }
}
```

- [ ] **Step 7: Auditoria nos cadastros de configuração**

Injetar `AuditoriaService` e acrescentar `Authentication auth` nos métodos que mudam estado:
- `TipoServicoController`: `cadastrar` → `"TIPO_SERVICO_CADASTRADO"` com `salvo.getNome()`; `atualizar` → `"TIPO_SERVICO_EDITADO"` com `salvo.getNome() + " (efetivo " + salvo.getEfetivoNecessario() + ")"`; `desativar` → `"TIPO_SERVICO_DESATIVADO"` com `salvo.getNome()`.
- `QualificacaoController`: `cadastrar` → `"QUALIFICACAO_CADASTRADA"`; `atualizar` → `"QUALIFICACAO_EDITADA"` (descrição = nome); `vincular` → `"CURSO_VINCULADO"` com `"militar " + militarId + ", curso " + qualificacaoId`; `desvincular` → `"CURSO_DESVINCULADO"` (mesmo formato).

Padrão (exemplo `TipoServicoController.cadastrar`):
```java
    @PreAuthorize("hasRole('SARGENTEANTE')")
    @PostMapping
    public TipoServico cadastrar(@RequestBody TipoServico tipo, Authentication auth) {
        TipoServico salvo = tipoServicoService.cadastrar(tipo);
        auditoriaService.registrar(auth.getName(), "TIPO_SERVICO_CADASTRADO", salvo.getNome());
        return salvo;
    }
```

- [ ] **Step 8: Rodar todos os testes**

Run: `cd backend && mvn -q test`
Expected: PASS.

- [ ] **Step 9: Checkpoint** — diff, sugerir `feat: requisitos de elegibilidade editaveis; tipo novo nasce com regra padrao; auditoria de configuracoes` e aguardar o usuário commitar.

---

### Task 8: Tela de elegibilidade dos tipos de serviço (frontend)

**Files:**
- Create: `frontend/src/utils/requisitos.ts`, `frontend/src/utils/requisitos.test.ts`, `frontend/src/components/RequisitosPainel.tsx`
- Modify: `frontend/src/api/types.ts`, `frontend/src/pages/TiposServico.tsx`, `frontend/src/pages/Auditoria.tsx`

**Interfaces:**
- Consumes: endpoints da Tarefa 7; `TipoServico.quantidadeRequisitos`.
- Produces: `descreverRequisito(r: RequisitoServico): string`; componente `<RequisitosPainel tipo={TipoServico} podeEditar={boolean} onMudou={() => void} />`.

- [ ] **Step 1: Tipos** — em `src/api/types.ts`:
  - na interface `TipoServico`, acrescentar `quantidadeRequisitos: number;`;
  - acrescentar a interface nova:
```ts
export interface RequisitoServico {
  id: number;
  posto: PostoGraduacao;
  subunidade?: Subunidade | null;
  qualificacao?: Qualificacao | null;
  qualificacoesExcluidas: Qualificacao[];
  subunidadeExcluida?: Subunidade | null;
}
```

- [ ] **Step 2: Teste do texto legível (falha)**

`src/utils/requisitos.test.ts`:
```ts
import { describe, expect, it } from "vitest";
import type { RequisitoServico } from "../api/types";
import { descreverRequisito } from "./requisitos";

const posto = (sigla: string) => ({ id: 1, sigla, descricao: sigla, nivelHierarquico: 1 });
const sub = (sigla: string) => ({ id: 1, sigla, nome: sigla, ativo: true });
const curso = (nome: string) => ({ id: nome.length, nome });

describe("descreverRequisito", () => {
  it("só posto", () => {
    const r: RequisitoServico = { id: 1, posto: posto("Ten"), qualificacoesExcluidas: [] };
    expect(descreverRequisito(r)).toBe("Ten");
  });

  it("posto com curso exigido", () => {
    const r: RequisitoServico = { id: 1, posto: posto("Sd EP"), qualificacao: curso("CFC"), qualificacoesExcluidas: [] };
    expect(descreverRequisito(r)).toBe("Sd EP com CFC");
  });

  it("posto lotado numa subunidade", () => {
    const r: RequisitoServico = { id: 1, posto: posto("Sd EV"), subunidade: sub("Aprov"), qualificacoesExcluidas: [] };
    expect(descreverRequisito(r)).toBe("Sd EV lotado no Aprov");
  });

  it("exceções de curso (ordem alfabética) e de subunidade", () => {
    const r: RequisitoServico = {
      id: 1, posto: posto("Sd EP"),
      qualificacoesExcluidas: [curso("Motorista"), curso("CFC")],
      subunidadeExcluida: sub("Aprov"),
    };
    expect(descreverRequisito(r)).toBe("Sd EP, exceto quem tem CFC ou Motorista e lotados no Aprov");
  });
});
```

Run: `cd frontend && npm test` → FAIL (módulo não existe).

- [ ] **Step 3: `src/utils/requisitos.ts`**

```ts
import type { RequisitoServico } from "../api/types";

/** Frase curta pra quem não conhece o modelo de dados, ex.: "Sd EP, exceto quem tem CFC ou Motorista e lotados no Aprov". */
export function descreverRequisito(r: RequisitoServico): string {
  let texto = r.posto.sigla;
  if (r.subunidade) texto += ` lotado no ${r.subunidade.sigla}`;
  if (r.qualificacao) texto += ` com ${r.qualificacao.nome}`;

  const excecoes: string[] = [];
  if (r.qualificacoesExcluidas.length > 0) {
    const nomes = r.qualificacoesExcluidas.map((q) => q.nome).sort((a, b) => a.localeCompare(b, "pt-BR"));
    excecoes.push(`quem tem ${juntarComOu(nomes)}`);
  }
  if (r.subunidadeExcluida) excecoes.push(`lotados no ${r.subunidadeExcluida.sigla}`);
  if (excecoes.length > 0) texto += `, exceto ${excecoes.join(" e ")}`;
  return texto;
}

function juntarComOu(itens: string[]): string {
  if (itens.length <= 1) return itens.join("");
  return `${itens.slice(0, -1).join(", ")} ou ${itens[itens.length - 1]}`;
}
```

Run: `npm test` → PASS.

- [ ] **Step 4: `src/components/RequisitosPainel.tsx`**

```tsx
import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { PostoGraduacao, Qualificacao, RequisitoServico, Subunidade, TipoServico } from "../api/types";
import { descreverRequisito } from "../utils/requisitos";

/**
 * Quem pode tirar um tipo de serviço (RF06). Cada linha é uma combinação
 * aceita — basta a pessoa cumprir UMA delas pra ser elegível.
 */
export function RequisitosPainel({ tipo, podeEditar, onMudou }: { tipo: TipoServico; podeEditar: boolean; onMudou: () => void }) {
  const [requisitos, setRequisitos] = useState<RequisitoServico[]>([]);
  const [postos, setPostos] = useState<PostoGraduacao[]>([]);
  const [subunidades, setSubunidades] = useState<Subunidade[]>([]);
  const [cursos, setCursos] = useState<Qualificacao[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  const [postoId, setPostoId] = useState<number | "">("");
  const [subunidadeId, setSubunidadeId] = useState<number | "">("");
  const [cursoId, setCursoId] = useState<number | "">("");
  const [subunidadeExcluidaId, setSubunidadeExcluidaId] = useState<number | "">("");
  const [cursosExcluidos, setCursosExcluidos] = useState<number[]>([]);

  async function carregar() {
    setRequisitos(await api.get<RequisitoServico[]>(`/api/tipos-servico/${tipo.id}/requisitos`));
  }

  useEffect(() => {
    carregar();
    Promise.all([
      api.get<PostoGraduacao[]>("/api/postos-graduacao"),
      api.get<Subunidade[]>("/api/subunidades"),
      api.get<Qualificacao[]>("/api/qualificacoes"),
    ]).then(([p, s, q]) => {
      setPostos(p.slice().sort((a, b) => b.nivelHierarquico - a.nivelHierarquico));
      setSubunidades(s);
      setCursos(q);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tipo.id]);

  async function adicionar() {
    if (postoId === "") {
      setErro("Escolha o posto/graduação.");
      return;
    }
    setErro(null);
    try {
      await api.post(`/api/tipos-servico/${tipo.id}/requisitos`, {
        postoId,
        subunidadeId: subunidadeId === "" ? null : subunidadeId,
        qualificacaoId: cursoId === "" ? null : cursoId,
        qualificacoesExcluidasIds: cursosExcluidos,
        subunidadeExcluidaId: subunidadeExcluidaId === "" ? null : subunidadeExcluidaId,
      });
      setPostoId(""); setSubunidadeId(""); setCursoId(""); setSubunidadeExcluidaId(""); setCursosExcluidos([]);
      await carregar();
      onMudou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível adicionar.");
    }
  }

  async function remover(r: RequisitoServico) {
    if (!confirm(`Remover "${descreverRequisito(r)}" de ${tipo.nome}?`)) return;
    try {
      await api.delete(`/api/tipos-servico/${tipo.id}/requisitos/${r.id}`);
      await carregar();
      onMudou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível remover.");
    }
  }

  function alternarCursoExcluido(id: number) {
    setCursosExcluidos((atual) => (atual.includes(id) ? atual.filter((x) => x !== id) : [...atual, id]));
  }

  return (
    <div style={{ padding: "12px 16px", background: "var(--table-head-bg)" }}>
      <p className="sub" style={{ marginBottom: 8 }}>
        Quem pode tirar <strong>{tipo.nome}</strong> — basta cumprir uma das linhas:
      </p>
      {erro && <div className="error-box">{erro}</div>}
      {requisitos.length === 0 ? (
        <p style={{ fontSize: 12.5, color: "var(--red-text, #a33)" }}>
          Ninguém pode tirar este serviço ainda — toda geração vai deixar vaga em aberto.
        </p>
      ) : (
        <ul style={{ margin: "0 0 10px 18px" }}>
          {requisitos.map((r) => (
            <li key={r.id} style={{ fontSize: 13, marginBottom: 4 }}>
              {descreverRequisito(r)}
              {podeEditar && (
                <button className="btn btn-outline" style={{ marginLeft: 8, padding: "1px 8px" }} onClick={() => remover(r)}>
                  Remover
                </button>
              )}
            </li>
          ))}
        </ul>
      )}

      {podeEditar && (
        <div className="form-grid" style={{ marginTop: 8 }}>
          <div className="field">
            <label>Posto/graduação *</label>
            <select value={postoId} onChange={(e) => setPostoId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Escolha…</option>
              {postos.map((p) => <option key={p.id} value={p.id}>{p.descricao}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Só quem está lotado em</label>
            <select value={subunidadeId} onChange={(e) => setSubunidadeId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Qualquer subunidade</option>
              {subunidades.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Exige o curso</label>
            <select value={cursoId} onChange={(e) => setCursoId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Nenhum</option>
              {cursos.map((q) => <option key={q.id} value={q.id}>{q.nome}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Exceto lotados em</label>
            <select value={subunidadeExcluidaId} onChange={(e) => setSubunidadeExcluidaId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Ninguém excluído</option>
              {subunidades.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Exceto quem tem</label>
            <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
              {cursos.map((q) => (
                <label key={q.id} style={{ fontWeight: 400, display: "flex", gap: 4, alignItems: "center" }}>
                  <input type="checkbox" checked={cursosExcluidos.includes(q.id)} onChange={() => alternarCursoExcluido(q.id)} />
                  {q.nome}
                </label>
              ))}
            </div>
          </div>
          <div className="field" style={{ alignSelf: "end" }}>
            <button className="btn btn-primary" onClick={adicionar}>Adicionar combinação</button>
          </div>
        </div>
      )}
    </div>
  );
}
```

- [ ] **Step 5: Integrar na página `TiposServico.tsx`**

- imports: `import { Fragment } from "react";` (ou acrescentar `Fragment` ao import de `react`) e `import { RequisitosPainel } from "../components/RequisitosPainel";`
- estado: `const [abertoId, setAbertoId] = useState<number | null>(null);`
- cabeçalho: acrescentar `<th>Quem pode tirar</th>` depois de `<th>Situação</th>`;
- no `tipos.map`, trocar `<tr key={t.id}>` por `<Fragment key={t.id}><tr>` e, depois do `</tr>`, acrescentar o painel expandido. A nova célula entra depois da célula de Situação:
```tsx
                      <td>
                        <button className="btn btn-outline" onClick={() => setAbertoId((a) => (a === t.id ? null : t.id))}>
                          {t.quantidadeRequisitos === 0 ? "⚠ Ninguém — definir" : `${t.quantidadeRequisitos} combinação(ões)`}
                        </button>
                      </td>
```
```tsx
                    {abertoId === t.id && (
                      <tr>
                        <td colSpan={podeEditar ? 6 : 5} style={{ padding: 0 }}>
                          <RequisitosPainel tipo={t} podeEditar={podeEditar} onMudou={carregar} />
                        </td>
                      </tr>
                    )}
                  </Fragment>
```
- no `NovoTipoForm`, depois de criar, abrir o painel do tipo novo: trocar a assinatura para `function NovoTipoForm({ onCriado }: { onCriado: (novo: TipoServico) => void })`, usar `const novo = await api.post<TipoServico>("/api/tipos-servico", {...}); onCriado(novo);`, e no uso: `onCriado={(novo) => { setMostrarForm(false); carregar(); setAbertoId(novo.id); }}`.

`pages/Auditoria.tsx`: no mapa de rótulos, acrescentar:
```ts
  REQUISITO_ADICIONADO: "Requisito de serviço adicionado",
  REQUISITO_REMOVIDO: "Requisito de serviço removido",
  TIPO_SERVICO_CADASTRADO: "Tipo de serviço cadastrado",
  TIPO_SERVICO_EDITADO: "Tipo de serviço editado",
  TIPO_SERVICO_DESATIVADO: "Tipo de serviço desativado",
  QUALIFICACAO_CADASTRADA: "Curso cadastrado",
  QUALIFICACAO_EDITADA: "Curso editado",
  CURSO_VINCULADO: "Curso vinculado a militar",
  CURSO_DESVINCULADO: "Curso desvinculado de militar",
```

Run: `cd frontend && npm test && npm run lint && npm run build` → PASS.

- [ ] **Step 6: Teste manual**
  1. Como Sargenteante, criar o tipo "Sentinela Extra" com efetivo 1: o painel abre com o aviso "Ninguém pode tirar".
  2. Adicionar a combinação "Soldado EV, exceto lotados em Aprovisionamento" e ver a linha "Sd EV, exceto lotados no Aprov".
  3. Gerar 3 dias: o serviço novo aparece preenchido por Sd EV fora do Aprov.
  4. Remover a combinação. Como Cabo, a página mostra as combinações sem os botões de editar.
  5. No Log de auditoria aparecem as ações novas.

- [ ] **Step 7: Checkpoint** — diff, sugerir `feat(front): tela de elegibilidade dos tipos de servico` e aguardar o usuário commitar.

---

## Fechamento do Plano 2

- [ ] Rodar tudo: `cd backend && mvn test` e `cd ../frontend && npm test && npm run lint && npm run build`.
- [ ] Rodar `/code-review` e `/security-review` sobre o branch `melhoria/implementacoes`; tratar os achados confirmados (cada correção vira um checkpoint próprio).
- [ ] Atualizar `README.md` (seção "Perfis e regras de negócio principais") com: rascunho visível só para a sargenteação; senha temporária e troca obrigatória; conta criada no cadastro; máximo de serviços por mês; tela de elegibilidade.
- [ ] Apresentar ao usuário: resumo por tarefa, testes antes/depois e passos manuais não executados. O push e o PR ficam com o usuário.

## Fora de escopo (registrado para planos futuros)

- Pesos de fim de semana/feriado e o "avaliador de justiça" (decisão: ficam para depois).
- Migração para Java 25 / Spring Boot 4.1, que o documento de arquitetura cita.
- Limite de tentativas de login (proteção contra força bruta) e cookie `Secure`/HTTPS em produção.
- Marcar serviços passados como `CUMPRIDO` e usar `ENCERRADA` nas escalas antigas.
- Armazenar foto e imagens do Boletim fora do banco (arquivo/S3).
