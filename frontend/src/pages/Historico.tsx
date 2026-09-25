import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { Afastamento, ServicoEscalado, Solicitacao } from "../api/types";
import { PageHeader } from "../components/Shell";
import { useAuth } from "../context/AuthContext";
import { TIPO_AFASTAMENTO_LABEL } from "../utils/afastamentoTipos";
import { formatarDataBR } from "../utils/formatadores";

/**
 * "Meu histórico" — aberto a todo mundo, cada um vê o próprio
 * histórico (nunca o de outra pessoa; isso é feito na Ficha do
 * Militar, privativa de Cabo/Sd EP/Sargenteante). Mesmos três blocos
 * da Ficha do Militar, só que sem os dados pessoais e sem edição.
 */
export function HistoricoPage() {
  const { usuario } = useAuth();
  const [servicos, setServicos] = useState<ServicoEscalado[]>([]);
  const [afastamentos, setAfastamentos] = useState<Afastamento[]>([]);
  const [trocas, setTrocas] = useState<Solicitacao[]>([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    if (!usuario) return;
    const id = usuario.militarId;
    Promise.all([
      api.get<ServicoEscalado[]>(`/api/militares/${id}/historico-servicos`),
      api.get<Afastamento[]>(`/api/militares/${id}/historico-afastamentos`),
      api.get<Solicitacao[]>(`/api/militares/${id}/historico-trocas`),
    ]).then(([s, a, t]) => {
      setServicos(s);
      setAfastamentos(a);
      setTrocas(t);
      setCarregando(false);
    });
  }, [usuario]);

  if (carregando) {
    return (
      <>
        <PageHeader title="Meu histórico" subtitle="Carregando…" />
        <div className="body" />
      </>
    );
  }

  return (
    <>
      <PageHeader title="Meu histórico" subtitle={`Serviços, missões/dispensas e trocas de ${usuario?.nomeExibicao}`} />
      <div className="body">
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
                    <td>{TIPO_AFASTAMENTO_LABEL[a.tipo]}</td>
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
                    <td>{t.solicitante.id === usuario?.militarId ? "Pediu" : "Recebeu o pedido"}</td>
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
