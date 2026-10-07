import type { ReactNode } from "react";

type Tom = "positivo" | "atencao" | "sinal" | "neutro";

export function Etiqueta({ tom, children, title }: { tom: Tom; children: ReactNode; title?: string }) {
  return <span className={`pill etiqueta-${tom}`} title={title}>{children}</span>;
}
