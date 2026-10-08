# Changelog — smartscale-core

Formato: [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/). Versionamento: [SemVer](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2026-10-02

### Adicionado
- `MotorDeRodizio`: preenche as vagas de um turno com as pessoas mais bem posicionadas na fila, segundo um critério injetado. Tem uma sobrecarga que aceita um filtro extra (`Predicate`) antes da ordenação.
- Contratos `PessoaEscalada`, `TipoTurno` e `CriterioDeOrdenacao`, e o enum `SituacaoPessoa`.
- `CatalogoDeCriterios`: Singleton que registra critérios de ordenação por nome (ponto de variação RN01). Um nome desconhecido gera um erro que lista as opções válidas.
- README, licença MIT e este changelog empacotados em `META-INF/` do JAR, e o JAR de fontes (`-sources.jar`) publicado junto.
- 7 testes unitários, um deles simulando outro produto da linha que usa o motor com outro critério.
