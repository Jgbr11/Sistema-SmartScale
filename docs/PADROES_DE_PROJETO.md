# Padrões de projeto aplicados ao reuso

O MilScale atende à **Opção 01** do trabalho "Componentes de Reuso — Parte 02" com 8 exemplos codificados à mão:

- Singleton ×2;
- Template Method ×3;
- Strategy ×3.

Os três padrões não ficam isolados: o Singleton guarda as Strategies, a configuração escolhe uma delas, e o Template Method usa o outro Singleton no rodapé dos relatórios.

| Padrão | Exemplos | Onde | Teste |
|---|---|---|---|
| Strategy | `CriterioOrdenacaoMilitar`, `CriterioMenorCargaNaGeracao`, `CriterioMaisModernoPrimeiro` | `backend/.../domain` e `backend/.../application` | `CriteriosDeOrdenacaoTest` |
| Singleton | `CatalogoDeCriterios`, `IdentidadeDaOrganizacao` | `smartscale-core` e `backend/.../domain` | `CatalogoDeCriteriosTest`, `IdentidadeDaOrganizacaoTest` |
| Template Method | `RelatorioEscalaDoDia`, `RelatorioServicosDoMilitar`, `RelatorioAfastamentosDoMes` | `backend/.../application/relatorios` | `RelatoriosCsvTest` |

---

## Classes principais do sistema

O PDF pede ao menos 10 classes principais com atributos e métodos. O sistema tem 24 classes de domínio, 6 classes no núcleo e 24 serviços. As principais:

| Classe | Papel | Atributos principais | Métodos principais |
|---|---|---|---|
| `MotorDeRodizio` (núcleo) | Algoritmo de rodízio, reutilizável por qualquer produto | — | `preencherVagas(pool, turno, criterio)` |
| `Militar` | Pessoa escalável (implementa `PessoaEscalada`) | `nomeGuerra`, `cpf`, `posto`, `subunidade`, `qualificacoes`, `situacao`, `dataUltimoServico` | `getNomeExibicao()`, `getContadorRodizio()` |
| `TipoServico` | Função de serviço (implementa `TipoTurno`) | `nome`, `efetivoNecessario`, `horaInicio`, `duracaoHoras`, `ativo`, `requisitos` | os do contrato `TipoTurno` |
| `Escala` | Período gerado | `descricao`, `dataInicio`, `dataFim`, `situacao`, `servicos`, `usuarioGeracao` | — |
| `ServicoEscalado` | Uma vaga num dia | `data`, `tipoServico`, `militar`, `situacao`, `travado`, `observacao` | `isJaComecou()` |
| `Solicitacao` | Pedido de troca de serviço | `servicoOrigem`, `solicitante`, `substituto`, `tipoTroca`, `situacao`, `justificativa` | — |
| `Afastamento` | Missão, dispensa, férias | `militar`, `tipo`, `descricao`, `dataInicio`, `dataFim`, `loteMissao` | `cobre(dia)` |
| `RegraEscala` | Regras por tipo de serviço | `intervaloMinimo`, `diasFolga`, `maxServicosMes`, `pesoFimSemana`, `pesoFeriado` | — |
| `RequisitoServico` | Quem pode tirar um serviço | `posto`, `subunidade`, `qualificacao`, `qualificacoesExcluidas`, `subunidadeExcluida` | — |
| `PoliticaDeDescanso` | Regra de intervalo entre serviços | `INTERVALO_MINIMO_PADRAO`, `DISTANCIA_MINIMA_EM_TROCA` | `respeitaIntervalo()`, `ficariaEm1x1()`, `intervaloMinimo()` |
| `GerarEscalaService` | Caso de uso: gerar a escala | repositórios, `motor`, `criterio` | `gerar(dataInicio, dataFim, usuarioGeracao)` |
| `SolicitacaoService` | Caso de uso: trocas de serviço | repositórios, notificações | `criar()`, `criarTrocaMutua()`, `confirmarSubstituto()`, `triagem()`, `autorizar()`, `cancelar()` |
| `AfastamentoService` | Caso de uso: afastamentos | repositórios, elegibilidade | `cadastrarMissao()`, `atualizar()`, `cancelar()` |

---

## Strategy — critério de ordenação da fila (3 exemplos)

**O problema:** cada organização decide de um jeito quem tira o próximo serviço. O MilScale escolhe quem está há mais tempo sem servir, um hospital poderia escolher quem fez menos plantões e outra unidade pode preferir o mais moderno. O algoritmo de rodízio, porém, é o mesmo para todos, e não pode ser reescrito a cada produto.

**A solução:** a regra de ordenação vira um objeto separado, que o motor recebe pronto. O contrato fica no núcleo:

```java
// smartscale-core/src/main/java/br/com/smartscale/core/CriterioDeOrdenacao.java
@FunctionalInterface
public interface CriterioDeOrdenacao<T extends PessoaEscalada> {
    Comparator<T> comparator();
}
```

O motor usa o critério sem saber qual é:

```java
// smartscale-core/src/main/java/br/com/smartscale/core/MotorDeRodizio.java
public List<P> preencherVagas(List<P> elegiveisDisponiveis, T turno, CriterioDeOrdenacao<P> criterio) {
    List<P> fila = new ArrayList<>(elegiveisDisponiveis);
    fila.sort(criterio.comparator());
    ...
}
```

As três estratégias concretas:

| Estratégia | Quem vai primeiro | Desempate | Arquivo |
|---|---|---|---|
| `CriterioOrdenacaoMilitar` | mais dias sem servir | — | `backend/.../domain/CriterioOrdenacaoMilitar.java` |
| `CriterioMenorCargaNaGeracao` | menos serviços no período sendo gerado | mais dias sem servir | `backend/.../application/CriterioMenorCargaNaGeracao.java` |
| `CriterioMaisModernoPrimeiro` | menor nível hierárquico | mais dias sem servir | `backend/.../application/CriterioMaisModernoPrimeiro.java` |

```java
// backend/src/main/java/br/com/milscale/milscale/application/CriterioMenorCargaNaGeracao.java
public class CriterioMenorCargaNaGeracao implements CriterioDeOrdenacao<MilitarEmGeracao> {

    @Override
    public Comparator<MilitarEmGeracao> comparator() {
        return Comparator.comparingInt(MilitarEmGeracao::getServicosNaGeracao)
                .thenComparing(Comparator.comparingLong(MilitarEmGeracao::getContadorRodizio).reversed());
    }
}
```

**Sem o padrão:** o motor teria um `if/switch` com os três critérios dentro dele. Cada critério novo exigiria alterar o núcleo e publicar uma versão nova do JAR para todos os produtos da linha.

---

## Singleton — catálogo de critérios e identidade da organização (2 exemplos)

Os dois exemplos usam formas diferentes de Singleton, de propósito.

### 1. `CatalogoDeCriterios`: Singleton clássico com *holder* preguiçoso

**O problema:** as três estratégias precisam ficar registradas num lugar único, que o sistema inteiro consulta pelo nome (`"maior-folga"`, `"menor-carga"`, `"mais-moderno"`). Duas cópias desse registro poderiam divergir.

```java
// smartscale-core/src/main/java/br/com/smartscale/core/CatalogoDeCriterios.java
public final class CatalogoDeCriterios {

    private final Map<String, CriterioDeOrdenacao<?>> criterios = new ConcurrentHashMap<>();

    private CatalogoDeCriterios() {
    }

    private static final class Portador {
        private static final CatalogoDeCriterios INSTANCIA = new CatalogoDeCriterios();
    }

    public static CatalogoDeCriterios instancia() {
        return Portador.INSTANCIA;
    }
    ...
}
```

Como funciona:

- **Construtor `private`:** ninguém fora da classe consegue dar `new`.
- **Classe interna `Portador`:** a JVM só a carrega na primeira chamada de `instancia()`, e a carga de classe é *thread-safe* por definição. Assim a instância é criada uma única vez, de forma preguiçosa (*lazy*), sem `synchronized` nem *double-checked locking*.
- **`final` na classe:** impede subclasses que poderiam criar outra instância.
- **`ConcurrentHashMap`:** o registro também é seguro entre threads.

### 2. `IdentidadeDaOrganizacao`: Singleton por `enum`

**O problema:** o nome da OM aparecia escrito direto em vários lugares: e-mail de lembrete, PDF da escala, carteira do militar. Agora existe uma única fonte para ele.

```java
// backend/src/main/java/br/com/milscale/milscale/domain/IdentidadeDaOrganizacao.java
public enum IdentidadeDaOrganizacao {
    INSTANCIA;

    public String nome() { return "5º Batalhão de Suprimento"; }
    public String sigla() { return "5º B Sup"; }
    public String sistema() { return "MilScale"; }
    public String assinatura() { return sistema() + ", " + nome(); }
}
```

**Por que `enum`:** é a forma que Joshua Bloch recomenda em *Effective Java*. A JVM garante uma instância só, e ela resiste a dois ataques que quebram o Singleton clássico:

- **reflexão:** não dá para chamar o construtor de um enum com `setAccessible`;
- **serialização:** desserializar não cria cópia.

**Onde é usada:**
- `LembreteServicoScheduler`, na assinatura do e-mail;
- `RelatorioCsv`, no rodapé;
- `OrganizacaoController`, em `GET /api/organizacao`. O frontend usa esse endpoint no login, no PDF da escala e na carteira do militar.

**Diferença entre os dois:**

| | `CatalogoDeCriterios` | `IdentidadeDaOrganizacao` |
|---|---|---|
| Forma | classe com *holder* | `enum` |
| Criação | preguiçosa, no primeiro uso | quando a classe carrega |
| Estado | mutável (registro de critérios) | imutável |
| Onde mora | núcleo (reutilizável) | produto MilScale |

---

## Template Method — relatórios CSV (3 exemplos)

**O problema:** os três relatórios CSV seguem o mesmo roteiro e as mesmas regras:

- **Roteiro:** cabeçalho, uma linha por item, rodapé.
- **Formato:** separador `;`, aspas quando o texto contém `;`, marcador (BOM) para o Excel abrir os acentos certos.
- **Segurança:** proteção contra injeção de fórmula.

Repetir isso três vezes faria as cópias divergirem com o tempo.

**A solução:** a classe abstrata fixa o algoritmo num método `final`, e as subclasses só preenchem os passos.

```java
// backend/src/main/java/br/com/milscale/milscale/application/relatorios/RelatorioCsv.java
public abstract class RelatorioCsv<T> {

    public final String gerar() {
        StringBuilder csv = new StringBuilder(BOM_PARA_EXCEL);
        csv.append(linha(cabecalho())).append(QUEBRA);
        for (T item : itens()) {
            csv.append(linha(colunas(item))).append(QUEBRA);
        }
        csv.append(QUEBRA).append(escapar(rodape())).append(QUEBRA);
        return csv.toString();
    }

    public abstract String nomeDoArquivo();
    protected abstract List<String> cabecalho();
    protected abstract List<T> itens();
    protected abstract List<String> colunas(T item);

    protected String rodape() {
        return "Gerado por " + IdentidadeDaOrganizacao.INSTANCIA.assinatura();
    }
    ...
}
```

- **`gerar()` é o método-modelo:** é `final`, então nenhuma subclasse muda a ordem dos passos nem as regras de escape.
- **`cabecalho()`, `itens()` e `colunas()` são passos abstratos:** toda subclasse precisa implementá-los.
- **`rodape()` é um gancho (*hook*):** tem uma implementação padrão, e a subclasse pode ou não sobrescrever.

| Relatório | Itens | Personalização |
|---|---|---|
| `RelatorioEscalaDoDia` | serviços de um dia | vaga sem militar sai como "VAGA EM ABERTO" |
| `RelatorioServicosDoMilitar` | histórico de um militar | sobrescreve o gancho `rodape()` com o nome do militar |
| `RelatorioAfastamentosDoMes` | afastamentos que tocam o mês | — |

```java
// backend/src/main/java/br/com/milscale/milscale/application/relatorios/RelatorioServicosDoMilitar.java
@Override
protected String rodape() {
    return "Serviços de " + militar.getNomeExibicao() + " — " + super.rodape();
}
```

**Na tela:** o botão "Baixar CSV" aparece em Escala do dia, Escala do mês, Ficha do militar e Missões e dispensas, só para os perfis da sargenteação. Os endpoints ficam em `/api/relatorios/*`.

---

## Como os três padrões se combinam

```
 application.properties: milscale.lps.criterio-ordenacao=menor-carga      ← variabilidade
              │
 VariabilidadeConfig ── registra as 3 Strategies ──▶ CatalogoDeCriterios (Singleton)
              │                                              │ obter("menor-carga")
              ▼                                              ▼
 GerarEscalaService ── recebe o critério escolhido ──▶ MotorDeRodizio.preencherVagas(...)

 RelatorioCsv.gerar()  (Template Method)
   ├─ cabecalho() ┐
   ├─ itens()     ├─ cada relatório implementa
   ├─ colunas(x)  ┘
   └─ rodape() ──▶ IdentidadeDaOrganizacao.INSTANCIA (Singleton)
```

## Como rodar os testes de cada padrão

```bash
export JAVA_HOME="/c/Program Files/Java/jdk-23"
mvn install                                                   # na raiz: tudo
cd backend && mvn test -Dtest=CriteriosDeOrdenacaoTest        # Strategy
cd backend && mvn test -Dtest=IdentidadeDaOrganizacaoTest     # Singleton (enum)
cd smartscale-core && mvn test -Dtest=CatalogoDeCriteriosTest # Singleton (holder)
cd backend && mvn test -Dtest=RelatoriosCsvTest               # Template Method
cd backend && mvn test -Dtest=VariabilidadeIntegrationTest    # os três juntos, via configuração
```

## Perguntas prováveis na prova de autoria

1. **Por que o critério de ordenação é uma interface e não um `if` dentro do motor?**
   Para o motor do núcleo nunca mudar. Um critério novo é só uma classe nova, e o JAR do núcleo não precisa de versão nova.
2. **Por que o `CatalogoDeCriterios` usa uma classe interna em vez de `if (instancia == null)`?**
   O `if` sem sincronização pode criar duas instâncias se duas threads chegarem juntas. Na classe interna, quem garante que a criação acontece uma vez só é a própria JVM, ao carregar a classe.
3. **Qual a vantagem do Singleton por `enum`?**
   A JVM garante uma instância só, mesmo com reflexão e serialização, e o código fica mais curto.
4. **Por que `gerar()` é `final`?**
   Para nenhuma subclasse alterar a ordem dos passos nem pular o escape do CSV. Isso é o que caracteriza o Template Method.
5. **Qual a diferença entre passo abstrato e gancho?**
   Passo abstrato é obrigatório: `cabecalho()`, `itens()`, `colunas()`. Gancho tem um padrão e é opcional: `rodape()`.
6. **Onde a variabilidade da linha de produto aparece?**
   Na escolha do critério por configuração (`MILSCALE_CRITERIO_ORDENACAO`), detalhada em [`VARIABILIDADE.md`](VARIABILIDADE.md).
