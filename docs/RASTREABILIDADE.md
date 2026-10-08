# MilScale — rastreabilidade de requisitos

Onde cada requisito funcional (RF) e regra de negócio (RN) do catálogo do SmartScale está implementado.
Os códigos seguem os documentos "SMART SCALE - Ativos reutilizáveis, requisitos e arquitetura v2" e
"Modelo de regras de negócio v1". Núcleo = `br.com.smartscale.core` (reutilizável pela linha de produto).

## Requisitos funcionais

| Código | Requisito | Backend | Tela | Testes |
|---|---|---|---|---|
| RF01 | Autenticar por CPF e senha | `MilScaleUserDetailsService`, `Usuario`, `ContaService`, `MilitarService` (login acompanha o CPF) | Login | `ContaServiceIntegrationTest` |
| RF04 | Manter pessoas escaladas | Núcleo: `PessoaEscalada`, `SituacaoPessoa`. MilScale: `Militar`, `PostoGraduacao`, `Subunidade`, `MilitarService`, `MilitarController` | Militares, Ficha do militar | `MassAssignmentIntegrationTest`, `MilitarCadastroIntegrationTest` |
| RF05 | Manter qualificações | `Qualificacao`, `QualificacaoService`, `QualificacaoController` | Qualificações, Militares (cursos) | `ElegibilidadeServiceTest` |
| RF06 | Manter tipos de turno e elegibilidade | Núcleo: `TipoTurno`, `MotorDeRodizio`. MilScale: `TipoServico`, `RequisitoServico`, `ElegibilidadeService`, `TipoServicoService` | Tipos de serviço | `ElegibilidadeServiceTest` |
| RF07 | Manter regras da escala | `RegraEscala`, `RegraEscalaService`, `RegraEscalaController` | Regras da escala | `MaxServicosMesIntegrationTest` |
| RF08 | Gerar a escala automaticamente | `GerarEscalaService`, `Escala`, `ServicoEscalado`, `EscalaController` | Escala do mês | `EscalaGeracaoIntegrationTest` |
| RF11 | Publicar a escala | `PublicarEscalaService`, `EscalaController` | Escala do mês | `VisibilidadeRascunhoIntegrationTest` |
| RF12 | Travar dias da escala | `BloqueioDiaService`, `ServicoEscalado.isJaComecou` | Escala do mês | `BloqueioDiaIntegrationTest` |
| RF13 | Consultar a própria escala | `MinhaEscalaService`, `MinhaEscalaController` | Minha escala, Escala do dia | `VisibilidadeRascunhoIntegrationTest` |
| RF14 | Consultar a escala completa e do dia | `ConsultaEscalaService`, `EscalaController` | Escala do mês, Escala do dia, PDF | `EscalaDoMesIntegrationTest` |
| RF15 | Pedir troca de serviço | `SolicitacaoService`, `SolicitacaoController` | Trocas | `TrocaIntervaloIntegrationTest`, `TrocaConsistenciaIntegrationTest` |
| RF17 | Triagem da troca (Cabo) | `SolicitacaoService.triagem` | Trocas, Painel | `TrocaIntervaloIntegrationTest` |
| RF18 | Autorizar a troca (Sargenteante) | `SolicitacaoService.autorizar` | Trocas, Painel | `TrocaConsistenciaIntegrationTest` |
| RF19 | Consultar solicitações | `SolicitacaoService.minhas`, `MilitarController` (histórico) | Trocas, Meu histórico, Ficha | — |
| RF25 | Perfis e permissões | `PerfilAcesso`, `Usuario`, `UsuarioService`, `UsuarioController` | Perfis e permissões | `TratadorDeErrosIntegrationTest` |
| RF26 | Missões, dispensas, férias e licenças | `Afastamento`, `AfastamentoService`, `AfastamentoController`, `AvisoService` | Missões e dispensas, Avisos | `AfastamentoReconciliacaoIntegrationTest` |

## Regras de negócio

| Código | Regra | Onde |
|---|---|---|
| RN01 | Escala a pessoa mais bem posicionada na fila (ponto de variação) | Núcleo: `CriterioDeOrdenacao`, `MotorDeRodizio`. MilScale: `CriterioOrdenacaoMilitar`, `GerarEscalaService`, `AfastamentoService` |
| RN04 | Dia travado não aceita troca nem alteração | `BloqueioDiaService`, `GerarEscalaService`, `SolicitacaoService`, `AfastamentoService` |
| RN05 | Uma pessoa não ocupa duas vagas no mesmo dia | `MotorDeRodizio`, `GerarEscalaService`, `AfastamentoService` |
| RN06 | Intervalo mínimo de folga entre serviços | `PoliticaDeDescanso`, `RegraEscala`, `GerarEscalaService`, `SolicitacaoService` |
| RN11 | Manutenção de catálogo e regras é privativa do Sargenteante | `@PreAuthorize` em `TipoServicoController`, `RegraEscalaController`, `QualificacaoController`, `FeriadoController`, `EscalaController` |
| RN14 | Publicar escala e autorizar troca são privativos do Sargenteante | `EscalaController.publicar`, `SolicitacaoController.autorizar` |
| RN15 | Quem está afastado não é escalado | `Afastamento`, `GerarEscalaService`, `AfastamentoService` |
| RN20 | O ciclo da escala é decidido por quem gera (o motor só preenche vagas) | `MotorDeRodizio`, `RegraEscala` |

## Requisitos da disciplina (Plano 5)

| Item | Onde |
|---|---|
| Strategy (critério da fila) | `CriterioDeOrdenacao`, `CriterioOrdenacaoMilitar`, `CriterioMenorCargaNaGeracao`, `CriterioMaisModernoPrimeiro` |
| Singleton | `CatalogoDeCriterios`, `IdentidadeDaOrganizacao` |
| Template Method | `RelatorioCsv` e os 3 relatórios em `application/relatorios` |
| Variabilidade | `VariabilidadeConfig`, `milscale.lps.criterio-ordenacao` |
