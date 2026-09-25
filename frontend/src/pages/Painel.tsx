import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Afastamento, Escala, Militar, ServicoEscalado, Solicitacao } from "../api/types";
import { PageHeader } from "../components/Shell";
import { MilitarDetalheOverlay } from "../components/MilitarDetalheOverlay";
import { useAuth } from "../context/AuthContext";
import { ordenarPorTipo } from "../utils/ordemTipos";
import { capitalizar, formatarDataBR } from "../utils/formatadores";

export function PainelPage() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const podeTriagem = usuario?.perfil === "CABO_SARGENTEACAO" || usuario?.perfil === "SARGENTEANTE";
  const podeAutorizar = usuario?.perfil === "SARGENTEANTE";

  const [carregando, setCarregando] = useState(true);
  const [militares, setMilitares] = useState<Militar[]>([]);
  const [escala, setEscala] = useState<Escala | null>(null);
  const [servicosHoje, setServicosHoje] = useState<ServicoEscalado[]>([]);
  const [emTriagem, setEmTriagem] = useState<Solicitacao[]>([]);
  const [aguardandoAutorizacao, setAguardandoAutorizacao] = useState<Solicitacao[]>([]);
  const [afastamentos, setAfastamentos] = useState<Afastamento[]>([]);
  const [militarSelecionado, setMilitarSelecionado] = useState<number | null>(null);

  useEffect(() => {
    async function carregar() {
      setCarregando(true);
      const hoje = new Date().toISOString().slice(0, 10);
      const chamadas: Promise<unknown>[] = [
        api.get<Militar[]>("/api/militares").then(setMilitares),
        api.get<ServicoEscalado[]>(`/api/escalas/dia?data=${hoje}`).then((s) => setServicosHoje(ordenarPorTipo(s))),
        api.get<Afastamento[]>("/api/afastamentos").then(setAfastamentos),
      ];
      chamadas.push(
        api.get<Escala[]>("/api/escalas").then(async (lista) => {
          if (lista.length > 0) setEscala(await api.get<Escala>(`/api/escalas/${lista[0].id}`));
        })
      );
      if (podeTriagem) chamadas.push(api.get<Solicitacao[]>("/api/solicitacoes/triagem").then(setEmTriagem));
      if (podeAutorizar) chamadas.push(api.get<Solicitacao[]>("/api/solicitacoes/autorizacao").then(setAguardandoAutorizacao));
      await Promise.all(chamadas);
      setCarregando(false);
    }
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const ativos = militares.filter((m) => m.situacao === "ATIVO").length;
  const vagasAbertas = escala?.servicos.filter((s) => !s.militar).length ?? 0;
  const trocasPendentes = emTriagem.length + aguardandoAutorizacao.length;
  const hojeStr = new Date().toISOString().slice(0, 10);
  const afastamentosHoje = afastamentos.filter((a) => a.dataInicio <= hojeStr && a.dataFim >= hojeStr);

  const nomeMesEscala = escala
    ? new Date(escala.dataInicio + "T00:00:00").toLocaleDateString("pt-BR", { month: "long", year: "numeric" })
    : null;

  const hojeExtenso = new Date().toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" });

  return (
    <>
      <PageHeader title="Painel" subtitle="Visão geral da escala e principais informações do batalhão" />
      <div className="body">
        {carregando ? (
          <div className="card">Carregando…</div>
        ) : (
          <>
            <div className="stat-grid">
              <div className="stat-card">
                <div className="valor">{ativos}</div>
                <div className="rotulo">militares ativos</div>
              </div>
              <div className={"stat-card" + (vagasAbertas > 0 ? " atencao" : "")}>
                <div className="valor">{vagasAbertas}</div>
                <div className="rotulo">
                  vagas em aberto{nomeMesEscala ? ` — ${capitalizar(nomeMesEscala)}` : ""}
                </div>
              </div>
              <div className={"stat-card" + (trocasPendentes > 0 ? " atencao" : "")}>
                <div className="valor">{trocasPendentes}</div>
                <div className="rotulo">trocas aguardando decisão</div>
              </div>
              <div className="stat-card">
                <div className="valor">{afastamentosHoje.length}</div>
                <div className="rotulo">em missão/dispensa hoje</div>
              </div>
            </div>

            <div className="dashboard-grid">
              <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
                <div className="card">
                  <h3>Serviço de hoje — {capitalizar(hojeExtenso)}</h3>
                  <p className="sub">Quem está escalado neste exato momento</p>
                  {servicosHoje.length === 0 ? (
                    <p className="sub" style={{ marginTop: 8 }}>
                      Nenhuma escala publicada cobre o dia de hoje.
                    </p>
                  ) : (
                    <table>
                      <thead>
                        <tr>
                          <th>Serviço</th>
                          <th>Militar</th>
                          <th>Posto</th>
                          <th>Situação</th>
                        </tr>
                      </thead>
                      <tbody>
                        {servicosHoje.map((s) => (
                          <tr key={s.id}>
                            <td>{s.tipoServico.nome}</td>
                            <td>
                              {s.militar ? (
                                <button
                                  onClick={() => setMilitarSelecionado(s.militar!.id)}
                                  style={{ background: "none", border: "none", padding: 0, color: "var(--sidebar-active)", fontWeight: 600, cursor: "pointer", textDecoration: "underline" }}
                                >
                                  {s.militar.nomeGuerra.toUpperCase()}
                                </button>
                              ) : "—"}
                            </td>
                            <td>{s.militar?.posto.sigla ?? "—"}</td>
                            <td>
                              <span className={"pill " + (s.militar ? "pill-green" : "pill-red")}>
                                {s.militar ? "Confirmado" : "Falta militar"}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>

                {podeTriagem && trocasPendentes > 0 && (
                  <div className="card">
                    <h3>Trocas aguardando decisão</h3>
                    <p className="sub">O cabo analisa primeiro; a autorização final é sua</p>
                    <table>
                      <thead>
                        <tr>
                          <th>Quem pediu</th>
                          <th>Dia do serviço</th>
                          <th>Quem assume</th>
                          <th>Situação</th>
                        </tr>
                      </thead>
                      <tbody>
                        {[...emTriagem, ...aguardandoAutorizacao].slice(0, 5).map((s) => (
                          <tr key={s.id}>
                            <td>{s.solicitante.nomeExibicao}</td>
                            <td>{formatarDataBR(s.servicoOrigem.data)}</td>
                            <td>{s.substituto.nomeExibicao}</td>
                            <td>
                              <span className="pill pill-amber">
                                {s.situacao === "EM_TRIAGEM" ? "Com o cabo" : "Esperando você"}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    <button className="btn btn-outline" style={{ marginTop: 12 }} onClick={() => navigate("/trocas")}>
                      Ver todas as trocas
                    </button>
                  </div>
                )}
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
                <div className="card">
                  <h3>Ações rápidas</h3>
                  <div className="quick-actions" style={{ marginTop: 10 }}>
                    {(usuario?.perfil === "CABO_SARGENTEACAO" || usuario?.perfil === "SARGENTEANTE") && (
                      <button className="quick-action" onClick={() => navigate("/escala")}>
                        Montar a escala do mês
                      </button>
                    )}
                    <button className="quick-action" onClick={() => navigate("/trocas")}>
                      Ver trocas de serviço
                    </button>
                    {usuario?.perfil === "SARGENTEANTE" && (
                      <button className="quick-action" onClick={() => navigate("/bloqueio")}>
                        Travar um dia da escala
                      </button>
                    )}
                    <button className="quick-action" onClick={() => navigate("/missoes")}>
                      Registrar missão/dispensa
                    </button>
                  </div>
                </div>

                <div className="card">
                  <h3>Precisa de atenção</h3>
                  <div style={{ marginTop: 10 }}>
                    {vagasAbertas > 0 && (
                      <div className="alert-item critico">
                        <strong>{vagasAbertas} vaga(s) em aberto</strong>
                        A escala tem posto sem gente elegível ou disponível.
                      </div>
                    )}
                    {trocasPendentes > 0 && podeTriagem && (
                      <div className="alert-item">
                        <strong>{trocasPendentes} troca(s) pendente(s)</strong>
                        Aguardando triagem ou autorização.
                      </div>
                    )}
                    {afastamentosHoje.length > 0 && (
                      <div className="alert-item">
                        <strong>{afastamentosHoje.length} militar(es) fora hoje</strong>
                        Em missão, dispensa, férias ou licença.
                      </div>
                    )}
                    {vagasAbertas === 0 && trocasPendentes === 0 && afastamentosHoje.length === 0 && (
                      <p className="sub">Nenhum ponto de atenção no momento.</p>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
      <MilitarDetalheOverlay militarId={militarSelecionado} onFechar={() => setMilitarSelecionado(null)} />
    </>
  );
}
