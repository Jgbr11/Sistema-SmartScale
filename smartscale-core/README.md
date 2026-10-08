# SmartScale Core

Núcleo reutilizável da linha de produto **SmartScale**, para escalas por rodízio. O componente distribui pessoas em turnos de forma justa e não sabe nada do domínio de quem o usa: militar, enfermeiro ou vigilante. Cada produto da linha especializa os contratos e escolhe o critério de ordenação da fila.

- **Coordenadas Maven:** `br.com.smartscale:smartscale-core:1.0.0`
- **Requisitos:** Java 21 ou mais novo
- **Dependências em tempo de execução:** nenhuma (sem Spring, sem JPA)
- **Licença:** MIT (ver [LICENSE](LICENSE))
- **Histórico de versões:** [CHANGELOG.md](CHANGELOG.md)

## Instalação

O componente ainda não está publicado num repositório remoto. Instale-o no repositório Maven local:

```bash
cd smartscale-core
mvn install
```

O comando gera e instala em `~/.m2/repository/br/com/smartscale/smartscale-core/1.0.0/`:

- `smartscale-core-1.0.0.jar`: as classes, com este README, a licença e o changelog em `META-INF/`;
- `smartscale-core-1.0.0-sources.jar`: o código-fonte, para a IDE mostrar ao navegar;
- `smartscale-core-1.0.0.pom`: as coordenadas e a licença.

Depois, adicione a dependência ao projeto que vai usar o componente:

```xml
<dependency>
  <groupId>br.com.smartscale</groupId>
  <artifactId>smartscale-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

## API pública

| Tipo | O que é | Métodos |
|---|---|---|
| `PessoaEscalada` | interface: quem entra na fila | `getId()`, `getNomeExibicao()`, `getSituacao()`, `getContadorRodizio()` |
| `TipoTurno` | interface: um turno com N vagas | `getId()`, `getNome()`, `getEfetivoNecessario()`, `isAtivo()` |
| `CriterioDeOrdenacao<P>` | interface: como ordenar a fila (Strategy) | `comparator()` |
| `MotorDeRodizio<P, T>` | classe: o algoritmo de rodízio | `preencherVagas(pool, turno, criterio)` e `preencherVagas(pool, filtroExtra, turno, criterio)` |
| `CatalogoDeCriterios` | Singleton: registro de critérios por nome | `instancia()`, `registrar(nome, criterio)`, `obter(nome)`, `nomes()` |
| `SituacaoPessoa` | enum | `ATIVO`, `AFASTADO`, `DESLIGADO` |

**O que o motor faz:** ordena o pool com o critério recebido e devolve as primeiras `turno.getEfetivoNecessario()` pessoas.

**O que o motor não faz:** filtrar o pool. Elegibilidade (cargo, curso, unidade) e disponibilidade (afastamento, descanso, um turno por dia) ficam com o produto, que entrega o pool já filtrado. Se houver menos pessoas que vagas, o motor devolve quem tem, e quem chamou decide o que fazer com a vaga aberta.

## Exemplo de uso

```java
record Enfermeiro(Long id, String nome, long diasSemPlantao) implements PessoaEscalada {
    public Long getId() { return id; }
    public String getNomeExibicao() { return nome; }
    public SituacaoPessoa getSituacao() { return SituacaoPessoa.ATIVO; }
    public long getContadorRodizio() { return diasSemPlantao; }
}

record Plantao(String nome, int vagas) implements TipoTurno {
    public Long getId() { return 1L; }
    public String getNome() { return nome; }
    public int getEfetivoNecessario() { return vagas; }
    public boolean isAtivo() { return true; }
}

CriterioDeOrdenacao<Enfermeiro> maiorFolga =
        () -> Comparator.comparingLong(Enfermeiro::getContadorRodizio).reversed();

CatalogoDeCriterios.instancia().registrar("maior-folga", maiorFolga);

List<Enfermeiro> escalados = new MotorDeRodizio<Enfermeiro, Plantao>()
        .preencherVagas(equipe, new Plantao("Plantão noturno", 2),
                CatalogoDeCriterios.instancia().obter("maior-folga"));
```

## Ponto de variação: critério de ordenação

O critério é o que diferencia os produtos da linha. Para criar um, basta implementar `CriterioDeOrdenacao`, e o motor não muda. O MilScale, primeiro produto da linha, tem três:

| Nome | Quem vai primeiro |
|---|---|
| `maior-folga` | quem está há mais dias sem servir |
| `menor-carga` | quem tirou menos turnos no período sendo gerado |
| `mais-moderno` | o de menor cargo |

## Testes

```bash
mvn test
```

São 7 testes:
- **motor:** preenchimento de vagas, falta de gente, filtro extra e reuso com outro critério;
- **catálogo:** instância única, registro por nome e erro para nome desconhecido.

## Versionamento

O componente segue o [SemVer](https://semver.org/lang/pt-BR/):

| Versão | Quando |
|---|---|
| PATCH (1.0.x) | correção sem mudar a API |
| MINOR (1.x.0) | algo novo na API, compatível com quem já usa |
| MAJOR (x.0.0) | mudança que quebra quem já usa, como trocar a assinatura de `preencherVagas` |
