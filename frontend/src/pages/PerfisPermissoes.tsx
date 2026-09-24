import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import type { PerfilAcesso, UsuarioAdmin } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";
import { PERFIL_LABEL } from "../utils/perfis";

export function PerfisPermissoesPage() {
  const { usuario: euMesmo } = useAuth();
  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([]);
  const [perfis, setPerfis] = useState<PerfilAcesso[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [processando, setProcessando] = useState<number | null>(null);

  async function carregar() {
    setCarregando(true);
    const [u, p] = await Promise.all([
      api.get<UsuarioAdmin[]>("/api/usuarios"),
      api.get<PerfilAcesso[]>("/api/usuarios/perfis"),
    ]);
    u.sort((a, b) => a.militar.nomeExibicao.localeCompare(b.militar.nomeExibicao, "pt-BR"));
    setUsuarios(u);
    setPerfis(p);
    setCarregando(false);
  }

  useEffect(() => {
    carregar();
  }, []);

  async function alterarPerfil(usuarioId: number, perfilId: number) {
    setErro(null);
    setProcessando(usuarioId);
    try {
      await api.put(`/api/usuarios/${usuarioId}/perfil`, { perfilId });
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível alterar o perfil.");
    } finally {
      setProcessando(null);
    }
  }

  async function alternarAtivo(usuarioId: number, ativo: boolean) {
    if (!confirm(ativo ? "Reativar o acesso dessa pessoa?" : "Desativar o acesso dessa pessoa? Ela não vai mais conseguir logar.")) return;
    setErro(null);
    setProcessando(usuarioId);
    try {
      await api.put(`/api/usuarios/${usuarioId}/ativo`, { ativo });
      carregar();
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível alterar.");
    } finally {
      setProcessando(null);
    }
  }

  async function resetarSenha(usuarioId: number, nome: string) {
    if (!confirm(`Resetar a senha de ${nome} pro padrão (milscale123)?`)) return;
    setErro(null);
    setProcessando(usuarioId);
    try {
      await api.post(`/api/usuarios/${usuarioId}/resetar-senha`, {});
      alert("Senha resetada para milscale123 — avise a pessoa pra trocar assim que entrar.");
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível resetar a senha.");
    } finally {
      setProcessando(null);
    }
  }

  const filtrados = usuarios.filter((u) =>
    u.militar.nomeExibicao.toLowerCase().includes(busca.toLowerCase()) || u.login.toLowerCase().includes(busca.toLowerCase())
  );

  return (
    <>
      <PageHeader title="Perfis e permissões" subtitle="Quem tem acesso ao quê no sistema — RF25" />
      <div className="body">
        {erro && <div className="error-box">{erro}</div>}
        <div className="field" style={{ maxWidth: 320 }}>
          <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar por nome ou CPF…" />
        </div>

        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF (login)</th>
                  <th>Perfil de acesso</th>
                  <th>Situação</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {filtrados.map((u) => {
                  const souEu = euMesmo?.id === u.id;
                  return (
                    <tr key={u.id}>
                      <td>{u.militar.nomeExibicao}</td>
                      <td style={{ color: "var(--grey)" }}>{formatarCpf(u.login)}</td>
                      <td>
                        <select
                          value={u.perfil.id}
                          disabled={souEu || processando === u.id}
                          onChange={(e) => alterarPerfil(u.id, Number(e.target.value))}
                        >
                          {perfis.map((p) => (
                            <option key={p.id} value={p.id}>{PERFIL_LABEL[p.nome] ?? p.nome}</option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <span className={"pill " + (u.ativo ? "pill-green" : "pill-red")}>
                          {u.ativo ? "Ativo" : "Inativo"}
                        </span>
                      </td>
                      <td style={{ whiteSpace: "nowrap" }}>
                        <button
                          className="btn btn-outline"
                          style={{ marginRight: 6 }}
                          disabled={processando === u.id}
                          onClick={() => resetarSenha(u.id, u.militar.nomeExibicao)}
                        >
                          Resetar senha
                        </button>
                        <button
                          className="btn btn-outline"
                          disabled={souEu || processando === u.id}
                          onClick={() => alternarAtivo(u.id, !u.ativo)}
                        >
                          {u.ativo ? "Desativar" : "Reativar"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
        <p style={{ fontSize: 11, color: "var(--grey)" }}>
          Você não pode alterar o próprio perfil nem desativar o próprio acesso — peça pra outro Sargenteante, se houver.
        </p>
      </div>
    </>
  );
}

function formatarCpf(cpf: string) {
  if (cpf.length !== 11) return cpf;
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`;
}
