import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { CandidatoTrocaMutua, Militar, ServicoEscalado, Solicitacao } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { useAuth } from "../context/AuthContext";
import { formatarDataBR } from "../utils/formatadores";
import { usePermissoes } from "../hooks/usePermissoes";
import { hojeISO } from "../utils/datas";
import { useAoMudar } from "../hooks/useAoMudar";
import { useFeedback } from "../components/ui/Feedback";
import { TabelaSolicitacoes, type Coluna } from "../components/trocas/TabelaSolicitacoes";
import { Esqueleto } from "../components/ui/Esqueleto";
import { EstadoVazio } from "../components/ui/EstadoVazio";


const QUEM_PEDIU: Coluna = { titulo: "Quem pediu", valor: (s) => s.solicitante.nomeExibicao };
const SERVICO: Coluna = { titulo: "Serviço", valor: (s) => s.servicoOrigemTipo };
const DIA: Coluna = { titulo: "Dia", valor: (s) => formatarDataBR(s.servicoOrigemData) };
const TIPO: Coluna = { titulo: "Tipo", valor: (s) => <TipoTrocaPill tipo={s.tipoTroca} /> };
const SITUACAO: Coluna = { titulo: "Situação", valor: (s) => <SituacaoPill situacao={s.situacao} /> };
const JUSTIFICATIVA: Coluna = { titulo: "Justificativa", valor: (s) => <span className="texto-12">{s.justificativa}</span> };
const PARECER_DO_CABO: Coluna = { titulo: "Parecer do Cabo", valor: (s) => <span className="texto-12">{s.comentarioCabo || "—"}</span> };
const COM_QUEM: Coluna = {
  titulo: "Com quem",
  valor: (s) => (
    <>
      {s.substituto.nomeExibicao}
      {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestinoData && (
        <span className="nota-bloco">você assume o dia {formatarDataBR(s.servicoDestinoData)} dele</span>
      )}
    </>
  ),
};
const ASSUME: Coluna = {
  titulo: "Assume",
  valor: (s) => (
    <>
      {s.substituto.nomeExibicao}
      {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestinoData && (
        <span className="nota-bloco">e {s.solicitante.nomeExibicao} assume o dia {formatarDataBR(s.servicoDestinoData)} dele</span>
      )}
    </>
  ),
};
const TIPO_PARA_QUEM_ASSUME: Coluna = {
  titulo: "Tipo",
  valor: (s) => (
    <>
      <TipoTrocaPill tipo={s.tipoTroca} />
      {s.tipoTroca === "TROCA_MUTUA" && s.servicoDestinoData && (
        <span className="nota-bloco mt-2">
          você assumiria o dia {formatarDataBR(s.servicoOrigemData)}, e ele assumiria seu dia {formatarDataBR(s.servicoDestinoData)}
        </span>
      )}
    </>
  ),
};

export function TrocasPage() {
  const { avisar, confirmar, perguntar } = useFeedback();
  const { fazTriagem: podeTriagem, autorizaTrocas: podeAutorizar } = usePermissoes();

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

  useAoMudar(carregar);

  async function cancelar(id: number) {
    if (!(await confirmar("Cancelar este pedido de troca?"))) return;
    try {
      await api.post(`/api/solicitacoes/${id}/cancelar`);
      carregar();
      avisar("Pedido cancelado.", "sucesso");
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.", "erro");
    }
  }

  async function decidirConfirmacao(id: number, aceito: boolean) {
    const resposta = aceito ? "" : await perguntar("Motivo da recusa (opcional):");
    if (resposta === null) return;
    const comentario = resposta;
    try {
      await api.post(`/api/solicitacoes/${id}/confirmar-substituto`, { aceito, comentario });
      avisar(aceito ? "Você assumiu o pedido — agora vai para a triagem." : "Pedido recusado.", "sucesso");
      carregar();
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.", "erro");
    }
  }

  async function decidirTriagem(id: number, aprovado: boolean) {
    const resposta = await perguntar(aprovado ? "Comentário (opcional):" : "Motivo da recusa:", { obrigatorio: !aprovado });
    if (resposta === null) return;
    const comentario = resposta;
    if (!aprovado && !comentario) return;
    try {
      await api.post(`/api/solicitacoes/${id}/triagem`, { aprovado, comentario });
      carregar();
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.", "erro");
    }
  }

  async function decidirAutorizacao(id: number, aprovado: boolean) {
    const resposta = await perguntar(aprovado ? "Comentário (opcional):" : "Motivo da recusa:", { obrigatorio: !aprovado });
    if (resposta === null) return;
    const comentario = resposta;
    if (!aprovado && !comentario) return;
    try {
      await api.post(`/api/solicitacoes/${id}/autorizacao`, { aprovado, comentario });
      carregar();
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível decidir.", "erro");
    }
  }

  return (
    <>
      <PageHeader
        title="Trocas de serviço"
        subtitle="Pedido → substituto confirma → triagem do Cabo → autorização do Sargenteante"
      />
      <div className="body">
        <div className="linha">
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
            <div className="linha-fim">
              <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
                {mostrarForm ? "Cancelar" : "Pedir troca"}
              </button>
            </div>
            {mostrarForm && <PedirTrocaForm onCriado={() => { setMostrarForm(false); carregar(); }} />}
            <div className="card card-tabela">
              {carregando ? (
                <Esqueleto />
              ) : (
                <TabelaSolicitacoes
                  itens={minhas}
                  vazio={
                    <EstadoVazio
                      titulo="Nenhum pedido de troca"
                      descricao="Precisa passar um serviço ou trocar de dia? Faça o pedido e acompanhe aqui."
                      acao={!mostrarForm && <button className="btn btn-primary" onClick={() => setMostrarForm(true)}>Pedir troca</button>}
                    />
                  }
                  colunas={[SERVICO, DIA, TIPO, COM_QUEM, SITUACAO]}
                  acoes={(s) =>
                    (s.situacao === "AGUARDANDO_SUBSTITUTO" || s.situacao === "EM_TRIAGEM") && (
                      <button className="btn btn-outline" onClick={() => cancelar(s.id)}>Cancelar</button>
                    )}
                />
              )}
            </div>
          </>
        )}

        {aba === "confirmar" && (
          <div className="card card-tabela">
            <TabelaSolicitacoes
              itens={aguardandoConfirmacao}
              vazio="Ninguém te pediu pra assumir um serviço no momento."
              colunas={[QUEM_PEDIU, SERVICO, DIA, TIPO_PARA_QUEM_ASSUME, JUSTIFICATIVA]}
              acoes={(s) => (
                <>
                  <button className="btn btn-primary mr-6" onClick={() => decidirConfirmacao(s.id, true)}>Assumir o serviço</button>
                  <button className="btn btn-outline" onClick={() => decidirConfirmacao(s.id, false)}>Recusar</button>
                </>
              )}
            />
          </div>
        )}

        {aba === "triagem" && podeTriagem && (
          <div className="card card-tabela">
            <TabelaSolicitacoes
              itens={emTriagem}
              vazio="Nada esperando triagem."
              colunas={[QUEM_PEDIU, SERVICO, DIA, TIPO, ASSUME, JUSTIFICATIVA]}
              acoes={(s) => (
                <>
                  <button className="btn btn-primary mr-6" onClick={() => decidirTriagem(s.id, true)}>Aprovar</button>
                  <button className="btn btn-outline" onClick={() => decidirTriagem(s.id, false)}>Negar</button>
                </>
              )}
            />
          </div>
        )}

        {aba === "autorizacao" && podeAutorizar && (
          <div className="card card-tabela">
            <TabelaSolicitacoes
              itens={aguardandoAutorizacao}
              vazio="Nada esperando autorização."
              colunas={[QUEM_PEDIU, SERVICO, DIA, TIPO, ASSUME, PARECER_DO_CABO]}
              acoes={(s) => (
                <>
                  <button className="btn btn-primary mr-6" onClick={() => decidirAutorizacao(s.id, true)}>Autorizar</button>
                  <button className="btn btn-outline" onClick={() => decidirAutorizacao(s.id, false)}>Negar</button>
                </>
              )}
            />
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
  const { avisar } = useFeedback();
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
      const hojeStr = hojeISO(hoje);
      setMeusServicos([...a, ...b].filter((s) => s.data >= hojeStr && !s.travado));
    });
  }, []);

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
      const destinatario = tipoTroca === "SUBSTITUICAO"
        ? elegiveis.find((m) => m.id === substitutoId)?.nomeExibicao
        : candidatosMutua.find((c) => c.servicoId === servicoDestinoId)?.militar.nomeExibicao;
      avisar(destinatario ? `Pedido enviado para ${destinatario}.` : "Pedido enviado.", "sucesso");
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
      <p className="sub">Escolha um serviço seu, o tipo de troca, e quem vai participar. A outra pessoa ainda precisa aceitar.</p>
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
          <p className="nota-pequena mt-4">
            Você não tem serviços futuros disponíveis pra trocar (ou estão travados).
          </p>
        )}
      </div>

      {servicoId !== "" && (
        <div className="field">
          <label>Tipo de troca</label>
          <div className="pilha-compacta mt-4">
            {TIPOS_TROCA.map((t) => (
              <label
                key={t.valor}
                className={"opcao-troca" + (tipoTroca === t.valor ? " escolhida" : "")}
              >
                <span className="linha">
                  <input type="radio" name="tipoTroca" checked={tipoTroca === t.valor} onChange={() => setTipoTroca(t.valor)} />
                  <strong className="texto-13">{t.nome}</strong>
                </span>
                <span className="trocas-detalhe">{t.explicacao}</span>
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
          <p className="nota-pequena mt-4">
            Só aparece quem respeita o intervalo mínimo de descanso da regra desse serviço. Se
            combinou com alguém que não está na lista (troca 1-pra-1 espontânea), fale com o Cabo
            pra registrar manualmente.
          </p>
          {elegiveis.length === 0 && (
            <p className="nota-pequena mt-4">
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
          <p className="nota-pequena mt-4">
            Só aparece quem tem serviço do mesmo tipo e continua com folga suficiente dos dois
            lados depois da troca (você assumindo o dia dele, e ele assumindo o seu).
          </p>
          {candidatosMutua.length === 0 && (
            <p className="nota-pequena mt-4">
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
        {salvando ? "Enviando…" : "Pedir troca"}
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
