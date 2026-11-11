# Plano 6 — Modelo de características, níveis de reuso (MPS.BR) e Database First (SmartScale)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar as três atividades seguintes da disciplina Desenvolvimento Orientado a Reuso de Software (PUCPR, prof. Tiago Navarro):
1. **Modelo de características:**
   - o modelo completo da linha de produto no FeatureIDE, com a imagem exportada;
   - pelo menos 10 restrições usando Implies, And, Or e Not;
   - justificativas para Abstract, Concrete, Optional, Mandatory, Or Group e Alternative Group;
   - entrega em Word/PDF.
2. **Níveis de reuso no MPS.BR 2016:**
   - classificação dos resultados GRU 1–5 (p. 36) e DRU 1–9 (p. 42) em Verde, Amarelo ou Vermelho, com justificativa;
   - proposta de adequação para GRU 1–5 e DRU 6–9;
   - entrega em Word/PDF.
3. **Database First:**
   - gerar classes, CRUD e telas a partir do banco com uma ferramenta;
   - um integrante demonstra ao vivo.

**Architecture:**
- **Modelo de características:** é um projeto FeatureIDE versionado em `docs/modelo-de-caracteristicas/SmartScale/`. Um validador em Python (resolvedor SAT) garante, em teste automatizado, o que a entrega exige: modelo sem feature morta, sem feature falsamente opcional e sem restrição redundante.
- **Documentos de entrega:** os dois documentos são escritos em Markdown em `docs/`, como os demais documentos do projeto. Um gerador Node (`marked` + `docx`) converte cada um em `.docx`, pintando as células Verde/Amarelo/Vermelho, e o Word exporta o PDF.
- **Database First:** é um projeto Maven separado, `database-first/`, fora do agregador da raiz:
  - o Hibernate Tools faz a engenharia reversa da cópia do banco do MilScale (entidades JPA);
  - com 5 templates FreeMarker próprios, o mesmo Hibernate Tools gera, para cada tabela, o repositório, o controller com o CRUD e as telas de lista e de cadastro (Thymeleaf).

**Tech Stack:** FeatureIDE 3.12 (Eclipse 2026-09), Python 3.12 + `python-sat`, Node 24 + `docx` 9 + `marked` 14, Microsoft Word (exportação PDF), Spring Boot 3.3.4, Hibernate Tools 6.5.3.Final, MySQL 8.0 (Docker), Thymeleaf.

## Global Constraints

- **Quem commita é o usuário, tarefa a tarefa.** Cada task termina num Checkpoint: mostrar o diff, sugerir a mensagem e esperar. Push, merge e tag também são do usuário.
- **Branch:** `entrega/modelo-reuso-database-first`, criada a partir da `main` atualizada.
- **O código do MilScale não muda.** Nada em `backend/`, `frontend/` ou `smartscale-core/` é alterado. Migrations do Flyway são imutáveis.
- **O `database-first/` não entra no `pom.xml` da raiz.** Ele precisa do banco no ar para compilar e não pode quebrar o `mvn install` da raiz.
- **Nunca escrever no banco `milscale`.** O Database First usa a cópia `milscale_dbfirst`, recriada pelo `preparar-banco.sh`.
- **Senhas:** nenhum arquivo versionado contém senha. O `banco.properties` é gerado a partir do `.env` e fica no `.gitignore`.
- **JDK:** o `JAVA_HOME` da máquina é o JDK 11. Rodar o Maven com `export JAVA_HOME="/c/Program Files/Java/jdk-23"`.
- **Verificação geral ao fim de cada task:**
  - `mvn -q install` na raiz continua verde;
  - os testes da própria task passam.
- **Regra da disciplina sobre IA:** o conteúdo passa por prova de autoria, então a equipe precisa ler os documentos antes da apresentação. O uso de IA é declarado no padrão PUCPR (Resolução 274/2024 CONSUN).
- **Texto dos documentos:** português claro, sem jargão interno. Todo nome de classe citado precisa existir no código. As Tasks 2 e 3 conferem isso com `grep`.

## Registro de execução

| Ordem | Task | Status | Commit | Resumo |
|---|---|---|---|---|
| 1 | 0 — Preparação | ✅ Concluída | `511db04` | Branch `entrega/modelo-reuso-database-first`. Linha de base verde após `mvn clean install` (core 7, backend 160); o primeiro `mvn install` falhou por classes antigas em `target/`, não por código. Decisões: o usuário demonstra o Database First; sem Django (Task 7 cancelada); Task 5 passa a gerar repositório, controller e telas HTML (Thymeleaf) por templates |
| 2 | 1 — Modelo no FeatureIDE e validador | ✅ Concluída | `a3fd1b4` | 73 features e 12 restrições; 13 testes verdes; validador OK; 3 configurações válidas. Falta abrir no Eclipse (Step 7, junto com a Task 2) |
| 3 | 2 — Imagem e documento do modelo | 🟡 Aguardando commit | — | Imagem exportada pelo usuário (532×1718); dividida em duas partes no corte vazio entre as linhas 888–895, para cada parte caber numa página com texto legível; o `model.xml` abriu no FeatureIDE sem avisos |
| 4 | 3 — Classificação GRU e DRU | ✅ Concluída | `07aea2d` | GRU: 4 amarelo e 1 vermelho; DRU: 4 verde, 3 amarelo e 2 vermelho; evidências conferidas |
| 5 | 4 — Gerador Word/PDF e entrega dos documentos | 🟡 Aguardando commit | `8bba54d` (gerador) | Gerador com 7 testes (limite de altura da imagem e legenda presa à figura); `01-modelo-de-caracteristicas` com 9 páginas e `02-niveis-de-reuso-gru-dru` com 6, conferidas página a página |
| 6 | 5 — Database First: gerador | ✅ Concluída | `ccec55e` | Versão intuitiva: 5 templates geram entidade, repositório, controller e telas; 57 Java + 35 telas; `verificar.sh` OK |
| 7 | 6 — Database First: documentação e roteiro | ✅ Concluída (falta o ensaio) | `ccec55e` | Falta o ensaio do usuário |
| 8 | 7 — (Opcional) Telas completas com Django | ❌ Cancelada | — | Decisão do usuário em 2026-11-11: telas geradas em Java (Task 5) |

## Como o plano cobre os enunciados

| Exigência | Onde |
|---|---|
| Modelo de características completo | Task 1: 73 features em 13 grupos de primeiro nível, cobrindo todos os módulos do MilScale |
| Imagem exportada do FeatureIDE | Task 2, Step 2 |
| ≥ 10 restrições com Implies, And, Or, Not | Task 1: 12 restrições (R01–R12), todas de dependências reais do código; o validador reprova se faltar algum operador |
| Justificativas de Abstract, Concrete, Optional, Mandatory, Or Group, Alternative Group | Task 2: `docs/MODELO_DE_CARACTERISTICAS.md`, seções 3 a 5 |
| GRU 1–5 classificados e justificados (Verde/Amarelo/Vermelho) | Task 3: `docs/NIVEIS_DE_REUSO_MPSBR.md`, seção 1.1 |
| Proposta de adequação GRU 1–5 | Task 3: seção 1.2 |
| DRU classificados e justificados | Task 3: seção 2.1 (DRU 1–9) |
| Proposta de adequação DRU 6–9 | Task 3: seção 2.2 |
| Entrega em Word/PDF | Task 4: `docs/entregas/01-…docx/.pdf` e `02-…docx/.pdf` |
| Database First: classes e código completos a partir do banco | Task 5: 21 entidades, 19 repositórios e 17 controllers gerados a cada build |
| Database First: CRUD e telas automáticos | Task 5: listar, criar, editar e excluir pelas telas geradas (17 listas, 17 formulários e a página inicial) |
| Um integrante demonstra | Task 6: `database-first/ROTEIRO_DEMONSTRACAO.md` |

## Revisão de 2026-11-11 (decisões do usuário)

- **Sem Django.** A Task 7 foi cancelada.
- **Código gerado o mais intuitivo possível.** A Task 5 deixa de usar Spring Data REST e HAL Explorer. Para cada tabela, o gerador cria:
  - `<Tabela>.java` (entidade);
  - `<Tabela>Repository.java`;
  - `<Tabela>Controller.java`, com `listar`, `formulario`, `salvar` e `excluir`;
  - as telas `<tabela>/lista.html` e `<tabela>/formulario.html` (Thymeleaf);
  - além de uma página inicial com links para todas as tabelas.
- **Como:** templates FreeMarker aplicados pelo `hbmtemplate` do Hibernate Tools (Ant, via `maven-antrun-plugin`). As datas passam a `LocalDate` e `LocalDateTime`.
- **Onde está o código:** o código final da Task 5 e o texto da Task 6 substituem os blocos originais quando forem provados. A Task 5 registra o resultado.
- **Apresentação:** quem demonstra o Database First é o usuário.

## O que já foi provado antes de escrever este plano (2026-11-10)

As peças deste plano foram executadas numa cópia temporária, fora do repositório, antes de virar plano:
- **Modelo:** `model.xml` com 73 features e 12 restrições. O validador deu OK, os 13 testes passaram e as 3 configurações de produto foram validadas.
- **Gerador de documentos:**
  - os 5 testes passaram;
  - os dois documentos viraram `.docx`, e o Word exportou os PDFs (7 e 6 páginas);
  - as páginas foram conferidas como imagem.
- **Database First:**
  - o `preparar-banco.sh` copiou as 20 tabelas;
  - o `mvn spring-boot:run` gerou 40 arquivos e subiu a aplicação;
  - o `verificar.sh` confirmou 19 recursos, com POST, PATCH e DELETE funcionando e a tela respondendo.
- **Django:** gerou 19 modelos, e as 17 telas de listagem e cadastro responderam 200.

Os blocos de código abaixo são os arquivos exatamente como foram testados.

## Estrutura de arquivos

```
.gitignore                                         (modificar: venvs, node_modules do gerador, banco.properties)
docs/
  MODELO_DE_CARACTERISTICAS.md                     (criar — Task 2)
  NIVEIS_DE_REUSO_MPSBR.md                         (criar — Task 3)
  DATABASE_FIRST.md                                (criar — Task 6)
  modelo-de-caracteristicas/
    requirements.txt                               (criar — Task 1)
    validar_modelo.py                              (criar — Task 1)
    test_validar_modelo.py                         (criar — Task 1)
    gerar_configuracoes.py                         (criar — Task 1)
    SmartScale-modelo.png                          (criar — Task 2, exportada do FeatureIDE)
    SmartScale/
      .project                                     (criar — Task 1, projeto FeatureIDE)
      model.xml                                    (criar — Task 1)
      configs/MilScale-Docker.xml                  (gerado — Task 1)
      configs/MilScale-Desenvolvimento.xml         (gerado — Task 1)
      configs/PlantaoHospitalar.xml                (gerado — Task 1)
  entregas/
    gerador/package.json                           (criar — Task 4)
    gerador/gerar-documentos.mjs                   (criar — Task 4)
    gerador/gerar-documentos.test.mjs              (criar — Task 4)
    gerador/exportar-pdf.ps1                       (criar — Task 4)
    01-modelo-de-caracteristicas.docx/.pdf         (gerado — Task 4)
    02-niveis-de-reuso-gru-dru.docx/.pdf           (gerado — Task 4)
  PLANO DE EXECUÇÃO/plans/README.md                (modificar — Task 0 e fechamento)
database-first/
  pom.xml, hibernate.reveng.xml                    (criar — Task 5)
  templates/dao/daohome.ftl                        (criar — Task 5)
  src/main/java/br/com/smartscale/dbfirst/DatabaseFirstApplication.java   (criar — Task 5)
  src/main/resources/application.properties       (criar — Task 5)
  preparar-banco.sh, verificar.sh                  (criar — Task 5)
  ROTEIRO_DEMONSTRACAO.md                          (criar — Task 6)
  django-admin/…                                   (criar — Task 7, opcional)
```

---

### Task 0: Preparação

**Files:**
- Modify: `docs/PLANO DE EXECUÇÃO/plans/README.md`

- [ ] **Step 1: Branch e linha de base**

```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
git checkout main && git pull
git checkout -b entrega/modelo-reuso-database-first
export JAVA_HOME="/c/Program Files/Java/jdk-23"
mvn -q install && echo "raiz verde"
```

Expected: `raiz verde`.

- [ ] **Step 2: Confirmar ferramentas**

```bash
ls /c/Users/jgcre/.p2/pool/plugins | grep -c "de.ovgu.featureide.fm.core_3.12"
ls "/c/Program Files/Microsoft Office/root/Office16/WINWORD.EXE"
docker compose ps --format '{{.Service}} {{.State}}'
python --version && node --version
```

Expected:
- `1`, ou seja, a FeatureIDE 3.12 está no Eclipse;
- o caminho do Word;
- Python 3.12 ou mais novo e Node 24.

Se o MySQL não aparecer como `running`, rode `docker compose up -d`. O banco precisa ter as tabelas do MilScale para as Tasks 5 a 7.

- [ ] **Step 3: Decisões com a equipe** (registrar a resposta no "Registro de execução"):
  - **Quem demonstra o Database First** na aula. Essa pessoa lê o roteiro da Task 6 e roda o passo a passo uma vez antes.
  - **Se a Task 7 (Django) entra.** Ela dá telas de cadastro de verdade, mas é outra linguagem. Sem ela, as telas são as do HAL Explorer, que são genéricas.

- [ ] **Step 4: Índice dos planos.** Em `docs/PLANO DE EXECUÇÃO/plans/README.md`:
  - **Linhas 2 a 4:** o Status passa para `✅ Concluído e na main (merge e4f0f6a)`.
  - **Linha 5:** o Status passa para `✅ Concluído e na main. Falta a tag smartscale-core-v1.0.0`.
  - **Nova linha na tabela:**

```markdown
| 6 | [Modelo de características, MPS.BR e Database First](2026-11-10-plano-6-modelo-reuso-database-first.md) | Modelo FeatureIDE com 12 restrições, classificação GRU/DRU, documentos Word/PDF, Database First com Hibernate Tools e Spring Data REST | 0–7 | 🟡 Em execução |
```

- [ ] **Step 5: `.gitignore`.** Acrescentar ao fim:

```gitignore

# ---- Plano 6 ----
**/.venv/
docs/entregas/gerador/node_modules/
database-first/banco.properties
```

- [ ] **Step 6: Checkpoint.** Mostrar o diff. Mensagem sugerida: `docs: plano 6 no indice e gitignore`.

---

### Task 1: Modelo de características no FeatureIDE e validador

**Files:**
- Create: `docs/modelo-de-caracteristicas/requirements.txt`
- Create: `docs/modelo-de-caracteristicas/validar_modelo.py`
- Create: `docs/modelo-de-caracteristicas/test_validar_modelo.py`
- Create: `docs/modelo-de-caracteristicas/gerar_configuracoes.py`
- Create: `docs/modelo-de-caracteristicas/SmartScale/.project`
- Create: `docs/modelo-de-caracteristicas/SmartScale/model.xml`
- Generated: `docs/modelo-de-caracteristicas/SmartScale/configs/*.xml`

**Interfaces:**
- **Produces** (usado pela Task 2):
  - `carregar(origem) -> Modelo`;
  - `analisar(modelo) -> dict`, com as chaves `satisfazivel`, `mortas`, `falso_opcionais` e `redundantes`;
  - `verificar_exigencias(modelo) -> list[str]`;
  - `validar_configuracao(modelo, origem) -> list[str]`;
  - na linha de comando: `python validar_modelo.py <model.xml> [configs...]`, que sai com 0 quando está tudo OK.
- **Produces** (usado pelas Tasks 2 e 3): os nomes das 73 features e os códigos R01–R12, que os documentos citam.

**Desenho do modelo** (raiz `SmartScale`, abstrata; os 13 filhos de primeiro nível):

| Feature | Tipo | Filhos |
|---|---|---|
| `Nucleo` | Mandatory, Abstract, And | `MotorDeRodizio`, `CatalogoDeCriterios`, `ContratosDoNucleo` (todas Mandatory) |
| `CriterioDeOrdenacao` | Mandatory, Abstract, **Alternative** | `MaiorFolga`, `MenorCarga`, `MaisModerno` |
| `Cadastros` | Mandatory, Abstract, And | `Pessoas`, `Cargos`, `Unidades`, `TiposDeTurno` (Mandatory); `Qualificacoes`, `Feriados` (Optional) |
| `Escala` | Mandatory, Abstract, And | `GeracaoAutomatica` (Mandatory); `Elegibilidade` (Mandatory, **Or**: `PorCargo`, `PorUnidade`, `PorQualificacao`); `RegrasDaEscala` (Mandatory, **Or**: `IntervaloMinimo`, `MaximoMensal`, `PesoFimDeSemana`, `PesoFeriado`); `Publicacao`, `TravamentoDeDias`, `ConsultaPessoal` (Optional) |
| `Trocas` | **Optional**, Abstract, And | `TiposDeTroca` (Mandatory, **Or**: `Substituicao`, `TrocaMutua`); `AprovacaoEmDuasEtapas` (Optional) |
| `Afastamentos` | Optional, Abstract, And | `TiposDeAfastamento` (Mandatory, **Or**: `Missao`, `Dispensa`, `Ferias`, `Licenca`, `Curso`, `OutroAfastamento`); `ReconciliacaoAutomatica` (Optional) |
| `Comunicacao` | Optional, Abstract, **Or** | `NotificacoesNoSistema`, `LembretePorEmail`, `BoletimInterno`, `Avisos` |
| `Relatorios` | Optional, Abstract, **Or** | `CsvEscalaDoDia`, `CsvServicosDaPessoa`, `CsvAfastamentosDoMes`, `PdfEscalaDoDia` |
| `Seguranca` | Mandatory, Abstract, And | `Autenticacao` (Mandatory); `ControleDeAcesso` (Mandatory, **Alternative**: `PerfisEPermissoes`, `AcessoUnico`); `Auditoria`, `ProtecaoForcaBruta`, `SenhaTemporaria` (Optional) |
| `Persistencia` | Mandatory, Abstract, **Alternative** | `H2`, `MySQL` |
| `Implantacao` | Mandatory, Abstract, **Alternative** | `ExecucaoLocal`, `DockerCompose` |
| `Interface` | Mandatory, Abstract, And | `PainelWeb`, `CalendarioMensal` (Mandatory); `FitaDoServico`, `LayoutResponsivo` (Optional) |

As 12 restrições e a origem de cada uma no código estão no documento da Task 2 (seção 6). Nenhuma delas pode ter uma feature obrigatória como consequente, porque isso a tornaria redundante, e o validador reprova.

- [ ] **Step 1: Ambiente Python**

`docs/modelo-de-caracteristicas/requirements.txt`:

```text
python-sat==1.9.dev21
```

```bash
cd docs/modelo-de-caracteristicas
python -m venv .venv
.venv/Scripts/python -m pip install -r requirements.txt
```

- [ ] **Step 2: Testes do validador (falham)**

`docs/modelo-de-caracteristicas/test_validar_modelo.py`:

```python
import unittest
from pathlib import Path

from validar_modelo import analisar, carregar, validar_configuracao, verificar_exigencias

AQUI = Path(__file__).parent

MINIMO = """<featureModel><struct>
<and abstract="true" mandatory="true" name="Raiz">
  <alt abstract="true" mandatory="true" name="Banco"><feature name="A"/><feature name="B"/></alt>
  <feature name="C"/>
  <feature mandatory="true" name="D"/>
</and></struct><constraints>{regras}</constraints></featureModel>"""


def modelo(regras=''):
    return carregar(MINIMO.format(regras=regras))


def regra(corpo):
    return f'<rule><description>teste</description>{corpo}</rule>'


class AnaliseTest(unittest.TestCase):
    def test_modelo_sem_restricoes_e_valido(self):
        a = analisar(modelo())
        self.assertTrue(a['satisfazivel'])
        self.assertEqual([], a['mortas'])
        self.assertEqual([], a['falso_opcionais'])

    def test_detecta_feature_morta(self):
        a = analisar(modelo(regra('<imp><var>A</var><var>B</var></imp>')))
        self.assertEqual(['A'], a['mortas'])

    def test_detecta_falso_opcional(self):
        a = analisar(modelo(regra('<imp><var>D</var><var>C</var></imp>')))
        self.assertIn('C', a['falso_opcionais'])

    def test_detecta_restricao_redundante(self):
        a = analisar(modelo(regra('<imp><var>A</var><not><var>B</var></not></imp>')))
        self.assertEqual(['teste'], a['redundantes'])

    def test_modelo_contraditorio_nao_e_satisfazivel(self):
        a = analisar(modelo(regra('<not><var>D</var></not>')))
        self.assertFalse(a['satisfazivel'])


class ExigenciasTest(unittest.TestCase):
    def test_cobra_dez_restricoes_e_os_quatro_operadores(self):
        problemas = verificar_exigencias(modelo(regra('<imp><var>C</var><var>A</var></imp>')))
        self.assertIn('são 1 restrições; a entrega pede pelo menos 10', problemas)
        self.assertIn('nenhuma restrição usa And', problemas)
        self.assertIn('nenhuma restrição usa Or', problemas)
        self.assertIn('nenhuma restrição usa Not', problemas)
        self.assertNotIn('nenhuma restrição usa Implies', problemas)

    def test_cobra_or_group(self):
        problemas = verificar_exigencias(modelo())
        self.assertIn('o modelo não tem nenhuma feature do tipo Or Group', problemas)

    def test_acusa_variavel_inexistente(self):
        problemas = verificar_exigencias(modelo(regra('<imp><var>C</var><var>Z</var></imp>')))
        self.assertIn('restrição cita feature inexistente: Z', problemas)


class ConfiguracaoTest(unittest.TestCase):
    def config(self, selecionadas):
        nomes = ['Raiz', 'Banco', 'A', 'B', 'C', 'D']
        linhas = ''.join(
            f'<feature manual="{"selected" if n in selecionadas else "unselected"}" name="{n}"/>' for n in nomes)
        return f'<configuration>{linhas}</configuration>'

    def test_configuracao_valida(self):
        self.assertEqual([], validar_configuracao(modelo(), self.config({'Raiz', 'Banco', 'A', 'D'})))

    def test_alternativa_com_dois_filhos(self):
        problemas = validar_configuracao(modelo(), self.config({'Raiz', 'Banco', 'A', 'B', 'D'}))
        self.assertIn('Banco (Alternative) precisa de exatamente um filho, tem 2', problemas)

    def test_obrigatoria_ausente_e_restricao_violada(self):
        m = modelo(regra('<imp><var>C</var><var>B</var></imp>'))
        problemas = validar_configuracao(m, self.config({'Raiz', 'Banco', 'A', 'C'}))
        self.assertIn('D é obrigatória em Raiz', problemas)
        self.assertIn('viola teste', problemas)


class ModeloRealTest(unittest.TestCase):
    def test_modelo_do_smartscale_cumpre_a_entrega(self):
        m = carregar(AQUI / 'SmartScale' / 'model.xml')
        a = analisar(m)
        self.assertEqual([], verificar_exigencias(m))
        self.assertTrue(a['satisfazivel'])
        self.assertEqual([], a['mortas'])
        self.assertEqual([], a['falso_opcionais'])
        self.assertEqual([], a['redundantes'])

    def test_configuracoes_dos_produtos_sao_validas(self):
        m = carregar(AQUI / 'SmartScale' / 'model.xml')
        for config in sorted((AQUI / 'SmartScale' / 'configs').glob('*.xml')):
            with self.subTest(config=config.name):
                self.assertEqual([], validar_configuracao(m, config))


if __name__ == '__main__':
    unittest.main()
```

Run: `.venv/Scripts/python -m unittest -v test_validar_modelo`

Expected: FAIL, com `ModuleNotFoundError: No module named 'validar_modelo'`.

- [ ] **Step 3: Validador**

`docs/modelo-de-caracteristicas/validar_modelo.py`:

```python
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

from pysat.solvers import Glucose3

GRUPOS = {'and', 'or', 'alt'}
OPERADORES_EXIGIDOS = {'imp': 'Implies', 'conj': 'And', 'disj': 'Or', 'not': 'Not'}


@dataclass
class Feature:
    nome: str
    pai: str | None
    grupo: str
    abstrata: bool
    obrigatoria: bool
    filhos: list = field(default_factory=list)


@dataclass
class Modelo:
    raiz: str
    features: dict
    restricoes: list


def _ler_expr(elemento):
    tag = elemento.tag
    filhos = [e for e in elemento if e.tag != 'description']
    if tag == 'var':
        return ('var', elemento.text.strip())
    if tag == 'not':
        return ('not', _ler_expr(filhos[0]))
    if tag in ('conj', 'disj'):
        return (tag, [_ler_expr(f) for f in filhos])
    if tag in ('imp', 'eq'):
        return (tag, _ler_expr(filhos[0]), _ler_expr(filhos[1]))
    raise ValueError(f'operador desconhecido: {tag}')


def _ler_feature(elemento, pai, features):
    nome = elemento.get('name')
    if nome in features:
        raise ValueError(f'feature repetida: {nome}')
    grupo = elemento.tag if elemento.tag in GRUPOS else 'and'
    features[nome] = Feature(nome, pai, grupo, elemento.get('abstract') == 'true', elemento.get('mandatory') == 'true')
    if pai:
        features[pai].filhos.append(nome)
    for filho in elemento:
        if filho.tag in GRUPOS or filho.tag == 'feature':
            _ler_feature(filho, nome, features)
    return nome


def carregar(origem):
    texto = Path(origem).read_text(encoding='utf-8') if Path(str(origem)).exists() else origem
    raiz_xml = ET.fromstring(texto)
    features = {}
    struct = raiz_xml.find('struct')
    raiz = _ler_feature(list(struct)[0], None, features)
    restricoes = []
    for regra in raiz_xml.find('constraints') if raiz_xml.find('constraints') is not None else []:
        descricao = regra.findtext('description', default='').strip()
        corpo = [e for e in regra if e.tag != 'description'][0]
        restricoes.append((descricao, _ler_expr(corpo)))
    return Modelo(raiz, features, restricoes)


def _variaveis(expr):
    if expr[0] == 'var':
        return {expr[1]}
    if expr[0] == 'not':
        return _variaveis(expr[1])
    if expr[0] in ('conj', 'disj'):
        return set().union(*(_variaveis(e) for e in expr[1]))
    return _variaveis(expr[1]) | _variaveis(expr[2])


def _operadores(expr):
    if expr[0] == 'var':
        return set()
    if expr[0] == 'not':
        return {'not'} | _operadores(expr[1])
    if expr[0] in ('conj', 'disj'):
        return {expr[0]}.union(*(_operadores(e) for e in expr[1]))
    return {expr[0]} | _operadores(expr[1]) | _operadores(expr[2])


class _Cnf:
    def __init__(self, nomes):
        self.ids = {n: i + 1 for i, n in enumerate(nomes)}
        self.proximo = len(nomes) + 1
        self.clausulas = []

    def novo(self):
        self.proximo += 1
        return self.proximo - 1

    def codificar(self, expr):
        tipo = expr[0]
        if tipo == 'var':
            return self.ids[expr[1]]
        if tipo == 'not':
            return -self.codificar(expr[1])
        if tipo == 'imp':
            return self.codificar(('disj', [('not', expr[1]), expr[2]]))
        if tipo == 'eq':
            return self.codificar(('conj', [('imp', expr[1], expr[2]), ('imp', expr[2], expr[1])]))
        literais = [self.codificar(e) for e in expr[1]]
        t = self.novo()
        if tipo == 'conj':
            self.clausulas += [[-t, l] for l in literais]
            self.clausulas.append([t] + [-l for l in literais])
        else:
            self.clausulas.append([-t] + literais)
            self.clausulas += [[t, -l] for l in literais]
        return t


def _cnf(modelo, ignorar=None):
    cnf = _Cnf(list(modelo.features))
    v = cnf.ids
    cnf.clausulas.append([v[modelo.raiz]])
    for f in modelo.features.values():
        if f.pai:
            cnf.clausulas.append([-v[f.nome], v[f.pai]])
        filhos = [v[c] for c in f.filhos]
        if not filhos:
            continue
        if f.grupo == 'and':
            for c in f.filhos:
                if modelo.features[c].obrigatoria:
                    cnf.clausulas.append([-v[f.nome], v[c]])
        else:
            cnf.clausulas.append([-v[f.nome]] + filhos)
            if f.grupo == 'alt':
                for i, a in enumerate(filhos):
                    for b in filhos[i + 1:]:
                        cnf.clausulas.append([-a, -b])
    for i, (_, expr) in enumerate(modelo.restricoes):
        if i != ignorar:
            cnf.clausulas.append([cnf.codificar(expr)])
    return cnf


def _resolve(clausulas, suposicoes=()):
    with Glucose3(bootstrap_with=clausulas) as s:
        return s.solve(assumptions=list(suposicoes))


def analisar(modelo):
    cnf = _cnf(modelo)
    v = cnf.ids
    satisfazivel = _resolve(cnf.clausulas)
    mortas = [n for n in modelo.features if not _resolve(cnf.clausulas, [v[n]])]
    falso_opcionais = [
        n for n, f in modelo.features.items()
        if f.pai and not (f.obrigatoria and modelo.features[f.pai].grupo == 'and')
        and n not in mortas and not _resolve(cnf.clausulas, [v[f.pai], -v[n]])
    ]
    redundantes = []
    for i, (descricao, expr) in enumerate(modelo.restricoes):
        sem = _cnf(modelo, ignorar=i)
        negacao = sem.codificar(expr)
        if not _resolve(sem.clausulas + [[-negacao]]):
            redundantes.append(descricao or f'restrição {i + 1}')
    return {'satisfazivel': satisfazivel, 'mortas': mortas,
            'falso_opcionais': falso_opcionais, 'redundantes': redundantes}


def verificar_exigencias(modelo):
    problemas = []
    if len(modelo.restricoes) < 10:
        problemas.append(f'são {len(modelo.restricoes)} restrições; a entrega pede pelo menos 10')
    usados = set().union(*(_operadores(e) for _, e in modelo.restricoes)) if modelo.restricoes else set()
    for op, nome in OPERADORES_EXIGIDOS.items():
        if op not in usados:
            problemas.append(f'nenhuma restrição usa {nome}')
    for _, expr in modelo.restricoes:
        for nome in _variaveis(expr) - set(modelo.features):
            problemas.append(f'restrição cita feature inexistente: {nome}')
    fs = [f for f in modelo.features.values() if f.pai]
    tipos = {
        'Abstract': any(f.abstrata for f in modelo.features.values()),
        'Concrete': any(not f.abstrata for f in modelo.features.values()),
        'Mandatory': any(f.obrigatoria and modelo.features[f.pai].grupo == 'and' for f in fs),
        'Optional': any(not f.obrigatoria and modelo.features[f.pai].grupo == 'and' for f in fs),
        'Or Group': any(f.grupo == 'or' and f.filhos for f in modelo.features.values()),
        'Alternative Group': any(f.grupo == 'alt' and f.filhos for f in modelo.features.values()),
    }
    problemas += [f'o modelo não tem nenhuma feature do tipo {t}' for t, ok in tipos.items() if not ok]
    return problemas


def _avaliar(expr, sel):
    tipo = expr[0]
    if tipo == 'var':
        return expr[1] in sel
    if tipo == 'not':
        return not _avaliar(expr[1], sel)
    if tipo == 'conj':
        return all(_avaliar(e, sel) for e in expr[1])
    if tipo == 'disj':
        return any(_avaliar(e, sel) for e in expr[1])
    if tipo == 'imp':
        return not _avaliar(expr[1], sel) or _avaliar(expr[2], sel)
    return _avaliar(expr[1], sel) == _avaliar(expr[2], sel)


def validar_configuracao(modelo, origem):
    texto = Path(origem).read_text(encoding='utf-8') if Path(str(origem)).exists() else origem
    estado = {e.get('name'): e.get('manual') or e.get('automatic') for e in ET.fromstring(texto).iter('feature')}
    problemas = [f'feature fora do modelo: {n}' for n in estado if n not in modelo.features]
    problemas += [f'feature sem decisão: {n}' for n in modelo.features if estado.get(n) not in ('selected', 'unselected')]
    sel = {n for n, s in estado.items() if s == 'selected'}
    if modelo.raiz not in sel:
        problemas.append('a raiz precisa estar selecionada')
    for f in modelo.features.values():
        if f.nome in sel and f.pai and f.pai not in sel:
            problemas.append(f'{f.nome} selecionada sem o pai {f.pai}')
        if f.nome not in sel or not f.filhos:
            continue
        escolhidos = [c for c in f.filhos if c in sel]
        if f.grupo == 'and':
            problemas += [f'{c} é obrigatória em {f.nome}' for c in f.filhos
                          if modelo.features[c].obrigatoria and c not in sel]
        elif f.grupo == 'or' and not escolhidos:
            problemas.append(f'{f.nome} (Or) precisa de ao menos um filho')
        elif f.grupo == 'alt' and len(escolhidos) != 1:
            problemas.append(f'{f.nome} (Alternative) precisa de exatamente um filho, tem {len(escolhidos)}')
    for descricao, expr in modelo.restricoes:
        if not _avaliar(expr, sel):
            problemas.append(f'viola {descricao}')
    return problemas


def main(argv):
    modelo = carregar(argv[1])
    analise = analisar(modelo)
    problemas = verificar_exigencias(modelo)
    if not analise['satisfazivel']:
        problemas.append('o modelo não tem nenhuma configuração válida (void)')
    problemas += [f'feature morta: {n}' for n in analise['mortas']]
    problemas += [f'feature falsamente opcional: {n}' for n in analise['falso_opcionais']]
    problemas += [f'restrição redundante: {d}' for d in analise['redundantes']]
    for config in argv[2:]:
        problemas += [f'{Path(config).name}: {p}' for p in validar_configuracao(modelo, config)]
    fs = list(modelo.features.values())
    print(f'features: {len(fs)} (abstratas {sum(f.abstrata for f in fs)}, concretas {sum(not f.abstrata for f in fs)})')
    print(f'restrições: {len(modelo.restricoes)}')
    for p in problemas:
        print('PROBLEMA:', p)
    print('OK' if not problemas else f'{len(problemas)} problema(s)')
    return 0 if not problemas else 1


if __name__ == '__main__':
    sys.exit(main(sys.argv))
```

**Como funciona:**
- **Tradução para lógica:** o modelo vira fórmulas em forma normal conjuntiva (cláusulas). A raiz é verdadeira, cada filho implica o pai, as obrigatórias são implicadas pelo pai, os grupos Or e Alternative ganham "pelo menos um" e "no máximo um", e as restrições entram pela transformação de Tseitin.
- **Feature morta:** o resolvedor não encontra configuração que a selecione.
- **Feature falsamente opcional:** é declarada opcional, mas não há configuração em que o pai esteja selecionado e ela não.
- **Restrição redundante:** o modelo sem ela já implica ela.

Run: `.venv/Scripts/python -m unittest -v test_validar_modelo`

Expected: os testes de `AnaliseTest`, `ExigenciasTest` e `ConfiguracaoTest` passam. Os dois de `ModeloRealTest` falham porque o `model.xml` ainda não existe.

- [ ] **Step 4: Projeto FeatureIDE e modelo**

`docs/modelo-de-caracteristicas/SmartScale/.project`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>SmartScale-ModeloDeCaracteristicas</name>
	<comment>Modelo de características da linha de produto SmartScale</comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>de.ovgu.featureide.core.extensibleFeatureProjectBuilder</name>
			<arguments>
				<dictionary>
					<key>build</key>
					<value>src</value>
				</dictionary>
				<dictionary>
					<key>composer</key>
					<value>de.ovgu.featureide.core.FeatureModeling</value>
				</dictionary>
				<dictionary>
					<key>equations</key>
					<value>configs</value>
				</dictionary>
				<dictionary>
					<key>source</key>
					<value>features</value>
				</dictionary>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>de.ovgu.featureide.core.featureProjectNature</nature>
	</natures>
</projectDescription>
```

`docs/modelo-de-caracteristicas/SmartScale/model.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<featureModel>
	<properties/>
	<struct>
		<and abstract="true" mandatory="true" name="SmartScale">
			<and abstract="true" mandatory="true" name="Nucleo">
				<feature mandatory="true" name="MotorDeRodizio"/>
				<feature mandatory="true" name="CatalogoDeCriterios"/>
				<feature mandatory="true" name="ContratosDoNucleo"/>
			</and>
			<alt abstract="true" mandatory="true" name="CriterioDeOrdenacao">
				<feature name="MaiorFolga"/>
				<feature name="MenorCarga"/>
				<feature name="MaisModerno"/>
			</alt>
			<and abstract="true" mandatory="true" name="Cadastros">
				<feature mandatory="true" name="Pessoas"/>
				<feature mandatory="true" name="Cargos"/>
				<feature mandatory="true" name="Unidades"/>
				<feature mandatory="true" name="TiposDeTurno"/>
				<feature name="Qualificacoes"/>
				<feature name="Feriados"/>
			</and>
			<and abstract="true" mandatory="true" name="Escala">
				<feature mandatory="true" name="GeracaoAutomatica"/>
				<or abstract="true" mandatory="true" name="Elegibilidade">
					<feature name="PorCargo"/>
					<feature name="PorUnidade"/>
					<feature name="PorQualificacao"/>
				</or>
				<or abstract="true" mandatory="true" name="RegrasDaEscala">
					<feature name="IntervaloMinimo"/>
					<feature name="MaximoMensal"/>
					<feature name="PesoFimDeSemana"/>
					<feature name="PesoFeriado"/>
				</or>
				<feature name="Publicacao"/>
				<feature name="TravamentoDeDias"/>
				<feature name="ConsultaPessoal"/>
			</and>
			<and abstract="true" name="Trocas">
				<or abstract="true" mandatory="true" name="TiposDeTroca">
					<feature name="Substituicao"/>
					<feature name="TrocaMutua"/>
				</or>
				<feature name="AprovacaoEmDuasEtapas"/>
			</and>
			<and abstract="true" name="Afastamentos">
				<or abstract="true" mandatory="true" name="TiposDeAfastamento">
					<feature name="Missao"/>
					<feature name="Dispensa"/>
					<feature name="Ferias"/>
					<feature name="Licenca"/>
					<feature name="Curso"/>
					<feature name="OutroAfastamento"/>
				</or>
				<feature name="ReconciliacaoAutomatica"/>
			</and>
			<or abstract="true" name="Comunicacao">
				<feature name="NotificacoesNoSistema"/>
				<feature name="LembretePorEmail"/>
				<feature name="BoletimInterno"/>
				<feature name="Avisos"/>
			</or>
			<or abstract="true" name="Relatorios">
				<feature name="CsvEscalaDoDia"/>
				<feature name="CsvServicosDaPessoa"/>
				<feature name="CsvAfastamentosDoMes"/>
				<feature name="PdfEscalaDoDia"/>
			</or>
			<and abstract="true" mandatory="true" name="Seguranca">
				<feature mandatory="true" name="Autenticacao"/>
				<alt abstract="true" mandatory="true" name="ControleDeAcesso">
					<feature name="PerfisEPermissoes"/>
					<feature name="AcessoUnico"/>
				</alt>
				<feature name="Auditoria"/>
				<feature name="ProtecaoForcaBruta"/>
				<feature name="SenhaTemporaria"/>
			</and>
			<alt abstract="true" mandatory="true" name="Persistencia">
				<feature name="H2"/>
				<feature name="MySQL"/>
			</alt>
			<alt abstract="true" mandatory="true" name="Implantacao">
				<feature name="ExecucaoLocal"/>
				<feature name="DockerCompose"/>
			</alt>
			<and abstract="true" mandatory="true" name="Interface">
				<feature mandatory="true" name="PainelWeb"/>
				<feature mandatory="true" name="CalendarioMensal"/>
				<feature name="FitaDoServico"/>
				<feature name="LayoutResponsivo"/>
			</and>
		</and>
	</struct>
	<constraints>
		<rule>
			<description>R01 - Troca exige intervalo minimo de descanso e notificacao ao substituto</description>
			<imp>
				<var>Trocas</var>
				<conj>
					<var>IntervaloMinimo</var>
					<var>NotificacoesNoSistema</var>
				</conj>
			</imp>
		</rule>
		<rule>
			<description>R02 - Troca so acontece em escala publicada</description>
			<imp>
				<var>Trocas</var>
				<var>Publicacao</var>
			</imp>
		</rule>
		<rule>
			<description>R03 - Aprovacao em duas etapas precisa de dois perfis distintos</description>
			<not>
				<conj>
					<var>AcessoUnico</var>
					<var>AprovacaoEmDuasEtapas</var>
				</conj>
			</not>
		</rule>
		<rule>
			<description>R04 - Reconciliacao escolhe substituto respeitando o descanso</description>
			<imp>
				<var>ReconciliacaoAutomatica</var>
				<var>IntervaloMinimo</var>
			</imp>
		</rule>
		<rule>
			<description>R05 - Avisos sao montados a partir de feriados ou afastamentos</description>
			<imp>
				<var>Avisos</var>
				<disj>
					<var>Feriados</var>
					<var>Afastamentos</var>
				</disj>
			</imp>
		</rule>
		<rule>
			<description>R06 - Relatorio de afastamentos precisa do modulo de afastamentos</description>
			<imp>
				<var>CsvAfastamentosDoMes</var>
				<var>Afastamentos</var>
			</imp>
		</rule>
		<rule>
			<description>R07 - PDF da escala do dia mostra so escala publicada</description>
			<imp>
				<var>PdfEscalaDoDia</var>
				<var>Publicacao</var>
			</imp>
		</rule>
		<rule>
			<description>R08 - Lembrete por e-mail so avisa servico de escala publicada</description>
			<imp>
				<var>LembretePorEmail</var>
				<var>Publicacao</var>
			</imp>
		</rule>
		<rule>
			<description>R09 - Elegibilidade por qualificacao precisa do cadastro de qualificacoes</description>
			<imp>
				<var>PorQualificacao</var>
				<var>Qualificacoes</var>
			</imp>
		</rule>
		<rule>
			<description>R10 - O Docker Compose sobe MySQL, nunca o banco embarcado</description>
			<imp>
				<var>DockerCompose</var>
				<not>
					<var>H2</var>
				</not>
			</imp>
		</rule>
		<rule>
			<description>R11 - Peso de feriado so faz sentido com feriados cadastrados</description>
			<imp>
				<var>PesoFeriado</var>
				<var>Feriados</var>
			</imp>
		</rule>
		<rule>
			<description>R12 - A consulta pessoal mostra so escala publicada</description>
			<imp>
				<var>ConsultaPessoal</var>
				<var>Publicacao</var>
			</imp>
		</rule>
	</constraints>
</featureModel>
```

- [ ] **Step 5: Configurações de produto**

`docs/modelo-de-caracteristicas/gerar_configuracoes.py`:

```python
from pathlib import Path

from validar_modelo import carregar

AQUI = Path(__file__).parent
MODELO = AQUI / 'SmartScale' / 'model.xml'

COMUM_MILSCALE = {
    'MotorDeRodizio', 'CatalogoDeCriterios', 'ContratosDoNucleo', 'MaiorFolga',
    'Pessoas', 'Cargos', 'Unidades', 'TiposDeTurno', 'Qualificacoes', 'Feriados',
    'GeracaoAutomatica', 'PorCargo', 'PorUnidade', 'PorQualificacao',
    'IntervaloMinimo', 'MaximoMensal', 'PesoFimDeSemana', 'PesoFeriado',
    'Publicacao', 'TravamentoDeDias', 'ConsultaPessoal',
    'Substituicao', 'TrocaMutua', 'AprovacaoEmDuasEtapas',
    'Missao', 'Dispensa', 'Ferias', 'Licenca', 'Curso', 'OutroAfastamento', 'ReconciliacaoAutomatica',
    'NotificacoesNoSistema', 'LembretePorEmail', 'BoletimInterno', 'Avisos',
    'CsvEscalaDoDia', 'CsvServicosDaPessoa', 'CsvAfastamentosDoMes', 'PdfEscalaDoDia',
    'Autenticacao', 'PerfisEPermissoes', 'Auditoria', 'ProtecaoForcaBruta', 'SenhaTemporaria',
    'PainelWeb', 'CalendarioMensal', 'FitaDoServico', 'LayoutResponsivo',
}

PRODUTOS = {
    'MilScale-Docker': COMUM_MILSCALE | {'MySQL', 'DockerCompose'},
    'MilScale-Desenvolvimento': COMUM_MILSCALE | {'H2', 'ExecucaoLocal'},
    'PlantaoHospitalar': {
        'MotorDeRodizio', 'CatalogoDeCriterios', 'ContratosDoNucleo', 'MenorCarga',
        'Pessoas', 'Cargos', 'Unidades', 'TiposDeTurno', 'Qualificacoes', 'Feriados',
        'GeracaoAutomatica', 'PorCargo', 'PorQualificacao', 'IntervaloMinimo', 'MaximoMensal',
        'Publicacao', 'ConsultaPessoal', 'Substituicao',
        'Ferias', 'Licenca', 'Curso', 'ReconciliacaoAutomatica',
        'NotificacoesNoSistema', 'LembretePorEmail', 'CsvEscalaDoDia', 'CsvServicosDaPessoa',
        'Autenticacao', 'AcessoUnico', 'Auditoria', 'MySQL', 'DockerCompose',
        'PainelWeb', 'CalendarioMensal', 'LayoutResponsivo',
    },
}


def gerar():
    modelo = carregar(MODELO)
    pasta = AQUI / 'SmartScale' / 'configs'
    pasta.mkdir(exist_ok=True)
    for nome, escolhidas in PRODUTOS.items():
        desconhecidas = escolhidas - set(modelo.features)
        if desconhecidas:
            raise ValueError(f'{nome}: features fora do modelo: {sorted(desconhecidas)}')
        selecionadas = set(escolhidas)
        for f in escolhidas:
            pai = modelo.features[f].pai
            while pai:
                selecionadas.add(pai)
                pai = modelo.features[pai].pai
        linhas = [
            f'\t<feature manual="{"selected" if n in selecionadas else "unselected"}" name="{n}"/>'
            for n in modelo.features
        ]
        texto = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<configuration>\n' + '\n'.join(linhas) + '\n</configuration>\n'
        (pasta / f'{nome}.xml').write_text(texto, encoding='utf-8')
        print(f'{nome}: {len(selecionadas)} de {len(modelo.features)} features selecionadas')


if __name__ == '__main__':
    gerar()
```

Run: `.venv/Scripts/python gerar_configuracoes.py`

Expected:
```
MilScale-Docker: 68 de 73 features selecionadas
MilScale-Desenvolvimento: 68 de 73 features selecionadas
PlantaoHospitalar: 52 de 73 features selecionadas
```

- [ ] **Step 6: Tudo verde**

```bash
.venv/Scripts/python -m unittest -v test_validar_modelo
.venv/Scripts/python validar_modelo.py SmartScale/model.xml SmartScale/configs/*.xml
```

Expected:
- `Ran 13 tests ... OK`;
- o validador imprime `features: 73 (abstratas 18, concretas 55)`, `restrições: 12` e `OK`.

- [ ] **Step 7: Abrir no FeatureIDE**
  1. **Importar o projeto:** no Eclipse, `File > Import > General > Existing Projects into Workspace`, escolher a pasta `docs/modelo-de-caracteristicas/SmartScale` e, sem marcar "Copy projects into workspace", clicar em Finish.
  2. **Conferir o modelo:** abrir o `model.xml`. O editor não pode mostrar nenhum aviso (triângulo amarelo) de "dead feature", "false optional" ou "redundant constraint".
  3. **Conferir as configurações:** abrir as três em `configs/`. As três precisam aparecer como válidas.

  Se o FeatureIDE acusar algo que o validador não acusou, o FeatureIDE é a referência. Corrija o modelo e acrescente o caso em `test_validar_modelo.py`.

- [ ] **Step 8: Checkpoint.** Mostrar o diff e a saída do Step 6. Mensagem sugerida: `feat(lps): modelo de caracteristicas no FeatureIDE com 12 restricoes e validador`.

---

### Task 2: Imagem do FeatureIDE e documento do modelo

**Files:**
- Create: `docs/modelo-de-caracteristicas/SmartScale-modelo.png`
- Create: `docs/MODELO_DE_CARACTERISTICAS.md`

**Interfaces:**
- **Consumes:**
  - o modelo da Task 1;
  - os nomes de classe citados, que precisam existir no código.
- **Produces:** `docs/MODELO_DE_CARACTERISTICAS.md`, que referencia a imagem `modelo-de-caracteristicas/SmartScale-modelo.png` e é consumido pela Task 4.

- [ ] **Step 1: Layout do diagrama.** No editor do `model.xml`, clique com o botão direito no fundo do diagrama, entre em **Set Layout** e escolha um layout da família **Left to Right**. Com 73 features, o layout de cima para baixo fica largo demais para a página. Ainda pelo botão direito, deixe a legenda visível (**Show Legend**). Ela mostra os símbolos de Mandatory, Optional, Or, Alternative, Abstract e Concrete e ajuda o professor a ler a imagem.

- [ ] **Step 2: Exportar a imagem.** Ainda com o botão direito no fundo do diagrama, use **Export As…** e salve em PNG como `docs/modelo-de-caracteristicas/SmartScale-modelo.png`.

  Se a sua versão não tiver essa opção no menu, use `File > Export… > FeatureIDE` e procure a exportação de imagem do modelo. Como último recurso, maximize o editor, use o zoom para caber tudo e capture com `Win+Shift+S`.

  A imagem precisa mostrar o diagrama inteiro, as restrições (que o FeatureIDE desenha abaixo do diagrama) e a legenda.

- [ ] **Step 3: Documento.** Criar `docs/MODELO_DE_CARACTERISTICAS.md`:

````markdown
# Modelo de características da linha de produto SmartScale

Desenvolvimento Orientado a Reuso de Software · PUCPR · Engenharia de Software, 6º período · prof. Tiago Navarro

## 1. O domínio e seus limites

A SmartScale é uma linha de produto de **escalas de serviço por rodízio**: distribuir pessoas em turnos de forma justa, respeitando quem pode tirar cada turno, o descanso entre turnos e as ausências. O primeiro produto da linha é o **MilScale**, a escala de serviço de 24 horas de um batalhão. Um segundo produto, **Plantão Hospitalar**, é usado neste documento para mostrar como a mesma linha gera um sistema diferente.

**Dentro do domínio:** cadastro de quem é escalado e da estrutura da organização, geração e publicação da escala, regras de rodízio, trocas de turno, afastamentos, comunicação da escala, relatórios, segurança de acesso, persistência e implantação.

**Fora do domínio, com a relação de cada um:**

| Domínio vizinho | Relação com a SmartScale |
|---|---|
| Recursos humanos e folha de pagamento | Fornece as pessoas. Hoje o cadastro é manual; a integração seria uma feature nova (importação) |
| Controle de ponto e frequência | Consome a escala. Os relatórios CSV são o ponto de troca de dados |
| Autenticação corporativa (SSO, LDAP) | Poderia substituir a feature `Autenticacao`. Fica fora do modelo atual |
| Controle de material e logística | Sem relação. O MilScale roda num batalhão de suprimento, mas não gerencia material |

## 2. O diagrama

![Modelo de características da SmartScale no FeatureIDE](modelo-de-caracteristicas/SmartScale-modelo.png)

O modelo foi feito no **FeatureIDE 3.12** (Eclipse) e está versionado em `docs/modelo-de-caracteristicas/SmartScale/model.xml`.

| Número | Quantidade |
|---|---|
| Características (features) | 73 |
| Abstratas | 18 |
| Concretas | 55 |
| Obrigatórias (Mandatory) | 24 |
| Opcionais (Optional) | 16 |
| Grupos Or | 6 grupos, 23 features |
| Grupos Alternative | 4 grupos, 9 features |
| Restrições (constraints) | 12 |

O modelo tem configurações válidas, nenhuma feature morta, nenhuma feature falsamente opcional e nenhuma restrição redundante. Isso foi verificado pela análise do próprio FeatureIDE e pelo script `docs/modelo-de-caracteristicas/validar_modelo.py` (seção 6).

## 3. Abstract e Concrete

**Feature abstrata** é um agrupamento que existe para organizar o modelo e não corresponde, sozinha, a código. **Feature concreta** corresponde a código, configuração ou arquivo do sistema.

| Tipo | Features | Justificativa |
|---|---|---|
| Abstract | `SmartScale`, `Nucleo`, `CriterioDeOrdenacao`, `Cadastros`, `Escala`, `Elegibilidade`, `RegrasDaEscala`, `Trocas`, `TiposDeTroca`, `Afastamentos`, `TiposDeAfastamento`, `Comunicacao`, `Relatorios`, `Seguranca`, `ControleDeAcesso`, `Persistencia`, `Implantacao`, `Interface` | São categorias. "Comunicacao", por exemplo, não tem uma classe: quem tem código são as filhas (`NotificacaoService`, `LembreteServicoService`). Marcar como abstrata evita que a configuração de um produto "selecione" algo que não gera nada |
| Concrete | as outras 55 | Cada uma aponta para um artefato real. Exemplos na tabela abaixo |

| Feature concreta | Onde está |
|---|---|
| `MotorDeRodizio` | `smartscale-core/.../MotorDeRodizio.java` (núcleo reutilizável) |
| `CatalogoDeCriterios` | `smartscale-core/.../CatalogoDeCriterios.java` (Singleton) |
| `ContratosDoNucleo` | `PessoaEscalada`, `TipoTurno` e `CriterioDeOrdenacao` no núcleo |
| `MaiorFolga`, `MenorCarga`, `MaisModerno` | `CriterioOrdenacaoMilitar`, `CriterioMenorCargaNaGeracao`, `CriterioMaisModernoPrimeiro` (Strategy) |
| `Pessoas`, `Cargos`, `Unidades`, `TiposDeTurno` | `Militar`, `PostoGraduacao`, `Subunidade`, `TipoServico` e suas telas |
| `GeracaoAutomatica` | `GerarEscalaService` |
| `IntervaloMinimo`, `MaximoMensal` | `PoliticaDeDescanso` e `RegraEscala.maxServicosMes` |
| `PesoFimDeSemana`, `PesoFeriado` | parâmetros de `RegraEscala`, cadastrados na tela Regras da escala |
| `Publicacao`, `TravamentoDeDias`, `ConsultaPessoal` | `PublicarEscalaService`, `BloqueioDiaService`, `MinhaEscalaService` |
| `Substituicao`, `TrocaMutua`, `AprovacaoEmDuasEtapas` | `SolicitacaoService` (`criar`, `criarTrocaMutua`, `triagem`, `autorizar`) |
| `ReconciliacaoAutomatica` | `AfastamentoService.reconciliarServicosJaMarcados` |
| `NotificacoesNoSistema`, `LembretePorEmail`, `BoletimInterno`, `Avisos` | `NotificacaoService`, `LembreteServicoService`, `BoletimService`, `AvisoService` |
| `CsvEscalaDoDia`, `CsvServicosDaPessoa`, `CsvAfastamentosDoMes` | `RelatorioEscalaDoDia`, `RelatorioServicosDoMilitar`, `RelatorioAfastamentosDoMes` (Template Method) |
| `PdfEscalaDoDia` | tela `EscalaPdf.tsx` |
| `Autenticacao`, `PerfisEPermissoes` | `MilScaleUserDetailsService`, `PerfilAcesso` e `@PreAuthorize` |
| `Auditoria`, `ProtecaoForcaBruta`, `SenhaTemporaria` | `AuditoriaService`, `ProtecaoContraForcaBruta`, `SenhaTemporariaFilter` |
| `H2`, `MySQL` | `application.properties` e `application-mysql.properties` |
| `ExecucaoLocal`, `DockerCompose` | `mvn spring-boot:run` e `docker-compose.yml` |
| `PainelWeb`, `CalendarioMensal`, `FitaDoServico`, `LayoutResponsivo` | `Painel.tsx`, `CalendarioMensal.tsx`, `FitaDoServico.tsx`, regras `@media` de `styles.css` |

`AcessoUnico` é a única feature concreta sem implementação no MilScale: é a variante prevista para organizações pequenas, com um só perfil que faz tudo. Ela existe no modelo para o produto Plantão Hospitalar.

## 4. Mandatory e Optional

**Mandatory (obrigatória):** todo produto da linha tem. **Optional (opcional):** o produto pode ter ou não.

| Tipo | Feature | Justificativa |
|---|---|---|
| Mandatory | `Nucleo` e as três filhas | É o ativo reutilizável (`smartscale-core` 1.0.0). Sem ele não há linha de produto |
| Mandatory | `CriterioDeOrdenacao` | Toda escala por rodízio precisa decidir quem vai primeiro na fila |
| Mandatory | `Cadastros` com `Pessoas`, `Cargos`, `Unidades`, `TiposDeTurno` | Sem quem escalar, onde e em qual turno, o motor não tem entrada |
| Mandatory | `Escala` com `GeracaoAutomatica`, `Elegibilidade`, `RegrasDaEscala` | É o propósito do sistema: gerar a escala respeitando quem pode e quando pode |
| Mandatory | `TiposDeTroca`, `TiposDeAfastamento` | Obrigatórias **dentro** de `Trocas` e `Afastamentos`: se o produto tem o módulo, precisa ter ao menos um tipo |
| Mandatory | `Seguranca` com `Autenticacao` e `ControleDeAcesso` | A escala expõe dados pessoais; ninguém acessa sem login |
| Mandatory | `Persistencia`, `Implantacao` | Todo produto guarda dados e precisa rodar em algum lugar; o que varia é **qual** |
| Mandatory | `Interface` com `PainelWeb` e `CalendarioMensal` | A escala é consultada por calendário em qualquer produto |
| Optional | `Qualificacoes`, `Feriados` | Uma organização pode não exigir cursos nem tratar feriados de forma diferente |
| Optional | `Publicacao` | Um produto simples pode mostrar a escala assim que é gerada, sem rascunho |
| Optional | `TravamentoDeDias`, `ConsultaPessoal` | Recursos de controle e de autoatendimento que nem toda organização usa |
| Optional | `Trocas` e `AprovacaoEmDuasEtapas` | Há organizações que não permitem troca; e as que permitem podem dispensar a triagem |
| Optional | `Afastamentos` e `ReconciliacaoAutomatica` | Um produto pode tratar ausências fora do sistema; a realocação automática é um diferencial |
| Optional | `Comunicacao`, `Relatorios` | Valor agregado, não essencial para gerar a escala |
| Optional | `Auditoria`, `ProtecaoForcaBruta`, `SenhaTemporaria` | Endurecimento de segurança que depende do nível de exigência do cliente |
| Optional | `FitaDoServico`, `LayoutResponsivo` | Recursos de interface do MilScale que outro produto pode não querer |

## 5. Or Group e Alternative Group

**Or Group:** escolher **um ou mais** filhos. **Alternative Group:** escolher **exatamente um**.

| Tipo | Grupo | Filhos | Justificativa |
|---|---|---|---|
| Or | `Elegibilidade` | `PorCargo`, `PorUnidade`, `PorQualificacao` | Os filtros se somam: o MilScale usa os três ao mesmo tempo (`ElegibilidadeService`), um hospital pode usar só cargo e especialidade |
| Or | `RegrasDaEscala` | `IntervaloMinimo`, `MaximoMensal`, `PesoFimDeSemana`, `PesoFeriado` | As regras são independentes e combináveis; pelo menos uma é preciso para o rodízio ter critério de descanso ou carga |
| Or | `TiposDeTroca` | `Substituicao`, `TrocaMutua` | Um produto pode aceitar só substituição, só troca mútua ou as duas |
| Or | `TiposDeAfastamento` | `Missao`, `Dispensa`, `Ferias`, `Licenca`, `Curso`, `OutroAfastamento` | Cada organização tem seus motivos de ausência; o MilScale usa os seis (`TipoAfastamento`) |
| Or | `Comunicacao` | `NotificacoesNoSistema`, `LembretePorEmail`, `BoletimInterno`, `Avisos` | Canais que convivem; o e-mail, por exemplo, é ligado por configuração |
| Or | `Relatorios` | `CsvEscalaDoDia`, `CsvServicosDaPessoa`, `CsvAfastamentosDoMes`, `PdfEscalaDoDia` | Cada relatório atende um público e pode ser incluído sozinho |
| Alternative | `CriterioDeOrdenacao` | `MaiorFolga`, `MenorCarga`, `MaisModerno` | A fila só pode ter uma ordem. É o ponto de variação RN01, escolhido na inicialização por `MILSCALE_CRITERIO_ORDENACAO` |
| Alternative | `ControleDeAcesso` | `PerfisEPermissoes`, `AcessoUnico` | Ou o sistema separa papéis, ou todo usuário pode tudo; as duas coisas ao mesmo tempo são contraditórias |
| Alternative | `Persistencia` | `H2`, `MySQL` | Uma instalação usa um banco. Escolhido pelo perfil do Spring (`mysql` ou padrão) |
| Alternative | `Implantacao` | `ExecucaoLocal`, `DockerCompose` | A instalação roda de um jeito: pelo Maven na máquina ou pelos containers |

## 6. Restrições (constraints)

As 12 restrições usam os quatro operadores pedidos: **Implies** (⇒), **And** (∧), **Or** (∨) e **Not** (¬). Todas vêm de dependências reais do código, indicadas na última coluna.

| Código | Restrição | Operadores | Justificativa | Onde no código |
|---|---|---|---|---|
| R01 | `Trocas ⇒ (IntervaloMinimo ∧ NotificacoesNoSistema)` | Implies, And | A troca precisa conferir o descanso de quem assume e avisar o substituto | `SolicitacaoService` usa `PoliticaDeDescanso` e `NotificacaoService` |
| R02 | `Trocas ⇒ Publicacao` | Implies | Só se troca serviço de escala publicada; rascunho ainda pode mudar | `SolicitacaoService` exige `SituacaoEscala.PUBLICADA` |
| R03 | `¬(AcessoUnico ∧ AprovacaoEmDuasEtapas)` | Not, And | A aprovação em duas etapas precisa de dois papéis (Cabo faz a triagem, Sargenteante autoriza); com um perfil só não há quem divida o fluxo | `SolicitacaoController`: triagem para Cabo e Sargenteante, autorização só para Sargenteante (`@PreAuthorize`) |
| R04 | `ReconciliacaoAutomatica ⇒ IntervaloMinimo` | Implies | Ao realocar o serviço de quem se afastou, o substituto precisa respeitar o descanso | `AfastamentoService` usa `PoliticaDeDescanso` |
| R05 | `Avisos ⇒ (Feriados ∨ Afastamentos)` | Implies, Or | O mural de avisos é montado a partir de feriados ou de afastamentos; sem nenhum dos dois ficaria vazio | `AvisoService` lê `FeriadoRepository` e `AfastamentoRepository` |
| R06 | `CsvAfastamentosDoMes ⇒ Afastamentos` | Implies | O relatório lista afastamentos; sem o módulo não há dados | `RelatorioAfastamentosDoMes` |
| R07 | `PdfEscalaDoDia ⇒ Publicacao` | Implies | O PDF é o documento oficial do dia; só mostra escala publicada | `EscalaPdf.tsx` ("Nenhuma escala publicada cobre esse dia") |
| R08 | `LembretePorEmail ⇒ Publicacao` | Implies | O lembrete não pode avisar um serviço que ainda é rascunho | `LembreteServicoService` filtra `PUBLICADA` |
| R09 | `PorQualificacao ⇒ Qualificacoes` | Implies | Filtrar por curso exige o cadastro de cursos | `ElegibilidadeService` lê `Militar.getQualificacoes()` |
| R10 | `DockerCompose ⇒ ¬H2` | Implies, Not | O Compose sobe o MySQL e o backend no perfil `mysql`; o banco embarcado não é usado em container | `docker-compose.yml` e `backend/Dockerfile` |
| R11 | `PesoFeriado ⇒ Feriados` | Implies | O peso extra vale para os dias que o cadastro de feriados marca | `RegraEscala.pesoFeriado` e `Feriado` |
| R12 | `ConsultaPessoal ⇒ Publicacao` | Implies | O militar só vê a própria escala depois de publicada | `MinhaEscalaService` filtra `PUBLICADA` |

## 7. Configurações de produto

A engenharia de aplicação escolhe as features de cada produto. As configurações estão em `docs/modelo-de-caracteristicas/SmartScale/configs/` e o FeatureIDE as valida contra o modelo.

| Configuração | Escolhas que a diferenciam | Features selecionadas |
|---|---|---|
| `MilScale-Docker` | `MaiorFolga`, `PerfisEPermissoes`, `MySQL`, `DockerCompose`, todos os opcionais | 68 de 73 |
| `MilScale-Desenvolvimento` | igual à anterior, com `H2` e `ExecucaoLocal` | 68 de 73 |
| `PlantaoHospitalar` | `MenorCarga`, `AcessoUnico`, só `Substituicao`, sem `Avisos` nem `BoletimInterno`, sem `TravamentoDeDias` | 52 de 73 |

**Momento da escolha (binding time):**
- **Na inicialização:** o critério da fila, pela propriedade `milscale.lps.criterio-ordenacao`, e a persistência, pelo perfil do Spring.
- **Na implantação:** a forma de rodar, pelo uso ou não do `docker-compose.yml`.
- **Na construção do produto:** as demais features, pela presença do módulo no código.

## 8. Como o modelo foi validado

- **No FeatureIDE:** o editor do modelo não mostra avisos de feature morta, falsamente opcional ou restrição redundante, e as três configurações abrem como válidas.
- **Por script:**
  - `validar_modelo.py` traduz o modelo para lógica proposicional e usa um resolvedor SAT (`python-sat`) para conferir as mesmas propriedades;
  - também confere as exigências desta entrega: pelo menos 10 restrições, uso de Implies, And, Or e Not, e presença dos seis tipos de feature;
  - os 13 testes de `test_validar_modelo.py` provam que o script detecta cada problema.
````

- [ ] **Step 4: Conferir que cada classe citada existe**

```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
for c in MotorDeRodizio CatalogoDeCriterios PessoaEscalada TipoTurno CriterioOrdenacaoMilitar CriterioMenorCargaNaGeracao \
  CriterioMaisModernoPrimeiro GerarEscalaService PoliticaDeDescanso RegraEscala PublicarEscalaService BloqueioDiaService \
  MinhaEscalaService SolicitacaoService SolicitacaoController AfastamentoService NotificacaoService LembreteServicoService \
  BoletimService AvisoService RelatorioEscalaDoDia RelatorioServicosDoMilitar RelatorioAfastamentosDoMes \
  MilScaleUserDetailsService PerfilAcesso AuditoriaService ProtecaoContraForcaBruta SenhaTemporariaFilter ElegibilidadeService \
  TipoAfastamento Militar PostoGraduacao Subunidade TipoServico Feriado; do
  grep -rlq "\(class\|interface\|enum\|record\) $c\b" backend/src/main smartscale-core/src/main || echo "NAO EXISTE: $c"
done
for f in EscalaPdf.tsx CalendarioMensal.tsx FitaDoServico.tsx Painel.tsx; do
  find frontend/src -name "$f" | grep -q . || echo "NAO EXISTE: $f"
done
echo "conferencia concluida"
```

Expected: só `conferencia concluida`, sem nenhuma linha `NAO EXISTE`.

- [ ] **Step 5: Checkpoint.** Mostrar a imagem e o diff. Mensagem sugerida: `docs(lps): documento do modelo de caracteristicas com justificativas`.

---

### Task 3: Classificação GRU e DRU

**Files:**
- Create: `docs/NIVEIS_DE_REUSO_MPSBR.md`

**Interfaces:**
- **Consumes:** a evidência citada em cada linha (README do núcleo, `VARIABILIDADE.md`, ArchUnit, modelo da Task 1, `database-first/templates/` da Task 5).
- **Produces:** `docs/NIVEIS_DE_REUSO_MPSBR.md`, que a Task 4 consome. As células de situação usam exatamente `🟢 Verde`, `🟡 Amarelo` ou `🔴 Vermelho`, que é o que o gerador pinta.

**Texto dos resultados esperados:** copiado do guia (p. 36 e p. 42), sem paráfrase. A classificação foi decidida com a evidência real do repositório em 2026-11-10:
- **GRU:** nenhum Verde, 4 Amarelo e 1 Vermelho;
- **DRU:** 4 Verde, 3 Amarelo e 2 Vermelho.

A Task 1 é o que leva DRU 6 e 7 a Verde. Por isso esta task vem depois dela.

- [ ] **Step 1: Documento.** Criar `docs/NIVEIS_DE_REUSO_MPSBR.md`:

````markdown
# Níveis de reuso da SmartScale segundo o MPS.BR

Desenvolvimento Orientado a Reuso de Software · PUCPR · Engenharia de Software, 6º período · prof. Tiago Navarro

Referência: **MPS.BR — Guia Geral MPS de Software:2016**, processo Gerência de Reutilização (GRU, nível E, p. 36) e processo Desenvolvimento para Reutilização (DRU, nível C, p. 42).

**Legenda:**
- **Verde:** de acordo com o resultado esperado;
- **Amarelo:** identificação parcial;
- **Vermelho:** não encontrado.

**O que é ativo reutilizável neste projeto:**
- o componente `smartscale-core` 1.0.0, que contém o motor de rodízio, os contratos e o catálogo de critérios;
- o modelo de características;
- os padrões de projeto documentados;
- os templates do gerador Database First.

## 1. Gerência de Reutilização (GRU)

> Propósito: gerenciar o ciclo de vida dos ativos reutilizáveis.

### 1.1 Classificação

| Resultado esperado | Situação | Justificativa (evidência no projeto) |
|---|---|---|
| **GRU 1.** Uma estratégia de gerenciamento de ativos é documentada, com a definição de ativo reutilizável e os critérios de aceitação, certificação, classificação, descontinuidade e avaliação | 🟡 Amarelo | O que é o ativo e como usá-lo está documentado: README do `smartscale-core` (API pública, dependências, exemplo), `docs/VARIABILIDADE.md` (núcleo × produto) e o CHANGELOG com SemVer. **Falta** um documento de estratégia com os critérios de aceitação, certificação, classificação, descontinuidade e avaliação |
| **GRU 2.** Um mecanismo de armazenamento e recuperação de ativos reutilizáveis é implantado | 🟡 Amarelo | O componente é empacotado em JAR e instalado no repositório Maven local (`mvn install` → `~/.m2/.../smartscale-core/1.0.0/`); o código-fonte fica no GitHub. **Falta** um repositório remoto de artefatos: o próprio README diz que o componente "ainda não está publicado num repositório remoto", então outro computador só obtém o ativo recompilando |
| **GRU 3.** Os dados de utilização dos ativos reutilizáveis são registrados | 🔴 Vermelho | Não há registro de quem usa o ativo, em qual versão, desde quando. A única consumidora é o backend do MilScale, visível apenas lendo o `backend/pom.xml` |
| **GRU 4.** Os ativos reutilizáveis são periodicamente mantidos, segundo os critérios definidos, e suas modificações são controladas ao longo do ciclo de vida | 🟡 Amarelo | O controle de modificações existe: Git com branches e merge na `main`, versionamento semântico, CHANGELOG obrigatório, 7 testes do núcleo que rodam a cada build. **Falta** a manutenção periódica com critérios definidos: não há calendário de revisão nem integração contínua no repositório |
| **GRU 5.** Os usuários de ativos reutilizáveis são notificados sobre problemas, modificações, novas versões e descontinuidade | 🟡 Amarelo | O CHANGELOG viaja dentro do JAR (`META-INF/CHANGELOG.md`) e registra cada versão, então quem atualiza fica sabendo o que mudou. **Falta** notificação ativa: ninguém é avisado de uma versão nova, de um defeito conhecido ou de uma descontinuidade |

### 1.2 Como adequar o protótipo à GRU

| Resultado | Proposta | Artefato ou ferramenta | Como saber que atende |
|---|---|---|---|
| GRU 1 | Escrever a **estratégia de gerenciamento de ativos**. **Definição:** ativo é todo artefato versionado e documentado feito para ser usado por mais de um produto (componente de código, template de geração, modelo de domínio, documento de padrão). **Aceitação:** testes verdes, README, LICENSE e CHANGELOG presentes, e nenhuma dependência de framework em tempo de execução no núcleo. **Certificação:** revisão por dois integrantes e build verde na integração contínua. **Classificação:** por tipo, por domínio e por maturidade (experimental, estável, descontinuado). **Descontinuidade:** anunciada com uma versão MINOR de antecedência, com `@Deprecated`, e remoção só na MAJOR seguinte. **Avaliação:** semestral, com uso, defeitos e custo de manutenção | `docs/ESTRATEGIA_DE_ATIVOS.md` | O documento existe e cada ativo do catálogo cita a classificação e o critério de aceitação que cumpriu |
| GRU 2 | **Publicar** o `smartscale-core` num repositório remoto de artefatos e manter um **catálogo** com a localização de cada ativo | GitHub Packages (registro Maven) com `distributionManagement` no `pom.xml` e `mvn deploy`; tag `smartscale-core-v1.0.0`; `docs/CATALOGO_DE_ATIVOS.md` | Um computador sem o código-fonte baixa o JAR declarando só a dependência e o repositório |
| GRU 3 | **Registrar o uso**: a cada produto que adota ou troca a versão de um ativo, uma linha no catálogo (produto, ativo, versão, data, responsável) | Tabela "Quem usa" em `docs/CATALOGO_DE_ATIVOS.md`; estatística de downloads do GitHub Packages | O catálogo responde "quem seria afetado se a 1.0.0 mudar?" |
| GRU 4 | **Manutenção periódica**: revisão mensal com checklist (dependências, issues abertas, testes, documentação). **Controle de mudança**: toda alteração do núcleo entra por pull request com integração contínua e linha no CHANGELOG | GitHub Actions rodando `mvn -pl smartscale-core verify` em cada pull request; modelo de pull request com o item "atualizei o CHANGELOG" | Cada versão no CHANGELOG corresponde a um pull request com build verde |
| GRU 5 | **Notificar os usuários**: cada versão vira um GitHub Release com as notas da versão (quem acompanha o repositório é avisado); defeitos conhecidos ficam em issues com o rótulo do ativo; descontinuidade é avisada pelo `@Deprecated` (o compilador alerta quem usa) e nas notas da versão; os produtos do registro da GRU 3 são avisados diretamente | GitHub Releases, GitHub Issues, `@Deprecated` | Cada versão publicada tem um Release, e cada produto do registro recebeu o aviso |

## 2. Desenvolvimento para Reutilização (DRU)

> Propósito: identificar oportunidades de reutilização sistemática na organização e, se possível, estabelecer um programa de reutilização para desenvolver ativos a partir da engenharia de domínios de aplicação.

### 2.1 Classificação

| Resultado esperado | Situação | Justificativa (evidência no projeto) |
|---|---|---|
| **DRU 1.** Domínios de aplicação com oportunidades de reutilização são identificados, com o respectivo potencial | 🟢 Verde | O domínio "escalas de serviço por rodízio" está identificado em `docs/VARIABILIDADE.md` e no modelo de características, com produtos-alvo (militar, hospitalar, vigilância) e o que é comum a todos (o núcleo). O potencial foi demonstrado pelo teste `MotorDeRodizioTest.outroProdutoDaLinha_usaOutroCriterioSemMudarOMotor` |
| **DRU 2.** A capacidade de reutilização sistemática da organização é avaliada e ações corretivas são tomadas | 🔴 Vermelho | A equipe não fez uma avaliação formal da própria capacidade de reutilização (ferramentas, conhecimento, processo) |
| **DRU 3.** Um programa de reutilização, com propósitos, escopo, metas e objetivos, é planejado | 🟡 Amarelo | Os planos de implementação (em especial o Plano 5) definem objetivos de reuso: extrair o núcleo, empacotá-lo e versioná-lo. Mas são planos de projeto, não um programa de reutilização com metas para vários produtos |
| **DRU 4.** O programa de reutilização é implantado, monitorado e avaliado | 🟡 Amarelo | O que foi planejado foi implantado: o núcleo existe, é empacotado e o MilScale o consome como dependência. Não há monitoramento nem avaliação dos resultados do reuso |
| **DRU 5.** Propostas de reutilização são avaliadas para garantir que o resultado seja apropriado para a aplicação alvo | 🔴 Vermelho | As decisões de reuso foram tomadas pela equipe (por exemplo, deixar elegibilidade fora do núcleo), mas sem um procedimento ou registro de avaliação de propostas |
| **DRU 6.** Formas de representação para modelos e arquiteturas de domínio são selecionadas | 🟢 Verde | **Modelo de domínio:** modelo de características no FeatureIDE (`docs/modelo-de-caracteristicas/`). **Arquitetura:** arquitetura hexagonal (domínio, aplicação, adaptadores) com regras verificadas por testes ArchUnit, e a separação núcleo × especialização × adaptadores descrita em `docs/VARIABILIDADE.md` |
| **DRU 7.** Um modelo de domínio é desenvolvido, com limites e relações com outros domínios estabelecidos e mantidos, capaz de capturar características comuns, variantes, opcionais e obrigatórias | 🟢 Verde | O modelo de características da SmartScale (73 features, 12 restrições) captura o comum (obrigatórias), o variante (grupos Or e Alternative) e o opcional, e o documento do modelo registra os limites do domínio e a relação com RH, controle de ponto e autenticação corporativa. Está versionado com o código e é validado por script |
| **DRU 8.** Uma arquitetura de domínio, descrevendo uma família de aplicações, é desenvolvida e mantida | 🟡 Amarelo | A arquitetura existe e é verificada: núcleo sem Spring nem JPA, produto especializando os contratos, adaptadores separados, ArchUnit. O que falta é ela estar descrita **para a família** (como nasce um novo produto, onde ficam os pontos de variação, quando cada um é decidido) num documento próprio, com diagrama |
| **DRU 9.** Ativos do domínio são especificados, adquiridos ou desenvolvidos, e mantidos | 🟢 Verde | O `smartscale-core` é especificado (API pública no README), desenvolvido (módulo Maven com 7 testes) e mantido (versão 1.0.0, CHANGELOG, SemVer). Os templates do gerador Database First (`database-first/templates/`) são um segundo ativo, de geração de código |

### 2.2 Como adequar o protótipo aos resultados DRU 6, 7, 8 e 9

| Resultado | Situação | Proposta | Artefato ou ferramenta |
|---|---|---|---|
| DRU 6 | 🟢 Verde | Manter as representações escolhidas e registrar a decisão: FeatureIDE para o modelo de domínio e diagrama de componentes UML para a arquitetura | Seção "Representações adotadas" em `docs/ARQUITETURA_DE_DOMINIO.md` |
| DRU 7 | 🟢 Verde | **Manter o modelo vivo:** todo pull request que cria ou remove uma feature atualiza o `model.xml`, e a integração contínua roda o `validar_modelo.py`, que reprova modelo com feature morta, falsamente opcional ou restrição redundante | GitHub Actions; `docs/modelo-de-caracteristicas/validar_modelo.py` |
| DRU 8 | 🟡 Amarelo | **Escrever a arquitetura de domínio:** as três camadas da família (núcleo, especialização, aplicação e adaptadores), os pontos de variação com o momento de decisão (critério da fila na inicialização, persistência pelo perfil do Spring, implantação pelo Compose), as regras ArchUnit que a protegem e o passo a passo de um produto novo (Plantão Hospitalar), com diagrama de componentes | `docs/ARQUITETURA_DE_DOMINIO.md` |
| DRU 9 | 🟢 Verde | **Ampliar e catalogar os ativos:** registrar no catálogo da GRU o núcleo, o modelo de características e os templates do Database First, cada um com versão e responsável; publicar o núcleo num repositório remoto (proposta da GRU 2) | `docs/CATALOGO_DE_ATIVOS.md`; GitHub Packages |

**Observação sobre DRU 6 e 7:** antes deste trabalho, o modelo de domínio era só uma árvore em texto no `docs/VARIABILIDADE.md`, sem restrições nem validação, o que daria Amarelo nos dois. O modelo de características no FeatureIDE é o que os coloca em Verde.

## 3. Resumo

| Processo | 🟢 Verde | 🟡 Amarelo | 🔴 Vermelho |
|---|---|---|---|
| GRU (5 resultados) | 0 | 4 (GRU 1, 2, 4, 5) | 1 (GRU 3) |
| DRU (9 resultados) | 4 (DRU 1, 6, 7, 9) | 3 (DRU 3, 4, 8) | 2 (DRU 2, 5) |

O projeto é mais forte em **desenvolver para reutilização** (DRU): ele tem domínio, modelo, arquitetura e ativo. É mais fraco em **gerenciar o ciclo de vida** dos ativos (GRU). Isso é esperado num projeto acadêmico com um único produto. As propostas das tabelas 1.2 e 2.2 levam a GRU para Verde sem mudar o código do núcleo: elas são de processo, publicação e documentação.
````

- [ ] **Step 2: Conferir as evidências citadas**

```bash
cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale
grep -q "ainda não está publicado num repositório remoto" smartscale-core/README.md && echo "GRU2 ok"
grep -rq "outroProdutoDaLinha_usaOutroCriterioSemMudarOMotor" smartscale-core/src/test && echo "DRU1 ok"
grep -rlq "ArchRule\|ArchUnit" backend/src/test && echo "DRU6 ok"
test -f docs/modelo-de-caracteristicas/SmartScale/model.xml && echo "DRU7 ok"
grep -c "@Test" smartscale-core/src/test/java/br/com/smartscale/core/*.java
```

Expected:
- `GRU2 ok`, `DRU1 ok`, `DRU6 ok` e `DRU7 ok`;
- a contagem de `@Test` somando 7.

A citação de `database-first/templates/` em DRU 9 só passa a valer depois da Task 5. Se a Task 5 for cancelada, troque a frase dos templates por "Um segundo ativo, de geração de código, está proposto na tabela 2.2".

- [ ] **Step 3: Checkpoint.** Mensagem sugerida: `docs(reuso): classificacao GRU e DRU do MPS.BR 2016 com propostas`.

---

### Task 4: Gerador Word/PDF e entrega dos documentos

**Files:**
- Create: `docs/entregas/gerador/package.json`
- Create: `docs/entregas/gerador/gerar-documentos.mjs`
- Create: `docs/entregas/gerador/gerar-documentos.test.mjs`
- Create: `docs/entregas/gerador/exportar-pdf.ps1`
- Generated: `docs/entregas/01-modelo-de-caracteristicas.docx` e `.pdf`, `docs/entregas/02-niveis-de-reuso-gru-dru.docx` e `.pdf`

**Interfaces:**
- **Consumes:** `docs/MODELO_DE_CARACTERISTICAS.md` (Task 2) e `docs/NIVEIS_DE_REUSO_MPSBR.md` (Task 3). Imagens são referenciadas em relação à pasta `docs/`.
- **Produces:**
  - `montarDocumento(markdown, pastaBase) -> Document`;
  - `gerar(origem, destino) -> Promise<void>`;
  - `largurasDasColunas(colunasDeTexto) -> number[]`;
  - `CORES_SITUACAO`;
  - `node gerar-documentos.mjs`, que gera os dois `.docx`.

**O que o gerador faz:**
- **Títulos:** cada `#`, `##` e `###` vira o estilo de título do Word correspondente.
- **Texto:** negrito, itálico e código inline são preservados, e as listas viram listas numeradas ou com marcadores.
- **Tabelas:** cada coluna ganha pelo menos a largura da maior palavra que contém, e a sobra vai para as colunas de texto longo. Sem isso, o Word espreme as colunas curtas.
- **Situação:** uma célula com o texto `Verde`, `Amarelo` ou `Vermelho` (com ou sem emoji) é pintada de verde, amarelo ou vermelho claro, e o emoji é retirado.
- **Imagens:**
  - são embutidas centralizadas, com no máximo 640 px de largura e 900 px de altura, para nunca saírem da página;
  - o parágrafo logo antes de uma imagem (a legenda) fica na mesma página que ela.
- **Página:** A4, margens de 2 cm e numeração no rodapé.

- [ ] **Step 1: Pacote**

`docs/entregas/gerador/package.json`:

```json
{
  "name": "smartscale-gerador-documentos",
  "private": true,
  "type": "module",
  "scripts": {
    "test": "node --test",
    "gerar": "node gerar-documentos.mjs"
  },
  "dependencies": {
    "docx": "9.15.3",
    "marked": "14.1.4"
  },
  "devDependencies": {
    "jszip": "3.10.2"
  }
}
```

```bash
cd docs/entregas/gerador && npm install
```

- [ ] **Step 2: Testes (falham)**

`docs/entregas/gerador/gerar-documentos.test.mjs`:

```javascript
import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { test } from "node:test";
import { Packer } from "docx";
import JSZip from "jszip";
import { CORES_SITUACAO, largurasDasColunas, montarDocumento } from "./gerar-documentos.mjs";

const PNG_2X1 = Buffer.from(
  "iVBORw0KGgoAAAANSUhEUgAAAAIAAAABCAYAAAD0In+KAAAAEUlEQVR4nGNgYGD4z8DAwMAAAAQAAWnF0Q8AAAAASUVORK5CYII=",
  "base64",
);

async function xmlDe(markdown, arquivos = {}) {
  const pasta = mkdtempSync(join(tmpdir(), "gerador-"));
  for (const [nome, dados] of Object.entries(arquivos)) writeFileSync(join(pasta, nome), dados);
  const zip = await JSZip.loadAsync(await Packer.toBuffer(montarDocumento(markdown, pasta)));
  return {
    documento: await zip.file("word/document.xml").async("string"),
    midias: Object.keys(zip.files).filter((n) => n.startsWith("word/media/")),
  };
}

test("pinta as células de situação com a cor certa e tira o emoji", async () => {
  const { documento } = await xmlDe(
    "| Item | Situação |\n|---|---|\n| GRU 1 | 🟢 Verde |\n| GRU 2 | Amarelo |\n| GRU 3 | 🔴 Vermelho |\n",
  );
  for (const cor of Object.values(CORES_SITUACAO)) assert.match(documento, new RegExp(`w:fill="${cor}"`));
  assert.doesNotMatch(documento, /🟢|🔴/u);
  assert.match(documento, />Verde</);
});

test("títulos viram estilos de título do Word", async () => {
  const { documento } = await xmlDe("# Título\n\n## Seção\n\n### Subseção\n");
  assert.match(documento, /w:pStyle w:val="Heading1"/);
  assert.match(documento, /w:pStyle w:val="Heading2"/);
  assert.match(documento, /w:pStyle w:val="Heading3"/);
});

test("imagem do markdown é embutida no documento", async () => {
  const { documento, midias } = await xmlDe("![modelo](modelo.png)\n", { "modelo.png": PNG_2X1 });
  assert.equal(midias.length, 1);
  assert.match(documento, /<pic:pic/);
});

test("negrito, código e listas são preservados", async () => {
  const { documento } = await xmlDe("Texto **forte** e `codigo`.\n\n- um\n- dois\n");
  assert.match(documento, /<w:b\/>/);
  assert.match(documento, /Consolas/);
  assert.equal((documento.match(/<w:numPr>/g) ?? []).length, 2);
});

test("coluna curta não quebra palavra e a tabela ocupa a largura útil", () => {
  const larguras = largurasDasColunas([
    ["Código", "R01", "R02"],
    ["Operadores", "Implies, And", "Implies"],
    ["Justificativa", "A troca precisa conferir o descanso de quem assume e avisar o substituto pelo sistema"],
  ]);
  assert.equal(larguras.reduce((a, b) => a + b, 0), 9638);
  assert.ok(larguras[1] >= 12 * 115, `Operadores ficou com ${larguras[1]}`);
  assert.ok(larguras[2] > larguras[1], "a coluna de texto longo recebe a sobra");
});

test("imagem alta é reduzida para caber na altura da página", async () => {
  const alta = Buffer.from(PNG_2X1);
  alta.writeUInt32BE(400, 16);
  alta.writeUInt32BE(3000, 20);
  const { documento } = await xmlDe("![alta](alta.png)\n", { "alta.png": alta });
  const [, largura, altura] = documento.match(/<wp:extent cx="(\d+)" cy="(\d+)"/);
  assert.ok(Number(altura) <= 900 * 9525, `altura ${altura} passa da página`);
  assert.equal(Math.round((Number(largura) / Number(altura)) * 3000), 400, "mantém a proporção");
});

test("o parágrafo antes de uma imagem fica na mesma página que ela", async () => {
  const { documento } = await xmlDe("Legenda da figura\n\n![figura](figura.png)\n\nTexto solto\n", { "figura.png": PNG_2X1 });
  const paragrafos = documento.split("<w:p>").slice(1);
  const legenda = paragrafos.find((p) => p.includes("Legenda da figura"));
  const solto = paragrafos.find((p) => p.includes("Texto solto"));
  assert.match(legenda, /<w:keepNext\/>/);
  assert.doesNotMatch(solto, /<w:keepNext\/>/);
});
```

Run: `npm test`

Expected: FAIL, porque `gerar-documentos.mjs` ainda não existe.

- [ ] **Step 3: Gerador**

`docs/entregas/gerador/gerar-documentos.mjs`:

```javascript
import { readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import {
  AlignmentType, BorderStyle, Document, Footer, HeadingLevel, ImageRun, LevelFormat, Packer,
  PageNumber, Paragraph, ShadingType, Table, TableCell, TableLayoutType, TableRow, TextRun, WidthType,
} from "docx";
import { marked } from "marked";

const LARGURA_UTIL = 9638;
const LARGURA_IMAGEM_MAX = 640;
const ALTURA_IMAGEM_MAX = 900;
const LARGURA_CARACTERE = 120;
const PALAVRA_MAXIMA = 18;
const FONTE = "Arial";

export const CORES_SITUACAO = {
  Verde: "C6EFCE",
  Amarelo: "FFEB9C",
  Vermelho: "FFC7CE",
};

const SITUACAO = /^(?:🟢|🟡|🔴)?\s*(Verde|Amarelo|Vermelho)$/u;

function decodificar(texto) {
  return texto
    .replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"').replace(/&#39;/g, "'");
}

function trechos(tokens, estilo = {}) {
  const runs = [];
  for (const t of tokens ?? []) {
    if (t.type === "strong") runs.push(...trechos(t.tokens, { ...estilo, bold: true }));
    else if (t.type === "em") runs.push(...trechos(t.tokens, { ...estilo, italics: true }));
    else if (t.type === "codespan") runs.push(new TextRun({ text: decodificar(t.text), font: "Consolas", ...estilo }));
    else if (t.type === "link") runs.push(...trechos(t.tokens, { ...estilo, color: "1F4E79", underline: {} }));
    else if (t.type === "br") runs.push(new TextRun({ break: 1 }));
    else if (t.tokens) runs.push(...trechos(t.tokens, estilo));
    else runs.push(new TextRun({ text: decodificar(t.text ?? t.raw ?? ""), font: FONTE, ...estilo }));
  }
  return runs;
}

function tamanhoPng(buffer) {
  return { largura: buffer.readUInt32BE(16), altura: buffer.readUInt32BE(20) };
}

function imagem(token, pastaBase) {
  const dados = readFileSync(resolve(pastaBase, decodeURI(token.href)));
  const { largura, altura } = tamanhoPng(dados);
  const escala = Math.min(1, LARGURA_IMAGEM_MAX / largura, ALTURA_IMAGEM_MAX / altura);
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120, after: 120 },
    children: [new ImageRun({
      type: "png",
      data: dados,
      transformation: { width: Math.round(largura * escala), height: Math.round(altura * escala) },
    })],
  });
}

function celula(token, largura, cabecalho) {
  const texto = decodificar(token.text).trim();
  const situacao = texto.match(SITUACAO);
  const fundo = cabecalho ? "D9E2F3" : situacao ? CORES_SITUACAO[situacao[1]] : undefined;
  const filhos = situacao
    ? [new TextRun({ text: situacao[1], bold: true, font: FONTE })]
    : trechos(token.tokens, cabecalho ? { bold: true } : {});
  return new TableCell({
    width: { size: largura, type: WidthType.DXA },
    shading: fundo ? { type: ShadingType.CLEAR, color: "auto", fill: fundo } : undefined,
    margins: { top: 60, bottom: 60, left: 100, right: 100 },
    children: [new Paragraph({ children: filhos })],
  });
}

export function largurasDasColunas(colunasDeTexto) {
  const soma = (lista) => lista.reduce((a, b) => a + b, 0);
  const medidas = colunasDeTexto.map((textos) => {
    const maiorPalavra = Math.max(...textos.flatMap((t) => t.split(/\s+/)).map((p) => p.length));
    const maiorTexto = Math.max(...textos.map((t) => t.length));
    return {
      base: (Math.min(maiorPalavra, PALAVRA_MAXIMA) + 3) * LARGURA_CARACTERE,
      folga: Math.max(0, Math.min(maiorTexto, 160) - maiorPalavra),
    };
  });
  const bases = medidas.map((m) => m.base);
  const sobra = LARGURA_UTIL - soma(bases);
  const folgas = medidas.map((m) => m.folga);
  const partes = soma(folgas) > 0 ? folgas : medidas.map(() => 1);
  const larguras = sobra < 0
    ? bases.map((b) => Math.floor((b * LARGURA_UTIL) / soma(bases)))
    : bases.map((b, i) => Math.floor(b + (sobra * partes[i]) / soma(partes)));
  larguras[larguras.length - 1] += LARGURA_UTIL - soma(larguras);
  return larguras;
}

function tabela(token) {
  const colunasDeTexto = token.header.map((cabecalho, i) =>
    [cabecalho, ...token.rows.map((linha) => linha[i])].map((c) => decodificar(c.text).trim()));
  const larguras = largurasDasColunas(colunasDeTexto);
  const linhas = [
    new TableRow({ tableHeader: true, children: token.header.map((c, i) => celula(c, larguras[i], true)) }),
    ...token.rows.map((linha) => new TableRow({ children: linha.map((c, i) => celula(c, larguras[i], false)) })),
  ];
  return new Table({
    width: { size: LARGURA_UTIL, type: WidthType.DXA },
    columnWidths: larguras,
    layout: TableLayoutType.FIXED,
    rows: linhas,
  });
}

const NIVEL = { 1: HeadingLevel.HEADING_1, 2: HeadingLevel.HEADING_2, 3: HeadingLevel.HEADING_3 };

const temImagem = (t) => t?.type === "paragraph" && t.tokens.some((x) => x.type === "image");

function blocos(tokens, pastaBase, nivelLista = 0) {
  const saida = [];
  const conteudo = tokens.filter((t) => t.type !== "space");
  for (const [i, t] of conteudo.entries()) {
    if (t.type === "heading") {
      saida.push(new Paragraph({ heading: NIVEL[Math.min(t.depth, 3)], children: trechos(t.tokens) }));
    } else if (t.type === "paragraph") {
      const imagens = t.tokens.filter((x) => x.type === "image");
      if (imagens.length) imagens.forEach((img) => saida.push(imagem(img, pastaBase)));
      else saida.push(new Paragraph({ spacing: { after: 120 }, keepNext: temImagem(conteudo[i + 1]), children: trechos(t.tokens) }));
    } else if (t.type === "list") {
      for (const item of t.items) {
        const [primeiro, ...resto] = item.tokens;
        const texto = primeiro?.tokens ?? [];
        saida.push(new Paragraph({
          numbering: { reference: t.ordered ? "numeros" : "marcadores", level: nivelLista },
          children: trechos(texto),
        }));
        saida.push(...blocos(resto.filter((x) => x.type === "list"), pastaBase, nivelLista + 1));
      }
    } else if (t.type === "table") {
      saida.push(tabela(t), new Paragraph({ spacing: { after: 120 }, children: [] }));
    } else if (t.type === "blockquote") {
      for (const p of t.tokens.filter((x) => x.type === "paragraph")) {
        saida.push(new Paragraph({
          indent: { left: 360 },
          border: { left: { style: BorderStyle.SINGLE, size: 12, color: "8EAADB", space: 8 } },
          children: trechos(p.tokens, { italics: true }),
        }));
      }
    } else if (t.type === "code") {
      for (const linha of t.text.split("\n")) {
        saida.push(new Paragraph({
          shading: { type: ShadingType.CLEAR, color: "auto", fill: "F2F2F2" },
          spacing: { after: 0 },
          children: [new TextRun({ text: linha || " ", font: "Consolas", size: 18 })],
        }));
      }
      saida.push(new Paragraph({ children: [] }));
    }
  }
  return saida;
}

const ESTILO_TITULO = (tamanho, antes) => ({
  run: { font: FONTE, size: tamanho, bold: true, color: "1F3864" },
  paragraph: { spacing: { before: antes, after: 120 }, keepNext: true },
});

export function montarDocumento(markdown, pastaBase) {
  const tokens = marked.lexer(markdown);
  return new Document({
    styles: {
      default: {
        document: { run: { font: FONTE, size: 22 } },
        heading1: ESTILO_TITULO(32, 360),
        heading2: ESTILO_TITULO(26, 280),
        heading3: ESTILO_TITULO(23, 200),
      },
    },
    numbering: {
      config: ["marcadores", "numeros"].map((reference) => ({
        reference,
        levels: [0, 1, 2].map((level) => ({
          level,
          format: reference === "numeros" ? LevelFormat.DECIMAL : LevelFormat.BULLET,
          text: reference === "numeros" ? `%${level + 1}.` : ["•", "◦", "▪"][level],
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720 * (level + 1), hanging: 360 } } },
        })),
      })),
    },
    sections: [{
      properties: { page: { margin: { top: 1134, bottom: 1134, left: 1134, right: 1134 } } },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ children: [PageNumber.CURRENT], font: FONTE, size: 18 })],
          })],
        }),
      },
      children: blocos(tokens, pastaBase),
    }],
  });
}

export async function gerar(origem, destino) {
  const documento = montarDocumento(readFileSync(origem, "utf8"), dirname(origem));
  writeFileSync(destino, await Packer.toBuffer(documento));
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const raiz = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..", "..");
  const entregas = [
    ["docs/MODELO_DE_CARACTERISTICAS.md", "docs/entregas/01-modelo-de-caracteristicas.docx"],
    ["docs/NIVEIS_DE_REUSO_MPSBR.md", "docs/entregas/02-niveis-de-reuso-gru-dru.docx"],
  ];
  for (const [origem, destino] of entregas) {
    await gerar(resolve(raiz, origem), resolve(raiz, destino));
    console.log(`gerado: ${destino}`);
  }
}
```

Run: `npm test`

Expected: `ℹ pass 7`, `ℹ fail 0`.

- [ ] **Step 4: Exportação para PDF pelo Word**

`docs/entregas/gerador/exportar-pdf.ps1`:

```powershell
param([Parameter(Mandatory)][string]$Pasta)
$arquivos = Get-ChildItem -Path $Pasta -Filter *.docx | Where-Object { -not $_.Name.StartsWith('~$') }
if (-not $arquivos) { throw "Nenhum .docx em $Pasta" }
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    foreach ($arquivo in $arquivos) {
        $pdf = [System.IO.Path]::ChangeExtension($arquivo.FullName, '.pdf')
        $doc = $word.Documents.Open($arquivo.FullName, $false, $true)
        $doc.ExportAsFixedFormat($pdf, 17)
        $doc.Close($false)
        "PDF: $pdf"
    }
} finally {
    $word.Quit()
}
```

- [ ] **Step 5: Gerar a entrega**

```bash
cd docs/entregas/gerador
npm run gerar
powershell -NoProfile -ExecutionPolicy Bypass -File exportar-pdf.ps1 -Pasta ..
ls -la ..
```

Expected: `01-modelo-de-caracteristicas.docx/.pdf` e `02-niveis-de-reuso-gru-dru.docx/.pdf` em `docs/entregas/`.

- [ ] **Step 6: Conferência visual.** Abra os dois PDFs e confira:
  - **Imagem:** a imagem do modelo aparece inteira e legível. Se ficar pequena demais, exporte de novo com outro layout.
  - **Tabelas:** nenhuma coluna quebra uma palavra curta letra por letra.
  - **Cores:** as células Verde, Amarelo e Vermelho estão pintadas.
  - **Páginas:** os números de página aparecem no rodapé.

- [ ] **Step 7: Checkpoint.** Mostrar os PDFs. Mensagem sugerida: `docs(entregas): gerador word/pdf e documentos do modelo e dos niveis de reuso`.

---

### Task 5: Database First: gerador (entidades, repositórios, controllers e telas)

**Files:**
- Create: `database-first/pom.xml`
- Create: `database-first/hibernate.reveng.xml`
- Create: `database-first/templates/repositorio.ftl`, `controlador.ftl`, `lista.ftl`, `formulario.ftl`, `inicio.ftl`
- Create: `database-first/src/main/java/br/com/smartscale/dbfirst/DatabaseFirstApplication.java`
- Create: `database-first/src/main/java/br/com/smartscale/dbfirst/Rotulo.java`
- Create: `database-first/src/main/resources/application.properties`
- Create: `database-first/src/main/resources/static/estilo.css`
- Create: `database-first/preparar-banco.sh`
- Create: `database-first/verificar.sh`
- Generated (não versionado): `database-first/banco.properties`, `database-first/target/generated-sources/dbfirst/**`, `database-first/target/generated-resources/dbfirst/**`

**Interfaces:**
- **Consumes:**
  - o `.env` da raiz: `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`;
  - o serviço `mysql` do `docker-compose.yml` com as tabelas do MilScale.
- **Produces:**
  - o banco `milscale_dbfirst`;
  - no pacote `br.com.smartscale.dbfirst.entidades`, para cada tabela: `<Entidade>`, `<Entidade>Repository extends JpaRepository` e `<Entidade>Controller`;
  - as telas `<Entidade>/lista.html` e `<Entidade>/formulario.html`, e a página inicial `index.html`;
  - as rotas `http://localhost:8081/` e `/<entidade>`, `/<entidade>/novo`, `/<entidade>/{id}` e `POST /<entidade>/{id}/excluir`.

**Como funciona:**
- **O motor:** o `maven-antrun-plugin` executa o Hibernate Tools (tarefa Ant `hibernatetool`) na fase `generate-sources`. São dois passos: um gera o código Java e o outro gera as telas.
- **As entidades:** vêm do exportador padrão `hbm2java`.
- **O resto:** os demais arquivos vêm do `hbmtemplate`, que aplica um template FreeMarker a cada entidade (`foreach="entity"`) e decide o nome do arquivo pelo `filepattern`.
- **As duas sintaxes nos templates:** `${...}` é o gerador preenchendo o molde; `*{...}` é o Thymeleaf mostrando os dados quando a tela roda.
- **Tabelas de chave composta:** os templates de controller e telas produzem saída vazia para elas, e o Hibernate Tools não cria arquivo vazio.

- [x] **Step 1: Configuração do gerador**

`database-first/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
    <relativePath/>
  </parent>

  <groupId>br.com.smartscale</groupId>
  <artifactId>smartscale-database-first</artifactId>
  <version>0.1.0</version>
  <name>SmartScale Database First</name>
  <description>Classes, repositórios, controllers e telas gerados a partir do banco do MilScale.</description>

  <properties>
    <java.version>21</java.version>
    <hibernate-tools.version>6.5.3.Final</hibernate-tools.version>
    <dbfirst.pacote>br.com.smartscale.dbfirst.entidades</dbfirst.pacote>
    <dbfirst.codigo>${project.build.directory}/generated-sources/dbfirst</dbfirst.codigo>
    <dbfirst.telas>${project.build.directory}/generated-resources/dbfirst</dbfirst.telas>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <artifactId>maven-antrun-plugin</artifactId>
        <executions>
          <execution>
            <id>gerar-a-partir-do-banco</id>
            <phase>generate-sources</phase>
            <goals><goal>run</goal></goals>
            <configuration>
              <target>
                <taskdef name="hibernatetool" classname="org.hibernate.tool.ant.HibernateToolTask"
                         classpathref="maven.plugin.classpath"/>

                <hibernatetool destdir="${dbfirst.codigo}" templatepath="${project.basedir}/templates">
                  <jdbcconfiguration propertyfile="${project.basedir}/banco.properties"
                                     revengfile="${project.basedir}/hibernate.reveng.xml"
                                     packagename="${dbfirst.pacote}" detectmanytomany="false"/>
                  <hbm2java ejb3="true" jdk5="true"/>
                  <hbmtemplate template="repositorio.ftl" filepattern="{package-name}/{class-name}Repository.java" foreach="entity"/>
                  <hbmtemplate template="controlador.ftl" filepattern="{package-name}/{class-name}Controller.java" foreach="entity"/>
                </hibernatetool>

                <hibernatetool destdir="${dbfirst.telas}/templates" templatepath="${project.basedir}/templates">
                  <jdbcconfiguration propertyfile="${project.basedir}/banco.properties"
                                     revengfile="${project.basedir}/hibernate.reveng.xml"
                                     packagename="${dbfirst.pacote}" detectmanytomany="false"/>
                  <hbmtemplate template="lista.ftl" filepattern="{class-name}/lista.html" foreach="entity"/>
                  <hbmtemplate template="formulario.ftl" filepattern="{class-name}/formulario.html" foreach="entity"/>
                  <hbmtemplate template="inicio.ftl" filepattern="index.html" foreach="configuration"/>
                </hibernatetool>
              </target>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>org.hibernate.tool</groupId>
            <artifactId>hibernate-tools-ant</artifactId>
            <version>${hibernate-tools.version}</version>
          </dependency>
          <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>${mysql.version}</version>
          </dependency>
        </dependencies>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>usar-codigo-gerado</id>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
              <sources><source>${dbfirst.codigo}</source></sources>
            </configuration>
          </execution>
          <execution>
            <id>usar-telas-geradas</id>
            <phase>generate-resources</phase>
            <goals><goal>add-resource</goal></goals>
            <configuration>
              <resources><resource><directory>${dbfirst.telas}</directory></resource></resources>
            </configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

`database-first/hibernate.reveng.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-reverse-engineering PUBLIC "-//Hibernate/Hibernate Reverse Engineering DTD 3.0//EN"
        "http://hibernate.org/dtd/hibernate-reverse-engineering-3.0.dtd">
<hibernate-reverse-engineering>
  <type-mapping>
    <sql-type jdbc-type="DATE" hibernate-type="java.time.LocalDate"/>
    <sql-type jdbc-type="TIMESTAMP" hibernate-type="java.time.LocalDateTime"/>
    <sql-type jdbc-type="TIME" hibernate-type="java.time.LocalTime"/>
  </type-mapping>
  <table-filter match-name="flyway_schema_history" exclude="true"/>
  <table name="afastamento"><primary-key><generator class="identity"/></primary-key></table>
  <table name="boletim"><primary-key><generator class="identity"/></primary-key></table>
  <table name="feriado"><primary-key><generator class="identity"/></primary-key></table>
  <table name="notificacao"><primary-key><generator class="identity"/></primary-key></table>
  <table name="solicitacao"><primary-key><generator class="identity"/></primary-key></table>
</hibernate-reverse-engineering>
```

- [x] **Step 2: Templates**

`database-first/templates/repositorio.ftl`:

```text
${pojo.getPackageDeclaration()}
// Gerado pelo Hibernate Tools a partir da tabela ${clazz.table.name}

import org.springframework.data.jpa.repository.JpaRepository;

<#assign entidade = pojo.getDeclarationName()>
<#assign tipoId = pojo.getJavaTypeName(clazz.identifierProperty, true)>
<#if tipoId == "long"><#assign tipoId = "Long"><#elseif tipoId == "int"><#assign tipoId = "Integer"></#if>
public interface ${entidade}Repository extends JpaRepository<${entidade}, ${tipoId}> {
}
```

`database-first/templates/controlador.ftl`:

```text
<#if !c2j.isComponent(clazz.identifierProperty)>
<#assign entidade = pojo.getDeclarationName()>
<#assign rota = entidade?uncap_first>
<#assign tipoId = pojo.getJavaTypeName(clazz.identifierProperty, true)>
<#if tipoId == "long"><#assign tipoId = "Long"><#elseif tipoId == "int"><#assign tipoId = "Integer"></#if>
<#assign relacoes = []>
<#list pojo.getAllPropertiesIterator() as campo>
<#if c2h.isManyToOne(campo)>
<#assign tipo = pojo.getJavaTypeName(campo, true)>
<#if !relacoes?seq_contains(tipo)><#assign relacoes = relacoes + [tipo]></#if>
</#if>
</#list>
${pojo.getPackageDeclaration()}
// Gerado pelo Hibernate Tools a partir da tabela ${clazz.table.name}

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/${rota}")
public class ${entidade}Controller {

    private final ${entidade}Repository repositorio;
<#list relacoes as r>
    private final ${r}Repository repositorio${r};
</#list>

    public ${entidade}Controller(${entidade}Repository repositorio<#list relacoes as r>, ${r}Repository repositorio${r}</#list>) {
        this.repositorio = repositorio;
<#list relacoes as r>
        this.repositorio${r} = repositorio${r};
</#list>
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("registros", repositorio.findAll());
        return "${entidade}/lista";
    }

    @GetMapping({"/novo", "/{id}"})
    public String formulario(@PathVariable(required = false) ${tipoId} id, Model model) {
        ${entidade} registro = id == null ? new ${entidade}() : repositorio.findById(id).orElseThrow();
        model.addAttribute("registro", registro);
<#list relacoes as r>
        model.addAttribute("opcoes${r}", repositorio${r}.findAll());
</#list>
        return "${entidade}/formulario";
    }

    @PostMapping
    public String salvar(@ModelAttribute("registro") ${entidade} registro) {
        repositorio.save(registro);
        return "redirect:/${rota}";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable ${tipoId} id) {
        repositorio.deleteById(id);
        return "redirect:/${rota}";
    }
}
</#if>
```

`database-first/templates/lista.ftl`:

```html
<#if !c2j.isComponent(clazz.identifierProperty)>
<#assign entidade = pojo.getDeclarationName()>
<#assign rota = entidade?uncap_first>
<#assign id = clazz.identifierProperty.name>
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>${entidade}</title>
  <link rel="stylesheet" href="/estilo.css">
</head>
<body>
<nav><a href="/">Todas as tabelas</a></nav>
<h1>${entidade}</h1>
<p class="origem">Tela gerada a partir da tabela <code>${clazz.table.name}</code></p>
<a class="botao" href="/${rota}/novo">Novo registro</a>
<div class="rolagem">
<table>
  <thead>
  <tr>
    <th>${id}</th>
<#list pojo.getAllPropertiesIterator() as campo>
<#if campo.name != id && !c2h.isCollection(campo)>
    <th>${campo.name}</th>
</#if>
</#list>
    <th></th>
  </tr>
  </thead>
  <tbody>
  <tr th:each="r : *{registros}">
    <td th:text="*{r.${id}}"></td>
<#list pojo.getAllPropertiesIterator() as campo>
<#if campo.name != id && !c2h.isCollection(campo)>
<#if c2h.isManyToOne(campo)>
    <td th:text="*{@rotulo.de(r.${campo.name})}"></td>
<#else>
    <td th:text="*{r.${campo.name}}"></td>
</#if>
</#if>
</#list>
    <td class="acoes">
      <a th:href="@{/${rota}/{id}(id=*{r.${id}})}">Editar</a>
      <form method="post" th:action="@{/${rota}/{id}/excluir(id=*{r.${id}})}">
        <button type="submit">Excluir</button>
      </form>
    </td>
  </tr>
  </tbody>
</table>
</div>
</body>
</html>
</#if>
```

`database-first/templates/formulario.ftl`:

```html
<#if !c2j.isComponent(clazz.identifierProperty)>
<#assign entidade = pojo.getDeclarationName()>
<#assign rota = entidade?uncap_first>
<#assign id = clazz.identifierProperty.name>
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>${entidade}</title>
  <link rel="stylesheet" href="/estilo.css">
</head>
<body>
<nav><a href="/${rota}">Voltar para ${entidade}</a></nav>
<h1>${entidade}</h1>
<p class="origem">Formulário gerado a partir da tabela <code>${clazz.table.name}</code></p>
<form method="post" action="/${rota}">
  <input type="hidden" name="${id}" th:value="*{registro.${id}}">
<#list pojo.getAllPropertiesIterator() as campo>
<#if campo.name != id && !c2h.isCollection(campo)>
<#assign tipo = pojo.getJavaTypeName(campo, true)>
  <label>${campo.name}
<#if c2h.isManyToOne(campo)>
<#assign alvo = md.getEntityBinding(campo.value.referencedEntityName)>
<#assign idAlvo = alvo.identifierProperty.name>
    <select name="${campo.name}">
      <option value="">(nenhum)</option>
      <option th:each="o : *{opcoes${tipo}}" th:value="*{o.${idAlvo}}" th:text="*{@rotulo.de(o)}"
              th:selected="*{registro.${campo.name} != null and registro.${campo.name}.${idAlvo} == o.${idAlvo}}"></option>
    </select>
<#elseif tipo == "boolean" || tipo == "Boolean">
    <input type="hidden" name="_${campo.name}" value="on">
    <input type="checkbox" name="${campo.name}" th:checked="*{registro.${campo.name}}">
<#elseif tipo == "LocalDate">
    <input type="date" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "LocalDateTime">
    <input type="datetime-local" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "LocalTime">
    <input type="time" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "long" || tipo == "Long" || tipo == "int" || tipo == "Integer" || tipo == "short" || tipo == "Short" || tipo == "byte" || tipo == "Byte">
    <input type="number" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#elseif tipo == "BigDecimal" || tipo == "double" || tipo == "Double" || tipo == "float" || tipo == "Float">
    <input type="number" step="any" name="${campo.name}" th:value="*{registro.${campo.name}}">
<#else>
    <input type="text" name="${campo.name}" th:value="*{registro.${campo.name}}">
</#if>
  </label>
</#if>
</#list>
  <button type="submit">Salvar</button>
</form>
</body>
</html>
</#if>
```

`database-first/templates/inicio.ftl`:

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <title>SmartScale · Database First</title>
  <link rel="stylesheet" href="/estilo.css">
</head>
<body>
<h1>SmartScale · Database First</h1>
<p class="origem">Cada link abaixo é uma tabela do banco. As telas e o código foram gerados a partir dela.</p>
<ul class="tabelas">
<#list md.getEntityBindings() as tabela>
<#if !c2j.isComponent(tabela.identifierProperty)>
<#assign entidade = tabela.className?keep_after_last(".")>
  <li><a href="/${entidade?uncap_first}">${entidade}</a> <code>${tabela.table.name}</code></li>
</#if>
</#list>
</ul>
</body>
</html>
```

- [x] **Step 3: Arquivos de apoio escritos à mão** (não dependem de nenhuma tabela)

`database-first/src/main/java/br/com/smartscale/dbfirst/DatabaseFirstApplication.java`:

```java
package br.com.smartscale.dbfirst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatabaseFirstApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseFirstApplication.class, args);
    }
}
```

`database-first/src/main/java/br/com/smartscale/dbfirst/Rotulo.java`:

```java
package br.com.smartscale.dbfirst;

import java.util.List;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component("rotulo")
public class Rotulo {

    private static final List<String> CAMPOS_DE_NOME = List.of("nomeGuerra", "nome", "sigla", "titulo", "descricao", "login", "data");

    public String de(Object registro) {
        if (registro == null) {
            return "";
        }
        BeanWrapper bean = new BeanWrapperImpl(registro);
        for (String campo : CAMPOS_DE_NOME) {
            if (bean.isReadableProperty(campo) && bean.getPropertyValue(campo) != null) {
                return bean.getPropertyValue(campo).toString();
            }
        }
        return registro.getClass().getSimpleName();
    }
}
```

`database-first/src/main/resources/application.properties`:

```properties
spring.application.name=smartscale-database-first
server.port=8081
spring.config.import=optional:file:./banco.properties[.properties]
spring.datasource.url=${hibernate.connection.url}
spring.datasource.username=${hibernate.connection.username}
spring.datasource.password=${hibernate.connection.password}
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=true
spring.mvc.format.date=iso
spring.mvc.format.date-time=iso
spring.mvc.format.time=iso
server.error.include-message=always
```

`database-first/src/main/resources/static/estilo.css`:

```css
body { font-family: system-ui, sans-serif; margin: 24px; color: #1f2a18; background: #f4f5f0; }
h1 { margin: 8px 0 4px; }
.origem { color: #55604b; margin: 0 0 16px; }
nav a, a { color: #3a4a27; }
.botao { display: inline-block; background: #4a5d32; color: #fff; padding: 6px 12px; border-radius: 4px; text-decoration: none; margin-bottom: 12px; }
.rolagem { overflow-x: auto; }
table { border-collapse: collapse; background: #fff; font-size: 14px; }
th, td { border: 1px solid #d8d3c0; padding: 6px 8px; text-align: left; white-space: nowrap; }
th { background: #e6e2d4; }
td.acoes form { display: inline; }
form label { display: block; margin-bottom: 10px; font-weight: 600; }
form label input, form label select { display: block; margin-top: 4px; padding: 4px 6px; min-width: 280px; font-weight: 400; }
form label input[type=checkbox] { min-width: 0; }
button { cursor: pointer; }
.tabelas li { margin: 4px 0; }
```

- [x] **Step 4: Scripts**

`database-first/preparar-banco.sh`:

```bash
#!/usr/bin/env bash
# Cria o banco milscale_dbfirst, cópia do banco do MilScale, e o banco.properties usado pelo gerador.
set -euo pipefail
AQUI="$(cd "$(dirname "$0")" && pwd)"
RAIZ="${RAIZ:-$(cd "$AQUI/.." && pwd)}"

set -a
source "$RAIZ/.env"
set +a

compose() { docker compose --project-directory "$RAIZ" -f "$RAIZ/docker-compose.yml" "$@"; }
mysql_root() { compose exec -T mysql sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" $1" 2>/dev/null; }

compose up -d mysql
until compose exec -T mysql sh -c 'mysqladmin ping -h localhost -uroot -p"$MYSQL_ROOT_PASSWORD"' 2>/dev/null | grep -q alive; do
  sleep 2
done

tabelas=$(mysql_root "-N -e \"select count(*) from information_schema.tables where table_schema='$MYSQL_DATABASE'\"")
if [ "${tabelas:-0}" -lt 2 ]; then
  echo "O banco '$MYSQL_DATABASE' ainda está vazio. Suba o MilScale uma vez (docker compose up -d) para o Flyway criar as tabelas."
  exit 1
fi

mysql_root "-e \"DROP DATABASE IF EXISTS milscale_dbfirst; CREATE DATABASE milscale_dbfirst; GRANT ALL ON milscale_dbfirst.* TO '$MYSQL_USER'@'%';\""
compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --no-tablespaces "$MYSQL_DATABASE" | mysql -uroot -p"$MYSQL_ROOT_PASSWORD" milscale_dbfirst' 2>/dev/null

cat > "$AQUI/banco.properties" <<EOF
hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver
hibernate.connection.url=jdbc:mysql://localhost:3306/milscale_dbfirst?allowPublicKeyRetrieval=true&useSSL=false
hibernate.connection.username=$MYSQL_USER
hibernate.connection.password=$MYSQL_PASSWORD
hibernate.default_catalog=milscale_dbfirst
EOF

echo "Banco milscale_dbfirst pronto ($tabelas tabelas copiadas de $MYSQL_DATABASE)."
```

`database-first/verificar.sh`:

```bash
#!/usr/bin/env bash
# Confere, com a aplicação no ar, que as telas e o CRUD gerados a partir do banco funcionam.
set -euo pipefail
SITE="${SITE:-http://localhost:8081}"
status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

rotas=$(curl -sf "$SITE/" | grep -oE 'href="/[A-Za-z]+"' | sed 's/href="//; s/"//')
total=$(echo "$rotas" | wc -l)
echo "Tabelas na página inicial: $total"
[ "$total" -eq 17 ]

for rota in $rotas; do
  [ "$(status "$SITE$rota")" = "200" ] && [ "$(status "$SITE$rota/novo")" = "200" ] || { echo "Falhou: $rota"; exit 1; }
done
echo "Listas e formulários das $total tabelas: OK"

campos=(--data-urlencode "dataInicio=2026-12-24" --data-urlencode "dataFim=2026-12-25" --data-urlencode "tipo=NACIONAL")
[ "$(status -X POST "$SITE/feriado" --data-urlencode "idFeriado=" "${campos[@]}" --data-urlencode "descricao=Teste Database First")" = "302" ]
id=$(curl -sf "$SITE/feriado" | grep -oE '/feriado/[0-9]+"' | tail -1 | grep -oE '[0-9]+')
echo "Criar:   feriado $id salvo pelo formulário"

[ "$(status -X POST "$SITE/feriado" --data-urlencode "idFeriado=$id" "${campos[@]}" --data-urlencode "descricao=Teste editado")" = "302" ]
curl -sf "$SITE/feriado" | grep -q "Teste editado"
echo "Editar:  descrição alterada"

[ "$(status -X POST "$SITE/feriado/$id/excluir")" = "302" ]
! curl -sf "$SITE/feriado" | grep -q "Teste editado"
echo "Excluir: feriado removido"

echo "Telas e CRUD gerados a partir do banco: OK ($SITE)"
```

- [x] **Step 5: Gerar, subir e verificar**

```bash
cd database-first
bash preparar-banco.sh
export JAVA_HOME="/c/Program Files/Java/jdk-23"
mvn spring-boot:run
```

Em outro terminal, na mesma pasta: `bash verificar.sh`.

Resultado em 2026-11-11:
- **Arquivos gerados:** 57 arquivos Java (21 entidades, 19 repositórios e 17 controllers) e 35 telas.
- **Telas:** a página inicial lista 17 tabelas, e as 17 listas e os 17 formulários respondem 200.
- **CRUD pelo formulário:** criar, editar e excluir um feriado funcionaram.
- **Chave estrangeira:** um afastamento com militar e usuário foi criado, e o militar certo aparece selecionado ao editar.

- [x] **Step 6: A raiz continua intacta.** O `mvn -q install` na raiz continua verde, e o `git status` lista só os 13 arquivos dos Steps 1 a 4.

- [ ] **Step 7: Checkpoint.** Mensagem sugerida: `feat(database-first): entidades, repositorios, controllers e telas gerados a partir do banco`.

---

### Task 6: Database First: documentação e roteiro

**Files:**
- Create: `docs/DATABASE_FIRST.md`
- Create: `database-first/ROTEIRO_DEMONSTRACAO.md`

- [x] **Step 1:** `docs/DATABASE_FIRST.md` explica:
  - Code First × Database First;
  - onde fica e o que muda no projeto;
  - como funciona (tabela dos 5 templates);
  - os ajustes que o banco exigiu;
  - as limitações;
  - a relação com o reuso.
- [x] **Step 2:** `database-first/ROTEIRO_DEMONSTRACAO.md` traz o passo a passo da apresentação (6 a 8 minutos) e o texto curto para o professor. O comando do passo 1 foi testado.
- [ ] **Step 3: Ensaio.** O usuário, que vai apresentar, roda o roteiro inteiro uma vez e cronometra.
- [ ] **Step 4: Checkpoint.** Mensagem sugerida: `docs(database-first): explicacao e roteiro da demonstracao`.

---

### Task 7 (cancelada): Telas completas com Django

Cancelada por decisão do usuário em 2026-11-11. As telas são geradas em Java pela Task 5.

---

## Fechamento do Plano 6

- [ ] **Verificação final:**
  - `mvn -q install` na raiz;
  - `.venv/Scripts/python -m unittest test_validar_modelo` e o validador em `docs/modelo-de-caracteristicas`;
  - `npm test` em `docs/entregas/gerador`;
  - o `verificar.sh` da Task 5 (com `mvn spring-boot:run` no ar).

  Tudo verde.
- [ ] **Entrega no AVA:** `docs/entregas/01-modelo-de-caracteristicas.pdf` e `02-niveis-de-reuso-gru-dru.pdf`, ou os `.docx` se o professor pedir Word.
- [ ] **Índice:** em `docs/PLANO DE EXECUÇÃO/plans/README.md`, a linha 6 passa para `✅ Concluído`.
- [ ] **Merge:** o merge na `main` e o push ficam com o usuário.
- [ ] **Prova de autoria:** a equipe lê os três documentos (`MODELO_DE_CARACTERISTICAS.md`, `NIVEIS_DE_REUSO_MPSBR.md` e `DATABASE_FIRST.md`) antes da aula.
