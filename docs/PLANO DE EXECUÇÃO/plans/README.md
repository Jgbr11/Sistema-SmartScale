# Planos de implementação — SmartScale / MilScale

| # | Plano | Foco | Tasks | Status |
|---|---|---|---|---|
| 1 | [Estrutura do código](2026-09-24-plano-1-estrutura-do-codigo.md) | DTOs, enums, erros padronizados, Flyway, configuração por ambiente | 0–11 | ✅ Concluído e na `main`. Falta o fechamento: rodada final e `/code-review` |
| 2 | [Segurança e regras de negócio](2026-09-24-plano-2-implementacoes-necessarias.md) | Rotas de auth, cookie, mass assignment, força bruta, fuso, trocas, regeneração com histórico, botão de travar só em dias futuros | 0–18 | ⬜ Pendente |
| 3 | [Qualidade e organização](2026-09-25-plano-3-qualidade-e-organizacao.md) | Limpeza de comentários, rastreabilidade RF/RN, ArchUnit, porta de e-mail, hooks e pastas do front, feedback sem `alert`/`confirm` | 0–11 | ⬜ Pendente |
| 4 | [Frontend diferenciado](2026-09-25-plano-4-frontend-diferenciado.md) | Identidade visual (tokens, tipografia, ícones), "Fita do serviço", calendário, login, acessibilidade, microcopy | 0–9 | ⬜ Pendente |
| 5 | [Requisitos da disciplina](2026-09-25-plano-5-requisitos-da-disciplina.md) | Strategy ×3, Singleton ×2, Template Method ×3, 8 telas CRUD (equipe de 4), módulo `smartscale-core` 1.0.0, variabilidade, roteiro de gravação | 0–8 | 🔄 Em execução (vem primeiro, pelo prazo da disciplina) |

## Ordem recomendada

```
Plano 1 (fechamento) ─► Plano 2 ─► Plano 3 ─► Plano 4
                                      ▲
Plano 5 ──────────────────────────────┘  (independente; pode ir primeiro se o prazo da disciplina apertar)
```

**Dependências**
- **O Plano 4 depende do Plano 3.** O Plano 4 usa a reorganização de pastas, o `FeedbackProvider`, o `usePermissoes` e o `CalendarioMensal` que o Plano 3 cria.
- **Plano 3 depois do Plano 5:** se o Plano 5 vier antes, o ArchUnit do Plano 3 (Task 5) passa a referenciar o pacote `br.com.smartscale.core`.
- **Planos 2 e 5 mexem nos mesmos pontos:**
  - `TipoFeriado` e `DadosFeriado` (Plano 2, Task 12) são usados pelo Plano 5, Task 5. Quem vier primeiro cria;
  - o `Dockerfile` muda em ambos (Plano 2, Task 15, e Plano 5, Task 1). A tarefa que vier depois preserva a mudança da outra.

## Regras que valem para todos os planos
- Cada task termina num **Checkpoint**: mostrar o diff e esperar. **Quem commita é o usuário.**
- Rodar o Maven com o JDK 23: `export JAVA_HOME="/c/Program Files/Java/jdk-23"`.
- Migrations do Flyway são imutáveis. Mudança de schema entra na próxima versão livre (V2, V3…).
- Comentários: só delimitadores de seção. A rastreabilidade RF/RN fica em `docs/RASTREABILIDADE.md` (Plano 3, Task 1).
- Ao concluir cada task, preencher o "Registro de execução" do plano com o status, o commit e um resumo.
