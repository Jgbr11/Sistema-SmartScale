# Variabilidade planejada — linha de produto SmartScale

A SmartScale é uma linha de produto para escalas de rodízio. O **núcleo** (`smartscale-core`) é comum a todos os produtos. Cada **produto** (o MilScale é o primeiro) especializa os contratos do núcleo e escolhe as variantes de que precisa.

O exemplo principal de variabilidade é o **critério de ordenação da fila (ponto de variação RN01)**, escolhido por configuração, sem mudar código.

## Modelo de features

```
SmartScale
├── Núcleo  [obrigatória]
│   └── MotorDeRodizio, PessoaEscalada, TipoTurno, CriterioDeOrdenacao, CatalogoDeCriterios
│
├── Critério de ordenação da fila  [alternativa — escolher exatamente 1]   ← ponto de variação RN01
│   ├── maior-folga    quem está há mais dias sem servir         (padrão do MilScale)
│   ├── menor-carga    quem tirou menos serviços no período      (ex.: escala hospitalar)
│   └── mais-moderno   menor posto/cargo primeiro                (tradição militar)
│
├── Estrutura da organização  [obrigatória — ponto de adaptação]
│   ├── Cargos/postos com nível hierárquico      (MilScale: Postos e graduações)
│   └── Unidades organizacionais                  (MilScale: Subunidades)
│
├── Elegibilidade  [obrigatória]
│   └── posto + subunidade + curso exigido, com exclusões
│
├── Descanso mínimo entre turnos  [obrigatória, parametrizável por tipo de turno]
│
├── Trocas de turno  [opcional]
│   ├── Substituição
│   └── Troca mútua
│
├── Afastamentos  [opcional]
│   └── reconciliação automática da escala já gerada
│
├── Comunicação  [opcional]
│   ├── Notificações no sistema
│   └── Lembrete por e-mail             (ligado por MILSCALE_EMAIL_HABILITADO)
│
└── Relatórios CSV  [opcional]
    ├── Escala do dia
    ├── Serviços da pessoa
    └── Afastamentos do mês
```

| Tipo de feature | Significado |
|---|---|
| Obrigatória | todo produto da linha tem |
| Alternativa | o produto escolhe exatamente uma variante |
| Opcional | o produto pode ter ou não |
| Ponto de adaptação | a feature existe sempre, mas o conteúdo é cadastrado por cada organização |

## Como a variante é escolhida (engenharia de aplicação)

A escolha é feita na **inicialização** do sistema (*binding time*), por uma propriedade:

```properties
# backend/src/main/resources/application.properties
milscale.lps.criterio-ordenacao=${MILSCALE_CRITERIO_ORDENACAO:maior-folga}
```

```java
// backend/src/main/java/br/com/milscale/milscale/adapters/config/VariabilidadeConfig.java
@Bean
public CriterioDeOrdenacao<MilitarEmGeracao> criterioDeOrdenacao(
        @Value("${milscale.lps.criterio-ordenacao:maior-folga}") String nome) {
    CatalogoDeCriterios catalogo = CatalogoDeCriterios.instancia();
    catalogo.registrar("maior-folga", new CriterioOrdenacaoMilitar<MilitarEmGeracao>());
    catalogo.registrar("menor-carga", new CriterioMenorCargaNaGeracao());
    catalogo.registrar("mais-moderno", new CriterioMaisModernoPrimeiro());
    return catalogo.obter(nome);
}
```

1. A `VariabilidadeConfig` registra as três variantes no `CatalogoDeCriterios`, que é um Singleton.
2. Ela pega a variante cujo nome veio da configuração.
3. O Spring injeta essa variante no `GerarEscalaService`, que a repassa ao `MotorDeRodizio`.
4. Um nome inválido impede o sistema de subir, com a mensagem `Critério de ordenação desconhecido: <nome>. Opções: [...]`. O erro aparece na hora, e não no meio de uma geração de escala.

**Onde configurar:**

| Ambiente | Como |
|---|---|
| Local | `export MILSCALE_CRITERIO_ORDENACAO=mais-moderno` antes do `mvn spring-boot:run` |
| Docker Compose | `MILSCALE_CRITERIO_ORDENACAO=mais-moderno` no `.env` |
| Linha de comando | `java -jar app.jar --milscale.lps.criterio-ordenacao=menor-carga` |

## Demonstração: a mesma escala com duas variantes

Duas instâncias do sistema, com banco em memória e os mesmos dados iniciais, geraram a escala de 02 a 04/11/2026. Resultado do primeiro dia:

| Serviço | `maior-folga` | `mais-moderno` |
|---|---|---|
| Oficial de Dia | Ten Ribas | Ten Ribas |
| Graduado de Dia | **2 Sgt Zeni** | **3 Sgt Lima** |
| Comandante da Guarda | **3 Sgt Lima** | **3 Sgt Sato** |
| Cozinheiro de Dia | **Cb Fagundes** | **Sd EP Cauan** |
| Cabo da Guarda | Cb Menezes | Cb Menezes |

Onde mais de um posto pode tirar o serviço, `mais-moderno` escala o de menor posto. Onde só um posto é elegível, como Oficial de Dia e Cabo da Guarda, o resultado é o mesmo.

O teste automático `VariabilidadeIntegrationTest` confirma que a propriedade troca o critério injetado:

```java
@SpringBootTest(properties = "milscale.lps.criterio-ordenacao=menor-carga")
class VariabilidadeIntegrationTest {
    @Autowired private CriterioDeOrdenacao<MilitarEmGeracao> criterio;

    @Test
    void oCriterioDaFilaVemDaConfiguracao() {
        assertThat(criterio).isInstanceOf(CriterioMenorCargaNaGeracao.class);
    }
}
```

## Como um novo produto da linha reusa o núcleo

Exemplo: uma escala de plantões hospitalares.

1. **Adiciona a dependência:**
   ```xml
   <dependency>
     <groupId>br.com.smartscale</groupId>
     <artifactId>smartscale-core</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```
2. **Implementa os contratos** com o próprio vocabulário: `Enfermeiro implements PessoaEscalada` e `Plantao implements TipoTurno`.
3. **Escolhe ou cria um critério:** registra um `CriterioDeOrdenacao<Enfermeiro>` no `CatalogoDeCriterios`.
4. **Chama o motor:** `new MotorDeRodizio<Enfermeiro, Plantao>().preencherVagas(pool, plantao, criterio)`.

Nenhuma linha do núcleo muda. O teste `MotorDeRodizioTest.outroProdutoDaLinha_usaOutroCriterioSemMudarOMotor`, no `smartscale-core`, simula exatamente isso.

## O que é núcleo e o que é produto

| Camada | Pacote | Reuso |
|---|---|---|
| Núcleo | `br.com.smartscale.core` (módulo `smartscale-core`) | igual em todos os produtos; sem Spring, sem JPA |
| Especialização | `br.com.milscale.milscale.domain` | específica do MilScale: `Militar`, `TipoServico`, `CriterioOrdenacaoMilitar` |
| Aplicação e adaptadores | `br.com.milscale.milscale.application`, `adapters` | específicos do MilScale, mas seguem o mesmo desenho em qualquer produto |
