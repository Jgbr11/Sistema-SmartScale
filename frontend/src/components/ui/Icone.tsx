import type { ReactNode } from "react";

export type NomeIcone =
  | "painel" | "boletim" | "escala" | "trocas" | "militares" | "cursos" | "missoes" | "tipos"
  | "regras" | "bloqueio" | "feriados" | "perfis" | "auditoria" | "historico" | "avisos" | "sair" | "postos" | "subunidades";

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
  postos: <path d="M5 20l7-5 7 5M5 14l7-5 7 5M5 8l7-5 7 5" />,
  subunidades: <><path d="M4 21V9l8-5 8 5v12" /><path d="M9 21v-6h6v6" /></>,
};

export function Icone({ nome, tamanho = 18 }: { nome: NomeIcone; tamanho?: number }) {
  return (
    <svg viewBox="0 0 24 24" width={tamanho} height={tamanho} fill="none" stroke="currentColor"
      strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {TRACOS[nome]}
    </svg>
  );
}
