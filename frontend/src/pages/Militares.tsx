import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../api/client";
import type { Militar, PostoGraduacao, Qualificacao, Subunidade } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";
import { mascararCpf, mascararFusex, mascararTelefone, somenteDigitos } from "../utils/mascaras";

export function MilitaresPage() {
  const { usuario } = useAuth();
  // RF04 / RN11: manter o cadastro é privativo de Cabo da Sargenteação e Sargenteante.
  // Sd EP tem "Militares" no menu, mas só em modo de consulta.
  const podeEditar = usuario?.perfil === "CABO_SARGENTEACAO" || usuario?.perfil === "SARGENTEANTE";

  const [militares, setMilitares] = useState<Militar[]>([]);
  const [postos, setPostos] = useState<PostoGraduacao[]>([]);
  const [subunidades, setSubunidades] = useState<Subunidade[]>([]);
  const [quals, setQuals] = useState<Qualificacao[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [gerenciandoId, setGerenciandoId] = useState<number | null>(null);

  async function carregar() {
    setCarregando(true);
    const [m, p, s, q] = await Promise.all([
      api.get<Militar[]>("/api/militares"),
      api.get<PostoGraduacao[]>("/api/postos-graduacao"),
      api.get<Subunidade[]>("/api/subunidades"),
      api.get<Qualificacao[]>("/api/qualificacoes"),
    ]);
    setMilitares(m);
    setPostos(p);
    setSubunidades(s);
    setQuals(q);
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  const ativos = militares.filter((m) => m.situacao === "ATIVO").length;
  // Ordena por hierarquia de posto primeiro (Tenente no topo, Sd EV embaixo),
  // e só dentro do mesmo posto por ordem alfabética do nome de guerra.
  const militaresOrdenados = militares.slice().sort((a, b) => {
    const comparaPosto = b.posto.nivelHierarquico - a.posto.nivelHierarquico;
    if (comparaPosto !== 0) return comparaPosto;
    return a.nomeGuerra.localeCompare(b.nomeGuerra, "pt-BR");
  });

  return (
    <>
      <PageHeader title="Militares" subtitle={`${ativos} militares ativos`} />
      <div className="body">
        {podeEditar && (
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-primary" onClick={() => setMostrarForm((v) => !v)}>
              {mostrarForm ? "Cancelar" : "Novo militar"}
            </button>
          </div>
        )}

        {mostrarForm && podeEditar && (
          <NovoMilitarForm
            postos={postos}
            subunidades={subunidades}
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
                  <th>Nome de guerra</th>
                  <th>Posto</th>
                  <th>Subunidade</th>
                  <th>Cursos</th>
                  <th>Último serviço</th>
                  <th>Situação</th>
                  {podeEditar && <th></th>}
                </tr>
              </thead>
              <tbody>
                {militaresOrdenados.map((m) => (
                  <tr key={m.id}>
                    <td>
                      <Link to={`/militares/${m.id}`} style={{ color: "var(--sidebar-active)", fontWeight: 600, textDecoration: "underline" }}>
                        {m.nomeExibicao}
                      </Link>
                    </td>
                    <td>{m.posto.descricao}</td>
                    <td>{m.subunidade.sigla}</td>
                    <td>
                      {m.qualificacoes.length === 0 ? (
                        <span style={{ color: "var(--grey)", fontSize: 11 }}>—</span>
                      ) : (
                        m.qualificacoes.map((q) => (
                          <span key={q.id} className="pill pill-grey" style={{ marginRight: 4 }}>
                            {q.nome}
                          </span>
                        ))
                      )}
                    </td>
                    <td>{formatarContador(m.contadorRodizio)}</td>
                    <td>
                      <span
                        className={
                          "pill " +
                          (m.situacao === "ATIVO" ? "pill-green" : m.situacao === "AFASTADO" ? "pill-amber" : "pill-red")
                        }
                      >
                        {m.situacao}
                      </span>
                    </td>
                    {podeEditar && (
                      <td>
                        <button className="btn btn-outline" onClick={() => setGerenciandoId(gerenciandoId === m.id ? null : m.id)}>
                          Cursos
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {podeEditar && gerenciandoId !== null && (
          <GerenciarCursos
            militar={militares.find((m) => m.id === gerenciandoId)!}
            todasQuals={quals}
            onFechar={() => setGerenciandoId(null)}
            onMudou={carregar}
          />
        )}

        {!podeEditar && (
          <div className="card" style={{ background: "var(--amber-bg)", border: "none" }}>
            <p style={{ fontSize: 12, color: "var(--amber-text)" }}>
              Seu perfil só consulta o efetivo (RF04 / RN11). Cadastro é privativo do Cabo da
              Sargenteação e do Sargenteante.
            </p>
          </div>
        )}
      </div>
    </>
  );
}

function NovoMilitarForm({
  postos,
  subunidades,
  onCriado,
}: {
  postos: PostoGraduacao[];
  subunidades: Subunidade[];
  onCriado: () => void;
}) {
  const [nomeCompleto, setNomeCompleto] = useState("");
  const [nomeGuerra, setNomeGuerra] = useState("");
  const [cpf, setCpf] = useState("");
  const [numeroRegistro, setNumeroRegistro] = useState("");
  const [dataNascimento, setDataNascimento] = useState("");
  const [fusex, setFusex] = useState("");
  const [telefone, setTelefone] = useState("");
  const [fotoBase64, setFotoBase64] = useState<string | null>(null);
  const [postoId, setPostoId] = useState<number | "">("");
  const [subunidadeId, setSubunidadeId] = useState<number | "">("");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  function selecionarFoto(e: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = e.target.files?.[0];
    if (!arquivo) return;
    const leitor = new FileReader();
    leitor.onload = () => setFotoBase64(leitor.result as string);
    leitor.readAsDataURL(arquivo);
  }

  async function salvar() {
    if (!nomeCompleto || !nomeGuerra || !cpf || !postoId || !subunidadeId) {
      setErro("Preencha todos os campos obrigatórios.");
      return;
    }
    if (somenteDigitos(cpf).length !== 11) {
      setErro("O CPF precisa ter 11 números.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      await api.post("/api/militares", {
        nomeCompleto,
        nomeGuerra,
        cpf: somenteDigitos(cpf),
        numeroRegistro: numeroRegistro || null,
        dataNascimento: dataNascimento || null,
        fusex: fusex || null,
        telefone: telefone ? somenteDigitos(telefone) : null,
        fotoBase64,
        posto: { id: postoId },
        subunidade: { id: subunidadeId },
      });
      onCriado();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível cadastrar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="card">
      <h3>Novo militar</h3>
      <p className="sub">Dados da carteira de identidade militar — RF04</p>
      {erro && <div className="error-box">{erro}</div>}
      <div className="cadastro-foto-layout" style={{ display: "flex", gap: 20 }}>
        <div style={{ flexShrink: 0 }}>
          <div className="field" style={{ marginBottom: 6 }}>
            <label>Foto 3x4</label>
          </div>
          <div
            style={{
              width: 108, height: 130, borderRadius: 4, background: "var(--table-head-bg)",
              border: "1px solid var(--border)", overflow: "hidden", marginBottom: 8,
              display: "flex", alignItems: "center", justifyContent: "center",
            }}
          >
            {fotoBase64 ? (
              <img src={fotoBase64} alt="" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
            ) : (
              <span style={{ fontSize: 11, color: "var(--grey)", textAlign: "center", padding: 8 }}>Sem foto</span>
            )}
          </div>
          <input type="file" accept="image/*" onChange={selecionarFoto} style={{ fontSize: 11, width: 108 }} />
        </div>

        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="form-grid">
            <div className="field">
              <label>Nome completo</label>
              <input value={nomeCompleto} onChange={(e) => setNomeCompleto(e.target.value)} />
            </div>
            <div className="field">
              <label>Nome de guerra</label>
              <input value={nomeGuerra} onChange={(e) => setNomeGuerra(e.target.value)} />
            </div>
            <div className="field">
              <label>CPF</label>
              <input value={cpf} onChange={(e) => setCpf(mascararCpf(e.target.value))} placeholder="000.000.000-00" />
            </div>
            <div className="field">
              <label>NR Registro</label>
              <input value={numeroRegistro} onChange={(e) => setNumeroRegistro(e.target.value)} placeholder="Ex.: 1107544171" />
            </div>
            <div className="field">
              <label>Data de nascimento</label>
              <input type="date" value={dataNascimento} onChange={(e) => setDataNascimento(e.target.value)} />
            </div>
            <div className="field">
              <label>FUSEX</label>
              <input value={fusex} onChange={(e) => setFusex(mascararFusex(e.target.value))} placeholder="Ex.: XXX-XX" />
            </div>
            <div className="field">
              <label>Telefone</label>
              <input value={telefone} onChange={(e) => setTelefone(mascararTelefone(e.target.value))} placeholder="(00) 00000-0000" />
            </div>
            <div className="field">
              <label>Posto/graduação</label>
              <select value={postoId} onChange={(e) => setPostoId(Number(e.target.value))}>
                <option value="">Selecione</option>
                {postos.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.descricao}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Subunidade</label>
              <select value={subunidadeId} onChange={(e) => setSubunidadeId(Number(e.target.value))}>
                <option value="">Selecione</option>
                {subunidades.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.nome}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>
      <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar cadastro"}
      </button>
    </div>
  );
}

function formatarContador(dias: number): string {
  if (dias > 100000) return "nunca serviu";
  return `último serviço há ${Math.abs(dias)} dia${Math.abs(dias) === 1 ? "" : "s"}`;
}

function GerenciarCursos({
  militar,
  todasQuals,
  onFechar,
  onMudou,
}: {
  militar: Militar;
  todasQuals: Qualificacao[];
  onFechar: () => void;
  onMudou: () => void;
}) {
  const [processando, setProcessando] = useState<number | null>(null);
  const temQual = (qId: number) => militar.qualificacoes.some((q) => q.id === qId);

  async function alternar(q: Qualificacao) {
    setProcessando(q.id);
    try {
      if (temQual(q.id)) {
        await api.delete(`/api/militares/${militar.id}/qualificacoes/${q.id}`);
      } else {
        await api.post(`/api/militares/${militar.id}/qualificacoes/${q.id}`);
      }
      await onMudou();
    } finally {
      setProcessando(null);
    }
  }

  return (
    <div className="card">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
        <h3>Cursos de {militar.nomeExibicao}</h3>
        <button className="btn btn-outline" onClick={onFechar}>Fechar</button>
      </div>
      <p className="sub">Clique num curso pra marcar ou desmarcar</p>
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
        {todasQuals.map((q) => {
          const marcado = temQual(q.id);
          return (
            <button
              key={q.id}
              className={marcado ? "btn btn-primary" : "btn btn-outline"}
              disabled={processando === q.id}
              onClick={() => alternar(q)}
            >
              {q.nome}
            </button>
          );
        })}
        {todasQuals.length === 0 && <p className="sub">Nenhuma qualificação cadastrada ainda.</p>}
      </div>
    </div>
  );
}
