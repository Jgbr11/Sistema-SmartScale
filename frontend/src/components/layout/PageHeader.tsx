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
