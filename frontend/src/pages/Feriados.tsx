import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { Feriado } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";

const TIPOS: { valor: Feriado["tipo"]; label: string }[] = [
  { valor: "NACIONAL", label: "Nacional" },
  { valor: "MILITAR", label: "Militar" },
  { valor: "OM", label: "OM" },
];

export function FeriadosPage() {
  const { usuario } = useAuth();
  const podeEditar = usuario?.perfil === "SARGENTEANTE";

  const [feriados, setFeriados] = useState<Feriado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);

  async function carregar() {
    setCarregando(true);
    setFeriados(await api.get<Feriado[]>("/api/feriados"));
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  async function remover(id: number) {
    if (!confirm("Remover este feriado?")) return;
    await api.delete(`/api/feriados/${id}`);
    carregar();
  }

  return (
    <>
      <PageHeader
        title="Calendário de feriados"
        subtitle="Usado pelo peso de feriado nas regras da escala — pode ser um período (ex.: ponte, recesso)"
      />
      <div className="body">
        {podeEditar && (
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
              {mostrarForm ? "Cancelar" : "Novo feriado"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <NovoFeriadoForm onCriado={() => { setMostrarForm(false); carregar(); }} />
        )}

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : feriados.length === 0 ? (
            <div style={{ padding: 20, color: "var(--grey)", fontSize: 13 }}>Nenhum feriado cadastrado.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Período</th>
                  <th>Descrição</th>
                  <th>Tipo</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {feriados.map((f) => (
                  <tr key={f.id}>
                    <td>{formatarPeriodo(f.dataInicio, f.dataFim)}</td>
                    <td>{f.descricao}</td>
                    <td>
                      <span className={"pill " + (f.tipo === "NACIONAL" ? "pill-green" : f.tipo === "MILITAR" ? "pill-grey" : "pill-amber")}>
                        {TIPOS.find((t) => t.valor === f.tipo)?.label ?? f.tipo}
                      </span>
                    </td>
                    {podeEditar && (
                      <td>
                        <button className="btn btn-outline" onClick={() => remover(f.id)}>Remover</button>
                      </td>
                    )}
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

function NovoFeriadoForm({ onCriado }: { onCriado: () => void }) {
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [descricao, setDescricao] = useState("");
  const [tipo, setTipo] = useState<Feriado["tipo"]>("NACIONAL");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar() {
    if (!dataInicio || !dataFim || !descricao) {
      setErro("Preencha todos os campos.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/feriados", { dataInicio, dataFim, descricao, tipo });
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Novo feriado</h3>
      <p className="sub">Escolha um período — para um único dia, use a mesma data nos dois campos</p>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>De</label>
          <input type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="field">
          <label>Até</label>
          <input type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
        <div className="field">
          <label>Tipo</label>
          <select value={tipo} onChange={(e) => setTipo(e.target.value as Feriado["tipo"])}>
            {TIPOS.map((t) => <option key={t.valor} value={t.valor}>{t.label}</option>)}
          </select>
        </div>
      </div>
      <div className="field">
        <label>Descrição</label>
        <input value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Ex.: Recesso de fim de ano" />
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar"}
      </button>
    </div>
  );
}

function formatarPeriodo(inicio: string, fim: string) {
  if (inicio === fim) return formatarDataBR(inicio);
  return `${formatarDataBR(inicio)} a ${formatarDataBR(fim)}`;
}
function formatarDataBR(iso: string) {
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}
