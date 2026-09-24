import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";
import type { ServicoEscalado } from "../api/types";
import { ordenarPorTipo } from "../utils/ordemTipos";

/**
 * Página só de impressão — sem menu lateral, sem chrome nenhum.
 * Aberta numa aba nova a partir do botão "Gerar PDF"; assim que os
 * dados carregam, chama window.print() sozinha (o navegador oferece
 * "Salvar como PDF" como opção de impressora, que é como isso vira
 * um PDF de verdade sem precisar de biblioteca nenhuma no backend).
 */
export function EscalaPdfPage() {
  const { data } = useParams<{ data: string }>();
  const [servicos, setServicos] = useState<ServicoEscalado[] | null>(null);

  useEffect(() => {
    if (!data) return;
    api.get<ServicoEscalado[]>(`/api/escalas/dia?data=${data}`).then((s) => {
      setServicos(ordenarPorTipo(s));
      // Pequeno atraso pra garantir que o layout terminou de renderizar
      // antes do navegador abrir o diálogo de impressão.
      setTimeout(() => window.print(), 300);
    });
  }, [data]);

  if (!data || servicos === null) {
    return <div style={{ padding: 40, fontFamily: "sans-serif" }}>Carregando…</div>;
  }

  return (
    <div className="pdf-escala-dia">
      <div className="no-print" style={{ padding: "12px 20px", background: "#f4f1e8", borderBottom: "1px solid #ddd" }}>
        <button onClick={() => window.print()} style={{ padding: "6px 14px", cursor: "pointer" }}>
          Imprimir / Salvar como PDF
        </button>
        <span style={{ marginLeft: 10, fontSize: 12, color: "#666" }}>
          Essa barra não aparece na impressão.
        </span>
      </div>

      <div style={{ padding: "30px 40px", fontFamily: "'IBM Plex Sans Condensed', sans-serif" }}>
        <h1 style={{ fontSize: 20, marginBottom: 2 }}>MilScale — Escala do dia</h1>
        <p style={{ fontSize: 13, color: "#555", marginBottom: 20 }}>
          5º Batalhão de Suprimento — {formatarDataExtensa(data)}
        </p>

        {servicos.length === 0 ? (
          <p>Nenhuma escala publicada cobre esse dia.</p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
            <thead>
              <tr style={{ borderBottom: "2px solid #333" }}>
                <th style={{ textAlign: "left", padding: "6px 8px" }}>Função</th>
                <th style={{ textAlign: "left", padding: "6px 8px" }}>Posto</th>
                <th style={{ textAlign: "left", padding: "6px 8px" }}>Nome de guerra</th>
                <th style={{ textAlign: "left", padding: "6px 8px" }}>Situação</th>
              </tr>
            </thead>
            <tbody>
              {servicos.map((s) => (
                <tr key={s.id} style={{ borderBottom: "1px solid #ccc" }}>
                  <td style={{ padding: "6px 8px" }}>{s.tipoServico.nome}</td>
                  <td style={{ padding: "6px 8px" }}>{s.militar?.posto.descricao ?? "—"}</td>
                  <td style={{ padding: "6px 8px" }}>{s.militar?.nomeGuerra ?? "VAGA EM ABERTO"}</td>
                  <td style={{ padding: "6px 8px" }}>{s.travado ? "Travado" : s.situacao}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <p style={{ marginTop: 30, fontSize: 10.5, color: "#888" }}>
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
