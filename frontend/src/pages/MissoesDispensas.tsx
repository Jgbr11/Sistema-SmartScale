import { useMemo, useState } from "react";
import { api, ApiError } from "../api/client";
import type { Afastamento, Militar } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { BotaoBaixarCsv } from "../components/ui/BotaoBaixarCsv";
import { TIPOS_AFASTAMENTO as TIPOS } from "../utils/afastamentoTipos";
import { formatarDataBR } from "../utils/formatadores";
import { usePermissoes } from "../hooks/usePermissoes";
import { hojeISO } from "../utils/datas";
import { useAoMudar } from "../hooks/useAoMudar";
import { useFeedback } from "../components/ui/Feedback";
import { Esqueleto } from "../components/ui/Esqueleto";

interface GrupoAfastamento {
  loteOuId: string;
  tipo: Afastamento["tipo"];
  descricao: string;
  dataInicio: string;
  dataFim: string;
  militares: Militar[];
  ids: number[];
}

export function MissoesDispensasPage() {
  const { avisar, confirmar } = useFeedback();
  const { gerenciaCadastros: podeEditar } = usePermissoes();

  const [afastamentos, setAfastamentos] = useState<Afastamento[]>([]);
  const [militares, setMilitares] = useState<Militar[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<GrupoAfastamento | null>(null);
  const [ofertaRegenerar, setOfertaRegenerar] = useState<{ dataInicio: string; dataFim: string } | null>(null);
  const [regenerando, setRegenerando] = useState(false);
  const [erroRegenerar, setErroRegenerar] = useState<string | null>(null);

  async function carregar() {
    setCarregando(true);
    const [a, m] = await Promise.all([
      api.get<Afastamento[]>("/api/afastamentos"),
      api.get<Militar[]>("/api/militares"),
    ]);
    a.sort((x, y) => x.dataInicio.localeCompare(y.dataInicio));
    setAfastamentos(a);
    setMilitares(m);
    setCarregando(false);
  }

  useAoMudar(carregar);

  const grupos: GrupoAfastamento[] = useMemo(() => {
    const mapa = new Map<string, GrupoAfastamento>();
    for (const a of afastamentos) {
      const chave = a.loteMissao ?? `solo-${a.id}`;
      if (!mapa.has(chave)) {
        mapa.set(chave, {
          loteOuId: chave, tipo: a.tipo, descricao: a.descricao,
          dataInicio: a.dataInicio, dataFim: a.dataFim, militares: [], ids: [],
        });
      }
      const g = mapa.get(chave)!;
      g.militares.push(a.militar);
      g.ids.push(a.id);
    }
    return Array.from(mapa.values());
  }, [afastamentos]);

  async function cancelarGrupo(ids: number[], dataInicio: string, dataFim: string) {
    if (!(await confirmar(ids.length > 1 ? "Cancelar esse afastamento pra todo mundo dessa missão?" : "Cancelar este afastamento?"))) return;
    try {
      await Promise.all(ids.map((id) => api.delete(`/api/afastamentos/${id}`)));
      setOfertaRegenerar({ dataInicio, dataFim });
      carregar();
      avisar("Afastamento cancelado.", "sucesso");
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.", "erro");
    }
  }

  async function regenerarPeriodo() {
    if (!ofertaRegenerar) return;
    setRegenerando(true);
    setErroRegenerar(null);
    try {
      await api.post("/api/escalas/gerar", ofertaRegenerar);
      setOfertaRegenerar(null);
    } catch (e) {
      setErroRegenerar(e instanceof ApiError ? e.message : "Não foi possível regenerar.");
    } finally {
      setRegenerando(false);
    }
  }

  const hoje = hojeISO();

  return (
    <>
      <PageHeader
        title="Missões e dispensas"
        subtitle="Quem está afastado não é escalado nesse período"
      />
      <div className="body">
        {ofertaRegenerar && (
          <div className="card card-atencao">
            <p className={"texto-13 " + (erroRegenerar ? "mb-4" : "mb-10")}>
              Afastamento cancelado. A escala de {formatarDataBR(ofertaRegenerar.dataInicio)} a{" "}
              {formatarDataBR(ofertaRegenerar.dataFim)} ainda reflete a redistribuição feita na hora do
              cadastro. Quer regenerar esse período agora, pra redistribuir de forma justa?
            </p>
            {erroRegenerar && <div className="error-box mb-10">{erroRegenerar}</div>}
            <div className="linha">
              <button className="btn btn-primary" onClick={regenerarPeriodo} disabled={regenerando}>
                {regenerando ? "Regenerando…" : "Regenerar este período"}
              </button>
              <button className="btn btn-outline" onClick={() => setOfertaRegenerar(null)}>Dispensar</button>
            </div>
          </div>
        )}

        <div className="linha-fim">
          <BotaoBaixarCsv caminho={`afastamentos.csv?mes=${hoje.slice(0, 7)}`} rotulo="Baixar CSV do mês" />
          {podeEditar && (
            <button className="btn btn-primary" onClick={() => { setEditando(null); setMostrarForm((v) => !v); }}>
              {mostrarForm ? "Cancelar" : "Novo afastamento"}
            </button>
          )}
        </div>

        {mostrarForm && podeEditar && (
          <NovoAfastamentoForm militares={militares} onCriado={() => { setMostrarForm(false); carregar(); }} />
        )}

        {editando && podeEditar && (
          <EditarAfastamentoForm
            key={editando.loteOuId}
            grupo={editando}
            onSalvo={() => { setEditando(null); carregar(); }}
            onCancelar={() => setEditando(null)}
          />
        )}

        <div className="card card-tabela">
          {carregando ? (
            <Esqueleto />
          ) : grupos.length === 0 ? (
            <div className="vazio">Nenhum afastamento registrado.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Militar(es)</th>
                  <th>Tipo</th>
                  <th>Descrição</th>
                  <th>Período</th>
                  <th>Situação</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {grupos.map((g) => {
                  const emAndamento = g.dataInicio <= hoje && g.dataFim >= hoje;
                  const futuro = g.dataInicio > hoje;
                  return (
                    <tr key={g.loteOuId}>
                      <td>{g.militares.map((m) => m.nomeExibicao).join(", ")}</td>
                      <td>{TIPOS.find((t) => t.valor === g.tipo)?.label ?? g.tipo}</td>
                      <td>{g.descricao}</td>
                      <td>{formatarDataBR(g.dataInicio)} a {formatarDataBR(g.dataFim)}</td>
                      <td>
                        <span className={"pill " + (emAndamento ? "pill-amber" : futuro ? "pill-grey" : "pill-green")}>
                          {emAndamento ? "Em andamento" : futuro ? "Agendado" : "Encerrado"}
                        </span>
                      </td>
                      {podeEditar && (
                        <td className="nowrap">
                          <button className="btn btn-outline mr-6" onClick={() => { setMostrarForm(false); setEditando(g); }}>Editar</button>
                          <button className="btn btn-outline" onClick={() => cancelarGrupo(g.ids, g.dataInicio, g.dataFim)}>Cancelar</button>
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

function NovoAfastamentoForm({ militares, onCriado }: { militares: Militar[]; onCriado: () => void }) {
  const [militarIds, setMilitarIds] = useState<Set<number>>(new Set());
  const [busca, setBusca] = useState("");
  const [tipo, setTipo] = useState<Afastamento["tipo"]>("MISSAO");
  const [descricao, setDescricao] = useState("");
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const militaresFiltrados = militares.filter((m) =>
    m.nomeExibicao.toLowerCase().includes(busca.toLowerCase())
  );

  function alternar(id: number) {
    setMilitarIds((atual) => {
      const novo = new Set(atual);
      if (novo.has(id)) novo.delete(id); else novo.add(id);
      return novo;
    });
  }

  async function salvar() {
    if (militarIds.size === 0 || !descricao || !dataInicio || !dataFim) {
      setErro("Selecione ao menos um militar e preencha os campos.");
      return;
    }
    if (dataFim < dataInicio) {
      setErro("A data final não pode ser antes da data inicial.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/afastamentos", {
        militarIds: Array.from(militarIds),
        tipo,
        descricao,
        dataInicio,
        dataFim,
      });
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível registrar o afastamento.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Novo afastamento</h3>
      <p className="sub">Pode selecionar mais de uma pessoa (ex.: equipe inteira numa missão)</p>
      {erro && <div className="error-box">{erro}</div>}

      <div className="field">
        <label>Militares ({militarIds.size} selecionado{militarIds.size === 1 ? "" : "s"})</label>
        <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar por nome…" className="mb-8" />
        <div className="lista-rolagem">
          {militaresFiltrados.slice(0, 60).map((m) => (
            <label key={m.id} className="opcao-check">
              <input type="checkbox" checked={militarIds.has(m.id)} onChange={() => alternar(m.id)} />
              {m.nomeExibicao}
            </label>
          ))}
          {militaresFiltrados.length === 0 && <p className="sub">Ninguém encontrado.</p>}
        </div>
      </div>

      <div className="form-grid">
        <div className="field">
          <label>Tipo</label>
          <select value={tipo} onChange={(e) => setTipo(e.target.value as Afastamento["tipo"])}>
            {TIPOS.map((t) => <option key={t.valor} value={t.valor}>{t.label}</option>)}
          </select>
        </div>
        <div className="field">
          <label>De</label>
          <input type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="field">
          <label>Até</label>
          <input type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
      </div>
      <div className="field">
        <label>Descrição</label>
        <input value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Ex.: Curso de tiro na 5ª RM" />
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Registrar afastamento"}
      </button>
    </div>
  );
}

function EditarAfastamentoForm({ grupo, onSalvo, onCancelar }: { grupo: GrupoAfastamento; onSalvo: () => void; onCancelar: () => void }) {
  const [tipo, setTipo] = useState<Afastamento["tipo"]>(grupo.tipo);
  const [descricao, setDescricao] = useState(grupo.descricao);
  const [dataInicio, setDataInicio] = useState(grupo.dataInicio);
  const [dataFim, setDataFim] = useState(grupo.dataFim);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar() {
    if (!descricao || !dataInicio || !dataFim) {
      setErro("Preencha todos os campos.");
      return;
    }
    if (dataFim < dataInicio) {
      setErro("A data final não pode ser antes da data inicial.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.put(`/api/afastamentos/${grupo.ids[0]}`, { tipo, descricao, dataInicio, dataFim });
      onSalvo();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Editar afastamento</h3>
      <p className="sub">
        Vale para {grupo.militares.length === 1 ? "o militar" : `os ${grupo.militares.length} militares`}: {grupo.militares.map((m) => m.nomeExibicao).join(", ")}
      </p>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>Tipo</label>
          <select value={tipo} onChange={(e) => setTipo(e.target.value as Afastamento["tipo"])}>
            {TIPOS.map((t) => <option key={t.valor} value={t.valor}>{t.label}</option>)}
          </select>
        </div>
        <div className="field">
          <label>De</label>
          <input type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="field">
          <label>Até</label>
          <input type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
      </div>
      <div className="field">
        <label>Descrição</label>
        <input value={descricao} onChange={(e) => setDescricao(e.target.value)} />
      </div>
      <div className="linha">
        <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
          {salvando ? "Salvando…" : "Salvar alterações"}
        </button>
        <button className="btn btn-outline" onClick={onCancelar}>Cancelar</button>
      </div>
    </div>
  );
}
