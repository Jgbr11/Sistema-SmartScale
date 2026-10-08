import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";
import type { ServicoEscalado } from "../api/types";
import { useOrganizacao } from "../hooks/useOrganizacao";
import { ordenarPorTipo } from "../utils/ordemTipos";

export function EscalaPdfPage() {
  const { data } = useParams<{ data: string }>();
  const [servicos, setServicos] = useState<ServicoEscalado[] | null>(null);
  const organizacao = useOrganizacao();

  useEffect(() => {
    if (!data) return;
    api.get<ServicoEscalado[]>(`/api/escalas/dia?data=${data}`).then((s) => {
      setServicos(ordenarPorTipo(s));

      setTimeout(() => window.print(), 300);
    });
  }, [data]);

  if (!data || servicos === null) {
    return <div className="pdf-pagina">Carregando…</div>;
  }

  return (
    <div className="pdf-escala-dia">
      <div className="no-print pdf-barra">
        <button onClick={() => window.print()} className="pdf-botao">
          Imprimir / Salvar como PDF
        </button>
        <span className="pdf-dica">
          Essa barra não aparece na impressão.
        </span>
      </div>

      <div className="pdf-conteudo">
        <h1 className="pdf-titulo">MilScale — Escala do dia</h1>
        <p className="pdf-sub">
          {organizacao ? `${organizacao.nome} — ` : ""}{formatarDataExtensa(data)}
        </p>

        {servicos.length === 0 ? (
          <p>Nenhuma escala publicada cobre esse dia.</p>
        ) : (
          <table className="pdf-tabela">
            <thead>
              <tr className="pdf-linha-cabeca">
                <th className="pdf-th">Função</th>
                <th className="pdf-th">Posto</th>
                <th className="pdf-th">Nome de guerra</th>
                <th className="pdf-th">Situação</th>
              </tr>
            </thead>
            <tbody>
              {servicos.map((s) => (
                <tr key={s.id} className="pdf-linha">
                  <td className="pdf-td">{s.tipoServico.nome}</td>
                  <td className="pdf-td">{s.militar?.posto.descricao ?? "—"}</td>
                  <td className="pdf-td">{s.militar?.nomeGuerra ?? "VAGA EM ABERTO"}</td>
                  <td className="pdf-td">{s.travado ? "Travado" : s.situacao}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <p className="pdf-rodape">
          Gerado pelo MilScale em {new Date().toLocaleString("pt-BR")}
        </p>
      </div>
    </div>
  );
}

function formatarDataExtensa(iso: string) {
  const [ano, mes, dia] = iso.split("-").map(Number);
  const d = new Date(ano, mes - 1, dia);
  const texto = d.toLocaleDateString("pt-BR", { weekday: "long", day: "2-digit", month: "long", year: "numeric" });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
