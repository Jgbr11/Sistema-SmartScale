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
