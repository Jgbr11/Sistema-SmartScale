import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { TipoServico } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";

export function TiposServicoPage() {
  const { usuario } = useAuth();
  const podeEditar = usuario?.perfil === "SARGENTEANTE";

  const [tipos, setTipos] = useState<TipoServico[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [efetivoRascunho, setEfetivoRascunho] = useState(1);

  async function carregar() {
    setCarregando(true);
    setTipos(await api.get<TipoServico[]>("/api/tipos-servico"));
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  function iniciarEdicao(t: TipoServico) {
    setEditandoId(t.id);
    setEfetivoRascunho(t.efetivoNecessario);
  }

  async function salvarEfetivo(t: TipoServico) {
    await api.put(`/api/tipos-servico/${t.id}`, { ...t, efetivoNecessario: efetivoRascunho });
    setEditandoId(null);
    carregar();
  }

  return (
    <>
      <PageHeader
        title="Tipos de serviço"
        subtitle="Cada serviço tem uma graduação e, às vezes, um curso exigido"
      />
      <div className="body">
        {podeEditar && (
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
              {mostrarForm ? "Cancelar" : "Novo tipo de serviço"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <NovoTipoForm
            onCriado={() => {
              setMostrarForm(false);
              carregar();
            }}
          />
        )}

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Serviço</th>
                  <th>Efetivo necessário</th>
                  <th>Duração</th>
                  <th>Situação</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {tipos.map((t) => {
                  const editando = editandoId === t.id;
                  const ehPlantao = t.nome === "Plantão ao Alojamento";
                  return (
                    <tr key={t.id}>
                      <td>{t.nome}</td>
                      <td>
                        {editando ? (
                          <div>
                            <input
                              type="number"
                              style={{ width: 70 }}
                              min={1}
                              value={efetivoRascunho}
                              onChange={(e) => setEfetivoRascunho(Number(e.target.value))}
                            />
                            {ehPlantao && (
                              <div style={{ fontSize: 11, color: "var(--grey)", marginTop: 2 }}>
                                Padrão da OM: entre 3 e 6
                              </div>
                            )}
                          </div>
                        ) : (
                          t.efetivoNecessario
                        )}
                      </td>
                      <td>{t.duracaoHoras}h</td>
                      <td>
                        <span className={"pill " + (t.ativo ? "pill-green" : "pill-grey")}>
                          {t.ativo ? "Em uso" : "Inativo"}
                        </span>
                      </td>
                      {podeEditar && (
                        <td>
                          {editando ? (
                            <>
                              <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => salvarEfetivo(t)}>
                                Salvar
                              </button>
                              <button className="btn btn-outline" onClick={() => setEditandoId(null)}>
                                Cancelar
                              </button>
                            </>
                          ) : (
                            <button className="btn btn-outline" onClick={() => iniciarEdicao(t)}>
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

        {!podeEditar && (
          <div className="card" style={{ background: "var(--amber-bg)", border: "none" }}>
            <p style={{ fontSize: 12, color: "var(--amber-text)" }}>
              Somente o Sargenteante mantém tipos de serviço (RN11). Você está vendo em modo de
              consulta.
            </p>
          </div>
        )}
      </div>
    </>
  );
}

function NovoTipoForm({ onCriado }: { onCriado: () => void }) {
  const [nome, setNome] = useState("");
  const [efetivo, setEfetivo] = useState(1);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar() {
    if (!nome) {
      setErro("Informe o nome do serviço.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/tipos-servico", { nome, efetivoNecessario: efetivo, duracaoHoras: 24 });
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Novo tipo de serviço</h3>
      <p className="sub">RF06</p>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>Nome</label>
          <input value={nome} onChange={(e) => setNome(e.target.value)} />
        </div>
        <div className="field">
          <label>Efetivo necessário por dia</label>
          <input
            type="number"
            min={1}
            value={efetivo}
            onChange={(e) => setEfetivo(Number(e.target.value))}
          />
        </div>
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar"}
      </button>
    </div>
  );
}
