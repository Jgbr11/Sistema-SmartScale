import type { ReactNode } from "react";
import type { Solicitacao } from "../../api/types";

export interface Coluna {
  titulo: string;
  valor: (s: Solicitacao) => ReactNode;
}

export function TabelaSolicitacoes({ itens, colunas, vazio, acoes }: {
  itens: Solicitacao[];
  colunas: Coluna[];
  vazio: string;
  acoes?: (s: Solicitacao) => ReactNode;
}) {
  if (itens.length === 0) return <div className="vazio">{vazio}</div>;
  return (
    <table>
      <thead>
        <tr>
          {colunas.map((c) => <th key={c.titulo}>{c.titulo}</th>)}
          {acoes && <th />}
        </tr>
      </thead>
      <tbody>
        {itens.map((s) => (
          <tr key={s.id}>
            {colunas.map((c) => <td key={c.titulo}>{c.valor(s)}</td>)}
            {acoes && <td className="acoes">{acoes(s)}</td>}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
