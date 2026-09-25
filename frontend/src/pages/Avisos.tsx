import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Aviso, Boletim } from "../api/types";
import { PageHeader } from "../components/Shell";
import { capitalizar, formatarDataBR, formatarPeriodo } from "../utils/formatadores";

const DIAS_SEMANA = ["DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"];

const TIPO_LABEL: Record<string, string> = {
  FERIADO: "Feriado",
  MISSAO: "Missão",
  DISPENSA: "Dispensa",
  FERIAS: "Férias",
  LICENCA: "Licença",
  CURSO: "Curso",
  OUTRO: "Outro",
};

/**
 * Calendário de avisos - igual em estrutura ao calendário de escala,
 * mas em vez de serviços mostra dias com feriado ou missão/afastamento
 * cadastrados. Clicar num dia mostra o detalhe embaixo; clicar de novo
 * no mesmo dia (ou escolher outro) desmarca a seleção anterior.
 */
export function AvisosPage() {
  const hoje = new Date();
  const navigate = useNavigate();
  const [mesExibido, setMesExibido] = useState({ ano: hoje.getFullYear(), mes: hoje.getMonth() });
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const [boletins, setBoletins] = useState<Boletim[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [diaEscolhido, setDiaEscolhido] = useState<string | null>(null);

  async function carregar(ano: number, mes: number) {
    setCarregando(true);
    const mesStr = `${ano}-${String(mes + 1).padStart(2, "0")}`;
    const [resultado, listaBoletins] = await Promise.all([
      api.get<Aviso[]>(`/api/avisos?mes=${mesStr}`),
      api.get<Boletim[]>("/api/boletins"),
    ]);
    setAvisos(resultado);
    setBoletins(listaBoletins);
    setCarregando(false);
  }

  function boletimRelacionado(chave: string) {
    return boletins.find((b) => b.avisoRelacionado === chave);
  }

  useEffect(() => {
    carregar(mesExibido.ano, mesExibido.mes);
    setDiaEscolhido(null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mesExibido]);

  function mudarMes(delta: number) {
    let novoMes = mesExibido.mes + delta;
    let novoAno = mesExibido.ano;
    if (novoMes < 0) { novoMes = 11; novoAno -= 1; }
    if (novoMes > 11) { novoMes = 0; novoAno += 1; }
    setMesExibido({ ano: novoAno, mes: novoMes });
  }

  function avisosDoDia(dataStr: string) {
    return avisos.filter((a) => dataStr >= a.dataInicio && dataStr <= a.dataFim);
  }

  function clicarDia(dataStr: string) {
    setDiaEscolhido((atual) => (atual === dataStr ? null : dataStr));
  }

  const primeiroDia = new Date(mesExibido.ano, mesExibido.mes, 1);
  const diasNoMes = new Date(mesExibido.ano, mesExibido.mes + 1, 0).getDate();
  const offsetInicial = primeiroDia.getDay();
  const celulas: (number | null)[] = [...Array(offsetInicial).fill(null), ...Array.from({ length: diasNoMes }, (_, i) => i + 1)];
  const nomeMes = capitalizar(primeiroDia.toLocaleDateString("pt-BR", { month: "long", year: "numeric" }));

  const avisosDoDiaEscolhido = diaEscolhido ? avisosDoDia(diaEscolhido) : [];

  return (
    <>
      <PageHeader title="Avisos" subtitle="Feriados e missões do mês — clique num dia pra ver o detalhe" />
      <div className="body">
        <div className="card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
            <h3>{nomeMes}</h3>
            <div style={{ display: "flex", gap: 8 }}>
              <button className="btn btn-outline" onClick={() => mudarMes(-1)}>← Mês anterior</button>
              <button className="btn btn-outline" onClick={() => mudarMes(1)}>Próximo mês →</button>
            </div>
          </div>

          {carregando ? (
            <p className="sub">Carregando…</p>
          ) : (
            <div className="calendar-grid">
              {DIAS_SEMANA.map((d) => <div key={d} className="dow">{d}</div>)}
              {celulas.map((dia, idx) => {
                if (dia === null) return <div key={idx} className="calendar-cell empty" />;
                const dataStr = `${mesExibido.ano}-${String(mesExibido.mes + 1).padStart(2, "0")}-${String(dia).padStart(2, "0")}`;
                const doDia = avisosDoDia(dataStr);
                const temFeriado = doDia.some((a) => a.tipo === "FERIADO");
                const temMissao = doDia.some((a) => a.tipo !== "FERIADO");
                const selecionado = diaEscolhido === dataStr;
                return (
                  <button
                    key={idx}
                    className="calendar-cell"
                    style={{
                      cursor: "pointer",
                      textAlign: "left",
                      background: selecionado ? "var(--sidebar-active)" : temFeriado ? "var(--green-pill-bg)" : temMissao ? "var(--amber-bg)" : "#fbfcfa",
                      color: selecionado ? "#fff" : "var(--dark)",
                      border: "1px solid var(--border-2)",
                    }}
                    onClick={() => clicarDia(dataStr)}
                  >
                    {dia}
                    {doDia.length > 0 && (
                      <div className="tipo" style={{ color: selecionado ? "#d8e2cc" : temFeriado ? "var(--green-pill-text)" : "var(--amber-text)" }}>
                        {doDia.length === 1 ? TIPO_LABEL[doDia[0].tipo] ?? doDia[0].tipo : `${doDia.length} avisos`}
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {diaEscolhido && (
          <div className="card">
            <h3 style={{ marginBottom: 12 }}>Avisos de {formatarDataBR(diaEscolhido)}</h3>
            {avisosDoDiaEscolhido.length === 0 ? (
              <p className="sub">Nada registrado para esse dia.</p>
            ) : (
              avisosDoDiaEscolhido.map((a, i) => <AvisoDetalhe key={i} aviso={a} boletim={boletimRelacionado(a.chave)} onVerBoletim={() => navigate("/boletim")} />)
            )}
          </div>
        )}

        <div className="card">
          <h3 style={{ marginBottom: 12 }}>Todos os avisos do mês</h3>
          {avisos.length === 0 ? (
            <p className="sub">Nenhum feriado ou missão neste mês.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Período</th>
                  <th>Tipo</th>
                  <th>Observação</th>
                  <th>Militares</th>
                </tr>
              </thead>
              <tbody>
                {avisos.map((a, i) => (
                  <tr key={i}>
                    <td>{formatarPeriodo(a.dataInicio, a.dataFim)}</td>
                    <td>
                      <span className={"pill " + (a.tipo === "FERIADO" ? "pill-green" : "pill-amber")}>
                        {TIPO_LABEL[a.tipo] ?? a.tipo}
                      </span>
                    </td>
                    <td>
                      {a.descricao}
                      {boletimRelacionado(a.chave) && (
                        <button onClick={() => navigate("/boletim")} style={{ marginLeft: 8, fontSize: 11, color: "var(--sidebar-active)", background: "none", border: "none", cursor: "pointer", textDecoration: "underline" }}>
                          Ver Boletim
                        </button>
                      )}
                    </td>
                    <td style={{ fontSize: 12 }}>
                      {a.militares.length > 0 ? a.militares.map((m) => m.nomeExibicao).join(", ") : "—"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}

function AvisoDetalhe({ aviso, boletim, onVerBoletim }: { aviso: Aviso; boletim?: Boletim; onVerBoletim: () => void }) {
  return (
    <div className="card" style={{ background: aviso.tipo === "FERIADO" ? "var(--green-pill-bg)" : "var(--amber-bg)", border: "none", marginBottom: 10 }}>
      <strong style={{ fontSize: 13 }}>{TIPO_LABEL[aviso.tipo] ?? aviso.tipo} — {aviso.descricao}</strong>
      <p style={{ fontSize: 12, marginTop: 4 }}>
        Período: {formatarPeriodo(aviso.dataInicio, aviso.dataFim)}
      </p>
      {aviso.militares.length > 0 && (
        <p style={{ fontSize: 12, marginTop: 4 }}>
          Militares: {aviso.militares.map((m) => m.nomeExibicao).join(", ")}
        </p>
      )}
      {boletim && (
        <button onClick={onVerBoletim} className="btn btn-outline" style={{ marginTop: 8, fontSize: 11.5 }}>
          Ver Boletim relacionado — {boletim.titulo}
        </button>
      )}
    </div>
  );
}
