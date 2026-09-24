import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { Qualificacao } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";

export function QualificacoesPage() {
  const { usuario } = useAuth();
  const podeEditar = usuario?.perfil === "SARGENTEANTE";

  const [quals, setQuals] = useState<Qualificacao[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [rascunho, setRascunho] = useState({ nome: "", descricao: "" });

  async function carregar() {
    setCarregando(true);
    setQuals(await api.get<Qualificacao[]>("/api/qualificacoes"));
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  function iniciarEdicao(q: Qualificacao) {
    setEditandoId(q.id);
    setRascunho({ nome: q.nome, descricao: q.descricao ?? "" });
  }

  async function salvarEdicao(q: Qualificacao) {
    await api.put(`/api/qualificacoes/${q.id}`, { ...q, ...rascunho });
    setEditandoId(null);
    carregar();
  }

  return (
    <>
      <PageHeader
        title="Qualificações"
        subtitle="Cursos e habilitações que podem ser exigidos por um tipo de serviço (RF05)"
      />
      <div className="body">
        {podeEditar && (
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
              {mostrarForm ? "Cancelar" : "Nova qualificação"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <NovaQualificacaoForm onCriado={() => { setMostrarForm(false); carregar(); }} />
        )}

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Descrição</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {quals.map((q) => {
                  const editando = editandoId === q.id;
                  return (
                    <tr key={q.id}>
                      <td>
                        {editando ? (
                          <input
                            style={{ width: 160 }}
                            value={rascunho.nome}
                            onChange={(e) => setRascunho((s) => ({ ...s, nome: e.target.value }))}
                          />
                        ) : (
                          q.nome
                        )}
                      </td>
                      <td>
                        {editando ? (
                          <input
                            style={{ width: "100%" }}
                            value={rascunho.descricao}
                            onChange={(e) => setRascunho((s) => ({ ...s, descricao: e.target.value }))}
                          />
                        ) : (
                          q.descricao ?? "—"
                        )}
                      </td>
                      {podeEditar && (
                        <td>
                          {editando ? (
                            <>
                              <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => salvarEdicao(q)}>
                                Salvar
                              </button>
                              <button className="btn btn-outline" onClick={() => setEditandoId(null)}>
                                Cancelar
                              </button>
                            </>
                          ) : (
                            <button className="btn btn-outline" onClick={() => iniciarEdicao(q)}>
                              Editar
                            </button>
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

        <div className="card" style={{ background: "var(--amber-bg)", border: "none" }}>
          <p style={{ fontSize: 12, color: "var(--amber-text)" }}>
            Vincular ou remover um curso de uma pessoa é feito na tela de Militares, no cadastro
            dela. Aqui você só mantém o catálogo dos cursos que existem.
          </p>
        </div>
      </div>
    </>
  );
}

function NovaQualificacaoForm({ onCriado }: { onCriado: () => void }) {
  const [nome, setNome] = useState("");
  const [descricao, setDescricao] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar() {
    if (!nome) {
      setErro("Informe o nome do curso.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/qualificacoes", { nome, descricao });
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Nova qualificação</h3>
      <p className="sub">RF05</p>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>Nome</label>
          <input value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Ex.: CFC" />
        </div>
        <div className="field">
          <label>Descrição</label>
          <input value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Ex.: Curso de Formação de Cabos" />
        </div>
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar"}
      </button>
    </div>
  );
}
