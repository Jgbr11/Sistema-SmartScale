import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { Escala, ServicoEscalado } from "../api/types";
import { PageHeader } from "../components/Shell";
import { MilitarDetalheOverlay } from "../components/MilitarDetalheOverlay";
import { useAuth } from "../context/AuthContext";
import { ordenarPorTipo } from "../utils/ordemTipos";
import { capitalizar, formatarDataBR } from "../utils/formatadores";

const DIAS_SEMANA = ["DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"];

export function EscalaDoMesPage() {
  const { usuario } = useAuth();
  const podeGerar = usuario?.perfil === "CABO_SARGENTEACAO" || usuario?.perfil === "SARGENTEANTE";
  const podePublicar = usuario?.perfil === "SARGENTEANTE";

  const [selecionada, setSelecionada] = useState<Escala | null>(null);
  const [totalEscalas, setTotalEscalas] = useState(0);
  const [carregando, setCarregando] = useState(true);
  const [gerando, setGerando] = useState(false);
  const [publicando, setPublicando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [diaEscolhido, setDiaEscolhido] = useState<string | null>(null);
  const [mesExibido, setMesExibido] = useState<{ ano: number; mes: number } | null>(null);
  const [travando, setTravando] = useState(false);
  const [militarSelecionado, setMilitarSelecionado] = useState<number | null>(null);

  const hoje = new Date();
  const [dataInicio, setDataInicio] = useState(primeiroDiaDoMes(hoje));
  const [dataFim, setDataFim] = useState(ultimoDiaDoMes(hoje));

  async function carregar() {
    setCarregando(true);
    const lista = await api.get<Escala[]>("/api/escalas");
    setTotalEscalas(lista.length);
    if (lista.length > 0) {
      const detalhe = await api.get<Escala>(`/api/escalas/${lista[0].id}`);
      setSelecionada(detalhe);
      const d = new Date(detalhe.dataInicio + "T00:00:00");
      setMesExibido({ ano: d.getFullYear(), mes: d.getMonth() });
      setDiaEscolhido(null);
    }
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  async function gerar() {
    setGerando(true);
    setErro(null);
    try {
      const nova = await api.post<Escala>("/api/escalas/gerar", { dataInicio, dataFim });
      setSelecionada(nova);
      const d = new Date(nova.dataInicio + "T00:00:00");
      setMesExibido({ ano: d.getFullYear(), mes: d.getMonth() });
      setDiaEscolhido(null);
      carregar();
    } catch (e) {
      if (e instanceof ApiError && e.status === 403) {
        setErro("Seu perfil não gera escala.");
      } else if (e instanceof ApiError && e.status === 400) {
        setErro(e.message);
      } else {
        setErro("Não foi possível gerar a escala.");
      }
    } finally {
      setGerando(false);
    }
  }

  async function publicar() {
    if (!selecionada) return;
    setPublicando(true);
    setErro(null);
    try {
      const atualizada = await api.post<Escala>(`/api/escalas/${selecionada.id}/publicar`);
      setSelecionada(atualizada);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError && e.status === 403
        ? "Só o Sargenteante publica a escala (RN14)."
        : "Não foi possível publicar.");
    } finally {
      setPublicando(false);
    }
  }

  const previstos = selecionada?.servicos.filter((s) => s.militar).length ?? 0;
  const abertos = (selecionada?.servicos.length ?? 0) - previstos;

  function mudarMesExibido(delta: number) {
    if (!mesExibido) return;
    let novoMes = mesExibido.mes + delta;
    let novoAno = mesExibido.ano;
    if (novoMes < 0) { novoMes = 11; novoAno -= 1; }
    if (novoMes > 11) { novoMes = 0; novoAno += 1; }
    setMesExibido({ ano: novoAno, mes: novoMes });
    setDiaEscolhido(null);
  }

  const servicosPorDia = new Map<string, ServicoEscalado[]>();
  if (selecionada) {
    for (const s of selecionada.servicos) {
      if (!servicosPorDia.has(s.data)) servicosPorDia.set(s.data, []);
      servicosPorDia.get(s.data)!.push(s);
    }
  }

  const servicosDoDiaEscolhido = diaEscolhido
    ? ordenarPorTipo(servicosPorDia.get(diaEscolhido) ?? [])
    : [];
  const diaTravado = servicosDoDiaEscolhido.length > 0 && servicosDoDiaEscolhido.every((s) => s.travado);

  async function travarDiaEscolhido() {
    if (!selecionada || !diaEscolhido) return;
    setTravando(true);
    try {
      await api.post(`/api/escalas/${selecionada.id}/dias/${diaEscolhido}/travar`);
      const detalhe = await api.get<Escala>(`/api/escalas/${selecionada.id}`);
      setSelecionada(detalhe);
    } catch {
      setErro("Não foi possível travar o dia.");
    } finally {
      setTravando(false);
    }
  }

  async function destravarDiaEscolhido() {
    if (!selecionada || !diaEscolhido) return;
    setTravando(true);
    try {
      await api.post(`/api/escalas/${selecionada.id}/dias/${diaEscolhido}/destravar`);
      const detalhe = await api.get<Escala>(`/api/escalas/${selecionada.id}`);
      setSelecionada(detalhe);
    } catch {
      setErro("Não foi possível destravar o dia.");
    } finally {
      setTravando(false);
    }
  }

  let celulas: (number | null)[] = [];
  let nomeMesExibido = "";
  if (mesExibido) {
    const primeiroDia = new Date(mesExibido.ano, mesExibido.mes, 1);
    const diasNoMes = new Date(mesExibido.ano, mesExibido.mes + 1, 0).getDate();
    const offsetInicial = primeiroDia.getDay();
    celulas = [...Array(offsetInicial).fill(null), ...Array.from({ length: diasNoMes }, (_, i) => i + 1)];
    nomeMesExibido = capitalizar(primeiroDia.toLocaleDateString("pt-BR", { month: "long", year: "numeric" }));
  }

  return (
    <>
      <PageHeader title="Escala do mês" subtitle="Clique num dia do calendário pra ver quem está escalado" />
      <div className="body">
        {erro && <div className="error-box">{erro}</div>}

        {podeGerar && (
          <div className="card">
            <h3>Gerar nova escala</h3>
            <p className="sub">RF08 — o motor escolhe quem está há mais tempo sem tirar serviço</p>
            <div className="form-grid">
              <div className="field">
                <label>De</label>
                <input type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
              </div>
              <div className="field">
                <label>Até</label>
                <input type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
              </div>
            </div>
            <button className="btn btn-primary" onClick={gerar} disabled={gerando}>
              {gerando ? "Montando…" : "Montar a escala"}
            </button>
          </div>
        )}

        {carregando ? (
          <div className="card">Carregando…</div>
        ) : selecionada && mesExibido ? (
          <>
            <div className="card">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <h3>{selecionada.descricao}</h3>
                  <p className="sub">
                    {selecionada.dataInicio} a {selecionada.dataFim} ·{" "}
                    <span className={"pill " + (selecionada.situacao === "PUBLICADA" ? "pill-green" : "pill-amber")}>
                      {selecionada.situacao}
                    </span>
                  </p>
                </div>
                {podePublicar && selecionada.situacao === "RASCUNHO" && (
                  <button className="btn btn-primary" onClick={publicar} disabled={publicando}>
                    {publicando ? "Publicando…" : "Publicar para o efetivo"}
                  </button>
                )}
              </div>
              <div style={{ display: "flex", gap: 24, marginTop: 14, flexWrap: "wrap" }}>
                <Metric label="Vagas previstas" value={previstos} />
                <Metric label="Vagas em aberto" value={abertos} />
                <Metric label="Total de dias" value={diasEntre(selecionada.dataInicio, selecionada.dataFim)} />
                <Metric label="Escalas geradas ao todo" value={totalEscalas} />
              </div>
            </div>

            <div className="card">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                <h3>{nomeMesExibido}</h3>
                <div style={{ display: "flex", gap: 8 }}>
                  <button className="btn btn-outline" onClick={() => mudarMesExibido(-1)}>← Mês anterior</button>
                  <button className="btn btn-outline" onClick={() => mudarMesExibido(1)}>Próximo mês →</button>
                </div>
              </div>
              <div className="calendar-grid">
                {DIAS_SEMANA.map((d) => <div key={d} className="dow">{d}</div>)}
                {celulas.map((dia, idx) => {
                  if (dia === null) return <div key={idx} className="calendar-cell empty" />;
                  const dataStr = `${mesExibido.ano}-${String(mesExibido.mes + 1).padStart(2, "0")}-${String(dia).padStart(2, "0")}`;
                  const servicosDoDia = servicosPorDia.get(dataStr) ?? [];
                  const temVagaAberta = servicosDoDia.some((s) => !s.militar);
                  const travadoNoDia = servicosDoDia.length > 0 && servicosDoDia.every((s) => s.travado);
                  const dentroDaEscala = dataStr >= selecionada.dataInicio && dataStr <= selecionada.dataFim;
                  const selecionado = diaEscolhido === dataStr;
                  return (
                    <button
                      key={idx}
                      className="calendar-cell"
                      style={{
                        cursor: dentroDaEscala ? "pointer" : "default",
                        textAlign: "left",
                        background: selecionado ? "var(--sidebar-active)" : temVagaAberta ? "var(--amber-bg)" : dentroDaEscala ? "#fbfcfa" : "transparent",
                        color: selecionado ? "#fff" : "var(--dark)",
                        border: dentroDaEscala ? "1px solid var(--border-2)" : "none",
                      }}
                      disabled={!dentroDaEscala}
                      onClick={() => setDiaEscolhido((atual) => (atual === dataStr ? null : dataStr))}
                    >
                      {dia}
                      {dentroDaEscala && (
                        <div className="tipo" style={{ color: selecionado ? "#d8e2cc" : temVagaAberta ? "var(--amber-text)" : "var(--grey)" }}>
                          {servicosDoDia.length} serviço{servicosDoDia.length === 1 ? "" : "s"}
                          {travadoNoDia ? " · travado" : ""}
                        </div>
                      )}
                    </button>
                  );
                })}
              </div>
            </div>

            {diaEscolhido && (
              <div className="card">
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                  <h3>Escala de {formatarDataBR(diaEscolhido)}</h3>
                  <div style={{ display: "flex", gap: 8 }}>
                    <button className="btn btn-outline" onClick={() => window.open(`/escala/pdf/${diaEscolhido}`, "_blank")}>Gerar PDF</button>
                    {podePublicar && servicosDoDiaEscolhido.length > 0 && (
                      diaTravado ? (
                        <button className="btn btn-outline" onClick={destravarDiaEscolhido} disabled={travando}>
                          {travando ? "…" : "Destravar este dia"}
                        </button>
                      ) : (
                        <button className="btn btn-primary" onClick={travarDiaEscolhido} disabled={travando}>
                          {travando ? "…" : "Travar este dia"}
                        </button>
                      )
                    )}
                  </div>
                </div>
                {diaTravado && (
                  <div className="card" style={{ background: "var(--amber-bg)", border: "none", marginBottom: 12 }}>
                    <p style={{ fontSize: 12, color: "var(--amber-text)" }}>
                      Dia travado — nenhuma troca ou alteração manual é aceita aqui, nem pelo Sargenteante (RN04).
                    </p>
                  </div>
                )}
                {servicosDoDiaEscolhido.length === 0 ? (
                  <p className="sub">Nenhum serviço registrado para esse dia nesta escala.</p>
                ) : (
                  <table>
                    <tbody>
                      {servicosDoDiaEscolhido.map((s) => <FuncaoRow key={s.id} servico={s} onClicarMilitar={setMilitarSelecionado} />)}
                    </tbody>
                  </table>
                )}
              </div>
            )}
          </>
        ) : (
          <div className="card">Nenhuma escala gerada ainda.</div>
        )}
      </div>
      <MilitarDetalheOverlay militarId={militarSelecionado} onFechar={() => setMilitarSelecionado(null)} />
    </>
  );
}

function FuncaoRow({ servico, onClicarMilitar }: { servico: ServicoEscalado; onClicarMilitar: (id: number) => void }) {
  return (
    <tr>
      <td style={{ fontWeight: 600, width: 220 }}>{servico.tipoServico.nome}</td>
      <td style={{ color: "var(--grey)", width: 90 }}>{servico.militar?.posto.sigla ?? "—"}</td>
      <td>
        {servico.militar ? (
          <button
            onClick={() => onClicarMilitar(servico.militar!.id)}
            style={{ background: "none", border: "none", padding: 0, color: "var(--sidebar-active)", fontWeight: 600, cursor: "pointer", textDecoration: "underline" }}
          >
            {servico.militar.nomeGuerra.toUpperCase()}
          </button>
        ) : (
          <span className="pill pill-red">vaga em aberto</span>
        )}
      </td>
      <td style={{ width: 90, fontSize: 11 }}>
        {servico.travado && <span className="pill pill-grey" title="Travado manualmente pelo Sargenteante">Travado</span>}
        {!servico.travado && servico.jaComecou && <span className="pill pill-grey" title="Já começou — não pode mais mudar">Concluído</span>}
      </td>
    </tr>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <div>
      <div style={{ fontSize: 22, fontWeight: 700 }}>{value}</div>
      <div style={{ fontSize: 11, color: "var(--grey)" }}>{label}</div>
    </div>
  );
}

function primeiroDiaDoMes(d: Date) { return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().slice(0, 10); }
function ultimoDiaDoMes(d: Date) { return new Date(d.getFullYear(), d.getMonth() + 1, 0).toISOString().slice(0, 10); }
function diasEntre(a: string, b: string) { return Math.round((new Date(b).getTime() - new Date(a).getTime()) / 86400000) + 1; }
