# Plano 4 — Frontend Diferenciado (MilScale)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dar ao MilScale uma identidade visual própria de "quadro de escala de quartel", **sem mudar a estrutura das telas nem as rotas**:
- o mesmo menu lateral, as mesmas páginas e os mesmos fluxos;
- as mesmas classes CSS, que ganham um visual novo;
- componentes pequenos acrescentados onde fazem diferença.

**Architecture:** Pré-requisito: **Plano 3 concluído**. Este plano usa `components/layout`, `components/ui`, `FeedbackProvider`, `CalendarioMensal`, `utils/datas.ts` e `useCarregamento`.

Três frentes:
- **Tokens:** o sistema de design mora em `styles.css`, como tokens em `:root`. Os nomes antigos das variáveis ficam como apelidos dos novos, para nada quebrar no meio do caminho.
- **Componentes:** os novos ficam em `src/components/ui/`.
- **Estilos inline:** os `style={{…}}` das páginas migram para classes. Hoje são 285; a meta é abaixo de 60.

**Tech Stack:** React 19, TypeScript, CSS puro (sem biblioteca de UI), Google Fonts (Barlow Condensed, Public Sans, JetBrains Mono, Saira Stencil One), Vitest + @testing-library/react (do Plano 3).

## Global Constraints

- **Quem commita é o usuário, tarefa a tarefa.** Checkpoint ao fim de cada tarefa, como nos outros planos.
- **Branch:** `melhoria/visual`, a partir da `main` com o Plano 3 mergeado. A criação depende do ok do usuário.
- **Regra de comentários do Plano 3:** só delimitadores `/* ---- Nome ---- */`, `// ---- Nome ----` e `{/* Nome */}`.
- **Estrutura estável:** nenhuma rota, endpoint ou regra de negócio muda. Os textos de tela mudam só onde a Task 9 lista.
- **Piso de qualidade em todas as tarefas:**
  - responsivo até 360 px de largura;
  - foco de teclado visível;
  - `prefers-reduced-motion` respeitado;
  - contraste WCAG AA (4,5:1) em todo texto.
- **Verificação:** `cd frontend && npm test && npm run lint && npm run build`. Conferir cada tela alterada nos 4 perfis e em largura de celular (DevTools → 390×844).

## Direção de design

**Assunto:** a escala de serviço de 24h de um batalhão. Quem usa é a sargenteação (quem monta a escala) e o efetivo (quem consulta). O trabalho principal da interface é responder rápido: **quem está de serviço, quando é o meu, o que falta decidir**.

**Paleta.** O bege atual (`#F4F1E8`) sai. É o fundo "creme" genérico que aparece em qualquer interface. No lugar entra o papel de ofício verde-acinzentado dos documentos de quartel:

| Token | Hex | Uso |
|---|---|---|
| `--papel` | `#E8EBE3` | fundo da aplicação |
| `--folha` | `#FAFBF7` | cartões, tabelas, formulários |
| `--grafite` | `#191D16` | texto |
| `--caserna` | `#1F2A18` | menu lateral e cabeçalho da fita |
| `--oliva` | `#4A5D32` | ação primária e item ativo |
| `--latao` | `#B8923F` | **marcador**: item ativo, foco, "hoje", ponteiro da fita. Nunca como texto sobre fundo claro (contraste 2,3:1) |
| `--sinal` | `#B42318` | vaga em aberto e erro |
| `--areia` | `#D8D3C0` | bordas e linhas de tabela |

**Tipografia:**

| Papel | Fonte | Uso |
|---|---|---|
| Letreiro | Barlow Condensed 600/700, caixa alta, tracking 0.04em | títulos de página, grupos do menu, cabeçalhos de tabela |
| Texto | Public Sans 400/500/600 | todo o resto |
| Dados | JetBrains Mono 500, `tabular-nums` | CPF, datas e horas em tabela, contadores, horas da fita |
| Numeral de estêncil | Saira Stencil One | **só** números do calendário e números grandes do painel |

**Layout:** o mesmo esqueleto de menu lateral + conteúdo. O cabeçalho de cada página ganha uma **sobrelinha com a seção do menu** (ESCALA, PESSOAL, CONFIGURAÇÃO, ADMINISTRAÇÃO). É informação verdadeira: diz onde a pessoa está no menu.

```
┌────────────┬──────────────────────────────────────────────┐
│ MILSCALE   │ ESCALA                          (sobrelinha) │
│            │ ESCALA DO MÊS                    [ações]      │
│ ▌ Painel   │ ─────────────────────────────────────────── │
│   Boletim  │ ┌─ Serviço de 25/09 ─────── 6h de 24h ─────┐ │
│ ESCALA     │ │ ████████▌··························· │ │
│   Escala   │ │ 08h  12h  16h  20h  00h  04h  08h         │ │
│   Trocas   │ └──────────────────────────────────────────┘ │
│ PESSOAL    │  [calendário com numerais em estêncil]        │
└────────────┴──────────────────────────────────────────────┘
```

**Assinatura, o elemento único:** a **fita do serviço**. É uma régua horizontal das 08h às 08h, com a parte cumprida preenchida em oliva e um ponteiro de latão no "agora". Ela aparece:
- no topo do Painel e da Escala do dia;
- em miniatura, como um filete no rodapé de cada dia do calendário: cheio para dias concluídos, parcial para hoje.

É a forma mais honesta de mostrar o conceito central do sistema: o serviço dura 24h e começa às 08h, então o "dia do serviço" não é o dia do calendário. Todo o resto fica quieto.

**Revisão contra o genérico:**
- **Paleta:** era "creme + serifada + acento terroso", o padrão número 1. Foi trocada por papel verde-acinzentado + letreiro condensado + latão.
- **Números grandes do Painel:** eram "número grande + rótulo pequeno + gradiente". O gradiente sai; o número ganha a fonte de estêncil e fica sem cor de destaque, só com o filete de latão quando pede atenção.
- **Numeração decorativa (01/02/03):** não usar em nenhum lugar, porque nada ali é sequência.

---

## Registro de execução

| Ordem | Task | Status | Commit | Resumo |
|---|---|---|---|---|
| 1 | 0 — Preparação e capturas "antes" | ⬜ Pendente | — | — |
| 2 | 1 — Tokens, fontes e base | ⬜ Pendente | — | — |
| 3 | 2 — Menu com ícones e cabeçalho com seção | ⬜ Pendente | — | — |
| 4 | 3 — Componentes de estado (vazio, carregando, etiqueta) | ⬜ Pendente | — | — |
| 5 | 4 — Assinatura: fita do serviço | ⬜ Pendente | — | — |
| 6 | 5 — Calendário "quadro de escala" | ⬜ Pendente | — | — |
| 7 | 6 — Tela de login | ⬜ Pendente | — | — |
| 8 | 7 — Tabelas, formulários e fim dos estilos inline | ⬜ Pendente | — | — |
| 9 | 8 — Acessibilidade e responsivo | ⬜ Pendente | — | — |
| 10 | 9 — Textos de interface | ⬜ Pendente | — | — |

---

### Task 0: Preparação e capturas "antes"

- [ ] **Step 1:** confirmar o Plano 3 na `main` e, com o ok do usuário, rodar `git checkout -b melhoria/visual`.
- [ ] **Step 2:** subir o sistema (README, Opção A) e tirar capturas de tela de Login, Painel, Escala do mês (com um dia aberto), Escala do dia, Trocas, Militares e Ficha, no desktop e no celular (390×844). Salvar em `docs/visual/antes/` (a pasta entra no git). Elas servem para comparar no fim.
- [ ] **Step 3:** anotar a contagem de estilos inline: `grep -rho 'style={{' frontend/src | wc -l`.

---

### Task 1: Tokens, fontes e base

**Files:**
- Modify: `frontend/src/styles.css` (import de fontes, `:root`, `body`, títulos, `.card`, `.btn*`, `.pill*`, `.field`, `table`, `.error-box`)

- [ ] **Step 1: Fontes** — trocar o `@import` do topo:
```css
@import url("https://fonts.googleapis.com/css2?family=Barlow+Condensed:wght@600;700&family=JetBrains+Mono:wght@500&family=Public+Sans:wght@400;500;600;700&family=Saira+Stencil+One&display=swap");
```

- [ ] **Step 2: Tokens** — substituir o bloco `:root` inteiro por:
```css
/* ---- Tokens ---- */
:root {
  --papel: #e8ebe3;
  --folha: #fafbf7;
  --grafite: #191d16;
  --grafite-suave: #4f5747;
  --caserna: #1f2a18;
  --caserna-texto: #c9d1bb;
  --caserna-apagado: #8e9a7c;
  --oliva: #4a5d32;
  --oliva-escura: #3a4a27;
  --latao: #b8923f;
  --sinal: #b42318;
  --sinal-fundo: #fbeceb;
  --areia: #d8d3c0;
  --areia-clara: #e6e2d4;
  --atencao-fundo: #f6eed9;
  --atencao-texto: #7a5b16;
  --positivo-fundo: #e3eedf;
  --positivo-texto: #2f5f2a;

  --fonte-letreiro: "Barlow Condensed", "Arial Narrow", sans-serif;
  --fonte-texto: "Public Sans", system-ui, sans-serif;
  --fonte-dados: "JetBrains Mono", ui-monospace, monospace;
  --fonte-estencil: "Saira Stencil One", "Barlow Condensed", sans-serif;

  --raio: 4px;
  --espaco-1: 4px;
  --espaco-2: 8px;
  --espaco-3: 12px;
  --espaco-4: 16px;
  --espaco-6: 24px;

  --bg: var(--papel);
  --white: var(--folha);
  --dark: var(--grafite);
  --grey: var(--grafite-suave);
  --grey-light: #7a8270;
  --border: var(--areia);
  --border-2: var(--areia-clara);
  --sidebar: var(--caserna);
  --sidebar-active: var(--oliva);
  --sidebar-text: var(--caserna-texto);
  --sidebar-sub: var(--caserna-apagado);
  --footer-bg: #18210f;
  --footer-border: var(--oliva);
  --accent: var(--latao);
  --table-head-bg: var(--areia-clara);
  --table-head-text: var(--oliva-escura);
  --green-pill-bg: var(--positivo-fundo);
  --green-pill-text: var(--positivo-texto);
  --amber-bg: var(--atencao-fundo);
  --amber-text: var(--atencao-texto);
  --red-bg: var(--sinal-fundo);
  --red-text: var(--sinal);
  --font-display: var(--fonte-letreiro);
  --font-body: var(--fonte-texto);
}
```
O segundo bloco são os **apelidos**: as variáveis antigas passam a apontar para os tokens novos. Nenhuma página quebra, e elas migram para os nomes novos na Task 7.

- [ ] **Step 3: Base**
```css
/* ---- Base ---- */
body { font-family: var(--fonte-texto); background: var(--papel); color: var(--grafite); font-size: 14px; line-height: 1.45; }
h1, h2, h3 { font-family: var(--fonte-letreiro); font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; }
.dado { font-family: var(--fonte-dados); font-variant-numeric: tabular-nums; font-size: 12.5px; }
.card { background: var(--folha); border: 1px solid var(--areia); border-radius: var(--raio); padding: var(--espaco-4); }
.btn { border-radius: var(--raio); font-weight: 600; padding: 7px 14px; font-size: 13px; }
.btn-primary { background: var(--oliva); color: #fff; border: 1px solid var(--oliva); }
.btn-primary:hover { background: var(--oliva-escura); }
.btn-outline { background: var(--folha); color: var(--grafite); border: 1px solid var(--areia); }
.btn-outline:hover { border-color: var(--oliva); }
.pill { border-radius: 2px; font-size: 11px; font-weight: 600; padding: 2px 7px; letter-spacing: 0.02em; }
th { font-family: var(--fonte-letreiro); text-transform: uppercase; letter-spacing: 0.05em; font-size: 12px; }
.error-box { background: var(--sinal-fundo); color: var(--sinal); border-left: 3px solid var(--sinal); border-radius: var(--raio); }
```
Ajustar as regras existentes dessas classes, em vez de duplicar: onde hoje há `.btn-primary { … }`, substituir o corpo.

- [ ] **Step 4:** `npm run build` → PASS. Conferir Login, Painel e Militares: tudo com a paleta nova e nada fora do lugar.
- [ ] **Step 5: Checkpoint** — diff, sugerir `style(front): tokens, fontes e base do visual novo` e aguardar o usuário commitar.

---

### Task 2: Menu com ícones e cabeçalho com a seção

**Files:**
- Create: `src/components/ui/Icone.tsx`, `src/components/layout/secoes.ts`, `src/components/layout/secoes.test.ts`
- Modify: `src/components/layout/Shell.tsx`, `src/components/layout/PageHeader.tsx`, `src/styles.css`

- [ ] **Step 1: Teste da seção (falha)**
```ts
import { describe, expect, it } from "vitest";
import { secaoDaRota } from "./secoes";

describe("secaoDaRota", () => {
  it("usa o grupo do menu", () => {
    expect(secaoDaRota("/trocas")).toBe("Escala");
    expect(secaoDaRota("/militares/12")).toBe("Pessoal");
    expect(secaoDaRota("/regras")).toBe("Configuração");
    expect(secaoDaRota("/auditoria")).toBe("Administração");
  });

  it("rotas de uso pessoal ficam sem seção", () => {
    expect(secaoDaRota("/minha-conta")).toBeNull();
  });
});
```

- [ ] **Step 2: `src/components/layout/secoes.ts`**
```ts
const SECAO_POR_ROTA: Record<string, string> = {
  escala: "Escala",
  trocas: "Escala",
  "minha-escala": "Escala",
  avisos: "Escala",
  militares: "Pessoal",
  qualificacoes: "Pessoal",
  missoes: "Pessoal",
  historico: "Pessoal",
  "tipos-servico": "Configuração",
  regras: "Configuração",
  feriados: "Configuração",
  perfis: "Administração",
  auditoria: "Administração",
  painel: "Visão geral",
  boletim: "Comunicados",
};

export function secaoDaRota(caminho: string): string | null {
  const primeiro = caminho.split("/").filter(Boolean)[0] ?? "";
  return SECAO_POR_ROTA[primeiro] ?? null;
}
```

- [ ] **Step 3: `src/components/ui/Icone.tsx`** (traço de 1,8 px, 24×24, cor do texto)
```tsx
import type { ReactNode } from "react";

export type NomeIcone =
  | "painel" | "boletim" | "escala" | "trocas" | "militares" | "cursos" | "missoes" | "tipos"
  | "regras" | "bloqueio" | "feriados" | "perfis" | "auditoria" | "historico" | "avisos" | "sair";

const TRACOS: Record<NomeIcone, ReactNode> = {
  painel: <><rect x="3" y="3" width="7" height="9" /><rect x="14" y="3" width="7" height="5" /><rect x="14" y="12" width="7" height="9" /><rect x="3" y="16" width="7" height="5" /></>,
  boletim: <><path d="M5 3h11l3 3v15H5z" /><path d="M9 9h6M9 13h6M9 17h4" /></>,
  escala: <><rect x="3" y="5" width="18" height="16" rx="1" /><path d="M3 10h18M8 3v4M16 3v4" /></>,
  trocas: <path d="M4 8h13l-3-3M20 16H7l3 3" />,
  militares: <><circle cx="12" cy="8" r="4" /><path d="M4 21c1-4 4.5-6 8-6s7 2 8 6" /></>,
  cursos: <><path d="M12 3l9 5-9 5-9-5z" /><path d="M7 10.5V16c3 2 7 2 10 0v-5.5" /></>,
  missoes: <path d="M5 21V4h11l-2 4 2 4H5" />,
  tipos: <path d="M4 6h16M4 12h16M4 18h10" />,
  regras: <><path d="M4 7h10M18 7h2M4 17h4M12 17h8" /><circle cx="16" cy="7" r="2" /><circle cx="10" cy="17" r="2" /></>,
  bloqueio: <><rect x="5" y="11" width="14" height="10" rx="1" /><path d="M8 11V7a4 4 0 0 1 8 0v4" /></>,
  feriados: <><circle cx="12" cy="12" r="3" /><path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1" /></>,
  perfis: <path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z" />,
  auditoria: <><circle cx="11" cy="11" r="6" /><path d="M20 20l-4.5-4.5" /></>,
  historico: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
  avisos: <><path d="M6 16v-5a6 6 0 0 1 12 0v5l2 2H4z" /><path d="M10 21h4" /></>,
  sair: <path d="M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10" />,
};

export function Icone({ nome, tamanho = 18 }: { nome: NomeIcone; tamanho?: number }) {
  return (
    <svg viewBox="0 0 24 24" width={tamanho} height={tamanho} fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {TRACOS[nome]}
    </svg>
  );
}
```

- [ ] **Step 4: Menu com ícones.** No `Shell.tsx`, a interface `Item` ganha `icone: NomeIcone`, e cada item do `MENU_POR_PERFIL` recebe o seu ícone:

| Itens | Ícone |
|---|---|
| Painel | `painel` |
| Boletim | `boletim` |
| Escala do mês, Escala do dia, Minha escala | `escala` |
| Trocas / Minhas trocas | `trocas` |
| Militares | `militares` |
| Qualificações | `cursos` |
| Missões e dispensas | `missoes` |
| Tipos de serviço | `tipos` |
| Regras da escala | `regras` |
| Bloqueio de dias | `bloqueio` |
| Feriados | `feriados` |
| Perfis e permissões | `perfis` |
| Log de auditoria | `auditoria` |
| Histórico / Meu histórico | `historico` |
| Avisos | `avisos` |

`ItemLink` renderiza `<Icone nome={item.icone} /> <span>{item.label}</span>`. O botão de sair vira `<Icone nome="sair" /> Sair`.

- [ ] **Step 5: CSS do menu** (substituir as regras de `.sidebar nav a`, `.section-label` e `.logout`)
```css
/* ---- Menu lateral ---- */
.sidebar { background: var(--caserna); }
.sidebar nav a { display: flex; align-items: center; gap: 10px; padding: 8px 12px 8px 14px; color: var(--caserna-texto); border-left: 3px solid transparent; font-size: 13px; text-decoration: none; }
.sidebar nav a:hover { color: #fff; background: rgba(255, 255, 255, 0.04); }
.sidebar nav a.active { color: #fff; background: rgba(74, 93, 50, 0.45); border-left-color: var(--latao); }
.sidebar .section-label { font-family: var(--fonte-letreiro); font-weight: 600; letter-spacing: 0.12em; font-size: 11px; color: var(--caserna-apagado); padding: 0 14px; margin-bottom: 4px; }
.sidebar .logout { display: flex; align-items: center; gap: 8px; }
```

- [ ] **Step 6: Cabeçalho com a sobrelinha da seção** — `PageHeader.tsx`:
```tsx
import { useLocation } from "react-router-dom";
import type { ReactNode } from "react";
import { secaoDaRota } from "./secoes";

export function PageHeader({ title, subtitle, acoes }: { title: string; subtitle?: string; acoes?: ReactNode }) {
  const secao = secaoDaRota(useLocation().pathname);
  return (
    <header className="header-bar">
      <div>
        {secao && <span className="sobrelinha">{secao}</span>}
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {acoes && <div className="header-acoes">{acoes}</div>}
    </header>
  );
}
```
```css
/* ---- Cabeçalho da página ---- */
.header-bar { display: flex; justify-content: space-between; align-items: flex-end; gap: var(--espaco-4); border-bottom: 1px solid var(--areia); }
.header-bar h2 { font-size: 26px; line-height: 1.1; }
.sobrelinha { display: block; font-family: var(--fonte-letreiro); font-weight: 600; font-size: 12px; letter-spacing: 0.14em; text-transform: uppercase; color: var(--oliva); border-left: 3px solid var(--latao); padding-left: 6px; margin-bottom: 4px; }
.header-acoes { display: flex; gap: var(--espaco-2); }
```
A nova prop `acoes` passa a receber os botões que hoje ficam soltos no topo das páginas ("Novo militar", "Novo boletim", "Pedir troca", "Novo afastamento" e as outras). Em cada página, mover o botão de dentro do `<div style={{ display: "flex", justifyContent: "flex-end" }}>` para `acoes`.

- [ ] **Step 7:** `npm test && npm run build` → PASS. Conferir o menu dos 4 perfis e o cabeçalho de 5 telas.
- [ ] **Step 8: Checkpoint** — diff, sugerir `style(front): menu com icones e cabecalho com secao` e aguardar o usuário commitar.

---

### Task 3: Componentes de estado — vazio, carregando e etiqueta

**Files:**
- Create: `src/components/ui/EstadoVazio.tsx`, `src/components/ui/Esqueleto.tsx`, `src/components/ui/Etiqueta.tsx`
- Modify: páginas que mostram "Carregando…" ou uma mensagem de lista vazia; `styles.css`

- [ ] **Step 1: Componentes**
```tsx
import type { ReactNode } from "react";

export function EstadoVazio({ titulo, descricao, acao }: { titulo: string; descricao?: string; acao?: ReactNode }) {
  return (
    <div className="estado-vazio">
      <strong>{titulo}</strong>
      {descricao && <p>{descricao}</p>}
      {acao}
    </div>
  );
}
```
```tsx
export function Esqueleto({ linhas = 4 }: { linhas?: number }) {
  return (
    <div className="esqueleto" aria-busy="true" aria-label="Carregando">
      {Array.from({ length: linhas }, (_, i) => <span key={i} />)}
    </div>
  );
}
```
```tsx
import type { ReactNode } from "react";

type Tom = "positivo" | "atencao" | "sinal" | "neutro";

export function Etiqueta({ tom, children, title }: { tom: Tom; children: ReactNode; title?: string }) {
  return <span className={`pill etiqueta-${tom}`} title={title}>{children}</span>;
}
```
```css
/* ---- Estados ---- */
.estado-vazio { padding: var(--espaco-6) var(--espaco-4); text-align: center; display: flex; flex-direction: column; align-items: center; gap: var(--espaco-2); color: var(--grafite-suave); }
.estado-vazio strong { font-family: var(--fonte-letreiro); text-transform: uppercase; letter-spacing: 0.05em; color: var(--grafite); }
.esqueleto { display: flex; flex-direction: column; gap: 10px; padding: var(--espaco-4); }
.esqueleto span { height: 12px; border-radius: 2px; background: linear-gradient(90deg, var(--areia-clara), var(--folha), var(--areia-clara)); background-size: 200% 100%; animation: esqueleto 1.2s linear infinite; }
.esqueleto span:nth-child(odd) { width: 80%; }
@keyframes esqueleto { to { background-position: -200% 0; } }
.etiqueta-positivo { background: var(--positivo-fundo); color: var(--positivo-texto); }
.etiqueta-atencao { background: var(--atencao-fundo); color: var(--atencao-texto); }
.etiqueta-sinal { background: var(--sinal-fundo); color: var(--sinal); }
.etiqueta-neutro { background: var(--areia-clara); color: var(--grafite-suave); }
```

- [ ] **Step 2: Usar nas páginas**
  - **Carregamento:** todo `{carregando ? <div …>Carregando…</div> : …}` vira `<Esqueleto />`.
  - **Listas vazias:** as mensagens de lista vazia viram `EstadoVazio` com uma ação quando a pessoa pode agir (textos na Task 9).
  - **Etiquetas:** `className="pill pill-green|pill-amber|pill-red|pill-grey"` vira `<Etiqueta tom="positivo|atencao|sinal|neutro">`. As classes `.pill-*` antigas ficam no CSS até a Task 7 acabar.

- [ ] **Step 3:** `npm run build` → PASS. Com o backend lento (DevTools → Network → "Slow 3G"), os esqueletos aparecem no lugar do texto.
- [ ] **Step 4: Checkpoint** — diff, sugerir `style(front): estados de vazio, carregando e etiquetas` e aguardar o usuário commitar.

---

### Task 4: Assinatura — a fita do serviço

**Files:**
- Create: `src/utils/servico.ts`, `src/utils/servico.test.ts`, `src/components/ui/FitaDoServico.tsx`
- Modify: `pages/Painel.tsx`, `pages/EscalaDoDia.tsx`, `src/styles.css`

**Interfaces:**
- Produces: `servicoEmCurso(agora?: Date, horaInicio?: number): { diaDoServico: string; inicio: Date; fim: Date; fracao: number; horasCumpridas: number }`, `<FitaDoServico horaInicio?={8} />`.

- [ ] **Step 1: Teste (falha)**
```ts
import { describe, expect, it } from "vitest";
import { servicoEmCurso } from "./servico";

describe("servicoEmCurso", () => {
  it("às 14h30 o serviço do dia está com 6h cumpridas", () => {
    const s = servicoEmCurso(new Date(2026, 8, 25, 14, 30));
    expect(s.diaDoServico).toBe("2026-09-25");
    expect(s.horasCumpridas).toBe(6);
    expect(s.fracao).toBeCloseTo(6.5 / 24, 5);
  });

  it("às 05h o serviço ainda é o do dia anterior", () => {
    const s = servicoEmCurso(new Date(2026, 8, 26, 5, 0));
    expect(s.diaDoServico).toBe("2026-09-25");
    expect(s.horasCumpridas).toBe(21);
  });

  it("às 08h em ponto começa o serviço novo", () => {
    const s = servicoEmCurso(new Date(2026, 8, 26, 8, 0));
    expect(s.diaDoServico).toBe("2026-09-26");
    expect(s.fracao).toBe(0);
  });
});
```

- [ ] **Step 2: `src/utils/servico.ts`**
```ts
import { hojeISO } from "./datas";

export interface ServicoEmCurso {
  diaDoServico: string;
  inicio: Date;
  fim: Date;
  fracao: number;
  horasCumpridas: number;
}

export function servicoEmCurso(agora: Date = new Date(), horaInicio = 8): ServicoEmCurso {
  const inicio = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate(), horaInicio);
  if (agora < inicio) inicio.setDate(inicio.getDate() - 1);
  const fim = new Date(inicio);
  fim.setDate(fim.getDate() + 1);
  const fracao = (agora.getTime() - inicio.getTime()) / (fim.getTime() - inicio.getTime());
  return { diaDoServico: hojeISO(inicio), inicio, fim, fracao, horasCumpridas: Math.floor(fracao * 24) };
}
```

- [ ] **Step 3: `src/components/ui/FitaDoServico.tsx`**
```tsx
import { useEffect, useState } from "react";
import { formatarDataBR } from "../../utils/formatadores";
import { servicoEmCurso } from "../../utils/servico";

const MARCAS = [0, 4, 8, 12, 16, 20, 24];

export function FitaDoServico({ horaInicio = 8 }: { horaInicio?: number }) {
  const [agora, setAgora] = useState(() => new Date());

  useEffect(() => {
    const id = setInterval(() => setAgora(new Date()), 60_000);
    return () => clearInterval(id);
  }, []);

  const s = servicoEmCurso(agora, horaInicio);
  const porcentagem = `${(s.fracao * 100).toFixed(2)}%`;

  return (
    <section className="fita" aria-label={`Serviço de ${formatarDataBR(s.diaDoServico)}: ${s.horasCumpridas} de 24 horas cumpridas`}>
      <div className="fita-cabecalho">
        <span className="fita-titulo">Serviço de {formatarDataBR(s.diaDoServico)}</span>
        <span className="fita-contagem dado">{s.horasCumpridas}h de 24h</span>
      </div>
      <div className="fita-trilho">
        <div className="fita-cumprido" style={{ width: porcentagem }} />
        <div className="fita-agora" style={{ left: porcentagem }} />
      </div>
      <div className="fita-horas dado" aria-hidden="true">
        {MARCAS.map((h) => (
          <span key={h} style={{ left: `${(h / 24) * 100}%` }}>
            {String((horaInicio + h) % 24).padStart(2, "0")}h
          </span>
        ))}
      </div>
    </section>
  );
}
```
Os três `style` que ficam aqui são posição e largura calculadas, não tema. Eles são permitidos e não contam para a meta da Task 7.

```css
/* ---- Fita do serviço ---- */
.fita { background: var(--caserna); color: var(--caserna-texto); border-radius: var(--raio); padding: var(--espaco-3) var(--espaco-4) var(--espaco-2); }
.fita-cabecalho { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: var(--espaco-2); }
.fita-titulo { font-family: var(--fonte-letreiro); text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700; color: #fff; }
.fita-contagem { color: var(--latao); }
.fita-trilho { position: relative; height: 10px; background: repeating-linear-gradient(90deg, rgba(255,255,255,0.08) 0 calc(100% / 24 - 1px), transparent calc(100% / 24 - 1px) calc(100% / 24)); border: 1px solid rgba(255,255,255,0.12); }
.fita-cumprido { position: absolute; inset: 0 auto 0 0; background: var(--oliva); }
.fita-agora { position: absolute; top: -4px; bottom: -4px; width: 3px; margin-left: -1px; background: var(--latao); }
.fita-horas { position: relative; height: 16px; margin-top: 4px; font-size: 10.5px; color: var(--caserna-apagado); }
.fita-horas span { position: absolute; transform: translateX(-50%); }
.fita-horas span:first-child { transform: none; }
.fita-horas span:last-child { transform: translateX(-100%); }
@media (prefers-reduced-motion: no-preference) { .fita-cumprido { transition: width 0.6s ease-out; } }
```

- [ ] **Step 4: Usar** no topo do `Painel` (antes dos números) e da `EscalaDoDia` (antes do seletor de dia), para todos os perfis.
- [ ] **Step 5:** `npm test && npm run build` → PASS. Conferir a fita de manhã cedo (antes das 08h, ela mostra o serviço do dia anterior), à tarde e no celular.
- [ ] **Step 6: Checkpoint** — diff, sugerir `feat(front): fita do servico de 24h no painel e na escala do dia` e aguardar o usuário commitar.

---

### Task 5: Calendário "quadro de escala"

**Files:**
- Modify: `src/components/ui/CalendarioMensal.tsx` (Plano 3), `pages/EscalaDoMes.tsx`, `pages/MinhaEscala.tsx`, `src/styles.css`

**Interfaces:**
- Produces: `InfoDoDia.progresso?: number` (0 a 1, o filete no rodapé do dia).

- [ ] **Step 1: Filete de progresso.** No `CalendarioMensal`, dentro do `<button>` do dia, depois do rótulo:
```tsx
            {info.progresso !== undefined && (
              <span className="dia-fita" aria-hidden="true">
                <span style={{ width: `${Math.min(1, info.progresso) * 100}%` }} />
              </span>
            )}
```
E acrescentar `progresso?: number;` em `InfoDoDia`.

- [ ] **Step 2: Progresso por dia** — em `EscalaDoMes` e `MinhaEscala`, no `infoDoDia`, para dias com serviço:
```tsx
    const emCurso = servicoEmCurso();
    const progresso = dataISO < emCurso.diaDoServico ? 1 : dataISO === emCurso.diaDoServico ? emCurso.fracao : 0;
```

- [ ] **Step 3: Estilo do quadro**
```css
/* ---- Calendário ---- */
.calendar-grid { gap: 3px; }
.dow { font-family: var(--fonte-letreiro); font-weight: 600; letter-spacing: 0.1em; font-size: 11px; color: var(--grafite-suave); }
.calendar-cell { position: relative; font-family: var(--fonte-estencil); font-size: 20px; line-height: 1; padding: 8px 8px 14px; background: var(--folha); border: 1px solid var(--areia-clara); border-radius: 2px; min-height: 64px; }
.calendar-cell .tipo { display: block; margin-top: 6px; font-family: var(--fonte-texto); font-size: 10.5px; font-weight: 500; }
.calendar-cell.hoje { border-color: var(--latao); box-shadow: inset 0 0 0 1px var(--latao); }
.calendar-cell.selecionado { background: var(--oliva); border-color: var(--oliva); color: #fff; }
.dia-fita { position: absolute; left: 8px; right: 8px; bottom: 5px; height: 3px; background: var(--areia-clara); }
.dia-fita span { display: block; height: 100%; background: var(--oliva); }
.calendar-cell.selecionado .dia-fita { background: rgba(255,255,255,0.25); }
.calendar-cell.selecionado .dia-fita span { background: var(--latao); }
.calendar-legenda { display: flex; gap: var(--espaco-4); flex-wrap: wrap; font-size: 11.5px; color: var(--grafite-suave); margin-top: var(--espaco-2); }
.calendar-legenda i { display: inline-block; width: 10px; height: 10px; margin-right: 5px; vertical-align: -1px; }
@media (max-width: 480px) { .calendar-cell { font-size: 15px; min-height: 46px; padding: 6px 5px 11px; } }
```
As regras de `.calendar-cell` do Plano 3 que conflitarem com estas saem, para não haver duas definições.

- [ ] **Step 4: Legenda** abaixo do calendário da Escala do mês:
```tsx
<div className="calendar-legenda">
  <span><i style={{ background: "var(--atencao-fundo)" }} />Vaga em aberto</span>
  <span><i style={{ background: "var(--latao)" }} />Hoje</span>
  <span><i style={{ background: "var(--oliva)" }} />Serviço cumprido</span>
</div>
```
- [ ] **Step 5:** `npm test && npm run build` → PASS. Conferir o calendário de um mês com escala no desktop e no celular.
- [ ] **Step 6: Checkpoint** — diff, sugerir `style(front): calendario em estilo quadro de escala` e aguardar o usuário commitar.

---

### Task 6: Tela de login

**Files:**
- Modify: `src/pages/Login.tsx`, `src/styles.css` (regras `.login-*`)

- [ ] **Step 1: Estrutura.** O lado esquerdo continua de identidade e ganha a fita do serviço. Ela mostra só a hora, sem nenhum dado sensível, e deixa claro já no login que o sistema gira em torno do serviço de 24h.
```tsx
      <div className="login-ident">
        <span className="sobrelinha sobrelinha-clara">5º Batalhão de Suprimento</span>
        <h1>MilScale</h1>
        <p>Escala de serviço de 24 horas, montada pela ordem de quem está há mais tempo sem tirar serviço.</p>
        <FitaDoServico />
      </div>
```
- [ ] **Step 2: Estilo**
```css
/* ---- Login ---- */
.login-wrap { display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr); min-height: 100vh; }
.login-ident { background: var(--caserna); color: var(--caserna-texto); padding: 56px 48px; display: flex; flex-direction: column; justify-content: center; gap: var(--espaco-4); }
.login-ident h1 { font-size: 64px; color: #fff; line-height: 0.95; }
.login-ident p { max-width: 42ch; font-size: 15px; }
.sobrelinha-clara { color: var(--caserna-texto); }
.login-form-wrap { display: flex; align-items: center; justify-content: center; padding: var(--espaco-6); background: var(--papel); }
.login-card { width: 100%; max-width: 360px; background: var(--folha); border: 1px solid var(--areia); border-top: 3px solid var(--oliva); border-radius: var(--raio); padding: 28px; }
@media (max-width: 860px) {
  .login-wrap { grid-template-columns: 1fr; }
  .login-ident { padding: 28px 20px; }
  .login-ident h1 { font-size: 44px; }
}
```
- [ ] **Step 3:** conferir o login no desktop e no celular, e a mensagem de erro com senha errada e com o servidor fora do ar (Plano 2, Task 14).
- [ ] **Step 4: Checkpoint** — diff, sugerir `style(front): tela de login com identidade do quartel` e aguardar o usuário commitar.

---

### Task 7: Tabelas, formulários e fim dos estilos inline

**Files:**
- Modify: `src/styles.css`, e as páginas com mais estilos inline. Lista da revisão de 25/09/2026:

| Arquivo | Estilos inline |
|---|---|
| `Trocas.tsx` | 34 |
| `MilitarDetalheOverlay.tsx` | 34 |
| `FichaMilitar.tsx` | 25 |
| `EscalaPdf.tsx` | 19 |
| `Militares.tsx` | 18 |
| `EscalaDoMes.tsx` | 16 |
| `Avisos.tsx` | 13 |
| `NotificacaoSino.tsx` | 12 |
| `MissoesDispensas.tsx` | 11 |
| `Historico.tsx` | 10 |
| `Boletim.tsx` | 10 |
| `TiposServico.tsx` | 8 |

- [ ] **Step 1: Classes utilitárias** para os padrões que se repetem inline:
```css
/* ---- Utilitários ---- */
.linha { display: flex; align-items: center; gap: var(--espaco-2); flex-wrap: wrap; }
.linha-entre { display: flex; justify-content: space-between; align-items: center; gap: var(--espaco-2); flex-wrap: wrap; }
.pilha { display: flex; flex-direction: column; gap: var(--espaco-4); }
.card-tabela { padding: 0; overflow-x: auto; }
.card-tabela > h3 { padding: var(--espaco-4) var(--espaco-4) 0; }
.nota { font-size: 12px; color: var(--grafite-suave); }
.nota-atencao { background: var(--atencao-fundo); color: var(--atencao-texto); border-left: 3px solid var(--latao); padding: var(--espaco-3); border-radius: var(--raio); font-size: 12.5px; }
.link-nome { background: none; border: none; padding: 0; color: var(--oliva); font-weight: 600; text-decoration: underline; text-underline-offset: 2px; cursor: pointer; }
.foto-3x4 { width: 108px; height: 130px; border: 1px solid var(--areia); background: var(--areia-clara); overflow: hidden; display: flex; align-items: center; justify-content: center; }
.foto-3x4 img { width: 100%; height: 100%; object-fit: cover; }
```

- [ ] **Step 2: Tabelas e campos**
```css
/* ---- Tabelas ---- */
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 9px 12px; background: var(--areia-clara); color: var(--oliva-escura); border-bottom: 1px solid var(--areia); }
td { padding: 9px 12px; border-bottom: 1px solid var(--areia-clara); vertical-align: top; }
tbody tr:hover td { background: #f2f4ee; }

/* ---- Campos ---- */
.field label { font-family: var(--fonte-letreiro); text-transform: uppercase; letter-spacing: 0.06em; font-weight: 600; font-size: 12px; color: var(--grafite-suave); }
.field input, .field select, .field textarea { border: 1px solid var(--areia); border-radius: var(--raio); background: #fff; padding: 7px 9px; }
.field input:focus, .field select:focus, .field textarea:focus { border-color: var(--oliva); outline: 2px solid color-mix(in srgb, var(--latao) 45%, transparent); outline-offset: 1px; }
```

- [ ] **Step 3: Migrar os estilos inline**, página por página, na ordem da tabela. Regra:
  - **Viram classe:** estilos de layout e tema (flex, gap, padding, cor, tamanho de fonte), usando as classes do Step 1 ou uma classe nova na seção da página no CSS.
  - **Ficam inline:** só valores calculados em tempo de execução (largura em %, posição).
  - **Dados em fonte monoespaçada:** colunas de data, CPF, NR e horário ganham `className="dado"`.

- [ ] **Step 4: Meta.** `grep -rho 'style={{' frontend/src | wc -l` **abaixo de 60**. Registrar antes e depois.
- [ ] **Step 5: Apelidos.** Com as páginas migradas, trocar nas regras do CSS as variáveis antigas pelos tokens novos e **apagar os apelidos** do `:root`. Conferir que não sobrou nenhum uso: `grep -rnE 'var\(--(bg|white|dark|grey|grey-light|border|border-2|sidebar[a-z-]*|footer-[a-z]+|accent|table-head-[a-z]+|green-pill-[a-z]+|amber-[a-z]+|red-[a-z]+|font-display|font-body)\)' frontend/src` → vazio.
- [ ] **Step 6:** `npm test && npm run lint && npm run build` → PASS. Percorrer as telas da lista no desktop e no celular.
- [ ] **Step 7: Checkpoint** — diff, sugerir `style(front): tabelas e campos no visual novo, estilos inline migrados para classes` e aguardar o usuário commitar.

---

### Task 8: Acessibilidade e responsivo

**Files:**
- Modify: `src/styles.css`, `src/components/layout/Shell.tsx`, `src/components/militar/MilitarDetalheOverlay.tsx`, `src/components/layout/NotificacaoSino.tsx`

- [ ] **Step 1: Foco visível e movimento reduzido**
```css
/* ---- Acessibilidade ---- */
:focus-visible { outline: 2px solid var(--latao); outline-offset: 2px; }
.sidebar :focus-visible { outline-color: #fff; }
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; }
}
```
- [ ] **Step 2: Semântica**
  - **Popup do militar:** ganha `role="dialog"`, `aria-modal="true"` e `aria-labelledby` apontando para o nome, e fecha com Esc (mesmo padrão do `FeedbackProvider`).
  - **Sininho:** o botão ganha `aria-expanded={aberto}`, e o badge ganha `aria-label="{n} notificações não lidas"`.
  - **Menu mobile:** o botão hambúrguer ganha `aria-expanded={menuAberto}` e `aria-controls="menu-lateral"`, e o `<aside>` ganha `id="menu-lateral"`.
- [ ] **Step 3: Contraste.** Conferir os pares abaixo com uma ferramenta de contraste (ex.: DevTools → inspecionar → "Contrast ratio") e registrar os valores na nota de execução. Todos precisam dar ≥ 4,5:1.

| Texto sobre fundo | Esperado |
|---|---|
| `--grafite` sobre `--papel` | ≈ 15:1 |
| `--grafite-suave` sobre `--folha` | ≥ 7:1 |
| branco sobre `--oliva` | ≈ 7:1 |
| `--caserna-texto` sobre `--caserna` | ≥ 9:1 |
| `--latao` sobre `--caserna` (contagem da fita) | ≈ 5:1 |
| `--atencao-texto` sobre `--atencao-fundo` | ≥ 5:1 |
| `--sinal` sobre `--folha` | ≈ 6:1 |
| `--oliva-escura` sobre `--areia-clara` (cabeçalho de tabela) | ≥ 7:1 |

- [ ] **Step 4: Celular (390×844).** Percorrer todas as telas dos 4 perfis. Nenhuma pode ter rolagem horizontal da página; tabelas rolam dentro do `.card-tabela`. Corrigir o que vazar.
- [ ] **Step 5: Teclado.** Percorrer Login → Painel → Escala do mês → abrir um dia → abrir o popup de um militar → fechar com Esc, só com o teclado (Tab, Enter, Esc).
- [ ] **Step 6: Checkpoint** — diff, sugerir `fix(front): foco visivel, movimento reduzido, semantica de dialogos e ajustes de celular` e aguardar o usuário commitar.

---

### Task 9: Textos de interface

Os textos seguem três regras:
- **Forma:** frase em caixa-alta só no início, verbo que diz exatamente o que acontece e o mesmo verbo do botão ao aviso de confirmação.
- **Estados vazios:** convidam a agir.
- **Erros:** dizem o que houve e o que fazer.

**Files:**
- Modify: páginas e componentes listados na tabela.

| Onde | Hoje | Depois |
|---|---|---|
| Menu (rodapé) | `↩ Log out` | `Sair` |
| Escala do mês | cartão "Gerar nova escala" + botão "Montar a escala" | cartão "Gerar escala" + botão "Gerar escala". Aviso: "Escala gerada." |
| Escala do mês | "Publicar para o efetivo" | "Publicar escala". Aviso: "Escala publicada." |
| Escala do mês (vazio) | "Nenhuma escala gerada ainda." | `EstadoVazio`: título "Nenhuma escala neste mês"; descrição "Gere a escala para distribuir os serviços do período."; ação "Gerar escala", só para quem pode |
| Escala do mês | "Travar este dia" / "Destravar este dia" | "Travar dia" / "Destravar dia". Avisos: "Dia travado." / "Dia destravado." |
| Trocas (vazio) | "Você ainda não pediu nenhuma troca." | título "Nenhum pedido de troca"; descrição "Precisa passar um serviço ou trocar de dia? Faça o pedido e acompanhe aqui."; ação "Pedir troca" |
| Trocas | "Enviar pedido" | "Pedir troca". Aviso: "Pedido enviado para {nome}." |
| Trocas (aceitar/recusar) | "Aceitar" / "Recusar" | "Assumir o serviço" / "Recusar". Avisos: "Você assumiu o pedido — agora vai para a triagem." / "Pedido recusado." |
| Militares | "Salvar cadastro" | "Cadastrar militar". Aviso: "Militar cadastrado." |
| Missões e dispensas | "Registrar afastamento" | "Registrar afastamento". Aviso: "Afastamento registrado." (sem mudança no botão; entra o aviso) |
| Boletim | "Publicar" (no formulário) | "Publicar boletim" / "Salvar alterações" (na edição). Avisos: "Boletim publicado." / "Boletim atualizado." |
| Painel (sem pendências) | "Nenhum ponto de atenção no momento." | "Nada pendente agora." |
| Erro genérico de carregamento | "Não foi possível carregar os dados." | "Não foi possível carregar. Confira a conexão e tente de novo." + botão "Tentar de novo" (chama `recarregar`) |

- [ ] **Step 1:** aplicar a tabela.
- [ ] **Step 2:** revisar os títulos de página. Todos em frase curta, sem sigla interna; a sobrelinha já dá o contexto da seção.
- [ ] **Step 3:** `npm test && npm run build` → PASS.
- [ ] **Step 4: Capturas "depois"**, nas mesmas telas e tamanhos da Task 0, em `docs/visual/depois/`.
- [ ] **Step 5: Checkpoint** — diff, sugerir `copy(front): textos de interface consistentes` e aguardar o usuário commitar.

---

## Fechamento do Plano 4

- [ ] Rodar `npm test && npm run lint && npm run build`.
- [ ] Montar um comparativo "antes e depois" em `docs/visual/README.md`, com as capturas lado a lado e 3 linhas por tela explicando o que mudou.
- [ ] Registrar os números finais: estilos inline e contraste dos pares da Task 8.
- [ ] O merge e o push ficam com o usuário.
