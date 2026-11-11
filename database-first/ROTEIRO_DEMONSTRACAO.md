# Roteiro da demonstração: Database First

Quem apresenta mostra, ao vivo, o sistema sendo gerado a partir do banco. **Duração:** de 6 a 8 minutos.

## Antes de começar

- **Docker:** o Docker Desktop está aberto, e o MilScale subiu pelo menos uma vez com `docker compose up -d`, na raiz do projeto. É isso que cria as tabelas no MySQL.
- **Terminal:** Git Bash aberto na pasta `database-first/`, com o JDK 23:
  ```bash
  cd /c/TRABALHOS/SMARTSCALE/Sistema-SmartScale/database-first
  export JAVA_HOME="/c/Program Files/Java/jdk-23"
  ```
- **VS Code:** aberto no projeto, com a pasta `database-first/` expandida.
- **Ensaio:** rode o roteiro inteiro uma vez antes da aula. A primeira execução baixa as bibliotecas e demora mais.

## 1. O ponto de partida é o banco (1 min)

```bash
docker compose -f ../docker-compose.yml exec mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "show tables"'
```

**Falar:**
- "Este é o banco do MilScale, com 19 tabelas de negócio.
- No Database First o banco vem primeiro, e o código é gerado a partir dele."

Mostre a pasta `database-first/` no VS Code:
- `pom.xml` e `hibernate.reveng.xml`: a configuração do gerador;
- `templates/`: os 5 moldes;
- `src/`: só 3 arquivos de apoio (a classe que sobe o sistema, o `Rotulo` e o CSS).

**Falar:** "Não existe nenhuma entidade, nenhum controller e nenhuma tela aqui. Tudo isso vai ser gerado."

## 2. Copiar o banco (30 s)

```bash
bash preparar-banco.sh
```

**Falar:** "Criamos uma cópia, `milscale_dbfirst`, para a demonstração não alterar os dados do sistema."

## 3. Gerar o código a partir do banco (2 min)

```bash
mvn generate-sources
```

Abra `target/generated-sources/dbfirst/br/com/smartscale/dbfirst/entidades/` e mostre:
1. **A lista de arquivos:** para cada tabela, a classe, o `Repository` e o `Controller`.
2. **`Afastamento.java`:**
   - `@Table(name="afastamento")`;
   - os `@Column` com os nomes das colunas;
   - os tipos `LocalDate`;
   - o `@ManyToOne` com `Militar`, que veio da chave estrangeira.
3. **`AfastamentoController.java`:** os 4 métodos (`listar`, `formulario`, `salvar`, `excluir`).
4. **O molde lado a lado:** abra `templates/controlador.ftl` ao lado. "Este é o molde. Onde está `${entidade}`, o gerador escreveu `Afastamento`. O mesmo molde gerou o controller de todas as tabelas."

Mostre também `target/generated-resources/dbfirst/templates/`: as telas `lista.html` e `formulario.html` de cada tabela.

## 4. Subir o sistema gerado (2 min)

```bash
mvn spring-boot:run
```

Quando aparecer `Started DatabaseFirstApplication`, abra **http://localhost:8081** e mostre:
1. **A página inicial:** uma linha por tabela do banco.
2. **Militar:** os 201 militares do banco. O posto e a subunidade aparecem pelo nome, porque vieram das chaves estrangeiras.
3. **Feriado → Novo registro:**
   - preencha `dataInicio`, `dataFim`, `tipo` (`NACIONAL`) e `descricao`, e salve;
   - em seguida **Editar**, mude a descrição e salve;
   - por fim **Excluir**.
4. **Afastamento → Novo registro:** mostre que `militar` é uma lista para escolher, gerada a partir da chave estrangeira.

**Falar:** "Não escrevemos nenhuma classe, nenhum controller e nenhuma tela. Tudo isso foi gerado a partir do banco."

## 5. A prova automática (30 s)

Em outro terminal, também em `database-first/`:

```bash
bash verificar.sh
```

O script abre as listas e os formulários das 17 tabelas e faz criar, editar e excluir um feriado pelos formulários.

## 6. Fechamento (30 s)

**Falar:**
- "O MilScale é Code First: as regras de negócio estão nas classes, que geraram o banco.
- O Database First mostrou o caminho inverso. Do banco saíram classes, CRUD e telas em minutos, mas sem regras de negócio, sem validação e sem segurança. Por isso ele serve para cadastros e painéis administrativos, não para substituir o sistema.
- Os templates são um ativo reutilizável: apontados para outro banco, geram o sistema daquele banco."

## Texto curto para explicar ao professor

> "Professor, o MilScale foi desenvolvido em Code First: escrevemos as classes Java com as regras de negócio, e o banco foi gerado a partir delas, com as migrations do Flyway.
>
> Para demonstrar o Database First, fizemos o caminho contrário, sem mexer no sistema principal. Criamos uma pasta separada no repositório, `database-first`. Ela parte do banco que o MilScale já tem, numa cópia, e gera um sistema novo a partir dele.
>
> A ferramenta é o Hibernate Tools. Ele lê as tabelas, colunas e chaves do MySQL e, com cinco templates que escrevemos, gera para cada tabela a classe, o repositório, o controller com o CRUD e as telas de lista e de cadastro. As chaves estrangeiras viram relacionamentos nas classes e listas de escolha nos formulários.
>
> O código gerado não fica no repositório, só o gerador. A cada execução ele é criado de novo a partir do banco, e é isso que caracteriza o Database First.
>
> A conclusão é que o Database First é ótimo para criar cadastros rápido sobre um banco existente. Mas o código gerado não tem as regras de negócio, como quem pode tirar serviço e o descanso entre serviços, nem validação ou segurança. Por isso o MilScale continua Code First."

## Ao terminar

- Para o sistema com Ctrl+C.
- Se quiser, apague a cópia do banco:
  ```bash
  docker compose -f ../docker-compose.yml exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "DROP DATABASE milscale_dbfirst"'
  ```
