import { useState } from "react";
import { api, ApiError } from "../api/client";
import type { Feriado } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { formatarPeriodo } from "../utils/formatadores";
import { usePermissoes } from "../hooks/usePermissoes";
import { useAoMudar } from "../hooks/useAoMudar";
import { useFeedback } from "../components/ui/Feedback";
import { Esqueleto } from "../components/ui/Esqueleto";

const TIPOS: { valor: Feriado["tipo"]; label: string }[] = [
  { valor: "NACIONAL", label: "Nacional" },
  { valor: "MILITAR", label: "Militar" },
  { valor: "OM", label: "OM" },
];

export function FeriadosPage() {
  const { avisar, confirmar } = useFeedback();
  const { mantemConfiguracoes: podeEditar } = usePermissoes();

  const [feriados, setFeriados] = useState<Feriado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<Feriado | null>(null);

  async function carregar() {
    setCarregando(true);
    setFeriados(await api.get<Feriado[]>("/api/feriados"));
    setCarregando(false);
  }

  useAoMudar(carregar);

  async function remover(id: number) {
    if (!(await confirmar("Remover este feriado?"))) return;
    try {
      await api.delete(`/api/feriados/${id}`);
      carregar();
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível remover.", "erro");
    }
  }

  function abrirEdicao(f: Feriado) {
    setMostrarForm(false);
    setEditando(f);
  }

  function fecharFormularios() {
    setMostrarForm(false);
    setEditando(null);
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
          <div className="linha-fim">
            <button className="btn btn-primary" onClick={() => { setEditando(null); setMostrarForm((v) => !v); }}>
              {mostrarForm ? "Cancelar" : "Novo feriado"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && <FeriadoForm onSalvo={fecharFormularios} />}

        {editando && podeEditar && (
          <FeriadoForm key={editando.id} feriado={editando} onSalvo={fecharFormularios} onCancelar={() => setEditando(null)} />
        )}

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <Esqueleto />
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
                      <td style={{ whiteSpace: "nowrap" }}>
                        <button className="btn btn-outline" style={{ marginRight: 6 }} onClick={() => abrirEdicao(f)}>Editar</button>
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

function FeriadoForm({ feriado, onSalvo, onCancelar }: { feriado?: Feriado; onSalvo: () => void; onCancelar?: () => void }) {
  const [dataInicio, setDataInicio] = useState(feriado?.dataInicio ?? "");
  const [dataFim, setDataFim] = useState(feriado?.dataFim ?? "");
  const [descricao, setDescricao] = useState(feriado?.descricao ?? "");
  const [tipo, setTipo] = useState<Feriado["tipo"]>(feriado?.tipo ?? "NACIONAL");
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
      const dados = { dataInicio, dataFim, descricao, tipo };
      if (feriado) await api.put(`/api/feriados/${feriado.id}`, dados);
      else await api.post("/api/feriados", dados);
      onSalvo();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>{feriado ? "Editar feriado" : "Novo feriado"}</h3>
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
      <div style={{ display: "flex", gap: 8 }}>
        <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
          {salvando ? "Salvando…" : feriado ? "Salvar alterações" : "Salvar"}
        </button>
        {onCancelar && <button className="btn btn-outline" onClick={onCancelar}>Cancelar</button>}
      </div>
    </div>
  );
}
