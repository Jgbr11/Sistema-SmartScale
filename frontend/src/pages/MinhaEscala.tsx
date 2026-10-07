import { useState } from "react";
import { api } from "../api/client";
import type { ServicoEscalado } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { capitalizar } from "../utils/formatadores";
import { useAoMudar } from "../hooks/useAoMudar";
import { CalendarioMensal } from "../components/ui/CalendarioMensal";
import { progressoDoDia } from "../utils/servico";

export function MinhaEscalaPage() {
  const hoje = new Date();
  const [ano, setAno] = useState(hoje.getFullYear());
  const [mes, setMes] = useState(hoje.getMonth());
  const [servicos, setServicos] = useState<ServicoEscalado[]>([]);
  const [carregando, setCarregando] = useState(true);

  async function carregar() {
    setCarregando(true);
    const mesStr = `${ano}-${String(mes + 1).padStart(2, "0")}`;
    const dados = await api.get<ServicoEscalado[]>(`/api/minha-escala?mes=${mesStr}`);
    setServicos(dados);
    setCarregando(false);
  }

  useAoMudar(carregar, `${ano}-${mes}`);

  function mudarMes(delta: number) {
    let novoMes = mes + delta;
    let novoAno = ano;
    if (novoMes < 0) { novoMes = 11; novoAno -= 1; }
    if (novoMes > 11) { novoMes = 0; novoAno += 1; }
    setMes(novoMes);
    setAno(novoAno);
  }

  const primeiroDia = new Date(ano, mes, 1);

  const servicoPorDia = new Map<number, ServicoEscalado>();
  for (const s of servicos) {
    const dia = Number(s.data.slice(8, 10));
    servicoPorDia.set(dia, s);
  }

  const nomeMes = primeiroDia.toLocaleDateString("pt-BR", { month: "long", year: "numeric" });

  return (
    <>
      <PageHeader title="Minha escala" subtitle={capitalizar(nomeMes)} />
      <div className="body">
        <div className="card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
            <div>
              <h3>
                {carregando
                  ? "Carregando…"
                  : servicos.length === 0
                  ? "Você não tem serviços neste mês"
                  : `Você tem ${servicos.length} serviço(s) neste mês`}
              </h3>
              <p className="sub">Nos outros dias você cumpre o expediente normal.</p>
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <button className="btn btn-outline" onClick={() => mudarMes(-1)}>
                ← Mês anterior
              </button>
              <button className="btn btn-outline" onClick={() => mudarMes(1)}>
                Próximo mês →
              </button>
            </div>
          </div>

          <CalendarioMensal
            ano={ano}
            mes={mes}
            infoDoDia={(dataStr) => {
              const servico = servicoPorDia.get(Number(dataStr.slice(8)));
              return servico ? { rotulo: servico.tipoServico.nome, destaque: "servico", progresso: progressoDoDia(dataStr) } : {};
            }}
          />
        </div>
      </div>
    </>
  );
}
