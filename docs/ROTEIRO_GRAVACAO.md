# Roteiro da gravação — empacotamento do núcleo reutilizável

Este é o passo a passo para gravar o item 03 do trabalho "Componentes de Reuso — Parte 02": criar o repositório e aplicar uma técnica de empacotamento de código focada em reuso.

- **Técnica:** empacotar o núcleo da linha de produto como um **componente Maven versionado** (`br.com.smartscale:smartscale-core:1.0.0`). O componente é instalado num repositório de artefatos e consumido pelo produto como dependência, e não copiado.
- **Duração sugerida:** 8 a 10 minutos.
- **Antes de gravar:**
  - feche os servidores do sistema, para liberar memória;
  - abra o Git Bash na raiz do projeto;
  - aumente a fonte do terminal.

```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
export JAVA_HOME="/c/Program Files/Java/jdk-23"
```

---

## 1. O repositório e o problema (1 min)

**Mostrar:** o repositório no GitHub (ou `git log --oneline | head`) e a árvore da raiz.

```bash
git log --oneline | head -10
ls
```

**Falar:**
- O repositório tem dois módulos Maven: `smartscale-core`, o núcleo reutilizável, e `backend`, o produto MilScale.
- O `pom.xml` da raiz só agrega os dois.
- Sem empacotamento, outro produto da linha teria de copiar as classes do motor, o reuso ad hoc que o professor mostrou nos slides. Com o empacotamento, ele declara uma dependência com versão.

## 2. O núcleo não conhece o produto (1 min)

**Mostrar:** `smartscale-core/src/main/java/br/com/smartscale/core/` e o `MotorDeRodizio.java`.

```bash
ls smartscale-core/src/main/java/br/com/smartscale/core/
```

**Falar:** o motor só enxerga `PessoaEscalada` e `TipoTurno`, ou seja, não sabe o que é um militar. O critério de ordenação é injetado (Strategy) e é o ponto de variação da linha.

## 3. O contrato do pacote: o `pom.xml` (1 min 30)

**Mostrar:** `smartscale-core/pom.xml`, destacando cada parte:

| Trecho | Por que importa para o reuso |
|---|---|
| `groupId`, `artifactId`, `version` 1.0.0 | identidade do componente; quem usa declara exatamente essa versão |
| `<licenses>` MIT | deixa claro como o componente pode ser reusado |
| `<dependencies>` só com escopo `test` | em tempo de execução, o componente não arrasta nenhuma biblioteca |
| `maven-source-plugin` | publica junto o JAR de fontes |
| `maven-jar-plugin` com `manifestEntries` | versão e nome do módulo gravados no manifesto |
| `<resources>` com README, LICENSE e CHANGELOG | a documentação viaja dentro do artefato |

## 4. Testar e empacotar (1 min 30)

```bash
cd smartscale-core
mvn clean install
```

**Mostrar:**
- `Tests run: 7, Failures: 0` e `BUILD SUCCESS`, se rodar sem `-q`;
- o artefato instalado no repositório Maven local:

```bash
ls ~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/
```

**Falar:**
- O `install` compila, roda os testes e só empacota se tudo passar.
- O resultado são três arquivos: o `.jar`, o `-sources.jar` e o `.pom` com as coordenadas.
- O `MotorDeRodizioTest.outroProdutoDaLinha_usaOutroCriterioSemMudarOMotor` prova o reuso: simula outro produto usando o mesmo motor com outro critério.

## 5. Abrir o artefato (1 min)

```bash
jar tf ~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/smartscale-core-1.0.0.jar
unzip -p ~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/smartscale-core-1.0.0.jar META-INF/MANIFEST.MF
unzip -p ~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/smartscale-core-1.0.0.jar META-INF/CHANGELOG.md
```

Se o `jar` não estiver no PATH, use `"$JAVA_HOME/bin/jar"`.

**Falar:**
- Dentro do JAR estão só as 6 classes públicas, mais `META-INF/` com README, LICENSE e CHANGELOG.
- O manifesto traz `Implementation-Version: 1.0.0` e `Automatic-Module-Name: br.com.smartscale.core`.
- Quem recebe o JAR recebe também a documentação, sem precisar do repositório.

## 6. O produto consome o componente (1 min 30)

**Mostrar:** a dependência em `backend/pom.xml`:

```xml
<dependency>
  <groupId>br.com.smartscale</groupId>
  <artifactId>smartscale-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

```bash
cd ../backend
mvn test
```

**Falar:**
- O backend não tem uma linha do motor no próprio código: ele baixa o componente do repositório local, como faria com qualquer biblioteca.
- Os 91 testes do produto rodam contra o JAR 1.0.0.

Opcional: `unzip -l target/milscale-backend-0.1.0.jar | grep smartscale` mostra o núcleo empacotado dentro do executável do produto, em `BOOT-INF/lib/`.

## 7. A variabilidade em ação (1 min 30)

Suba o sistema com uma variante do critério da fila, gere uma escala e repita com outra.

```bash
MILSCALE_CRITERIO_ORDENACAO=maior-folga mvn spring-boot:run
# no navegador (com o frontend rodando): Escala do mês → gerar 3 dias → anotar o Graduado de Dia
# Ctrl+C e:
MILSCALE_CRITERIO_ORDENACAO=mais-moderno mvn spring-boot:run
# gerar o mesmo período de novo → o Graduado de Dia passa a ser o de menor posto
```

A segunda geração substitui a primeira no mesmo período, então anote o resultado antes de trocar. A tabela com o resultado já medido está em [`VARIABILIDADE.md`](VARIABILIDADE.md).

**Falar:**
- O mesmo componente atende produtos com regras diferentes.
- A escolha da variante é feita na configuração da aplicação (engenharia de aplicação), sem recompilar o núcleo.

## 8. Evolução: como sairia a versão 1.1.0 (30 s)

**Mostrar:** `smartscale-core/CHANGELOG.md`.

**Falar:**
- Um critério novo dentro do núcleo seria compatível com quem já usa, então a versão seria 1.1.0 (SemVer, MINOR):
  1. registrar a mudança no CHANGELOG;
  2. trocar `<version>` no `pom.xml` do núcleo;
  3. rodar `mvn install`.
- O MilScale só passa a usar a 1.1.0 quando trocar a versão na dependência. Até lá, continua na 1.0.0 sem ser afetado.
- Mudar a assinatura de `preencherVagas` quebraria quem usa, então exigiria a 2.0.0.

## 9. Marcar a versão no repositório (30 s)

```bash
cd ..
git tag -a smartscale-core-v1.0.0 -m "smartscale-core 1.0.0"
git push origin smartscale-core-v1.0.0
git tag
```

**Falar:** a tag liga a versão publicada do componente ao commit exato que a gerou.

---

## Checklist depois de gravar

- [ ] O vídeo mostra: repositório, `pom.xml` do núcleo, `mvn clean install` verde, conteúdo do JAR, consumo pelo backend, variabilidade e tag.
- [ ] A tag `smartscale-core-v1.0.0` foi enviada ao GitHub.
- [ ] A equipe declarou o uso de IA conforme o padrão PUCPR (Resolução 274/2024 CONSUN).
- [ ] Todos estudaram `docs/PADROES_DE_PROJETO.md` para a prova de autoria.
