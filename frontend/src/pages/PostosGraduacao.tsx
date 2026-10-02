import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { PostoGraduacao } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";

interface Rascunho {
  sigla: string;
  descricao: string;
  nivelHierarquico: string;
}

const VAZIO: Rascunho = { sigla: "", descricao: "", nivelHierarquico: "" };

export function PostosGraduacaoPage() {
  const { usuario } = useAuth();
  const podeEditar = usuario?.perfil === "SARGENTEANTE";

  const [postos, setPostos] = useState<PostoGraduacao[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [rascunho, setRascunho] = useState<Rascunho>(VAZIO);
  const [erro, setErro] = useState<string | null>(null);

  async function carregar() {
    setCarregando(true);
    try {
      setPostos(await api.get<PostoGraduacao[]>("/api/postos-graduacao"));
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível carregar os postos.");
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  function iniciarEdicao(p: PostoGraduacao) {
    setErro(null);
    setEditandoId(p.id);
    setRascunho({ sigla: p.sigla, descricao: p.descricao, nivelHierarquico: String(p.nivelHierarquico) });
  }

  async function salvarEdicao(id: number) {
    setErro(null);
    try {
      await api.put(`/api/postos-graduacao/${id}`, { ...rascunho, nivelHierarquico: Number(rascunho.nivelHierarquico) });
      setEditandoId(null);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    }
  }

  async function excluir(p: PostoGraduacao) {
    if (!confirm(`Excluir o posto "${p.sigla}"?`)) return;
    setErro(null);
    try {
      await api.delete(`/api/postos-graduacao/${p.id}`);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível excluir.");
    }
  }

  return (
    <>
      <PageHeader
        title="Postos e graduações"
        subtitle="O nível hierárquico define a antiguidade: 1 é o mais moderno"
      />
      <div className="body">
        {podeEditar && (
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
              {mostrarForm ? "Cancelar" : "Novo posto"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <NovoPostoForm onCriado={() => { setMostrarForm(false); carregar(); }} />
        )}

        {erro && <div className="error-box">{erro}</div>}

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Nível</th>
                  <th>Sigla</th>
                  <th>Descrição</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {postos.map((p) => {
                  const editando = editandoId === p.id;
                  return (
                    <tr key={p.id}>
                      <td>
                        {editando ? (
                          <input type="number" min={1} max={99} style={{ width: 70 }} value={rascunho.nivelHierarquico}
                            onChange={(e) => setRascunho((s) => ({ ...s, nivelHierarquico: e.target.value }))} />
                        ) : p.nivelHierarquico}
                      </td>
                      <td>
                        {editando ? (
                          <input style={{ width: 90 }} value={rascunho.sigla}
                            onChange={(e) => setRascunho((s) => ({ ...s, sigla: e.target.value }))} />
                        ) : p.sigla}
                      </td>
                      <td>
                        {editando ? (
                          <input style={{ width: "100%" }} value={rascunho.descricao}
                            onChange={(e) => setRascunho((s) => ({ ...s, descricao: e.target.value }))} />
                        ) : p.descricao}
                      </td>
                      {podeEditar && (
                        <td style={{ whiteSpace: "nowrap" }}>
                          {editando ? (
                            <>
                              <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => salvarEdicao(p.id)}>Salvar</button>
                              <button className="btn btn-outline" onClick={() => setEditandoId(null)}>Cancelar</button>
                            </>
                          ) : (
                            <>
                              <button className="btn btn-outline" style={{ marginRight: 6 }} onClick={() => iniciarEdicao(p)}>Editar</button>
                              <button className="btn btn-outline" onClick={() => excluir(p)}>Excluir</button>
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

function NovoPostoForm({ onCriado }: { onCriado: () => void }) {
  const [dados, setDados] = useState<Rascunho>(VAZIO);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar() {
    if (!dados.sigla || !dados.descricao || !dados.nivelHierarquico) {
      setErro("Preencha sigla, descrição e nível.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/postos-graduacao", { ...dados, nivelHierarquico: Number(dados.nivelHierarquico) });
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Novo posto ou graduação</h3>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>Sigla</label>
          <input value={dados.sigla} onChange={(e) => setDados((s) => ({ ...s, sigla: e.target.value }))} placeholder="Ex.: 1º Ten" />
        </div>
        <div className="field">
          <label>Descrição</label>
          <input value={dados.descricao} onChange={(e) => setDados((s) => ({ ...s, descricao: e.target.value }))} placeholder="Ex.: Primeiro-Tenente" />
        </div>
        <div className="field">
          <label>Nível hierárquico</label>
          <input type="number" min={1} max={99} value={dados.nivelHierarquico}
            onChange={(e) => setDados((s) => ({ ...s, nivelHierarquico: e.target.value }))} placeholder="Ex.: 7" />
        </div>
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar"}
      </button>
    </div>
  );
}
