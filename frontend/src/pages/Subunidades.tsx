import { useState } from "react";
import { api, ApiError } from "../api/client";
import type { Subunidade } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { usePermissoes } from "../hooks/usePermissoes";
import { useAoMudar } from "../hooks/useAoMudar";
import { useFeedback } from "../components/ui/Feedback";

interface Rascunho {
  sigla: string;
  nome: string;
  ativo: boolean;
}

const VAZIO: Rascunho = { sigla: "", nome: "", ativo: true };

export function SubunidadesPage() {
  const { confirmar } = useFeedback();
  const { mantemConfiguracoes: podeEditar } = usePermissoes();

  const [subunidades, setSubunidades] = useState<Subunidade[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [rascunho, setRascunho] = useState<Rascunho>(VAZIO);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  async function carregar() {
    setCarregando(true);
    try {
      setSubunidades(await api.get<Subunidade[]>("/api/subunidades"));
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível carregar as subunidades.");
    } finally {
      setCarregando(false);
    }
  }

  useAoMudar(carregar);

  function iniciarEdicao(s: Subunidade) {
    setErro(null);
    setAviso(null);
    setEditandoId(s.id);
    setRascunho({ sigla: s.sigla, nome: s.nome, ativo: s.ativo });
  }

  async function salvarEdicao(id: number) {
    setErro(null);
    try {
      await api.put(`/api/subunidades/${id}`, rascunho);
      setEditandoId(null);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    }
  }

  async function excluir(s: Subunidade) {
    if (!(await confirmar(`Excluir a subunidade "${s.sigla}"? Se ela estiver em uso, será só desativada.`))) return;
    setErro(null);
    setAviso(null);
    try {
      const resultado = await api.delete<{ desativada: boolean }>(`/api/subunidades/${s.id}`);
      if (resultado?.desativada) {
        setAviso(`${s.sigla} está em uso por militares, requisitos ou escalas: foi desativada em vez de excluída.`);
      }
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível excluir.");
    }
  }

  return (
    <>
      <PageHeader
        title="Subunidades"
        subtitle="Companhias e seções da OM. Uma subunidade inativa não aparece no cadastro de militares"
      />
      <div className="body">
        {podeEditar && (
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
              {mostrarForm ? "Cancelar" : "Nova subunidade"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <NovaSubunidadeForm onCriada={() => { setMostrarForm(false); carregar(); }} />
        )}

        {erro && <div className="error-box">{erro}</div>}
        {aviso && (
          <div className="card" style={{ background: "var(--amber-bg)", border: "none" }}>
            <p style={{ fontSize: 13, color: "var(--amber-text)" }}>{aviso}</p>
          </div>
        )}

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Sigla</th>
                  <th>Nome</th>
                  <th>Situação</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {subunidades.map((s) => {
                  const editando = editandoId === s.id;
                  return (
                    <tr key={s.id}>
                      <td>
                        {editando ? (
                          <input style={{ width: 100 }} value={rascunho.sigla}
                            onChange={(e) => setRascunho((r) => ({ ...r, sigla: e.target.value }))} />
                        ) : s.sigla}
                      </td>
                      <td>
                        {editando ? (
                          <input style={{ width: "100%" }} value={rascunho.nome}
                            onChange={(e) => setRascunho((r) => ({ ...r, nome: e.target.value }))} />
                        ) : s.nome}
                      </td>
                      <td>
                        {editando ? (
                          <label style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 12.5 }}>
                            <input type="checkbox" checked={rascunho.ativo}
                              onChange={(e) => setRascunho((r) => ({ ...r, ativo: e.target.checked }))} />
                            Ativa
                          </label>
                        ) : (
                          <span className={"pill " + (s.ativo ? "pill-green" : "pill-grey")}>{s.ativo ? "Ativa" : "Inativa"}</span>
                        )}
                      </td>
                      {podeEditar && (
                        <td style={{ whiteSpace: "nowrap" }}>
                          {editando ? (
                            <>
                              <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => salvarEdicao(s.id)}>Salvar</button>
                              <button className="btn btn-outline" onClick={() => setEditandoId(null)}>Cancelar</button>
                            </>
                          ) : (
                            <>
                              <button className="btn btn-outline" style={{ marginRight: 6 }} onClick={() => iniciarEdicao(s)}>Editar</button>
                              <button className="btn btn-outline" onClick={() => excluir(s)}>Excluir</button>
                            </>
                          )}
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}

function NovaSubunidadeForm({ onCriada }: { onCriada: () => void }) {
  const [dados, setDados] = useState<Rascunho>(VAZIO);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar() {
    if (!dados.sigla || !dados.nome) {
      setErro("Preencha sigla e nome.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/subunidades", dados);
      onCriada();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Nova subunidade</h3>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>Sigla</label>
          <input value={dados.sigla} onChange={(e) => setDados((s) => ({ ...s, sigla: e.target.value }))} placeholder="Ex.: 2ª Cia" />
        </div>
        <div className="field">
          <label>Nome</label>
          <input value={dados.nome} onChange={(e) => setDados((s) => ({ ...s, nome: e.target.value }))} placeholder="Ex.: Segunda Companhia" />
        </div>
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar"}
      </button>
    </div>
  );
}
