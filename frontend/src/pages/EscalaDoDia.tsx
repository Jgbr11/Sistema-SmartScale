import { useState } from "react";
import { api } from "../api/client";
import type { ServicoEscalado } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { BotaoBaixarCsv } from "../components/ui/BotaoBaixarCsv";
import { MilitarDetalheOverlay } from "../components/militar/MilitarDetalheOverlay";
import { ordenarPorTipo } from "../utils/ordemTipos";
import { hojeISO } from "../utils/datas";
import { useAoMudar } from "../hooks/useAoMudar";
import { Esqueleto } from "../components/ui/Esqueleto";
import { FitaDoServico } from "../components/ui/FitaDoServico";

export function EscalaDoDiaPage() {
  const [data, setData] = useState(hojeISO());
  const [servicos, setServicos] = useState<ServicoEscalado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [militarSelecionado, setMilitarSelecionado] = useState<number | null>(null);

  async function carregar(dataAlvo: string) {
    setCarregando(true);
    const resultado = await api.get<ServicoEscalado[]>(`/api/escalas/dia?data=${dataAlvo}`);
    setServicos(ordenarPorTipo(resultado));
    setCarregando(false);
  }

  useAoMudar(() => carregar(data), data);

  function mudarDia(delta: number) {
    const d = new Date(data + "T00:00:00");
    d.setDate(d.getDate() + delta);
    setData(hojeISO(d));
  }

  return (
    <>
      <PageHeader title="Escala do dia" subtitle="Escolha um dia pra ver quem está escalado" />
      <div className="body">
        <FitaDoServico />
        <div className="card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
            <div className="field" style={{ marginBottom: 0 }}>
              <label>Dia</label>
              <input type="date" value={data} onChange={(e) => setData(e.target.value)} />
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <button className="btn btn-outline" onClick={() => mudarDia(-1)}>← Dia anterior</button>
              <button className="btn btn-outline" onClick={() => mudarDia(1)}>Próximo dia →</button>
              <button className="btn btn-outline" onClick={() => window.open(`/escala/pdf/${data}`, "_blank")}>Gerar PDF</button>
              <BotaoBaixarCsv caminho={`escala-do-dia.csv?data=${data}`} />
            </div>
          </div>

          {carregando ? (
            <Esqueleto />
          ) : servicos.length === 0 ? (
            <p className="sub">Nenhuma escala publicada para esse dia ainda.</p>
          ) : (
            <table>
              <tbody>
                {servicos.map((s) => (
                  <tr key={s.id}>
                    <td style={{ fontWeight: 600, width: 220 }}>{s.tipoServico.nome}</td>
                    <td style={{ color: "var(--grey)", width: 90 }}>{s.militar?.posto.sigla ?? "—"}</td>
                    <td>
                      {s.militar ? (
                        <button
                          onClick={() => setMilitarSelecionado(s.militar!.id)}
                          style={{ background: "none", border: "none", padding: 0, color: "var(--sidebar-active)", fontWeight: 600, cursor: "pointer", textDecoration: "underline" }}
                        >
                          {s.militar.nomeGuerra.toUpperCase()}
                        </button>
                      ) : (
                        <span className="pill pill-red">vaga em aberto</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
      <MilitarDetalheOverlay militarId={militarSelecionado} onFechar={() => setMilitarSelecionado(null)} />
    </>
  );
}
