import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { CandidatoTrocaMutua, Militar, ServicoEscalado, Solicitacao } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";
import { formatarDataBR } from "../utils/formatadores";

export function TrocasPage() {
  const { usuario } = useAuth();
  const podeTriagem = usuario?.perfil === "CABO_SARGENTEACAO" || usuario?.perfil === "SARGENTEANTE";
  const podeAutorizar = usuario?.perfil === "SARGENTEANTE";

  const [aba, setAba] = useState<"minhas" | "confirmar" | "triagem" | "autorizacao">("minhas");
  const [minhas, setMinhas] = useState<Solicitacao[]>([]);
  const [aguardandoConfirmacao, setAguardandoConfirmacao] = useState<Solicitacao[]>([]);
  const [emTriagem, setEmTriagem] = useState<Solicitacao[]>([]);
  const [aguardandoAutorizacao, setAguardandoAutorizacao] = useState<Solicitacao[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);

  async function carregar() {
    setCarregando(true);
    const chamadas: Promise<unknown>[] = [
      api.get<Solicitacao[]>("/api/solicitacoes/minhas").then(setMinhas),
      api.get<Solicitacao[]>("/api/solicitacoes/aguardando-minha-confirmacao").then(setAguardandoConfirmacao),
    ];
    if (podeTriagem) chamadas.push(api.get<Solicitacao[]>("/api/solicitacoes/triagem").then(setEmTriagem));
    if (podeAutorizar) chamadas.push(api.get<Solicitacao[]>("/api/solicitacoes/autorizacao").then(setAguardandoAutorizacao));
    await Promise.all(chamadas);
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function cancelar(id: number) {
    if (!confirm("Cancelar este pedido de troca?")) return;
    await api.post(`/api/solicitacoes/${id}/cancelar`);
    carregar();
  }

  async function decidirConfirmacao(id: number, aceito: boolean) {
    const comentario = aceito ? "" : (prompt("Motivo da recusa (opcional):") ?? "");
    await api.post(`/api/solicitacoes/${id}/confirmar-substituto`, { aceito, comentario });
    carregar();
  }

  async function decidirTriagem(id: number, aprovado: boolean) {
    const comentario = prompt(aprovado ? "Comentário (opcional):" : "Motivo da recusa:") ?? "";
    if (!aprovado && !comentario) return;
    await api.post(`/api/solicitacoes/${id}/triagem`, { aprovado, comentario });
    carregar();
  }

  async function decidirAutorizacao(id: number, aprovado: boolean) {
    const comentario = prompt(aprovado ? "Comentário (opcional):" : "Motivo da recusa:") ?? "";
    if (!aprovado && !comentario) return;
    try {
      await api.post(`/api/solicitacoes/${id}/autorizacao`, { aprovado, comentario });
      carregar();
    } catch (e) {
      alert(e instanceof ApiError ? e.message : "Não foi possível decidir.");
    }
  }

  return (
    <>
      <PageHeader
        title="Trocas de serviço"
        subtitle="Pedido → substituto confirma → triagem do Cabo → autorização do Sargenteante"
      />
      <div className="body">
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <button className={aba === "minhas" ? "btn btn-primary" : "btn btn-outline"} onClick={() => setAba("minhas")}>
            Minhas solicitações
          </button>
          <button className={aba === "confirmar" ? "btn btn-primary" : "btn btn-outline"} onClick={() => setAba("confirmar")}>
            Pedem pra eu assumir {aguardandoConfirmacao.length > 0 && `(${aguardandoConfirmacao.length})`}
          </button>
          {podeTriagem && (
            <button className={aba === "triagem" ? "btn btn-primary" : "btn btn-outline"} onClick={() => setAba("triagem")}>
              Triagem {emTriagem.length > 0 && `(${emTriagem.length})`}
            </button>
          )}
          {podeAutorizar && (
            <button className={aba === "autorizacao" ? "btn btn-primary" : "btn btn-outline"} onClick={() => setAba("autorizacao")}>
              Autorização final {aguardandoAutorizacao.length > 0 && `(${aguardandoAutorizacao.length})`}
            </button>
          )}
        </div>

        {aba === "minhas" && (
          <>
            <div style={{ display: "flex", justifyContent: "flex-end" }}>
              <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
                {mostrarForm ? "Cancelar" : "Pedir troca"}
              </button>
            </div>
            {mostrarForm && <PedirTrocaForm onCriado={() => { setMostrarForm(false); carregar(); }} />}
            <div className="card" style={{ padding: 0 }}>
              {carregando ? (
                <div style={{ padding: 20 }}>Carregando…</div>
              ) : minhas.length === 0 ? (
                <div style={{ padding: 20, color: "var(--grey)", fontSize: 13 }}>Você ainda não pediu nenhuma troca.</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Serviço</th>
                      <th>Dia</th>
                      <th>Tipo</th>
                      <th>Com quem</th>
                      <th>Situação</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {minhas.map((s) => (
                      <tr key={s.id}>
                        <td>{s.servicoOrigem.tipoServico.nome}</td>
                        <td>{formatarDataBR(s.servicoOrigem.data)}</td>
                        <td><TipoTrocaPill tipo={s.tipoTroca} /></td>
                        <td>
                          {s.substituto.nomeExibicao}
                          {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestino && (
                            <span style={{ display: "block", fontSize: 11, color: "var(--grey)" }}>
                              você assume o dia {formatarDataBR(s.servicoDestino.data)} dele
                            </span>
                          )}
                        </td>
                        <td><SituacaoPill situacao={s.situacao} /></td>
                        <td>
                          {(s.situacao === "AGUARDANDO_SUBSTITUTO" || s.situacao === "EM_TRIAGEM") && (
                            <button className="btn btn-outline" onClick={() => cancelar(s.id)}>Cancelar</button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}

        {aba === "confirmar" && (
          <div className="card" style={{ padding: 0 }}>
            {aguardandoConfirmacao.length === 0 ? (
              <div style={{ padding: 20, color: "var(--grey)", fontSize: 13 }}>
                Ninguém te pediu pra assumir um serviço no momento.
              </div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Quem pediu</th>
                    <th>Serviço</th>
                    <th>Dia</th>
                    <th>Tipo</th>
                    <th>Justificativa</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {aguardandoConfirmacao.map((s) => (
                    <tr key={s.id}>
                      <td>{s.solicitante.nomeExibicao}</td>
                      <td>{s.servicoOrigem.tipoServico.nome}</td>
                      <td>{formatarDataBR(s.servicoOrigem.data)}</td>
                      <td>
                        <TipoTrocaPill tipo={s.tipoTroca} />
                        {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestino && (
                          <span style={{ display: "block", fontSize: 11, color: "var(--grey)", marginTop: 3 }}>
                            você assumiria o dia {formatarDataBR(s.servicoOrigem.data)}, e ele assumiria seu dia {formatarDataBR(s.servicoDestino.data)}
                          </span>
                        )}
                      </td>
                      <td style={{ fontSize: 12 }}>{s.justificativa}</td>
                      <td style={{ whiteSpace: "nowrap" }}>
                        <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => decidirConfirmacao(s.id, true)}>
                          Aceitar
                        </button>
                        <button className="btn btn-outline" onClick={() => decidirConfirmacao(s.id, false)}>
                          Recusar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {aba === "triagem" && podeTriagem && (
          <div className="card" style={{ padding: 0 }}>
            {emTriagem.length === 0 ? (
              <div style={{ padding: 20, color: "var(--grey)", fontSize: 13 }}>Nada esperando triagem.</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Quem pediu</th>
                    <th>Serviço</th>
                    <th>Dia</th>
                    <th>Tipo</th>
                    <th>Assume</th>
                    <th>Justificativa</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {emTriagem.map((s) => (
                    <tr key={s.id}>
                      <td>{s.solicitante.nomeExibicao}</td>
                      <td>{s.servicoOrigem.tipoServico.nome}</td>
                      <td>{formatarDataBR(s.servicoOrigem.data)}</td>
                      <td><TipoTrocaPill tipo={s.tipoTroca} /></td>
                      <td>
                        {s.substituto.nomeExibicao}
                        {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestino && (
                          <span style={{ display: "block", fontSize: 11, color: "var(--grey)" }}>
                            e {s.solicitante.nomeExibicao} assume o dia {formatarDataBR(s.servicoDestino.data)} dele
                          </span>
                        )}
                      </td>
                      <td style={{ fontSize: 12 }}>{s.justificativa}</td>
                      <td style={{ whiteSpace: "nowrap" }}>
                        <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => decidirTriagem(s.id, true)}>Aprovar</button>
                        <button className="btn btn-outline" onClick={() => decidirTriagem(s.id, false)}>Negar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {aba === "autorizacao" && podeAutorizar && (
          <div className="card" style={{ padding: 0 }}>
            {aguardandoAutorizacao.length === 0 ? (
              <div style={{ padding: 20, color: "var(--grey)", fontSize: 13 }}>Nada esperando autorização.</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Quem pediu</th>
                    <th>Serviço</th>
                    <th>Dia</th>
                    <th>Tipo</th>
                    <th>Assume</th>
                    <th>Parecer do Cabo</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {aguardandoAutorizacao.map((s) => (
                    <tr key={s.id}>
                      <td>{s.solicitante.nomeExibicao}</td>
                      <td>{s.servicoOrigem.tipoServico.nome}</td>
                      <td>{formatarDataBR(s.servicoOrigem.data)}</td>
                      <td><TipoTrocaPill tipo={s.tipoTroca} /></td>
                      <td>
                        {s.substituto.nomeExibicao}
                        {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestino && (
                          <span style={{ display: "block", fontSize: 11, color: "var(--grey)" }}>
                            e {s.solicitante.nomeExibicao} assume o dia {formatarDataBR(s.servicoDestino.data)} dele
                          </span>
                        )}
                      </td>
                      <td style={{ fontSize: 12 }}>{s.comentarioCabo || "—"}</td>
                      <td style={{ whiteSpace: "nowrap" }}>
                        <button className="btn btn-primary" style={{ marginRight: 6 }} onClick={() => decidirAutorizacao(s.id, true)}>Autorizar</button>
                        <button className="btn btn-outline" onClick={() => decidirAutorizacao(s.id, false)}>Negar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </>
  );
}

const TIPOS_TROCA = [
  {
    valor: "SUBSTITUICAO" as const,
    nome: "Passar meu serviço",
    explicacao: "Alguém assume seu serviço no seu lugar. Você fica de folga até o seu próximo serviço normal — não pega o dia de ninguém em troca.",
  },
  {
    valor: "TROCA_MUTUA" as const,
    nome: "Trocar de dia com alguém",
    explicacao: "Vocês dois trocam de dia: você assume o serviço dele, e ele assume o seu. Só entre serviços do mesmo tipo (ex.: Cabo da Guarda por Cabo da Guarda).",
  },
];

function PedirTrocaForm({ onCriado }: { onCriado: () => void }) {
  const { usuario } = useAuth();
  const [meusServicos, setMeusServicos] = useState<ServicoEscalado[]>([]);
  const [servicoId, setServicoId] = useState<number | "">("");
  const [tipoTroca, setTipoTroca] = useState<"SUBSTITUICAO" | "TROCA_MUTUA" | "">("");
  const [elegiveis, setElegiveis] = useState<Militar[]>([]);
  const [candidatosMutua, setCandidatosMutua] = useState<CandidatoTrocaMutua[]>([]);
  const [substitutoId, setSubstitutoId] = useState<number | "">("");
  const [servicoDestinoId, setServicoDestinoId] = useState<number | "">("");
  const [justificativa, setJustificativa] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    const hoje = new Date();
    const mesAtual = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
    const proximoMes = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 1);
    const mesProx = `${proximoMes.getFullYear()}-${String(proximoMes.getMonth() + 1).padStart(2, "0")}`;
    Promise.all([
      api.get<ServicoEscalado[]>(`/api/minha-escala?mes=${mesAtual}`),
      api.get<ServicoEscalado[]>(`/api/minha-escala?mes=${mesProx}`),
    ]).then(([a, b]) => {
      const hojeStr = hoje.toISOString().slice(0, 10);
      setMeusServicos([...a, ...b].filter((s) => s.data >= hojeStr && !s.travado));
    });
  }, []);

  // O tipo só pode ser escolhido depois do serviço, e os candidatos só
  // depois do tipo — cada mudança de passo reseta o que vem depois, pra
  // nunca mandar uma combinação de um passo anterior com outro atual.
  useEffect(() => {
    setTipoTroca("");
    setSubstitutoId("");
    setServicoDestinoId("");
  }, [servicoId]);

  useEffect(() => {
    if (!servicoId || !usuario || !tipoTroca) return;
    setSubstitutoId("");
    setServicoDestinoId("");
    if (tipoTroca === "SUBSTITUICAO") {
      api.get<Militar[]>(`/api/solicitacoes/elegiveis?servicoId=${servicoId}&militarId=${usuario.militarId}`).then(setElegiveis);
    } else {
      api.get<CandidatoTrocaMutua[]>(`/api/solicitacoes/elegiveis-troca-mutua?servicoId=${servicoId}&militarId=${usuario.militarId}`).then(setCandidatosMutua);
    }
  }, [servicoId, tipoTroca, usuario]);

  async function salvar() {
    if (!servicoId || !tipoTroca || !justificativa) {
      setErro("Preencha todos os campos.");
      return;
    }
    if (tipoTroca === "SUBSTITUICAO" && !substitutoId) {
      setErro("Escolha quem vai assumir no seu lugar.");
      return;
    }
    if (tipoTroca === "TROCA_MUTUA" && !servicoDestinoId) {
      setErro("Escolha com quem você quer trocar de dia.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      if (tipoTroca === "SUBSTITUICAO") {
        await api.post("/api/solicitacoes", { servicoOrigemId: servicoId, substitutoId, justificativa });
      } else {
        await api.post("/api/solicitacoes/troca-mutua", { servicoOrigemId: servicoId, servicoDestinoId, justificativa });
      }
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível pedir a troca.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Pedir troca</h3>
      <p className="sub">RF15 — escolha um serviço seu, o tipo de troca, e quem vai participar. A outra pessoa ainda precisa aceitar.</p>
      {erro && <div className="error-box">{erro}</div>}
      <div className="field">
        <label>Qual serviço seu</label>
        <select value={servicoId} onChange={(e) => setServicoId(Number(e.target.value))}>
          <option value="">Selecione</option>
          {meusServicos.map((s) => (
            <option key={s.id} value={s.id}>
              {formatarDataBR(s.data)} — {s.tipoServico.nome}
            </option>
          ))}
        </select>
        {meusServicos.length === 0 && (
          <p style={{ fontSize: 11, color: "var(--grey)", marginTop: 4 }}>
            Você não tem serviços futuros disponíveis pra trocar (ou estão travados).
          </p>
        )}
      </div>

      {servicoId !== "" && (
        <div className="field">
          <label>Tipo de troca</label>
          <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 4 }}>
            {TIPOS_TROCA.map((t) => (
              <label
                key={t.valor}
                style={{
                  display: "flex", flexDirection: "column", gap: 3, padding: "10px 12px",
                  border: `1px solid ${tipoTroca === t.valor ? "var(--sidebar-active)" : "var(--border)"}`,
                  borderRadius: 6, cursor: "pointer",
                  background: tipoTroca === t.valor ? "var(--table-head-bg)" : "#fff",
                }}
              >
                <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input type="radio" name="tipoTroca" checked={tipoTroca === t.valor} onChange={() => setTipoTroca(t.valor)} />
                  <strong style={{ fontSize: 13 }}>{t.nome}</strong>
                </span>
                <span style={{ fontSize: 11.5, color: "var(--grey)", paddingLeft: 22 }}>{t.explicacao}</span>
              </label>
            ))}
          </div>
        </div>
      )}

      {servicoId !== "" && tipoTroca === "SUBSTITUICAO" && (
        <div className="field">
          <label>Quem assume no seu lugar</label>
          <select value={substitutoId} onChange={(e) => setSubstitutoId(Number(e.target.value))}>
            <option value="">Selecione</option>
            {elegiveis.map((m) => (
              <option key={m.id} value={m.id}>{m.nomeExibicao}</option>
            ))}
          </select>
          <p style={{ fontSize: 11, color: "var(--grey)", marginTop: 4 }}>
            Só aparece quem respeita o intervalo mínimo de descanso da regra desse serviço. Se
            combinou com alguém que não está na lista (troca 1-pra-1 espontânea), fale com o Cabo
            pra registrar manualmente.
          </p>
          {elegiveis.length === 0 && (
            <p style={{ fontSize: 11, color: "var(--grey)", marginTop: 4 }}>
              Ninguém mais elegível pra esse serviço no momento sem violar o intervalo de descanso.
            </p>
          )}
        </div>
      )}

      {servicoId !== "" && tipoTroca === "TROCA_MUTUA" && (
        <div className="field">
          <label>Trocar de dia com quem</label>
          <select value={servicoDestinoId} onChange={(e) => setServicoDestinoId(Number(e.target.value))}>
            <option value="">Selecione</option>
            {candidatosMutua.map((c) => (
              <option key={c.servicoId} value={c.servicoId}>{c.militar.nomeExibicao}</option>
            ))}
          </select>
          <p style={{ fontSize: 11, color: "var(--grey)", marginTop: 4 }}>
            Só aparece quem tem serviço do mesmo tipo e continua com folga suficiente dos dois
            lados depois da troca (você assumindo o dia dele, e ele assumindo o seu).
          </p>
          {candidatosMutua.length === 0 && (
            <p style={{ fontSize: 11, color: "var(--grey)", marginTop: 4 }}>
              Ninguém disponível pra trocar de dia com você sem violar o intervalo de descanso de algum dos dois lados.
            </p>
          )}
        </div>
      )}

      <div className="field">
        <label>Justificativa</label>
        <input value={justificativa} onChange={(e) => setJustificativa(e.target.value)} placeholder="Por que precisa trocar" />
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Enviando…" : "Enviar pedido"}
      </button>
    </div>
  );
}

function SituacaoPill({ situacao }: { situacao: Solicitacao["situacao"] }) {
  const mapa: Record<Solicitacao["situacao"], { texto: string; classe: string }> = {
    AGUARDANDO_SUBSTITUTO: { texto: "Aguardando o substituto aceitar", classe: "pill-amber" },
    EM_TRIAGEM: { texto: "Em triagem", classe: "pill-amber" },
    AGUARDANDO_AUTORIZACAO: { texto: "Aguardando autorização", classe: "pill-amber" },
    AUTORIZADA: { texto: "Autorizada", classe: "pill-green" },
    NEGADA: { texto: "Negada", classe: "pill-red" },
    CANCELADA: { texto: "Cancelada", classe: "pill-grey" },
  };
  const { texto, classe } = mapa[situacao];
  return <span className={"pill " + classe}>{texto}</span>;
}

function TipoTrocaPill({ tipo }: { tipo: Solicitacao["tipoTroca"] }) {
  return tipo === "TROCA_MUTUA"
    ? <span className="pill pill-grey" title="Vocês dois trocam de dia entre si">Troca de dia</span>
    : <span className="pill pill-grey" title="A outra pessoa assume, você fica sem nada até o próximo">Passar serviço</span>;
}
