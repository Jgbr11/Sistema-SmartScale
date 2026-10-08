import { useState } from "react";
import { api, ApiError } from "../api/client";
import type { PerfilAcesso, UsuarioAdmin } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { useAuth } from "../context/AuthContext";
import { PERFIL_LABEL } from "../utils/perfis";
import { formatarCpf } from "../utils/formatadores";
import { useAoMudar } from "../hooks/useAoMudar";
import { useFeedback } from "../components/ui/Feedback";
import { Esqueleto } from "../components/ui/Esqueleto";

export function PerfisPermissoesPage() {
  const { confirmar } = useFeedback();
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

  useAoMudar(carregar);

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
    if (!(await confirmar(ativo ? "Reativar o acesso dessa pessoa?" : "Desativar o acesso dessa pessoa? Ela não vai mais conseguir logar."))) return;
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
    if (!(await confirmar(`Gerar uma senha temporária para ${nome}? A senha atual deixa de funcionar.`))) return;
    setErro(null);
    setProcessando(usuarioId);
    try {
      const r = await api.post<{ senhaTemporaria: string }>(`/api/usuarios/${usuarioId}/resetar-senha`, {});
      await confirmar(`Senha temporária de ${nome}: ${r.senhaTemporaria}

Ela aparece só agora. Entregue pessoalmente — no primeiro acesso a pessoa vai ser obrigada a criar uma senha nova.`);
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
      <PageHeader title="Perfis e permissões" subtitle="Quem tem acesso ao quê no sistema" />
      <div className="body">
        {erro && <div className="error-box">{erro}</div>}
        <div className="field max-320">
          <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar por nome ou CPF…" />
        </div>

        <div className="card card-tabela">
          {carregando ? (
            <Esqueleto />
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
                      <td className="texto-suave">{formatarCpf(u.login)}</td>
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
                      <td className="nowrap">
                        <button
                          className="btn btn-outline mr-6"

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
        <p className="nota-pequena">
          Você não pode alterar o próprio perfil nem desativar o próprio acesso — peça pra outro Sargenteante, se houver.
        </p>
      </div>
    </>
  );
}
