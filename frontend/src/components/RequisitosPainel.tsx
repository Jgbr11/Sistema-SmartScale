import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { PostoGraduacao, Qualificacao, RequisitoServico, Subunidade, TipoServico } from "../api/types";
import { descreverRequisito } from "../utils/requisitos";

export function RequisitosPainel({ tipo, podeEditar, onMudou }: { tipo: TipoServico; podeEditar: boolean; onMudou: () => void }) {
  const [requisitos, setRequisitos] = useState<RequisitoServico[]>([]);
  const [postos, setPostos] = useState<PostoGraduacao[]>([]);
  const [subunidades, setSubunidades] = useState<Subunidade[]>([]);
  const [cursos, setCursos] = useState<Qualificacao[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  const [postoId, setPostoId] = useState<number | "">("");
  const [subunidadeId, setSubunidadeId] = useState<number | "">("");
  const [cursoId, setCursoId] = useState<number | "">("");
  const [subunidadeExcluidaId, setSubunidadeExcluidaId] = useState<number | "">("");
  const [cursosExcluidos, setCursosExcluidos] = useState<number[]>([]);

  async function carregar() {
    setRequisitos(await api.get<RequisitoServico[]>(`/api/tipos-servico/${tipo.id}/requisitos`));
  }

  useEffect(() => {
    carregar();
    Promise.all([
      api.get<PostoGraduacao[]>("/api/postos-graduacao"),
      api.get<Subunidade[]>("/api/subunidades"),
      api.get<Qualificacao[]>("/api/qualificacoes"),
    ]).then(([p, s, q]) => {
      setPostos(p.slice().sort((a, b) => b.nivelHierarquico - a.nivelHierarquico));
      setSubunidades(s);
      setCursos(q);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tipo.id]);

  async function adicionar() {
    if (postoId === "") {
      setErro("Escolha o posto/graduação.");
      return;
    }
    setErro(null);
    try {
      await api.post(`/api/tipos-servico/${tipo.id}/requisitos`, {
        postoId,
        subunidadeId: subunidadeId === "" ? null : subunidadeId,
        qualificacaoId: cursoId === "" ? null : cursoId,
        qualificacoesExcluidasIds: cursosExcluidos,
        subunidadeExcluidaId: subunidadeExcluidaId === "" ? null : subunidadeExcluidaId,
      });
      setPostoId(""); setSubunidadeId(""); setCursoId(""); setSubunidadeExcluidaId(""); setCursosExcluidos([]);
      await carregar();
      onMudou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível adicionar.");
    }
  }

  async function remover(r: RequisitoServico) {
    if (!confirm(`Remover "${descreverRequisito(r)}" de ${tipo.nome}?`)) return;
    try {
      await api.delete(`/api/tipos-servico/${tipo.id}/requisitos/${r.id}`);
      await carregar();
      onMudou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível remover.");
    }
  }

  function alternarCursoExcluido(id: number) {
    setCursosExcluidos((atual) => (atual.includes(id) ? atual.filter((x) => x !== id) : [...atual, id]));
  }

  return (
    <div style={{ padding: "12px 16px", background: "var(--table-head-bg)" }}>
      <p className="sub" style={{ marginBottom: 8 }}>
        Quem pode tirar <strong>{tipo.nome}</strong> — basta cumprir uma das linhas:
      </p>
      {erro && <div className="error-box">{erro}</div>}
      {requisitos.length === 0 ? (
        <p style={{ fontSize: 12.5, color: "var(--red-text, #a33)" }}>
          Ninguém pode tirar este serviço ainda — toda geração vai deixar vaga em aberto.
        </p>
      ) : (
        <ul style={{ margin: "0 0 10px 18px" }}>
          {requisitos.map((r) => (
            <li key={r.id} style={{ fontSize: 13, marginBottom: 4 }}>
              {descreverRequisito(r)}
              {podeEditar && (
                <button className="btn btn-outline" style={{ marginLeft: 8, padding: "1px 8px" }} onClick={() => remover(r)}>
                  Remover
                </button>
              )}
            </li>
          ))}
        </ul>
      )}

      {podeEditar && (
        <div className="form-grid" style={{ marginTop: 8 }}>
          <div className="field">
            <label>Posto/graduação *</label>
            <select value={postoId} onChange={(e) => setPostoId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Escolha…</option>
              {postos.map((p) => <option key={p.id} value={p.id}>{p.descricao}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Só quem está lotado em</label>
            <select value={subunidadeId} onChange={(e) => setSubunidadeId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Qualquer subunidade</option>
              {subunidades.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Exige o curso</label>
            <select value={cursoId} onChange={(e) => setCursoId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Nenhum</option>
              {cursos.map((q) => <option key={q.id} value={q.id}>{q.nome}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Exceto lotados em</label>
            <select value={subunidadeExcluidaId} onChange={(e) => setSubunidadeExcluidaId(e.target.value === "" ? "" : Number(e.target.value))}>
              <option value="">Ninguém excluído</option>
              {subunidades.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
            </select>
          </div>
          <div className="field">
            <label>Exceto quem tem</label>
            <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
              {cursos.map((q) => (
                <label key={q.id} style={{ fontWeight: 400, display: "flex", gap: 4, alignItems: "center" }}>
                  <input type="checkbox" checked={cursosExcluidos.includes(q.id)} onChange={() => alternarCursoExcluido(q.id)} />
                  {q.nome}
                </label>
              ))}
            </div>
          </div>
          <div className="field" style={{ alignSelf: "end" }}>
            <button className="btn btn-primary" onClick={adicionar}>Adicionar combinação</button>
          </div>
        </div>
      )}
    </div>
  );
}
