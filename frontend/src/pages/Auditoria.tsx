import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { LogAuditoria } from "../api/types";
import { PageHeader } from "../components/Shell";

const ACAO_LABEL: Record<string, string> = {
  MILITAR_CADASTRADO: "Militar cadastrado",
  MILITAR_EDITADO: "Militar editado",
  MILITAR_DESLIGADO: "Militar desligado",
  ESCALA_GERADA: "Escala gerada",
  ESCALA_PUBLICADA: "Escala publicada",
  DIA_TRAVADO: "Dia travado",
  DIA_DESTRAVADO: "Dia destravado",
  AFASTAMENTO_CADASTRADO: "Afastamento cadastrado",
  AFASTAMENTO_CANCELADO: "Afastamento cancelado",
  TROCA_PEDIDA: "Troca pedida",
  TROCA_SUBSTITUTO_ACEITOU: "Substituto aceitou troca",
  TROCA_SUBSTITUTO_RECUSOU: "Substituto recusou troca",
  TROCA_TRIAGEM_APROVADA: "Troca aprovada na triagem",
  TROCA_TRIAGEM_NEGADA: "Troca negada na triagem",
  TROCA_AUTORIZADA: "Troca autorizada",
  TROCA_NEGADA: "Troca negada",
  TROCA_CANCELADA: "Troca cancelada",
  FERIADO_CADASTRADO: "Feriado cadastrado",
  FERIADO_REMOVIDO: "Feriado removido",
  PERFIL_ALTERADO: "Perfil de acesso alterado",
  USUARIO_DESATIVADO: "Usuário desativado",
  USUARIO_REATIVADO: "Usuário reativado",
  SENHA_RESETADA: "Senha resetada (por outro usuário)",
  SENHA_TROCADA_PELO_PROPRIO: "Senha trocada pelo próprio usuário",
  BOLETIM_PUBLICADO: "Boletim publicado",
  BOLETIM_EDITADO: "Boletim editado",
  BOLETIM_REMOVIDO: "Boletim removido",
};

export function AuditoriaPage() {
  const [logs, setLogs] = useState<LogAuditoria[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");

  useEffect(() => {
    api.get<LogAuditoria[]>("/api/auditoria?limite=300").then((d) => {
      setLogs(d);
      setCarregando(false);
    });
  }, []);

  const filtrados = logs.filter((l) => {
    const termo = busca.toLowerCase();
    return (
      (l.usuarioNomeExibicao ?? "").toLowerCase().includes(termo) ||
      (ACAO_LABEL[l.acao] ?? l.acao).toLowerCase().includes(termo) ||
      (l.descricao ?? "").toLowerCase().includes(termo)
    );
  });

  return (
    <>
      <PageHeader title="Log de auditoria" subtitle="Quem fez o quê no sistema — só o Sargenteante vê" />
      <div className="body">
        <div className="field" style={{ maxWidth: 320 }}>
          <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar por pessoa, ação ou detalhe…" />
        </div>
        <div className="card" style={{ padding: 0 }}>
          {carregando ? (
            <div style={{ padding: 20 }}>Carregando…</div>
          ) : filtrados.length === 0 ? (
            <div style={{ padding: 20, color: "var(--grey)", fontSize: 13 }}>Nenhum registro ainda.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Data/hora</th>
                  <th>Quem</th>
                  <th>Ação</th>
                  <th>Detalhe</th>
                </tr>
              </thead>
              <tbody>
                {filtrados.map((l) => (
                  <tr key={l.id}>
                    <td style={{ whiteSpace: "nowrap", fontSize: 12 }}>{formatarDataHora(l.dataHora)}</td>
                    <td>{l.usuarioNomeExibicao ?? "sistema"}</td>
                    <td><span className="pill pill-grey">{ACAO_LABEL[l.acao] ?? l.acao}</span></td>
                    <td style={{ fontSize: 12 }}>{l.descricao ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        {!carregando && logs.length >= 300 && (
          <p className="sub">Mostrando os 300 registros mais recentes.</p>
        )}
      </div>
    </>
  );
}

function formatarDataHora(iso: string) {
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
}
