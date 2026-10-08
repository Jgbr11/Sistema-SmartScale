import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { LogAuditoria } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { formatarDataHora } from "../utils/formatadores";
import { Esqueleto } from "../components/ui/Esqueleto";

const ACAO_LABEL: Record<string, string> = {
  MILITAR_CADASTRADO: "Militar cadastrado",
  MILITAR_EDITADO: "Militar editado",
  MILITAR_DESLIGADO: "Militar desligado",
  ESCALA_GERADA: "Escala gerada",
  ESCALA_PUBLICADA: "Escala publicada",
  REGRA_ESCALA_ALTERADA: "Regra da escala alterada",
  REQUISITO_ADICIONADO: "Requisito de serviço adicionado",
  REQUISITO_REMOVIDO: "Requisito de serviço removido",
  TIPO_SERVICO_CADASTRADO: "Tipo de serviço cadastrado",
  TIPO_SERVICO_EDITADO: "Tipo de serviço editado",
  TIPO_SERVICO_DESATIVADO: "Tipo de serviço desativado",
  QUALIFICACAO_CADASTRADA: "Curso cadastrado",
  QUALIFICACAO_EDITADA: "Curso editado",
  QUALIFICACAO_EXCLUIDA: "Curso excluído",
  CURSO_VINCULADO: "Curso vinculado a militar",
  CURSO_DESVINCULADO: "Curso desvinculado de militar",
  POSTO_CADASTRADO: "Posto cadastrado",
  POSTO_EDITADO: "Posto editado",
  POSTO_EXCLUIDO: "Posto excluído",
  SUBUNIDADE_CADASTRADA: "Subunidade cadastrada",
  SUBUNIDADE_EDITADA: "Subunidade editada",
  SUBUNIDADE_EXCLUIDA: "Subunidade excluída",
  SUBUNIDADE_DESATIVADA: "Subunidade desativada",
  FERIADO_EDITADO: "Feriado editado",
  AFASTAMENTO_EDITADO: "Afastamento editado",
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
      <PageHeader title="Auditoria" subtitle="Quem fez o quê no sistema — só o Sargenteante vê" />
      <div className="body">
        <div className="field max-320">
          <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar por pessoa, ação ou detalhe…" />
        </div>
        <div className="card card-tabela">
          {carregando ? (
            <Esqueleto />
          ) : filtrados.length === 0 ? (
            <div className="vazio">Nenhum registro ainda.</div>
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
                    <td className="nowrap texto-12">{formatarDataHora(l.dataHora)}</td>
                    <td>{l.usuarioNomeExibicao ?? "sistema"}</td>
                    <td><span className="pill pill-grey">{ACAO_LABEL[l.acao] ?? l.acao}</span></td>
                    <td className="texto-12">{l.descricao ?? "—"}</td>
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
