import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { RegraEscala } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";

export function RegrasEscalaPage() {
  const { usuario } = useAuth();
  const podeEditar = usuario?.perfil === "SARGENTEANTE";

  const [regras, setRegras] = useState<RegraEscala[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [rascunho, setRascunho] = useState<{ intervaloMinimo: number; maxServicosMes: number | "" }>({
    intervaloMinimo: 7,
    maxServicosMes: "",
  });

  async function carregar() {
    setCarregando(true);
    setRegras(await api.get<RegraEscala[]>("/api/regras-escala"));
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  function iniciarEdicao(r: RegraEscala) {
    setEditandoId(r.id);
    setRascunho({
      intervaloMinimo: r.intervaloMinimo,
      maxServicosMes: r.maxServicosMes ?? "",
    });
  }

  async function salvar(r: RegraEscala) {
    setErro(null);
    try {
      await api.put(`/api/regras-escala/${r.id}`, {
        ...r,
        intervaloMinimo: rascunho.intervaloMinimo,
        maxServicosMes: rascunho.maxServicosMes === "" ? null : rascunho.maxServicosMes,
      });
      setEditandoId(null);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar a regra.");
    }
  }

  return (
    <>
      <PageHeader
        title="Regras da escala"
        subtitle="Um conjunto de regras por tipo de serviço — alimenta o motor de geração (RF07)"
      />
      <div className="body">
        {erro && <div className="error-box">{erro}</div>}
        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Tipo de serviço</th>
                  <th>Intervalo mínimo</th>
                  <th>Máx. no mês (por militar)</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {regras.map((r) => {
                  const editando = editandoId === r.id;
                  return (
                    <tr key={r.id}>
                      <td>{r.tipoServico.nome}</td>
                      <td>
                        {editando ? (
                          <input
                            type="number"
                            style={{ width: 70 }}
                            value={rascunho.intervaloMinimo}
                            onChange={(e) =>
                              setRascunho((s) => ({ ...s, intervaloMinimo: Number(e.target.value) }))
                            }
                          />
                        ) : (
                          `${r.intervaloMinimo} dias`
                        )}
                      </td>
                      <td>
                        {editando ? (
                          <input
                            type="number"
                            style={{ width: 70 }}
                            value={rascunho.maxServicosMes}
                            onChange={(e) =>
                              setRascunho((s) => ({
                                ...s,
                                maxServicosMes: e.target.value === "" ? "" : Number(e.target.value),
                              }))
                            }
                          />
                        ) : (
                          r.maxServicosMes ?? "—"
                        )}
                      </td>
                      {podeEditar && (
                        <td>
                          {editando ? (
                            <>
                              <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => salvar(r)}>
                                Salvar
                              </button>
                              <button className="btn btn-outline" onClick={() => setEditandoId(null)}>
                                Cancelar
                              </button>
                            </>
                          ) : (
                            <button className="btn btn-outline" onClick={() => iniciarEdicao(r)}>
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
            Só o Sargenteante altera estas regras (RN11). Mudanças valem a partir da próxima
            geração de escala — não afetam escalas já publicadas.
          </p>
          <p style={{ fontSize: 12, color: "var(--amber-text)", marginTop: 6 }}>
            O máximo no mês conta só os serviços deste tipo, por militar. Se faltar gente
            (muitas férias/missões ao mesmo tempo), a escala aperta pra não deixar vaga aberta e
            pode passar do limite — igual acontece com o intervalo mínimo.
          </p>
        </div>
      </div>
    </>
  );
}
