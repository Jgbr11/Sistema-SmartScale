import { useState } from "react";
import { api, ApiError } from "../api/client";
import type { Escala, EscalaDoMes, ServicoEscalado } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { BotaoBaixarCsv } from "../components/ui/BotaoBaixarCsv";
import { MilitarDetalheOverlay } from "../components/militar/MilitarDetalheOverlay";
import { ordenarPorTipo } from "../utils/ordemTipos";
import { capitalizar, formatarDataBR } from "../utils/formatadores";
import { usePermissoes } from "../hooks/usePermissoes";
import { useAoMudar } from "../hooks/useAoMudar";
import { CalendarioMensal } from "../components/ui/CalendarioMensal";
import { progressoDoDia } from "../utils/servico";
import { Esqueleto } from "../components/ui/Esqueleto";

interface MesAno {
  ano: number;
  mes: number;
}

export function EscalaDoMesPage() {
  const { geraEscala: podeGerar, publicaEscala: podePublicar } = usePermissoes();

  const hoje = new Date();
  const [mesExibido, setMesExibido] = useState<MesAno>({ ano: hoje.getFullYear(), mes: hoje.getMonth() });
  const [doMes, setDoMes] = useState<EscalaDoMes>({ escalas: [], servicos: [] });
  const [carregando, setCarregando] = useState(true);
  const [gerando, setGerando] = useState(false);
  const [publicandoId, setPublicandoId] = useState<number | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [diaEscolhido, setDiaEscolhido] = useState<string | null>(null);
  const [travando, setTravando] = useState(false);
  const [militarSelecionado, setMilitarSelecionado] = useState<number | null>(null);

  const [dataInicio, setDataInicio] = useState(isoLocal(new Date(hoje.getFullYear(), hoje.getMonth(), 1)));
  const [dataFim, setDataFim] = useState(isoLocal(new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0)));

  async function carregar(alvo: MesAno = mesExibido) {
    setCarregando(true);
    try {
      setDoMes(await api.get<EscalaDoMes>(`/api/escalas/mes?mes=${chaveMes(alvo)}`));
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível carregar a escala do mês.");
    } finally {
      setCarregando(false);
    }
  }

  useAoMudar(() => carregar(mesExibido), chaveMes(mesExibido));

  async function gerar() {
    setGerando(true);
    setErro(null);
    try {
      const nova = await api.post<Escala>("/api/escalas/gerar", { dataInicio, dataFim });
      const d = new Date(nova.dataInicio + "T00:00:00");
      setDiaEscolhido(null);
      const alvo = { ano: d.getFullYear(), mes: d.getMonth() };
      if (alvo.ano === mesExibido.ano && alvo.mes === mesExibido.mes) carregar(alvo);
      else setMesExibido(alvo);
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

  async function publicar(escalaId: number) {
    setPublicandoId(escalaId);
    setErro(null);
    try {
      await api.post(`/api/escalas/${escalaId}/publicar`);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError && e.status === 403
        ? "Só o Sargenteante publica a escala."
        : "Não foi possível publicar.");
    } finally {
      setPublicandoId(null);
    }
  }

  function mudarMesExibido(delta: number) {
    let novoMes = mesExibido.mes + delta;
    let novoAno = mesExibido.ano;
    if (novoMes < 0) { novoMes = 11; novoAno -= 1; }
    if (novoMes > 11) { novoMes = 0; novoAno += 1; }
    setMesExibido({ ano: novoAno, mes: novoMes });
    setDiaEscolhido(null);
  }

  const servicosPorDia = new Map<string, ServicoEscalado[]>();
  for (const s of doMes.servicos) {
    if (!servicosPorDia.has(s.data)) servicosPorDia.set(s.data, []);
    servicosPorDia.get(s.data)!.push(s);
  }

  const previstos = doMes.servicos.filter((s) => s.militar).length;
  const abertos = doMes.servicos.length - previstos;

  const servicosDoDiaEscolhido = diaEscolhido ? ordenarPorTipo(servicosPorDia.get(diaEscolhido) ?? []) : [];
  const diaTravado = servicosDoDiaEscolhido.length > 0 && servicosDoDiaEscolhido.every((s) => s.travado);
  const diaJaComecou = servicosDoDiaEscolhido.length > 0 && servicosDoDiaEscolhido.every((s) => s.jaComecou);

  async function alternarTravamento(acao: "travar" | "destravar") {
    if (!diaEscolhido) return;
    setTravando(true);
    setErro(null);
    try {
      await api.post(`/api/escalas/dias/${diaEscolhido}/${acao}`);
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : `Não foi possível ${acao} o dia.`);
    } finally {
      setTravando(false);
    }
  }

  const primeiroDia = new Date(mesExibido.ano, mesExibido.mes, 1);
  const nomeMesExibido = capitalizar(primeiroDia.toLocaleDateString("pt-BR", { month: "long", year: "numeric" }));

  return (
    <>
      <PageHeader title="Escala do mês" subtitle="Clique num dia do calendário pra ver quem está escalado" />
      <div className="body">
        {erro && <div className="error-box">{erro}</div>}

        {podeGerar && (
          <div className="card">
            <h3>Gerar nova escala</h3>
            <p className="sub">Quem está há mais tempo sem tirar serviço entra primeiro</p>
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

        <div className="card">
          <h3>Escalas de {nomeMesExibido}</h3>
          {doMes.escalas.length === 0 ? (
            <p className="sub">Nenhuma escala gerada para esse mês.</p>
          ) : (
            doMes.escalas.map((e) => (
              <div key={e.id} className="linha-item">
                <p className="sub m-0">
                  <strong>{e.descricao}</strong> · {formatarDataBR(e.dataInicio)} a {formatarDataBR(e.dataFim)} ·{" "}
                  <span className={"pill " + (e.situacao === "PUBLICADA" ? "pill-green" : "pill-amber")}>{e.situacao}</span>
                </p>
                {podePublicar && e.situacao === "RASCUNHO" && (
                  <button className="btn btn-primary" onClick={() => publicar(e.id)} disabled={publicandoId === e.id}>
                    {publicandoId === e.id ? "Publicando…" : "Publicar para o efetivo"}
                  </button>
                )}
              </div>
            ))
          )}
          <div className="linha-resumo">
            <Metric label="Vagas previstas" value={previstos} />
            <Metric label="Vagas em aberto" value={abertos} />
            <Metric label="Dias com escala" value={servicosPorDia.size} />
          </div>
        </div>

        <div className="card">
          <div className="linha-entre mb-12">
            <h3>{nomeMesExibido}</h3>
            <div className="linha">
              <button className="btn btn-outline" onClick={() => mudarMesExibido(-1)}>← Mês anterior</button>
              <button className="btn btn-outline" onClick={() => mudarMesExibido(1)}>Próximo mês →</button>
            </div>
          </div>
          {carregando ? (
            <Esqueleto />
          ) : (
            <CalendarioMensal
              ano={mesExibido.ano}
              mes={mesExibido.mes}
              diaSelecionado={diaEscolhido}
              onSelecionar={(dataStr) => setDiaEscolhido((atual) => (atual === dataStr ? null : dataStr))}
              infoDoDia={(dataStr) => {
                const servicosDoDia = servicosPorDia.get(dataStr) ?? [];
                if (servicosDoDia.length === 0) return { habilitado: false };
                const travadoNoDia = servicosDoDia.every((s) => s.travado);
                return {
                  rotulo: `${servicosDoDia.length} serviço${servicosDoDia.length === 1 ? "" : "s"}${travadoNoDia ? " · travado" : ""}`,
                  destaque: servicosDoDia.some((s) => !s.militar) ? "atencao" : undefined,
                  progresso: progressoDoDia(dataStr),
                };
              }}
            />
          )}
          {!carregando && (
            <div className="calendar-legenda">
              <span><i className="legenda-atencao" />Vaga em aberto</span>
              <span><i className="legenda-hoje" />Hoje</span>
              <span><i className="legenda-cumprido" />Serviço cumprido</span>
            </div>
          )}
        </div>

        {diaEscolhido && (
          <div className="card">
            <div className="linha-entre mb-12">
              <h3>Escala de {formatarDataBR(diaEscolhido)}</h3>
              <div className="linha">
                {diaJaComecou && (
                  <span className="pill pill-grey" title="O serviço deste dia já começou — ele está confirmado e não muda mais">
                    Dia concluído
                  </span>
                )}
                <button className="btn btn-outline" onClick={() => window.open(`/escala/pdf/${diaEscolhido}`, "_blank")}>Gerar PDF</button>
                <BotaoBaixarCsv caminho={`escala-do-dia.csv?data=${diaEscolhido}`} />
                {podePublicar && servicosDoDiaEscolhido.length > 0 && !diaJaComecou && (
                  diaTravado ? (
                    <button className="btn btn-outline" onClick={() => alternarTravamento("destravar")} disabled={travando}>
                      {travando ? "…" : "Destravar este dia"}
                    </button>
                  ) : (
                    <button className="btn btn-primary" onClick={() => alternarTravamento("travar")} disabled={travando}>
                      {travando ? "…" : "Travar este dia"}
                    </button>
                  )
                )}
              </div>
            </div>
            {diaTravado && (
              <div className="card card-atencao mb-12">
                <p className="nota-alerta">
                  Dia travado — nenhuma troca ou alteração manual é aceita aqui, nem pelo Sargenteante.
                </p>
              </div>
            )}
            {servicosDoDiaEscolhido.length === 0 ? (
              <p className="sub">Nenhum serviço registrado para esse dia.</p>
            ) : (
              <table>
                <tbody>
                  {servicosDoDiaEscolhido.map((s) => <FuncaoRow key={s.id} servico={s} onClicarMilitar={setMilitarSelecionado} />)}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
      <MilitarDetalheOverlay militarId={militarSelecionado} onFechar={() => setMilitarSelecionado(null)} />
    </>
  );
}

function FuncaoRow({ servico, onClicarMilitar }: { servico: ServicoEscalado; onClicarMilitar: (id: number) => void }) {
  return (
    <tr>
      <td className="col-nome">{servico.tipoServico.nome}</td>
      <td className="col-hora">{servico.militar?.posto.sigla ?? "—"}</td>
      <td>
        {servico.militar ? (
          <button
            onClick={() => onClicarMilitar(servico.militar!.id)}
            className="link-nome"
          >
            {servico.militar.nomeGuerra.toUpperCase()}
          </button>
        ) : (
          <span className="pill pill-red">vaga em aberto</span>
        )}
      </td>
      <td className="w-90 texto-11">
        {servico.travado && <span className="pill pill-grey" title="Travado manualmente pelo Sargenteante">Travado</span>}
        {!servico.travado && servico.jaComecou && <span className="pill pill-grey" title="Já começou — não pode mais mudar">Concluído</span>}
      </td>
    </tr>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <div>
      <div className="numero-grande">{value}</div>
      <div className="nota-pequena">{label}</div>
    </div>
  );
}

function chaveMes(m: MesAno) {
  return `${m.ano}-${String(m.mes + 1).padStart(2, "0")}`;
}

function isoLocal(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}
