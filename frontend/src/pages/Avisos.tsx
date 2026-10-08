import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Aviso, BoletimResumo } from "../api/types";
import { PageHeader } from "../components/layout/PageHeader";
import { capitalizar, formatarDataBR, formatarPeriodo } from "../utils/formatadores";
import { CalendarioMensal } from "../components/ui/CalendarioMensal";
import { Esqueleto } from "../components/ui/Esqueleto";

const TIPO_LABEL: Record<string, string> = {
  FERIADO: "Feriado",
  MISSAO: "Missão",
  DISPENSA: "Dispensa",
  FERIAS: "Férias",
  LICENCA: "Licença",
  CURSO: "Curso",
  OUTRO: "Outro",
};

export function AvisosPage() {
  const hoje = new Date();
  const navigate = useNavigate();
  const [mesExibido, setMesExibido] = useState({ ano: hoje.getFullYear(), mes: hoje.getMonth() });
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const [boletins, setBoletins] = useState<BoletimResumo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [diaEscolhido, setDiaEscolhido] = useState<string | null>(null);

  async function carregar(ano: number, mes: number) {
    setCarregando(true);
    const mesStr = `${ano}-${String(mes + 1).padStart(2, "0")}`;
    const [resultado, listaBoletins] = await Promise.all([
      api.get<Aviso[]>(`/api/avisos?mes=${mesStr}`),
      api.get<BoletimResumo[]>("/api/boletins"),
    ]);
    setAvisos(resultado);
    setBoletins(listaBoletins);
    setCarregando(false);
  }

  function boletimRelacionado(chave: string) {
    return boletins.find((b) => b.avisoRelacionado === chave);
  }

  useEffect(() => {
    carregar(mesExibido.ano, mesExibido.mes);
    setDiaEscolhido(null);
  }, [mesExibido]);

  function mudarMes(delta: number) {
    let novoMes = mesExibido.mes + delta;
    let novoAno = mesExibido.ano;
    if (novoMes < 0) { novoMes = 11; novoAno -= 1; }
    if (novoMes > 11) { novoMes = 0; novoAno += 1; }
    setMesExibido({ ano: novoAno, mes: novoMes });
  }

  function avisosDoDia(dataStr: string) {
    return avisos.filter((a) => dataStr >= a.dataInicio && dataStr <= a.dataFim);
  }

  function clicarDia(dataStr: string) {
    setDiaEscolhido((atual) => (atual === dataStr ? null : dataStr));
  }

  const primeiroDia = new Date(mesExibido.ano, mesExibido.mes, 1);
  const nomeMes = capitalizar(primeiroDia.toLocaleDateString("pt-BR", { month: "long", year: "numeric" }));

  const avisosDoDiaEscolhido = diaEscolhido ? avisosDoDia(diaEscolhido) : [];

  return (
    <>
      <PageHeader title="Avisos" subtitle="Feriados e missões do mês — clique num dia pra ver o detalhe" />
      <div className="body">
        <div className="card">
          <div className="linha-entre mb-16">
            <h3>{nomeMes}</h3>
            <div className="linha">
              <button className="btn btn-outline" onClick={() => mudarMes(-1)}>← Mês anterior</button>
              <button className="btn btn-outline" onClick={() => mudarMes(1)}>Próximo mês →</button>
            </div>
          </div>

          {carregando ? (
            <Esqueleto />
          ) : (
            <CalendarioMensal
              ano={mesExibido.ano}
              mes={mesExibido.mes}
              diaSelecionado={diaEscolhido}
              onSelecionar={clicarDia}
              infoDoDia={(dataStr) => {
                const doDia = avisosDoDia(dataStr);
                if (doDia.length === 0) return {};
                return {
                  rotulo: doDia.length === 1 ? TIPO_LABEL[doDia[0].tipo] ?? doDia[0].tipo : `${doDia.length} avisos`,
                  destaque: doDia.some((a) => a.tipo === "FERIADO") ? "positivo" : "atencao",
                };
              }}
            />
          )}
        </div>

        {diaEscolhido && (
          <div className="card">
            <h3 className="mb-12">Avisos de {formatarDataBR(diaEscolhido)}</h3>
            {avisosDoDiaEscolhido.length === 0 ? (
              <p className="sub">Nada registrado para esse dia.</p>
            ) : (
              avisosDoDiaEscolhido.map((a, i) => <AvisoDetalhe key={i} aviso={a} boletim={boletimRelacionado(a.chave)} onVerBoletim={() => navigate("/boletim")} />)
            )}
          </div>
        )}

        <div className="card">
          <h3 className="mb-12">Todos os avisos do mês</h3>
          {avisos.length === 0 ? (
            <p className="sub">Nenhum feriado ou missão neste mês.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Período</th>
                  <th>Tipo</th>
                  <th>Observação</th>
                  <th>Militares</th>
                </tr>
              </thead>
              <tbody>
                {avisos.map((a, i) => (
                  <tr key={i}>
                    <td>{formatarPeriodo(a.dataInicio, a.dataFim)}</td>
                    <td>
                      <span className={"pill " + (a.tipo === "FERIADO" ? "pill-green" : "pill-amber")}>
                        {TIPO_LABEL[a.tipo] ?? a.tipo}
                      </span>
                    </td>
                    <td>
                      {a.descricao}
                      {boletimRelacionado(a.chave) && (
                        <button onClick={() => navigate("/boletim")} className="link-pequeno ml-8">
                          Ver Boletim
                        </button>
                      )}
                    </td>
                    <td className="texto-12">
                      {a.militares.length > 0 ? a.militares.map((m) => m.nomeExibicao).join(", ") : "—"}
                    </td>
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

function AvisoDetalhe({ aviso, boletim, onVerBoletim }: { aviso: Aviso; boletim?: BoletimResumo; onVerBoletim: () => void }) {
  return (
    <div className={"card mb-10 " + (aviso.tipo === "FERIADO" ? "card-positivo" : "card-atencao")}>
      <strong className="texto-13">{TIPO_LABEL[aviso.tipo] ?? aviso.tipo} — {aviso.descricao}</strong>
      <p className="texto-12 mt-4">
        Período: {formatarPeriodo(aviso.dataInicio, aviso.dataFim)}
      </p>
      {aviso.militares.length > 0 && (
        <p className="texto-12 mt-4">
          Militares: {aviso.militares.map((m) => m.nomeExibicao).join(", ")}
        </p>
      )}
      {boletim && (
        <button onClick={onVerBoletim} className="btn btn-outline nota mt-8">
          Ver Boletim relacionado — {boletim.titulo}
        </button>
      )}
    </div>
  );
}
