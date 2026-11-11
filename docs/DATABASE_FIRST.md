# Database First na SmartScale

## Code First e Database First

| | Code First | Database First |
|---|---|---|
| Ponto de partida | as classes (entidades) | o banco de dados já existente |
| O que é gerado | o esquema do banco | as classes, o acesso a dados, o CRUD e as telas |
| Quando faz sentido | sistema novo, com regras de negócio ricas | banco legado, ou cadastros simples em cima de um banco pronto |

**O MilScale é Code First:**
- as entidades Java (`Militar`, `Escala`, `Afastamento`…) foram escritas à mão;
- a primeira migration do Flyway (`V1__schema_inicial.sql`) saiu delas;
- daí em diante o esquema é versionado pelas migrations.

**A demonstração de Database First faz o caminho contrário:** parte do banco que o MilScale já criou e gera um sistema novo a partir dele, sem ler nenhuma linha do código Java do MilScale.

## Onde fica e o que muda no projeto

- **Uma pasta separada.** Tudo fica em `database-first/`, um projeto Maven próprio. Ele **não** entra no `pom.xml` da raiz: precisa do banco no ar para gerar o código, e o `mvn install` do MilScale continua igual.
- **O MilScale não muda.** Nada em `backend/`, `frontend/` ou `smartscale-core/` foi alterado.
- **Os dados do MilScale não são tocados.** O primeiro passo copia o banco `milscale` para `milscale_dbfirst`, e tudo é gerado e testado na cópia.

## Como funciona

A ferramenta é o **Hibernate Tools 6.5.3**, que faz a **engenharia reversa** do banco:
1. Lê do MySQL as tabelas, as colunas, os tipos e as chaves.
2. Monta um modelo em memória: uma classe por tabela, um atributo por coluna, um relacionamento por chave estrangeira.
3. Aplica **templates** (moldes) a esse modelo. Cada template produz um tipo de arquivo, preenchido com os dados de cada tabela.

Para cada tabela do banco, são gerados:

| Arquivo gerado | Template que o produz | O que é |
|---|---|---|
| `Feriado.java` | padrão do Hibernate Tools (`hbm2java`) | a classe da tabela: um atributo por coluna, com as anotações JPA (`@Table`, `@Column`, `@ManyToOne`) |
| `FeriadoRepository.java` | `templates/repositorio.ftl` | o acesso ao banco, com o Spring Data (`findAll`, `findById`, `save`, `deleteById`) |
| `FeriadoController.java` | `templates/controlador.ftl` | o CRUD, com 4 métodos: `listar`, `formulario`, `salvar`, `excluir` |
| `Feriado/lista.html` | `templates/lista.ftl` | a tela de listagem, com os botões Editar e Excluir |
| `Feriado/formulario.html` | `templates/formulario.ftl` | a tela de cadastro e edição |

E uma página inicial (`index.html`, do template `inicio.ftl`) com o link de todas as tabelas.

**Nos templates há duas sintaxes:**
- `${...}` é o **gerador** preenchendo o molde, por exemplo `${entidade}` vira `Feriado`;
- `*{...}` é a **tela** mostrando os dados quando o sistema roda, por exemplo `*{r.descricao}`.

**O formulário escolhe o campo pelo tipo da coluna:**
- `DATE` vira um campo de data, e `DATETIME` vira data e hora;
- número vira campo numérico, e `BIT` vira caixa de marcar;
- chave estrangeira vira uma lista para escolher o registro relacionado (por exemplo, o militar de um afastamento).

**Escritos à mão há só três arquivos de apoio**, que não dependem de nenhuma tabela:
- `DatabaseFirstApplication.java`, que sobe o sistema;
- `Rotulo.java`, que mostra o nome de um registro nas listas de escolha (o nome de guerra do militar, a sigla do posto…);
- `estilo.css`, a aparência das telas.

**O resultado no banco do MilScale:**
- 21 classes (19 tabelas, mais 2 classes de chave composta), 19 repositórios e 17 controllers;
- 35 telas: 17 listas, 17 formulários e a página inicial.

O código gerado fica em `target/` e **não é versionado**: o que vai para o Git é o **gerador** (configuração e templates). A cada `mvn spring-boot:run`, tudo é gerado de novo a partir do banco.

## Ajustes que o banco exigiu

A engenharia reversa não acerta tudo sozinha. Os ajustes ficam em arquivos de configuração, nunca no código gerado:

| Problema encontrado | Ajuste | Onde |
|---|---|---|
| 5 tabelas saíam sem `@GeneratedValue`, embora a coluna seja `auto_increment` | gerador `identity` explícito para `afastamento`, `boletim`, `feriado`, `notificacao` e `solicitacao` | `hibernate.reveng.xml` |
| A tabela interna do Flyway virava entidade | `table-filter` exclui `flyway_schema_history` | `hibernate.reveng.xml` |
| As datas vinham como `java.sql.Date`, um tipo antigo | `type-mapping`: `DATE` → `LocalDate`, `DATETIME` → `LocalDateTime`, `TIME` → `LocalTime` | `hibernate.reveng.xml` |
| O mapeamento muitos-para-muitos gerado era rejeitado pelo Hibernate 6.5 | `detectmanytomany="false"`: as tabelas de ligação viram entidades com chave composta | `pom.xml` |
| O gerador também tentava aplicar os templates às classes de chave composta | `foreach="entity"` nos templates por tabela | `pom.xml` |

## Limitações (e por que o MilScale continua Code First)

- **Sem regras de negócio.** O código gerado só lê e grava tabelas. Elegibilidade, intervalo de descanso, travamento de dias e fluxo de troca, que são o valor do MilScale, não estão no banco e por isso não aparecem.
- **Sem validação nem segurança.** Um campo obrigatório vazio ou a exclusão de um registro em uso volta como página de erro. Não há login nem perfis, e a lista de usuários mostra até o hash da senha. Por isso o sistema gerado roda só na máquina local, contra a cópia do banco.
- **Nomes automáticos.**
  - As telas mostram os nomes técnicos das colunas (`dataInicio`, `nomeGuerra`).
  - As coleções geradas nas classes recebem plurais automáticos, às vezes estranhos (`militars`).
  - As 2 tabelas de ligação, de chave composta, ficam sem tela.
- **Sem arquitetura.** As entidades geradas acessam o banco direto. O MilScale separa núcleo, aplicação e adaptadores, e o núcleo reutilizável (`smartscale-core`) nem conhece JPA.

**Conclusão:** Database First é útil para criar rapidamente cadastros ou um painel administrativo em cima de um banco existente. Não serve para substituir um sistema com regras de domínio.

## Relação com o reuso

Os 5 templates são **ativos reutilizáveis de geração**: apontados para outro banco, produzem as classes, o CRUD e as telas daquele banco, sem mudar uma linha. É o reuso por geradores, ao lado do reuso por componente, que é o `smartscale-core`.

## Como rodar

Veja `database-first/ROTEIRO_DEMONSTRACAO.md`.
