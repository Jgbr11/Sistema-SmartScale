# Plano 5 — Requisitos da Disciplina: Padrões, CRUDs, Variabilidade e Empacotamento (SmartScale)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Atender o "Projeto da equipe" do PDF **Componentes de Reuso — Parte 02** (Desenvolvimento Orientado a Reuso de Software, PUCPR, prof. Tiago Navarro):
1. **Padrões de projeto focados em reuso, codificados à mão.** Opção 01: Singleton ×2, Template Method ×3 e mais um padrão ×3, com pelo menos 10 classes principais.
2. **Telas e banco:**
   - no mínimo 2 telas CRUD por integrante, ligadas ao banco;
   - pelo menos 3 tabelas;
   - um exemplo de como a variabilidade foi planejada.
3. **Repositório e empacotamento:** um repositório, com uma técnica de empacotamento de código focada em reuso e a gravação do passo a passo.

**Architecture:**
- **O núcleo vira componente:** o núcleo reutilizável da linha de produto (`core/domain`) sai do monólito e vira o **módulo Maven `smartscale-core`**. É um JAR versionado (SemVer 1.0.0), com README, LICENSE e CHANGELOG, instalado no repositório Maven e consumido pelo MilScale como dependência.
- **Os padrões resolvem problemas reais do sistema:**
  - **Strategy:** critérios de ordenação da fila, que são o ponto de variação RN01;
  - **Singleton:** catálogo de critérios e identidade da organização;
  - **Template Method:** relatórios CSV com o mesmo esqueleto.
- **A variabilidade é demonstrada:** o critério da fila é escolhido por configuração, como numa engenharia de aplicação da linha de produto.

**Tech Stack:** Java 21, Maven multimódulo, Spring Boot 3.3, JUnit 5; React 19.

## Global Constraints

- **Quem commita é o usuário, tarefa a tarefa.** Checkpoint ao fim de cada tarefa. Criar tag (`v1.0.0`) e publicar o JAR são ações do usuário.
- **Branch:** `entrega/componentes-reuso`, a partir da `main`. A criação depende do ok do usuário.
- **Regra da disciplina sobre IA:** o código passa por **prova de autoria**. Por isso a Task 6 produz `docs/PADROES_DE_PROJETO.md`, que explica cada padrão, onde está e por que foi escolhido, para a equipe estudar antes da apresentação. A equipe deve declarar o uso de IA no padrão PUCPR (Resolução 274/2024 CONSUN).
- **Comentários:** regra do Plano 3, só delimitadores de seção e as marcações LPS de uma linha. A explicação dos padrões fica no documento da Task 6, não no código.
- **Verificação:** `mvn -q install` na raiz verde, e `cd frontend && npm test && npm run build` verde.
- **JDK:** o `JAVA_HOME` da máquina é o JDK 11. Rodar o Maven com `export JAVA_HOME="/c/Program Files/Java/jdk-23"`.
- **Ordem entre planos:**
  - **Independente dos Planos 2–4:** este plano pode ser feito antes deles se o prazo da disciplina apertar. As tarefas não dependem deles, e onde um plano anterior muda uma assinatura, a tarefa avisa.
  - **Plano 3 depois deste:** o Plano 3, Task 5 (ArchUnit), passa a usar o pacote `br.com.smartscale.core` no lugar de `br.com.milscale.core`.

## Registro de execução

| Ordem | Task | Status | Commit | Resumo |
|---|---|---|---|---|
| 1 | 0 — Preparação e confirmações com a equipe | ⬜ Pendente | — | — |
| 2 | 1 — Módulo `smartscale-core` (empacotamento) | ⬜ Pendente | — | — |
| 3 | 2 — Strategy ×3: critérios de ordenação da fila | ⬜ Pendente | — | — |
| 4 | 3 — Singleton ×2 e variabilidade por configuração | ⬜ Pendente | — | — |
| 5 | 4 — Template Method ×3: relatórios CSV | ⬜ Pendente | — | — |
| 6 | 5 — CRUDs completos | ⬜ Pendente | — | — |
| 7 | 6 — Documentação de padrões e variabilidade | ⬜ Pendente | — | — |
| 8 | 7 — Release 1.0.0 e roteiro da gravação | ⬜ Pendente | — | — |

## Como o plano cobre o PDF

| Exigência do PDF | Onde |
|---|---|
| ≥ 10 classes principais com métodos e atributos | Já atende: 24 classes de domínio e mais os services. Listadas em `docs/PADROES_DE_PROJETO.md` (Task 6) |
| Singleton, 2 exemplos | Task 3: `CatalogoDeCriterios` (clássico, *holder* preguiçoso) e `IdentidadeDaOrganizacao` (enum) |
| Template Method, 3 exemplos | Task 4: `RelatorioCsv` com `RelatorioEscalaDoDia`, `RelatorioServicosDoMilitar` e `RelatorioAfastamentosDoMes` |
| Mais um padrão, 3 exemplos | Task 2: Strategy com `CriterioOrdenacaoMilitar` (maior folga), `CriterioMenorCargaNaGeracao` e `CriterioMaisModernoPrimeiro` |
| 2 telas CRUD por integrante | Task 5: 6 telas com CRUD completo (Militares, Tipos de serviço, Boletim, Qualificações, Feriados e Missões e dispensas), o que cobre até 3 integrantes |
| ≥ 3 tabelas | Já atende: 19 tabelas versionadas pelo Flyway |
| Exemplo de variabilidade | Task 3: `milscale.lps.criterio-ordenacao`. Task 6: modelo de features em `docs/VARIABILIDADE.md` |
| Repositório e empacotamento focado em reuso | Task 1: `smartscale-core` 1.0.0, um JAR com README, LICENSE e CHANGELOG, instalado via `mvn install` e consumido como dependência |
| Gravação do passo a passo | Task 7: `docs/ROTEIRO_GRAVACAO.md`. A gravação em si é feita pela equipe |

---

### Task 0: Preparação e confirmações com a equipe

- [ ] **Step 1: Confirmar com o usuário** (e ajustar este plano antes de seguir):
  - **Opção 01 ou 02 do PDF.** Este plano assume a Opção 01. Com a Opção 02, a Task 3 deixa de exigir 2 Singletons e a Task 2 fica com 2 exemplos em vez de 3; a Task 4 continua.
  - **Número de integrantes.** 6 telas CRUD cobrem até 3. Com 4 integrantes, acrescentar 2 telas na Task 5; as candidatas são Regras da escala (criar e excluir) e Perfis e permissões (criar conta avulsa).
  - **Prazo de entrega**, para decidir se este plano vem antes dos Planos 2–4.
- [ ] **Step 2:** com o ok do usuário, `git checkout -b entrega/componentes-reuso` a partir da `main`.
- [ ] **Step 3:** linha de base: `cd backend && mvn -q test` e `cd frontend && npm test && npm run build`.

---

### Task 1: Módulo `smartscale-core` — o núcleo como componente reutilizável

**Files:**
- Create:
  - `pom.xml` (raiz, agregador)
  - `smartscale-core/pom.xml`
  - `smartscale-core/src/main/java/br/com/smartscale/core/*.java` (movidos)
  - `smartscale-core/src/test/java/br/com/smartscale/core/MotorDeRodizioTest.java`
  - `.dockerignore` (raiz)
- Move: `backend/src/main/java/br/com/milscale/core/domain/*.java` → `smartscale-core/src/main/java/br/com/smartscale/core/`
- Modify:
  - `backend/pom.xml`
  - todos os imports de `br.com.milscale.core.domain` no backend
  - `backend/src/main/java/br/com/milscale/milscale/MilScaleApplication.java` (`scanBasePackages`)
  - `backend/Dockerfile`, `docker-compose.yml`
  - `README.md`

**Interfaces:**
- Produces: artefato `br.com.smartscale:smartscale-core:1.0.0` com o pacote `br.com.smartscale.core` (`PessoaEscalada`, `TipoTurno`, `CriterioDeOrdenacao`, `MotorDeRodizio`, `SituacaoPessoa`).

- [ ] **Step 1: POM do núcleo** — `smartscale-core/pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>br.com.smartscale</groupId>
  <artifactId>smartscale-core</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>
  <name>SmartScale Core</name>
  <description>Núcleo reutilizável da linha de produto SmartScale: motor de rodízio e contratos de pessoa, turno e critério de ordenação.</description>

  <licenses>
    <license>
      <name>MIT</name>
      <url>https://opensource.org/licenses/MIT</url>
    </license>
  </licenses>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.3</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.assertj</groupId>
      <artifactId>assertj-core</artifactId>
      <version>3.25.3</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <version>3.3.1</version>
        <executions>
          <execution>
            <id>anexar-fontes</id>
            <goals><goal>jar-no-fork</goal></goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.4.1</version>
        <configuration>
          <archive>
            <manifestEntries>
              <Implementation-Title>SmartScale Core</Implementation-Title>
              <Implementation-Version>${project.version}</Implementation-Version>
              <Automatic-Module-Name>br.com.smartscale.core</Automatic-Module-Name>
            </manifestEntries>
          </archive>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```
O núcleo **não depende de nada** além do Java, nem de Spring nem de JPA, e é isso que o torna reutilizável por qualquer produto da linha.

- [ ] **Step 2: Agregador** — `pom.xml` na raiz
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>br.com.smartscale</groupId>
  <artifactId>smartscale</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <name>SmartScale (linha de produto)</name>

  <modules>
    <module>smartscale-core</module>
    <module>backend</module>
  </modules>
</project>
```

- [ ] **Step 3: Mover o núcleo e renomear o pacote**
```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
mkdir -p smartscale-core/src/main/java/br/com/smartscale/core smartscale-core/src/test/java/br/com/smartscale/core
git mv backend/src/main/java/br/com/milscale/core/domain/*.java smartscale-core/src/main/java/br/com/smartscale/core/
sed -i 's/^package br\.com\.milscale\.core\.domain;/package br.com.smartscale.core;/' smartscale-core/src/main/java/br/com/smartscale/core/*.java
grep -rl 'br\.com\.milscale\.core\.domain' backend/src | xargs sed -i 's/br\.com\.milscale\.core\.domain/br.com.smartscale.core/g'
grep -rn 'milscale\.core' backend/src smartscale-core/src
```
Expected: o último `grep` sai vazio. Em `MilScaleApplication`: `scanBasePackages = "br.com.milscale"` continua (o núcleo não tem beans do Spring).

- [ ] **Step 4: O backend consome o JAR** — em `backend/pom.xml`, em `<dependencies>`:
```xml
    <dependency>
      <groupId>br.com.smartscale</groupId>
      <artifactId>smartscale-core</artifactId>
      <version>1.0.0</version>
    </dependency>
```

- [ ] **Step 5: O núcleo ganha testes próprios** — `smartscale-core/src/test/java/br/com/smartscale/core/MotorDeRodizioTest.java`
```java
package br.com.smartscale.core;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MotorDeRodizioTest {

    record Pessoa(Long id, long contador) implements PessoaEscalada {
        public String getNomeExibicao() { return "p" + id; }
        public Long getId() { return id; }
        public SituacaoPessoa getSituacao() { return SituacaoPessoa.ATIVO; }
        public long getContadorRodizio() { return contador; }
    }

    record Turno(int efetivo) implements TipoTurno {
        public Long getId() { return 1L; }
        public String getNome() { return "Plantão"; }
        public int getEfetivoNecessario() { return efetivo; }
        public boolean isAtivo() { return true; }
    }

    private final CriterioDeOrdenacao<Pessoa> maiorContadorPrimeiro =
            () -> Comparator.comparingLong(Pessoa::contador).reversed();

    @Test
    void preencheAsVagasComOsMaisBemPosicionadosNaFila() {
        List<Pessoa> pool = List.of(new Pessoa(1L, 3), new Pessoa(2L, 9), new Pessoa(3L, 5));
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>().preencherVagas(pool, new Turno(2), maiorContadorPrimeiro);
        assertThat(escalados).extracting(Pessoa::id).containsExactly(2L, 3L);
    }

    @Test
    void comMenosGenteQueVagas_devolveQuemTem() {
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>()
                .preencherVagas(List.of(new Pessoa(1L, 1)), new Turno(3), maiorContadorPrimeiro);
        assertThat(escalados).hasSize(1);
    }

    @Test
    void outroProdutoDaLinha_usaOutroCriterioSemMudarOMotor() {
        CriterioDeOrdenacao<Pessoa> menorContadorPrimeiro = () -> Comparator.comparingLong(Pessoa::contador);
        List<Pessoa> pool = List.of(new Pessoa(1L, 3), new Pessoa(2L, 9), new Pessoa(3L, 5));
        List<Pessoa> escalados = new MotorDeRodizio<Pessoa, Turno>().preencherVagas(pool, new Turno(1), menorContadorPrimeiro);
        assertThat(escalados).extracting(Pessoa::id).containsExactly(1L);
    }
}
```
O último teste é a prova de reuso: um "produto hospitalar" com critério de menor carga usa o mesmo motor, sem alterar uma linha dele.

- [ ] **Step 6: Docker com o módulo**
  - **Contexto de build:** o build do backend passa a precisar do núcleo. Por isso o contexto vira a raiz do repositório.
  - **`docker-compose.yml`, serviço `backend`:**
```yaml
    build:
      context: .
      dockerfile: backend/Dockerfile
```
  - **`backend/Dockerfile`, estágio de build:**
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY smartscale-core/pom.xml smartscale-core/
COPY backend/pom.xml backend/
RUN mvn -q -B -pl backend -am dependency:go-offline
COPY smartscale-core/src smartscale-core/src
COPY backend/src backend/src
RUN mvn -q -B -pl backend -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/target/milscale-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=mysql"]
```
  Se o Plano 2, Task 15, já tiver sido feito, manter o `ENV TZ` e o `-Duser.timezone` no `ENTRYPOINT`.
  - **`.dockerignore` na raiz:**
```
frontend
**/target
backend/data
.git
docs
```

- [ ] **Step 7: Rodar o build inteiro** — na raiz, `mvn -q install`.
  Expected: 
  - `smartscale-core` compila, passa nos 3 testes e é instalado em `~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/`;
  - o backend compila e passa em todos os testes.

  Depois: `cd backend && mvn -q test` também funciona sozinho, porque encontra o JAR instalado.
- [ ] **Step 8: `README.md`**, seção "Arquitetura": atualizar a árvore com o `smartscale-core/` na raiz e acrescentar o bloco "Como rodar → Backend":
```bash
mvn install              # na raiz: compila e instala o smartscale-core, depois o backend
cd backend && mvn spring-boot:run
```
- [ ] **Step 9: Checkpoint** — diff, sugerir `build: nucleo da linha de produto como modulo smartscale-core 1.0.0` e aguardar o usuário commitar.

---

### Task 2: Strategy ×3 — critérios de ordenação da fila (ponto de variação RN01)

**Files:**
- Create:
  - `backend/src/main/java/br/com/milscale/milscale/application/MilitarEmGeracao.java` (extraído de `GerarEscalaService`)
  - `application/CriterioMenorCargaNaGeracao.java`
  - `application/CriterioMaisModernoPrimeiro.java`
- Modify: `application/GerarEscalaService.java`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/CriteriosDeOrdenacaoTest.java`

**Interfaces:**
- Produces:
  - `public class MilitarEmGeracao implements PessoaEscalada`, com os métodos `militar()`, `getUltimoServico()`, `getServicosNaGeracao()`, `getNivelHierarquico()`, `marcarServico(LocalDate)` e `foiAtualizado()`;
  - `CriterioMenorCargaNaGeracao` e `CriterioMaisModernoPrimeiro`, que implementam `CriterioDeOrdenacao<MilitarEmGeracao>`.

- [ ] **Step 1: Teste (falha)**
```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.CriterioOrdenacaoMilitar;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.PostoGraduacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CriteriosDeOrdenacaoTest {

    private static MilitarEmGeracao militar(long id, int nivel, LocalDate ultimoServico, int servicosNaGeracao) {
        Militar m = Militar.builder().id(id).nomeGuerra("m" + id)
                .posto(PostoGraduacao.builder().id((long) nivel).sigla("P" + nivel).descricao("P").nivelHierarquico(nivel).build())
                .dataUltimoServico(ultimoServico).build();
        MilitarEmGeracao em = new MilitarEmGeracao(m);
        for (int i = 0; i < servicosNaGeracao; i++) em.marcarServico(ultimoServico);
        return em;
    }

    private static List<Long> ordenar(List<MilitarEmGeracao> fila, br.com.smartscale.core.CriterioDeOrdenacao<MilitarEmGeracao> criterio) {
        List<MilitarEmGeracao> copia = new ArrayList<>(fila);
        copia.sort(criterio.comparator());
        return copia.stream().map(MilitarEmGeracao::getId).toList();
    }

    private final LocalDate hoje = LocalDate.now();

    @Test
    void maiorFolga_quemServiuHaMaisTempoPrimeiro() {
        var fila = List.of(militar(1, 1, hoje.minusDays(2), 0), militar(2, 1, hoje.minusDays(9), 0), militar(3, 1, hoje.minusDays(5), 0));
        assertThat(ordenar(fila, new CriterioOrdenacaoMilitar<>())).containsExactly(2L, 3L, 1L);
    }

    @Test
    void menorCarga_quemServiuMenosNaGeracaoPrimeiro_empateVaiPelaFolga() {
        var fila = List.of(militar(1, 1, hoje.minusDays(9), 2), militar(2, 1, hoje.minusDays(3), 0), militar(3, 1, hoje.minusDays(8), 0));
        assertThat(ordenar(fila, new CriterioMenorCargaNaGeracao())).containsExactly(3L, 2L, 1L);
    }

    @Test
    void maisModerno_menorPostoPrimeiro_empateVaiPelaFolga() {
        var fila = List.of(militar(1, 3, hoje.minusDays(9), 0), militar(2, 1, hoje.minusDays(2), 0), militar(3, 1, hoje.minusDays(7), 0));
        assertThat(ordenar(fila, new CriterioMaisModernoPrimeiro())).containsExactly(3L, 2L, 1L);
    }
}
```
Run → FAIL de compilação.

- [ ] **Step 2: Extrair `MilitarEmGeracao`** (hoje é classe privada dentro do `GerarEscalaService`)
```java
package br.com.milscale.milscale.application;

import br.com.milscale.milscale.domain.Militar;
import br.com.smartscale.core.PessoaEscalada;
import br.com.smartscale.core.SituacaoPessoa;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MilitarEmGeracao implements PessoaEscalada {

    private final Militar militar;
    private LocalDate ultimoServico;
    private int servicosNaGeracao;
    private boolean atualizado;

    public MilitarEmGeracao(Militar militar) {
        this.militar = militar;
        this.ultimoServico = militar.getDataUltimoServico();
    }

    public Militar militar() { return militar; }
    public LocalDate getUltimoServico() { return ultimoServico; }
    public int getServicosNaGeracao() { return servicosNaGeracao; }
    public int getNivelHierarquico() { return militar.getPosto().getNivelHierarquico(); }
    public boolean foiAtualizado() { return atualizado; }

    public void marcarServico(LocalDate data) {
        ultimoServico = data;
        servicosNaGeracao++;
        atualizado = true;
    }

    @Override public Long getId() { return militar.getId(); }
    @Override public String getNomeExibicao() { return militar.getNomeExibicao(); }
    @Override public SituacaoPessoa getSituacao() { return militar.getSituacao(); }

    @Override
    public long getContadorRodizio() {
        return ultimoServico == null ? Integer.MAX_VALUE : ChronoUnit.DAYS.between(ultimoServico, LocalDate.now());
    }
}
```
No `GerarEscalaService`:
- apagar a classe interna;
- trocar `new MilitarEmGeracao(m, m.getDataUltimoServico())` por `new MilitarEmGeracao(m)`;
- trocar `em.militar` / `escolhido.militar` por `em.militar()` / `escolhido.militar()`.

Mudança de comportamento: `marcarServico` agora também soma `servicosNaGeracao`, e só os novos critérios usam esse número. Os testes existentes provam que a geração continua igual.

- [ ] **Step 3: As duas estratégias novas**
```java
package br.com.milscale.milscale.application;

import br.com.smartscale.core.CriterioDeOrdenacao;

import java.util.Comparator;

public class CriterioMenorCargaNaGeracao implements CriterioDeOrdenacao<MilitarEmGeracao> {

    @Override
    public Comparator<MilitarEmGeracao> comparator() {
        return Comparator.comparingInt(MilitarEmGeracao::getServicosNaGeracao)
                .thenComparing(Comparator.comparingLong(MilitarEmGeracao::getContadorRodizio).reversed());
    }
}
```
```java
package br.com.milscale.milscale.application;

import br.com.smartscale.core.CriterioDeOrdenacao;

import java.util.Comparator;

public class CriterioMaisModernoPrimeiro implements CriterioDeOrdenacao<MilitarEmGeracao> {

    @Override
    public Comparator<MilitarEmGeracao> comparator() {
        return Comparator.comparingInt(MilitarEmGeracao::getNivelHierarquico)
                .thenComparing(Comparator.comparingLong(MilitarEmGeracao::getContadorRodizio).reversed());
    }
}
```
- [ ] **Step 4:** `mvn -q install` (raiz) → PASS: 3 testes novos, e os de geração continuam verdes.
- [ ] **Step 5: Checkpoint** — diff, sugerir `feat: tres estrategias de ordenacao da fila (Strategy)` e aguardar o usuário commitar.

---

### Task 3: Singleton ×2 e variabilidade por configuração

**Files:**
- Create:
  - `smartscale-core/src/main/java/br/com/smartscale/core/CatalogoDeCriterios.java`
  - `backend/src/main/java/br/com/milscale/milscale/domain/IdentidadeDaOrganizacao.java`
  - `backend/src/main/java/br/com/milscale/milscale/adapters/config/VariabilidadeConfig.java`
  - `backend/src/main/java/br/com/milscale/milscale/adapters/web/OrganizacaoController.java`
- Modify:
  - `application/GerarEscalaService.java` (recebe o critério por injeção)
  - `LembreteServicoService` (Plano 3, Task 6) ou `LembreteServicoScheduler` (texto do e-mail)
  - `adapters/config/SecurityConfig.java` (liberar `/api/organizacao`)
  - `application.properties`, `smartscale-core/CHANGELOG.md` (criado na Task 7)
  - frontend: `pages/Login.tsx`, `pages/EscalaPdf.tsx`
- Test:
  - `smartscale-core/src/test/java/br/com/smartscale/core/CatalogoDeCriteriosTest.java`
  - `backend/src/test/java/br/com/milscale/milscale/adapters/config/VariabilidadeIntegrationTest.java`
  - `backend/src/test/java/br/com/milscale/milscale/domain/IdentidadeDaOrganizacaoTest.java`

**Interfaces:**
- Produces:
  - `CatalogoDeCriterios.instancia()`, `registrar(String, CriterioDeOrdenacao<?>)`, `<P extends PessoaEscalada> CriterioDeOrdenacao<P> obter(String)`, `Set<String> nomes()`
  - `IdentidadeDaOrganizacao.INSTANCIA` (`nome()`, `sigla()`, `sistema()`, `assinatura()`)
  - propriedade `milscale.lps.criterio-ordenacao` (`maior-folga` | `menor-carga` | `mais-moderno`)
  - `GET /api/organizacao` → `{ "nome", "sigla", "sistema" }` (público)

- [ ] **Step 1: Testes (falham)**
```java
package br.com.smartscale.core;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogoDeCriteriosTest {

    @Test
    void sempreAMesmaInstancia() {
        assertThat(CatalogoDeCriterios.instancia()).isSameAs(CatalogoDeCriterios.instancia());
    }

    @Test
    void devolveOCriterioRegistradoPeloNome() {
        CriterioDeOrdenacao<PessoaEscalada> porId = () -> Comparator.comparing(PessoaEscalada::getId);
        CatalogoDeCriterios.instancia().registrar("por-id", porId);
        assertThat(CatalogoDeCriterios.instancia().<PessoaEscalada>obter("por-id")).isSameAs(porId);
    }

    @Test
    void nomeDesconhecido_explicaAsOpcoes() {
        assertThatThrownBy(() -> CatalogoDeCriterios.instancia().obter("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inexistente");
    }
}
```
```java
package br.com.milscale.milscale.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentidadeDaOrganizacaoTest {

    @Test
    void existeUmaUnicaIdentidade() {
        assertThat(IdentidadeDaOrganizacao.values()).hasSize(1);
        assertThat(IdentidadeDaOrganizacao.INSTANCIA.assinatura()).isEqualTo("MilScale, 5º Batalhão de Suprimento");
    }
}
```
```java
package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.application.CriterioMenorCargaNaGeracao;
import br.com.milscale.milscale.application.MilitarEmGeracao;
import br.com.smartscale.core.CriterioDeOrdenacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "milscale.lps.criterio-ordenacao=menor-carga")
@ActiveProfiles("test")
class VariabilidadeIntegrationTest {

    @Autowired private CriterioDeOrdenacao<MilitarEmGeracao> criterio;

    @Test
    void oCriterioDaFilaVemDaConfiguracao() {
        assertThat(criterio).isInstanceOf(CriterioMenorCargaNaGeracao.class);
    }
}
```
Run → FAIL de compilação.

- [ ] **Step 2: Singleton clássico no núcleo** (*holder* preguiçoso: seguro entre threads sem `synchronized`)
```java
package br.com.smartscale.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CatalogoDeCriterios {

    private final Map<String, CriterioDeOrdenacao<?>> criterios = new ConcurrentHashMap<>();

    private CatalogoDeCriterios() {}

    private static final class Portador {
        private static final CatalogoDeCriterios INSTANCIA = new CatalogoDeCriterios();
    }

    public static CatalogoDeCriterios instancia() {
        return Portador.INSTANCIA;
    }

    public void registrar(String nome, CriterioDeOrdenacao<?> criterio) {
        criterios.put(nome, criterio);
    }

    @SuppressWarnings("unchecked")
    public <P extends PessoaEscalada> CriterioDeOrdenacao<P> obter(String nome) {
        CriterioDeOrdenacao<?> criterio = criterios.get(nome);
        if (criterio == null) {
            throw new IllegalArgumentException("Critério de ordenação desconhecido: " + nome + ". Opções: " + nomes());
        }
        return (CriterioDeOrdenacao<P>) criterio;
    }

    public Set<String> nomes() {
        return Set.copyOf(criterios.keySet());
    }
}
```

- [ ] **Step 3: Singleton por enum no produto**
```java
package br.com.milscale.milscale.domain;

public enum IdentidadeDaOrganizacao {
    INSTANCIA;

    public String nome() { return "5º Batalhão de Suprimento"; }
    public String sigla() { return "5º B Sup"; }
    public String sistema() { return "MilScale"; }
    public String assinatura() { return sistema() + ", " + nome(); }
}
```
Usar no texto do e-mail de lembrete: trocar o literal `"— MilScale, 5º Batalhão de Suprimento"` por `"— " + IdentidadeDaOrganizacao.INSTANCIA.assinatura()`.

- [ ] **Step 4: Variabilidade por configuração** — o ponto de variação RN01 é resolvido na engenharia da aplicação:
```java
package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.application.CriterioMaisModernoPrimeiro;
import br.com.milscale.milscale.application.CriterioMenorCargaNaGeracao;
import br.com.milscale.milscale.application.MilitarEmGeracao;
import br.com.milscale.milscale.domain.CriterioOrdenacaoMilitar;
import br.com.smartscale.core.CatalogoDeCriterios;
import br.com.smartscale.core.CriterioDeOrdenacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VariabilidadeConfig {

    @Bean
    public CriterioDeOrdenacao<MilitarEmGeracao> criterioDeOrdenacao(
            @Value("${milscale.lps.criterio-ordenacao:maior-folga}") String nome) {
        CatalogoDeCriterios catalogo = CatalogoDeCriterios.instancia();
        catalogo.registrar("maior-folga", new CriterioOrdenacaoMilitar<MilitarEmGeracao>());
        catalogo.registrar("menor-carga", new CriterioMenorCargaNaGeracao());
        catalogo.registrar("mais-moderno", new CriterioMaisModernoPrimeiro());
        return catalogo.obter(nome);
    }
}
```
No `GerarEscalaService`:
- apagar o campo `criterio = new CriterioOrdenacaoMilitar<>()`;
- receber `CriterioDeOrdenacao<MilitarEmGeracao> criterio` pelo construtor.

Em `application.properties`:
```properties
# ---- Linha de produto ----
milscale.lps.criterio-ordenacao=${MILSCALE_CRITERIO_ORDENACAO:maior-folga}
```
Acrescentar `MILSCALE_CRITERIO_ORDENACAO` na tabela de variáveis do `README.md`.

- [ ] **Step 5: Identidade exposta ao frontend**
```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.domain.IdentidadeDaOrganizacao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrganizacaoController {

    @GetMapping("/api/organizacao")
    public Map<String, String> organizacao() {
        IdentidadeDaOrganizacao o = IdentidadeDaOrganizacao.INSTANCIA;
        return Map.of("nome", o.nome(), "sigla", o.sigla(), "sistema", o.sistema());
    }
}
```
No `SecurityConfig`: `.requestMatchers("/api/auth/login", "/api/organizacao").permitAll()`. Se o Plano 2 ainda não foi feito, `/api/auth/**` já é público; nesse caso, acrescente só `/api/organizacao`.

No frontend:
- `Login.tsx` e `EscalaPdf.tsx` buscam `/api/organizacao` e usam `nome` no lugar do texto fixo "5º Batalhão de Suprimento";
- enquanto a busca não volta, mostram só "MilScale".

- [ ] **Step 6: Rodar** — `mvn -q install` → PASS; `npm run build` → PASS. Teste manual: subir com `MILSCALE_CRITERIO_ORDENACAO=mais-moderno`, gerar 3 dias e conferir na tela que os escalados mudam em relação ao `maior-folga`.
- [ ] **Step 7: Checkpoint** — diff, sugerir `feat: catalogo de criterios e identidade da organizacao (Singleton) e criterio da fila por configuracao` e aguardar o usuário commitar.

---

### Task 4: Template Method ×3 — relatórios CSV

**Files:**
- Create:
  - `application/relatorios/RelatorioCsv.java` (classe abstrata com o método-modelo)
  - `application/relatorios/RelatorioEscalaDoDia.java`
  - `application/relatorios/RelatorioServicosDoMilitar.java`
  - `application/relatorios/RelatorioAfastamentosDoMes.java`
  - `application/relatorios/RelatorioService.java`
  - `adapters/web/RelatorioController.java`
- Modify (front): `src/api/client.ts` (exportar `BASE_URL`), `pages/EscalaDoMes.tsx`, `pages/EscalaDoDia.tsx`, `pages/FichaMilitar.tsx`, `pages/MissoesDispensas.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/relatorios/RelatoriosCsvTest.java`, `backend/src/test/java/br/com/milscale/milscale/adapters/web/RelatorioControllerIntegrationTest.java`

**Interfaces:**
- Produces:
  - `RelatorioCsv<T>`: `final String gerar()`, `abstract String nomeDoArquivo()`, e os passos `cabecalho()`, `itens()`, `colunas(T)` e o gancho `rodape()`;
  - endpoints:
    - `GET /api/relatorios/escala-do-dia.csv?data=`
    - `GET /api/relatorios/militares/{id}/servicos.csv`
    - `GET /api/relatorios/afastamentos.csv?mes=`

  Os três são só para a sargenteação.

- [ ] **Step 1: Teste do método-modelo (falha)**
```java
package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelatoriosCsvTest {

    private final PostoGraduacao cabo = PostoGraduacao.builder().id(1L).sigla("Cb").descricao("Cabo").nivelHierarquico(3).build();
    private final Militar silva = Militar.builder().id(1L).nomeGuerra("Silva").posto(cabo).build();
    private final TipoServico guarda = TipoServico.builder().id(1L).nome("Cabo da Guarda; noturno").build();

    @Test
    void escalaDoDia_segueOEsqueletoEEscapaSeparador() {
        LocalDate dia = LocalDate.of(2030, 1, 10);
        ServicoEscalado s = ServicoEscalado.builder().data(dia).tipoServico(guarda).militar(silva).build();
        String csv = new RelatorioEscalaDoDia(dia, List.of(s)).gerar();

        String[] linhas = csv.split("\r\n");
        assertThat(linhas[0]).isEqualTo("﻿Função;Posto;Nome de guerra;Situação");
        assertThat(linhas[1]).isEqualTo("\"Cabo da Guarda; noturno\";Cb;Silva;PREVISTO");
        assertThat(csv).contains("MilScale, 5º Batalhão de Suprimento");
    }

    @Test
    void cadaRelatorioTemSeuNomeDeArquivo() {
        assertThat(new RelatorioEscalaDoDia(LocalDate.of(2030, 1, 10), List.of()).nomeDoArquivo()).isEqualTo("escala-2030-01-10.csv");
        assertThat(new RelatorioServicosDoMilitar(silva, List.of()).nomeDoArquivo()).isEqualTo("servicos-silva.csv");
        assertThat(new RelatorioAfastamentosDoMes(java.time.YearMonth.of(2030, 1), List.of()).nomeDoArquivo()).isEqualTo("afastamentos-2030-01.csv");
    }
}
```

- [ ] **Step 2: A classe abstrata com o método-modelo**
```java
package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.IdentidadeDaOrganizacao;

import java.util.List;

public abstract class RelatorioCsv<T> {

    private static final String SEPARADOR = ";";
    private static final String QUEBRA = "\r\n";
    private static final String BOM_PARA_EXCEL = "﻿";

    public final String gerar() {
        StringBuilder csv = new StringBuilder(BOM_PARA_EXCEL);
        csv.append(linha(cabecalho())).append(QUEBRA);
        for (T item : itens()) {
            csv.append(linha(colunas(item))).append(QUEBRA);
        }
        csv.append(QUEBRA).append(rodape()).append(QUEBRA);
        return csv.toString();
    }

    public abstract String nomeDoArquivo();

    protected abstract List<String> cabecalho();

    protected abstract List<T> itens();

    protected abstract List<String> colunas(T item);

    protected String rodape() {
        return "Gerado por " + IdentidadeDaOrganizacao.INSTANCIA.assinatura();
    }

    private String linha(List<String> valores) {
        return String.join(SEPARADOR, valores.stream().map(this::escapar).toList());
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        boolean precisaDeAspas = valor.contains(SEPARADOR) || valor.contains("\"") || valor.contains("\n");
        return precisaDeAspas ? "\"" + valor.replace("\"", "\"\"") + "\"" : valor;
    }
}
```

- [ ] **Step 3: Os três relatórios concretos**
```java
package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.ServicoEscalado;

import java.time.LocalDate;
import java.util.List;

public class RelatorioEscalaDoDia extends RelatorioCsv<ServicoEscalado> {

    private final LocalDate dia;
    private final List<ServicoEscalado> servicos;

    public RelatorioEscalaDoDia(LocalDate dia, List<ServicoEscalado> servicos) {
        this.dia = dia;
        this.servicos = servicos;
    }

    @Override public String nomeDoArquivo() { return "escala-" + dia + ".csv"; }
    @Override protected List<String> cabecalho() { return List.of("Função", "Posto", "Nome de guerra", "Situação"); }
    @Override protected List<ServicoEscalado> itens() { return servicos; }

    @Override
    protected List<String> colunas(ServicoEscalado s) {
        return List.of(s.getTipoServico().getNome(),
                s.getMilitar() != null ? s.getMilitar().getPosto().getSigla() : "",
                s.getMilitar() != null ? s.getMilitar().getNomeGuerra() : "VAGA EM ABERTO",
                s.getSituacao().name());
    }
}
```
```java
package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.ServicoEscalado;

import java.util.List;
import java.util.Locale;

public class RelatorioServicosDoMilitar extends RelatorioCsv<ServicoEscalado> {

    private final Militar militar;
    private final List<ServicoEscalado> servicos;

    public RelatorioServicosDoMilitar(Militar militar, List<ServicoEscalado> servicos) {
        this.militar = militar;
        this.servicos = servicos;
    }

    @Override public String nomeDoArquivo() { return "servicos-" + militar.getNomeGuerra().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-") + ".csv"; }
    @Override protected List<String> cabecalho() { return List.of("Data", "Serviço", "Situação"); }
    @Override protected List<ServicoEscalado> itens() { return servicos; }
    @Override protected List<String> colunas(ServicoEscalado s) { return List.of(s.getData().toString(), s.getTipoServico().getNome(), s.getSituacao().name()); }
    @Override protected String rodape() { return "Serviços de " + militar.getNomeExibicao() + " — " + super.rodape(); }
}
```
```java
package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.domain.Afastamento;

import java.time.YearMonth;
import java.util.List;

public class RelatorioAfastamentosDoMes extends RelatorioCsv<Afastamento> {

    private final YearMonth mes;
    private final List<Afastamento> afastamentos;

    public RelatorioAfastamentosDoMes(YearMonth mes, List<Afastamento> afastamentos) {
        this.mes = mes;
        this.afastamentos = afastamentos;
    }

    @Override public String nomeDoArquivo() { return "afastamentos-" + mes + ".csv"; }
    @Override protected List<String> cabecalho() { return List.of("Militar", "Tipo", "Descrição", "Início", "Fim"); }
    @Override protected List<Afastamento> itens() { return afastamentos; }

    @Override
    protected List<String> colunas(Afastamento a) {
        return List.of(a.getMilitar().getNomeExibicao(), a.getTipo().name(), a.getDescricao(),
                a.getDataInicio().toString(), a.getDataFim().toString());
    }
}
```

- [ ] **Step 4: Service e controller**
```java
package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.NoSuchElementException;

@Service
public class RelatorioService {

    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final MilitarRepository militarRepository;
    private final AfastamentoRepository afastamentoRepository;

    public RelatorioService(ServicoEscaladoRepository servicoEscaladoRepository, MilitarRepository militarRepository,
                            AfastamentoRepository afastamentoRepository) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.militarRepository = militarRepository;
        this.afastamentoRepository = afastamentoRepository;
    }

    public RelatorioCsv<?> escalaDoDia(LocalDate dia) {
        return new RelatorioEscalaDoDia(dia, servicoEscaladoRepository.findByData(dia));
    }

    public RelatorioCsv<?> servicosDoMilitar(Long militarId) {
        var militar = militarRepository.findById(militarId).orElseThrow(() -> new NoSuchElementException("Militar nao encontrado"));
        return new RelatorioServicosDoMilitar(militar, servicoEscaladoRepository.findByMilitar_IdOrderByDataDesc(militarId));
    }

    public RelatorioCsv<?> afastamentosDoMes(YearMonth mes) {
        return new RelatorioAfastamentosDoMes(mes,
                afastamentoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(mes.atEndOfMonth(), mes.atDay(1)));
    }
}
```
```java
package br.com.milscale.milscale.adapters.web;

import br.com.milscale.milscale.application.relatorios.RelatorioCsv;
import br.com.milscale.milscale.application.relatorios.RelatorioService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("hasAnyRole('CABO_SARGENTEACAO', 'SD_EP_SARGENTEACAO', 'SARGENTEANTE')")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/escala-do-dia.csv")
    public ResponseEntity<byte[]> escalaDoDia(@RequestParam String data) {
        return csv(relatorioService.escalaDoDia(LocalDate.parse(data)));
    }

    @GetMapping("/militares/{id}/servicos.csv")
    public ResponseEntity<byte[]> servicosDoMilitar(@PathVariable Long id) {
        return csv(relatorioService.servicosDoMilitar(id));
    }

    @GetMapping("/afastamentos.csv")
    public ResponseEntity<byte[]> afastamentos(@RequestParam String mes) {
        return csv(relatorioService.afastamentosDoMes(YearMonth.parse(mes)));
    }

    private ResponseEntity<byte[]> csv(RelatorioCsv<?> relatorio) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(relatorio.nomeDoArquivo()).build().toString())
                .body(relatorio.gerar().getBytes(StandardCharsets.UTF_8));
    }
}
```
Teste de integração, com o mesmo padrão MockMvc dos anteriores:
- como `00000000001`, `GET /api/relatorios/escala-do-dia.csv?data=<dia com escala>` → 200, `Content-Type` `text/csv`, e o `Content-Disposition` contém `escala-`;
- como `00000000004` → 403.

- [ ] **Step 5: Botões "Baixar CSV"** — em `client.ts`, `export const BASE_URL = …`. Nas páginas, um link simples (o cookie de sessão vai junto porque é o mesmo domínio):
```tsx
<a className="btn btn-outline" href={`${BASE_URL}/api/relatorios/escala-do-dia.csv?data=${data}`} download>Baixar CSV</a>
```
  - **Escala do dia e detalhe do dia na Escala do mês:** `escala-do-dia.csv`.
  - **Ficha do militar**, card do histórico de serviços: `militares/${id}/servicos.csv`.
  - **Missões e dispensas:** `afastamentos.csv?mes=` do mês atual.

  Os botões aparecem só para a sargenteação (`usePermissoes().daSargenteacao` do Plano 3, ou a checagem de perfil atual).
- [ ] **Step 6:** `mvn -q install` → PASS; `npm run build` → PASS. Teste manual: baixar os três CSVs e abrir no Excel. Os acentos aparecem certos, graças ao BOM, e as colunas vêm separadas.
- [ ] **Step 7: Checkpoint** — diff, sugerir `feat: relatorios CSV com Template Method` e aguardar o usuário commitar.

---

### Task 5: CRUDs completos (telas com criar, ver, editar e excluir)

Faltam operações em três telas:
- **Qualificações:** não tem **excluir**;
- **Feriados:** não tem **editar**;
- **Missões e dispensas:** não tem **editar**.

Com elas completas, o sistema fica com 6 telas de CRUD completo: Militares (excluir = desligar), Tipos de serviço (excluir = desativar), Boletim, Qualificações, Feriados e Missões.

**Files:**
- Modify (backend): `QualificacaoService`/`Controller`, `FeriadoService`/`Controller`, `AfastamentoService`/`Controller`, `MilitarRepository`, `RequisitoServicoRepository`, `AfastamentoRepository`
- Modify (front): `pages/Qualificacoes.tsx`, `pages/Feriados.tsx`, `pages/MissoesDispensas.tsx`
- Test: `backend/src/test/java/br/com/milscale/milscale/application/CrudsCompletosIntegrationTest.java`

**Interfaces:**
- Produces:
  - `DELETE /api/qualificacoes/{id}`: recusa com 400 se o curso estiver em uso;
  - `PUT /api/feriados/{id}`;
  - `PUT /api/afastamentos/{id}`: altera o lote inteiro, se houver lote.

- [ ] **Step 1: Testes (falham)**
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CrudsCompletosIntegrationTest {

    @Autowired private QualificacaoService qualificacaoService;
    @Autowired private QualificacaoRepository qualificacaoRepository;
    @Autowired private FeriadoService feriadoService;
    @Autowired private FeriadoRepository feriadoRepository;
    @Autowired private AfastamentoService afastamentoService;
    @Autowired private AfastamentoRepository afastamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    void excluirCursoSemUso_remove() {
        Qualificacao q = qualificacaoRepository.save(Qualificacao.builder().nome("Curso Avulso").build());
        qualificacaoService.excluir(q.getId());
        assertThat(qualificacaoRepository.findById(q.getId())).isEmpty();
    }

    @Test
    void excluirCursoEmUso_recusa() {
        Qualificacao cfc = qualificacaoRepository.findAll().stream().filter(q -> q.getNome().equals("CFC")).findFirst().orElseThrow();
        assertThatThrownBy(() -> qualificacaoService.excluir(cfc.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("em uso");
    }

    @Test
    void editarFeriado_alteraPeriodoEDescricao() {
        Feriado f = feriadoRepository.save(Feriado.builder().dataInicio(LocalDate.of(2030, 1, 1)).dataFim(LocalDate.of(2030, 1, 1))
                .descricao("Ano novo").tipo(TipoFeriado.NACIONAL).build());
        feriadoService.atualizar(f.getId(), new DadosFeriado(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2), "Ano novo + ponte", TipoFeriado.OM));
        Feriado depois = feriadoRepository.findById(f.getId()).orElseThrow();
        assertThat(depois.getDataFim()).isEqualTo(LocalDate.of(2030, 1, 2));
        assertThat(depois.getTipo()).isEqualTo(TipoFeriado.OM);
    }

    @Test
    void editarAfastamentoDeLote_alteraTodosDoLote() {
        List<Long> dois = usuarioRepository.findAll().stream().limit(2).map(u -> u.getMilitar().getId()).toList();
        LocalDate dia = LocalDate.now().plusMonths(3);
        List<Afastamento> criados = afastamentoService.cadastrarMissao(dois, TipoAfastamento.MISSAO, "Missão A", dia, dia, "00000000001");

        afastamentoService.atualizar(criados.get(0).getId(), TipoAfastamento.MISSAO, "Missão A (estendida)", dia, dia.plusDays(2));

        assertThat(afastamentoRepository.findAllById(criados.stream().map(Afastamento::getId).toList()))
                .allMatch(a -> a.getDescricao().equals("Missão A (estendida)") && a.getDataFim().equals(dia.plusDays(2)));
    }
}
```
(`TipoFeriado`/`DadosFeriado` vêm do Plano 2, Task 12. Se ela ainda não tiver sido feita, crie os dois aqui, com o código daquela tarefa.)

- [ ] **Step 2: Backend**
  - **Consultas de uso do curso:**
    - `MilitarRepository`: `boolean existsByQualificacoes_Id(Long qualificacaoId);`
    - `RequisitoServicoRepository`: `boolean existsByQualificacao_Id(Long qualificacaoId);` e `boolean existsByQualificacoesExcluidas_Id(Long qualificacaoId);`
  - **`QualificacaoService`:**
```java
    @Transactional
    public void excluir(Long id) {
        Qualificacao q = qualificacaoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Qualificacao nao encontrada"));
        if (militarRepository.existsByQualificacoes_Id(id) || requisitoServicoRepository.existsByQualificacao_Id(id)
                || requisitoServicoRepository.existsByQualificacoesExcluidas_Id(id)) {
            throw new IllegalArgumentException("Esse curso está em uso por militares ou por requisitos de serviço — desvincule antes de excluir");
        }
        qualificacaoRepository.delete(q);
    }
```
  - **`FeriadoService.atualizar(Long id, DadosFeriado d)`:** valida as datas como no cadastro e altera os 4 campos.
  - **`AfastamentoRepository`:** `List<Afastamento> findByLoteMissao(String loteMissao);`
  - **`AfastamentoService`:**
```java
    @Transactional
    public List<Afastamento> atualizar(Long id, TipoAfastamento tipo, String descricao, LocalDate dataInicio, LocalDate dataFim) {
        if (dataFim.isBefore(dataInicio)) throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        Afastamento base = afastamentoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Afastamento nao encontrado"));
        List<Afastamento> doLote = base.getLoteMissao() == null ? List.of(base) : afastamentoRepository.findByLoteMissao(base.getLoteMissao());
        for (Afastamento a : doLote) {
            a.setTipo(tipo);
            a.setDescricao(descricao);
            a.setDataInicio(dataInicio);
            a.setDataFim(dataFim);
            afastamentoRepository.save(a);
            reconciliarServicosJaMarcados(a);
        }
        return doLote;
    }
```
  - **Controllers**, com a mesma autorização do cadastro e registro na auditoria:
    - `QualificacaoController`: `@DeleteMapping("/qualificacoes/{id}")`, só Sargenteante;
    - `FeriadoController`: `@PutMapping("/{id}")`, só Sargenteante, com `@Valid @RequestBody DadosFeriado`;
    - `AfastamentoController`: `@PutMapping("/{id}")`, Cabo e Sargenteante, com um record `EditarAfastamentoRequest(@NotNull TipoAfastamento tipo, @NotBlank @Size(max=150) String descricao, @NotNull LocalDate dataInicio, @NotNull LocalDate dataFim)`.

- [ ] **Step 3: Frontend**
  - **`Qualificacoes.tsx`:** botão "Excluir" em cada linha, para o Sargenteante. Antes, pede confirmação (`useFeedback().confirmar` do Plano 3, ou `confirm` se o Plano 3 ainda não existir). O erro de "curso em uso" aparece para a pessoa.
  - **`Feriados.tsx`:** botão "Editar" na linha, que abre o mesmo formulário do cadastro já preenchido; ao salvar, chama `PUT`.
  - **`MissoesDispensas.tsx`:** botão "Editar" no grupo, que abre o `NovoAfastamentoForm` em modo edição. Nesse modo, a seleção de militares fica só leitura, porque a edição muda o lote e não a equipe. Ao salvar, chama `PUT /api/afastamentos/{primeiroId}`.
- [ ] **Step 4:** `mvn -q install` → PASS; `npm run build` → PASS. Teste manual: as operações de criar, ver, editar e excluir funcionam nas 6 telas.
- [ ] **Step 5: Checkpoint** — diff, sugerir `feat: CRUD completo em qualificacoes, feriados e missoes` e aguardar o usuário commitar.

---

### Task 6: Documentação de padrões e variabilidade (para a apresentação e a prova de autoria)

**Files:**
- Create: `docs/PADROES_DE_PROJETO.md`, `docs/VARIABILIDADE.md`

- [ ] **Step 1: `docs/PADROES_DE_PROJETO.md`** — para cada padrão:
  - o problema que ele resolve **neste sistema**;
  - os arquivos (links relativos);
  - um trecho de 10 a 20 linhas do código real;
  - "o que mudaria sem o padrão".

  Estrutura:
```markdown
# Padrões de projeto aplicados ao reuso

## Classes principais do sistema (≥ 10)
| Classe | Papel | Atributos principais | Métodos principais |
|---|---|---|---|
| `Militar` | pessoa escalada (especializa `PessoaEscalada`) | nomeGuerra, cpf, posto, subunidade, qualificacoes, dataUltimoServico | getContadorRodizio, isTemFoto |
| `TipoServico` | turno (especializa `TipoTurno`) | nome, efetivoNecessario, horaInicio, requisitos | getQuantidadeRequisitos |
| `Escala` | período gerado | dataInicio, dataFim, situacao, servicos | — |
| `ServicoEscalado` | uma vaga num dia | data, tipoServico, militar, travado | isJaComecou |
| `Solicitacao` | pedido de troca | tipoTroca, situacao, solicitante, substituto | — |
| `Afastamento` | missão/dispensa/férias | tipo, periodo, loteMissao | cobre |
| `RegraEscala` | regras por tipo de serviço | intervaloMinimo, maxServicosMes | — |
| `RequisitoServico` | quem pode tirar o serviço | posto, subunidade, qualificacao, exclusões | — |
| `MotorDeRodizio` (núcleo) | algoritmo de rodízio | — | preencherVagas |
| `GerarEscalaService` | caso de uso de geração | repositórios, motor, criterio | gerar |
| `SolicitacaoService` | fluxo de trocas | repositórios | criar, criarTrocaMutua, triagem, autorizar |
| `PoliticaDeDescanso` | regra de intervalo | constantes | respeitaIntervalo, ficariaEm1x1 |

## Strategy (3 exemplos) — critério de ordenação da fila
## Singleton (2 exemplos) — catálogo de critérios e identidade da organização
## Template Method (3 exemplos) — relatórios CSV
## Como os três se combinam
(o Singleton guarda as Strategies; a configuração escolhe uma; o relatório usa o Singleton de identidade no rodapé)
```
Preencher cada seção com o conteúdo real das Tasks 2–4. Para o Singleton, explicar a diferença entre as duas implementações: o *holder* preguiçoso do `CatalogoDeCriterios` e o `enum` da `IdentidadeDaOrganizacao`. O enum é à prova de reflexão e de serialização.

- [ ] **Step 2: `docs/VARIABILIDADE.md`** — o exemplo de variabilidade planejada, com o modelo de features em texto:
```markdown
# Variabilidade planejada — linha de produto SmartScale

## Modelo de features
SmartScale
├── Núcleo (obrigatório): MotorDeRodizio, PessoaEscalada, TipoTurno, CriterioDeOrdenacao
├── Critério de ordenação da fila (ponto de variação RN01) — alternativo, escolher 1
│   ├── maior-folga      (MilScale: padrão do batalhão)
│   ├── menor-carga      (produto hospitalar: equilíbrio de plantões)
│   └── mais-moderno     (tradição militar: o mais moderno tira primeiro)
├── Elegibilidade (obrigatório): posto + subunidade + curso, com exclusões
├── Trocas de serviço (opcional): substituição [e troca mútua]
├── Comunicação (opcional): notificações no sistema, lembrete por e-mail
└── Relatórios (opcional): CSV da escala do dia, dos serviços do militar, dos afastamentos

## Como a variante é escolhida (engenharia de aplicação)
MILSCALE_CRITERIO_ORDENACAO=menor-carga   →   VariabilidadeConfig registra os três no CatalogoDeCriterios e entrega o escolhido ao GerarEscalaService.

## Como um novo produto da linha reusa o núcleo
1. adiciona a dependência br.com.smartscale:smartscale-core:1.0.0;
2. implementa PessoaEscalada e TipoTurno com o seu vocabulário;
3. escolhe ou cria um CriterioDeOrdenacao;
4. chama MotorDeRodizio.preencherVagas — sem alterar o núcleo (ver MotorDeRodizioTest.outroProdutoDaLinha_...).
```
- [ ] **Step 3: Checkpoint** — diff, sugerir `docs: padroes de projeto e variabilidade da linha de produto` e aguardar o usuário commitar.

---

### Task 7: Release 1.0.0 do núcleo e roteiro da gravação

**Files:**
- Create: `smartscale-core/README.md`, `smartscale-core/LICENSE`, `smartscale-core/CHANGELOG.md`, `docs/ROTEIRO_GRAVACAO.md`

- [ ] **Step 1: `smartscale-core/README.md`** — descrição, instalação (`mvn install` ou dependência), exemplo mínimo de uso (o do `MotorDeRodizioTest`), contratos públicos e versão.
- [ ] **Step 2: `smartscale-core/LICENSE`** — texto da licença MIT com `Copyright (c) 2026 Equipe SmartScale — PUCPR`. Se a equipe preferir outra licença, trocar aqui e no `pom.xml`.
- [ ] **Step 3: `smartscale-core/CHANGELOG.md`**
```markdown
# Changelog — smartscale-core
Formato: Keep a Changelog. Versionamento: SemVer.

## [1.0.0] - 2026-09-25
### Adicionado
- `MotorDeRodizio`: preenche as vagas de um turno com as pessoas mais bem posicionadas na fila.
- Contratos `PessoaEscalada`, `TipoTurno`, `CriterioDeOrdenacao` e `SituacaoPessoa`.
- `CatalogoDeCriterios`: registro único de critérios de ordenação (ponto de variação RN01).
```
- [ ] **Step 4: `docs/ROTEIRO_GRAVACAO.md`** — o passo a passo que a equipe grava, com as falas-guia:
  1. **O problema:** mostrar o README e a pasta `smartscale-core`. "O núcleo não sabe o que é um militar."
  2. **O empacotamento:** mostrar o `smartscale-core/pom.xml`, com coordenadas, versão, licença e manifesto.
  3. **Os testes do núcleo:** na raiz, rodar `mvn -pl smartscale-core clean install`. Mostrar os 3 testes passando e o JAR em `~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/`, junto com o `-sources.jar`.
  4. **O conteúdo do JAR:** abrir o JAR (`jar tf smartscale-core-1.0.0.jar`) e mostrar as classes e o `MANIFEST.MF`.
  5. **O consumo:** mostrar a dependência em `backend/pom.xml` e rodar `mvn -pl backend test`. O MilScale usa o componente instalado.
  6. **A variabilidade:** subir com `MILSCALE_CRITERIO_ORDENACAO=maior-folga`, gerar 3 dias; repetir com `mais-moderno` e comparar.
  7. **O fechamento:** mostrar o `CHANGELOG.md` e explicar como uma versão 1.1.0 seria publicada.
- [ ] **Step 5: Tag** (ação do usuário, depois do commit): `git tag -a smartscale-core-v1.0.0 -m "smartscale-core 1.0.0"` e `git push origin smartscale-core-v1.0.0`.
- [ ] **Step 6: Checkpoint** — diff, sugerir `docs: release 1.0.0 do smartscale-core e roteiro da gravacao` e aguardar o usuário commitar.

---

## Fechamento do Plano 5

- [ ] Rodar na raiz `mvn clean install`, e no frontend `npm test && npm run build`.
- [ ] Conferir a tabela "Como o plano cobre o PDF", item por item, contra o código.
- [ ] A equipe grava o vídeo seguindo `docs/ROTEIRO_GRAVACAO.md` e declara o uso de IA conforme a regra PUCPR.
