import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api, ApiError } from "../api/client";
import type { Afastamento, Militar, PostoGraduacao, ServicoEscalado, Solicitacao, Subunidade, TipoServico } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";
import { mascararCpf, mascararFusex, mascararTelefone, somenteDigitos } from "../utils/mascaras";
import { TIPO_AFASTAMENTO_LABEL } from "../utils/afastamentoTipos";

export function FichaMilitarPage() {
  const { id } = useParams<{ id: string }>();
  const militarId = Number(id);
  const { usuario } = useAuth();
  const podeEditar = usuario?.perfil === "CABO_SARGENTEACAO" || usuario?.perfil === "SARGENTEANTE";

  const [militar, setMilitar] = useState<Militar | null>(null);
  const [funcoes, setFuncoes] = useState<TipoServico[]>([]);
  const [servicos, setServicos] = useState<ServicoEscalado[]>([]);
  const [afastamentos, setAfastamentos] = useState<Afastamento[]>([]);
  const [trocas, setTrocas] = useState<Solicitacao[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [editando, setEditando] = useState(false);

  async function carregar() {
    setCarregando(true);
    const [m, f, s, a, t] = await Promise.all([
      api.get<Militar>(`/api/militares/${militarId}`),
      api.get<TipoServico[]>(`/api/militares/${militarId}/funcoes-elegiveis`),
      api.get<ServicoEscalado[]>(`/api/militares/${militarId}/historico-servicos`),
      api.get<Afastamento[]>(`/api/militares/${militarId}/historico-afastamentos`),
      api.get<Solicitacao[]>(`/api/militares/${militarId}/historico-trocas`),
    ]);
    setMilitar(m);
    setFuncoes(f);
    setServicos(s);
    setAfastamentos(a);
    setTrocas(t);
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [militarId]);

  if (carregando || !militar) {
    return (
      <>
        <PageHeader title="Ficha do militar" subtitle="Carregando…" />
        <div className="body" />
      </>
    );
  }

  const hoje = new Date().toISOString().slice(0, 10);
  const afastamentoAtual = afastamentos.find((a) => hoje >= a.dataInicio && hoje <= a.dataFim);

  return (
    <>
      <PageHeader title={militar.nomeExibicao} subtitle={militar.nomeCompleto} />
      <div className="body">
        <Link to="/militares" style={{ fontSize: 12, color: "var(--sidebar-active)" }}>← Voltar pra lista de militares</Link>

        {afastamentoAtual && (
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span className="pill pill-amber">{TIPO_AFASTAMENTO_LABEL[afastamentoAtual.tipo]}</span>
            <span style={{ fontSize: 12, color: "var(--grey)" }}>
              até {formatarDataBR(afastamentoAtual.dataFim)} — ver detalhe em Missões e Dispensas
            </span>
          </div>
        )}

        <div className="card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
            <h3>Dados pessoais</h3>
            {podeEditar && !editando && (
              <button className="btn btn-outline" onClick={() => setEditando(true)}>Editar</button>
            )}
          </div>
          {editando ? (
            <EditarForm militar={militar} onSalvou={() => { setEditando(false); carregar(); }} onCancelar={() => setEditando(false)} />
          ) : (
            <VisaoDados militar={militar} />
          )}
        </div>

        <div className="card">
          <h3>Cursos</h3>
          <p className="sub">Pra vincular ou remover um curso, use o botão "Cursos" na tela de Militares</p>
          <div style={{ marginTop: 8 }}>
            {militar.qualificacoes.length === 0 ? (
              <span style={{ fontSize: 12, color: "var(--grey)" }}>Nenhum curso registrado</span>
            ) : (
              militar.qualificacoes.map((q) => <span key={q.id} className="pill pill-grey" style={{ marginRight: 6 }}>{q.nome}</span>)
            )}
          </div>
        </div>

        <div className="card">
          <h3>Pode servir em</h3>
          <div style={{ marginTop: 8 }}>
            {funcoes.length === 0 ? (
              <span style={{ fontSize: 12, color: "var(--grey)" }}>Nenhuma função elegível no momento</span>
            ) : (
              funcoes.map((f) => <span key={f.id} className="pill pill-green" style={{ marginRight: 6, marginBottom: 6, display: "inline-block" }}>{f.nome}</span>)
            )}
          </div>
        </div>

        <div className="card" style={{ padding: 0 }}>
          <h3 style={{ padding: "16px 16px 0" }}>Histórico de serviços ({servicos.length})</h3>
          {servicos.length === 0 ? (
            <div style={{ padding: 16, color: "var(--grey)", fontSize: 13 }}>Nenhum serviço registrado ainda.</div>
          ) : (
            <table>
              <thead><tr><th>Data</th><th>Serviço</th><th>Situação</th></tr></thead>
              <tbody>
                {servicos.slice(0, 50).map((s) => (
                  <tr key={s.id}>
                    <td>{formatarDataBR(s.data)}</td>
                    <td>{s.tipoServico.nome}</td>
                    <td><span className="pill pill-grey">{s.situacao}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {servicos.length > 50 && <p className="sub" style={{ padding: 12 }}>Mostrando os 50 mais recentes de {servicos.length}.</p>}
        </div>

        <div className="card" style={{ padding: 0 }}>
          <h3 style={{ padding: "16px 16px 0" }}>Histórico de missões e dispensas ({afastamentos.length})</h3>
          {afastamentos.length === 0 ? (
            <div style={{ padding: 16, color: "var(--grey)", fontSize: 13 }}>Nenhum afastamento registrado ainda.</div>
          ) : (
            <table>
              <thead><tr><th>Período</th><th>Tipo</th><th>Descrição</th></tr></thead>
              <tbody>
                {afastamentos.map((a) => (
                  <tr key={a.id}>
                    <td>{formatarDataBR(a.dataInicio)} a {formatarDataBR(a.dataFim)}</td>
                    <td>{a.tipo}</td>
                    <td>{a.descricao}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card" style={{ padding: 0 }}>
          <h3 style={{ padding: "16px 16px 0" }}>Histórico de trocas ({trocas.length})</h3>
          {trocas.length === 0 ? (
            <div style={{ padding: 16, color: "var(--grey)", fontSize: 13 }}>Nenhuma troca pedida ou recebida ainda.</div>
          ) : (
            <table>
              <thead><tr><th>Data</th><th>Papel</th><th>Serviço</th><th>Situação</th></tr></thead>
              <tbody>
                {trocas.map((t) => (
                  <tr key={t.id}>
                    <td>{formatarDataBR(t.dataSolicitacao.slice(0, 10))}</td>
                    <td>{t.solicitante.id === militarId ? "Pediu" : "Recebeu o pedido"}</td>
                    <td>{t.servicoOrigem.tipoServico.nome} — {formatarDataBR(t.servicoOrigem.data)}</td>
                    <td><span className="pill pill-grey">{t.situacao}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}

function VisaoDados({ militar }: { militar: Militar }) {
  return (
    <div className="form-grid" style={{ marginTop: 10 }}>
      <CampoLeitura label="Nome completo" valor={militar.nomeCompleto} />
      <CampoLeitura label="Nome de guerra" valor={militar.nomeGuerra} />
      <CampoLeitura label="CPF" valor={formatarCpf(militar.cpf)} />
      <CampoLeitura label="Posto/graduação" valor={militar.posto.descricao} />
      <CampoLeitura label="Subunidade" valor={militar.subunidade.nome} />
      <CampoLeitura label="Situação" valor={militar.situacao} />
      <CampoLeitura label="NR Registro" valor={militar.numeroRegistro || "—"} />
      <CampoLeitura label="Data de nascimento" valor={militar.dataNascimento ? formatarDataBR(militar.dataNascimento) : "—"} />
      <CampoLeitura label="FUSEX" valor={militar.fusex || "—"} />
      <CampoLeitura label="Telefone" valor={militar.telefone ? mascararTelefone(militar.telefone) : "—"} />
      <CampoLeitura label="Email" valor={militar.email || "—"} />
    </div>
  );
}

function CampoLeitura({ label, valor }: { label: string; valor: string }) {
  return (
    <div className="field">
      <label>{label}</label>
      <div style={{ padding: "7px 0", fontSize: 13, fontWeight: 600 }}>{valor}</div>
    </div>
  );
}

function EditarForm({ militar, onSalvou, onCancelar }: { militar: Militar; onSalvou: () => void; onCancelar: () => void }) {
  const [dados, setDados] = useState({
    nomeCompleto: militar.nomeCompleto,
    nomeGuerra: militar.nomeGuerra,
    cpf: mascararCpf(militar.cpf),
    postoId: militar.posto.id,
    subunidadeId: militar.subunidade.id,
    numeroRegistro: militar.numeroRegistro || "",
    dataNascimento: militar.dataNascimento || "",
    fusex: militar.fusex || "",
    telefone: militar.telefone ? mascararTelefone(militar.telefone) : "",
    email: militar.email || "",
  });
  const [postos, setPostos] = useState<PostoGraduacao[]>([]);
  const [subunidades, setSubunidades] = useState<Subunidade[]>([]);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      api.get<PostoGraduacao[]>("/api/postos-graduacao"),
      api.get<Subunidade[]>("/api/subunidades"),
    ]).then(([p, s]) => { setPostos(p); setSubunidades(s); });
  }, []);

  function campo<K extends keyof typeof dados>(chave: K, valor: (typeof dados)[K]) {
    setDados((atual) => ({ ...atual, [chave]: valor }));
  }

  async function salvar() {
    setErro(null);
    if (somenteDigitos(dados.cpf).length !== 11) {
      setErro("O CPF precisa ter 11 números.");
      return;
    }
    const mudouIdentidade =
      dados.nomeCompleto !== militar.nomeCompleto ||
      dados.nomeGuerra !== militar.nomeGuerra ||
      somenteDigitos(dados.cpf) !== militar.cpf;
    if (mudouIdentidade) {
      const ok = confirm(
        "Você está mudando nome completo, nome de guerra ou CPF — isso deveria ser raro, só pra corrigir um erro de cadastro. Confirma a alteração?"
      );
      if (!ok) return;
    }
    setSalvando(true);
    try {
      await api.put(`/api/militares/${militar.id}`, {
        nomeCompleto: dados.nomeCompleto,
        nomeGuerra: dados.nomeGuerra,
        cpf: somenteDigitos(dados.cpf),
        posto: { id: dados.postoId },
        subunidade: { id: dados.subunidadeId },
        numeroRegistro: dados.numeroRegistro || null,
        dataNascimento: dados.dataNascimento || null,
        fusex: dados.fusex || null,
        telefone: dados.telefone ? somenteDigitos(dados.telefone) : null,
        email: dados.email || null,
      });
      onSalvou();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível salvar.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div style={{ marginTop: 10 }}>
      {erro && <div className="error-box">{erro}</div>}
      <p className="sub" style={{ marginBottom: 10 }}>
        Nome completo, nome de guerra e CPF pedem confirmação extra ao salvar — só devem mudar pra corrigir um erro de cadastro.
      </p>
      <div className="form-grid">
        <div className="field">
          <label>Nome completo</label>
          <input value={dados.nomeCompleto} onChange={(e) => campo("nomeCompleto", e.target.value)} />
        </div>
        <div className="field">
          <label>Nome de guerra</label>
          <input value={dados.nomeGuerra} onChange={(e) => campo("nomeGuerra", e.target.value)} />
        </div>
        <div className="field">
          <label>CPF</label>
          <input value={dados.cpf} onChange={(e) => campo("cpf", mascararCpf(e.target.value))} />
        </div>
        <div className="field">
          <label>Posto/graduação</label>
          <select value={dados.postoId} onChange={(e) => campo("postoId", Number(e.target.value))}>
            {postos.map((p) => <option key={p.id} value={p.id}>{p.descricao}</option>)}
          </select>
        </div>
        <div className="field">
          <label>Subunidade</label>
          <select value={dados.subunidadeId} onChange={(e) => campo("subunidadeId", Number(e.target.value))}>
            {subunidades.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
          </select>
        </div>
        <div className="field">
          <label>NR Registro</label>
          <input value={dados.numeroRegistro} onChange={(e) => campo("numeroRegistro", e.target.value)} />
        </div>
        <div className="field">
          <label>Data de nascimento</label>
          <input type="date" value={dados.dataNascimento} onChange={(e) => campo("dataNascimento", e.target.value)} />
        </div>
        <div className="field">
          <label>FUSEX</label>
          <input value={dados.fusex} onChange={(e) => campo("fusex", mascararFusex(e.target.value))} />
        </div>
        <div className="field">
          <label>Telefone</label>
          <input value={dados.telefone} onChange={(e) => campo("telefone", mascararTelefone(e.target.value))} placeholder="(00) 00000-0000" />
        </div>
        <div className="field">
          <label>Email</label>
          <input value={dados.email} onChange={(e) => campo("email", e.target.value)} />
        </div>
      </div>
      <div style={{ display: "flex", gap: 8 }}>
        <button className="btn btn-primary" onClick={salvar} disabled={salvando}>
          {salvando ? "Salvando…" : "Salvar alterações"}
        </button>
        <button className="btn btn-outline" onClick={onCancelar} disabled={salvando}>Cancelar</button>
      </div>
    </div>
  );
}

function formatarCpf(cpf: string) {
  if (cpf.length !== 11) return cpf;
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`;
}
function formatarDataBR(iso: string) {
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}
