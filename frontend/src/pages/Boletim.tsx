import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiError } from "../api/client";
import type { Aviso, Boletim, BoletimResumo } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { RichEditor } from "../components/boletim/RichEditor";
import { TIPO_AFASTAMENTO_LABEL } from "../utils/afastamentoTipos";
import { formatarDataBR, formatarDataHora } from "../utils/formatadores";
import DOMPurify from "dompurify";
import { usePermissoes } from "../hooks/usePermissoes";
import { useAoMudar } from "../hooks/useAoMudar";
import { useFeedback } from "../components/ui/Feedback";
import { Esqueleto } from "../components/ui/Esqueleto";

export function BoletimPage() {
  const { avisar, confirmar } = useFeedback();
  const { gerenciaCadastros: podeEditar } = usePermissoes();
  const navigate = useNavigate();

  const [boletins, setBoletins] = useState<BoletimResumo[]>([]);
  const [conteudos, setConteudos] = useState<Map<number, string>>(new Map());
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<Boletim | null>(null);
  const [aberto, setAberto] = useState<number | null>(null);

  async function carregar() {
    setCarregando(true);
    setBoletins(await api.get<BoletimResumo[]>("/api/boletins"));
    setConteudos(new Map());
    setCarregando(false);
  }

  useAoMudar(carregar);

  async function detalhe(id: number): Promise<Boletim> {
    const b = await api.get<Boletim>(`/api/boletins/${id}`);
    setConteudos((atual) => new Map(atual).set(id, b.conteudoHtml));
    return b;
  }

  async function alternar(id: number) {
    if (aberto === id) {
      setAberto(null);
      return;
    }
    if (!conteudos.has(id)) await detalhe(id);
    setAberto(id);
  }

  async function editar(id: number) {
    setEditando(await detalhe(id));
    setMostrarForm(true);
  }

  async function remover(id: number) {
    if (!(await confirmar("Remover este boletim?"))) return;
    try {
      await api.delete(`/api/boletins/${id}`);
      carregar();
      avisar("Boletim removido.", "sucesso");
    } catch (e) {
      avisar(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.", "erro");
    }
  }

  return (
    <>
      <PageHeader title="Boletim Interno" subtitle="Comunicados do batalhão — texto e imagens" />
      <div className="body">
        {podeEditar && !mostrarForm && (
          <div className="linha-fim">
            <button className="btn btn-primary" onClick={() => { setEditando(null); setMostrarForm(true); }}>Novo boletim</button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <BoletimForm
            boletim={editando}
            onSalvou={() => { setMostrarForm(false); setEditando(null); carregar(); }}
            onCancelar={() => { setMostrarForm(false); setEditando(null); }}
          />
        )}

        {carregando ? (
          <Esqueleto />
        ) : boletins.length === 0 ? (
          <div className="card" style={{ color: "var(--grey)", fontSize: 13 }}>Nenhum boletim publicado ainda.</div>
        ) : (
          boletins.map((b) => (
            <div key={b.id} className="card">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                <div style={{ cursor: "pointer", flex: 1 }} onClick={() => alternar(b.id)}>
                  <h3 style={{ marginBottom: 2 }}>{b.numero ? `BI nº ${b.numero} — ` : ""}{b.titulo}</h3>
                  <p className="sub">
                    {b.autor} · {formatarDataHora(b.dataPublicacao)}
                    {b.dataAtualizacao && ` · editado ${formatarDataHora(b.dataAtualizacao)}`}
                  </p>
                  {b.avisoRelacionadoDescricao && (
                    <button
                      onClick={(e) => { e.stopPropagation(); navigate("/avisos"); }}
                      className="pill pill-amber"
                      style={{ marginTop: 4, cursor: "pointer", border: "none" }}
                    >
                      Relacionado: {b.avisoRelacionadoDescricao}
                    </button>
                  )}
                </div>
                {podeEditar && (
                  <div style={{ display: "flex", gap: 6, whiteSpace: "nowrap" }}>
                    <button className="btn btn-outline" onClick={() => editar(b.id)}>Editar</button>
                    <button className="btn btn-outline" onClick={() => remover(b.id)}>Remover</button>
                  </div>
                )}
              </div>
              {aberto === b.id && (
                <div
                  style={{ marginTop: 14, paddingTop: 14, borderTop: "1px solid var(--border-2)", fontSize: 13.5, lineHeight: 1.6 }}
                  dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(conteudos.get(b.id) ?? "") }}
                />
              )}
            </div>
          ))
        )}
      </div>
    </>
  );
}

function BoletimForm({ boletim, onSalvou, onCancelar }: { boletim: Boletim | null; onSalvou: () => void; onCancelar: () => void }) {
  const [numero, setNumero] = useState(boletim?.numero ?? "");
  const [titulo, setTitulo] = useState(boletim?.titulo ?? "");
  const [conteudoHtml, setConteudoHtml] = useState(boletim?.conteudoHtml ?? "");
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const [avisoChave, setAvisoChave] = useState(boletim?.avisoRelacionado ?? "");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    const hoje = new Date();
    const mesAtual = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, "0")}`;
    const proximo = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 1);
    const mesProx = `${proximo.getFullYear()}-${String(proximo.getMonth() + 1).padStart(2, "0")}`;
    Promise.all([
      api.get<Aviso[]>(`/api/avisos?mes=${mesAtual}`),
      api.get<Aviso[]>(`/api/avisos?mes=${mesProx}`),
    ]).then(([a, b]) => setAvisos([...a, ...b]));
  }, []);

  async function salvar() {
    if (!titulo || !conteudoHtml || conteudoHtml === "<br>") {
      setErro("Preencha o título e o conteúdo.");
      return;
    }
    setSalvando(true);
    setErro(null);
    const avisoEscolhido = avisos.find((a) => a.chave === avisoChave);
    const avisoRelacionadoDescricao = avisoEscolhido
      ? `${TIPO_AFASTAMENTO_LABEL[avisoEscolhido.tipo as keyof typeof TIPO_AFASTAMENTO_LABEL] ?? (avisoEscolhido.tipo === "FERIADO" ? "Feriado" : avisoEscolhido.tipo)} — ${avisoEscolhido.descricao}`
      : "";
    try {
      const payload = { numero, titulo, conteudoHtml, avisoRelacionado: avisoChave || null, avisoRelacionadoDescricao: avisoChave ? avisoRelacionadoDescricao : null };
      if (boletim) {
        await api.put(`/api/boletins/${boletim.id}`, payload);
      } else {
        await api.post("/api/boletins", payload);
      }
      onSalvou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>{boletim ? "Editar boletim" : "Novo boletim"}</h3>
      {erro && <div className="error-box">{erro}</div>}
      <div className="form-grid">
        <div className="field">
          <label>Número do BI (opcional)</label>
          <input value={numero} onChange={(e) => setNumero(e.target.value)} placeholder="Ex.: 107" />
        </div>
        <div className="field">
          <label>Título</label>
          <input value={titulo} onChange={(e) => setTitulo(e.target.value)} placeholder="Ex.: Alterações no efetivo" />
        </div>
      </div>
      <div className="field">
        <label>Relacionar a um feriado ou missão (opcional)</label>
        <select value={avisoChave} onChange={(e) => setAvisoChave(e.target.value)}>
          <option value="">Nenhum</option>
          {avisos.map((a) => (
            <option key={a.chave} value={a.chave}>
              {formatarDataBR(a.dataInicio)}
              {a.dataInicio !== a.dataFim ? ` a ${formatarDataBR(a.dataFim)}` : ""} — {a.descricao}
            </option>
          ))}
        </select>
        <p style={{ fontSize: 11, color: "var(--grey)", marginTop: 4 }}>
          Conecta esse boletim ao evento na tela de Avisos, pra quem estiver lá ver o comunicado relacionado.
        </p>
      </div>
      <div className="field">
        <label>Conteúdo</label>
        <RichEditor valorInicial={conteudoHtml} onChange={setConteudoHtml} />
      </div>
      <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
        <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
          {salvando ? "Salvando…" : "Publicar"}
        </button>
        <button className="btn btn-outline" onClick={onCancelar} disabled={salvando}>Cancelar</button>
      </div>
    </div>
  );
}
