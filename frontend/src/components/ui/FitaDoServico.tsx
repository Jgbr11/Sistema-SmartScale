import { useEffect, useState } from "react";
import { formatarDataBR } from "../../utils/formatadores";
import { servicoEmCurso } from "../../utils/servico";

const MARCAS = [0, 4, 8, 12, 16, 20, 24];

export function FitaDoServico({ horaInicio = 8 }: { horaInicio?: number }) {
  const [agora, setAgora] = useState(() => new Date());

  useEffect(() => {
    const id = setInterval(() => setAgora(new Date()), 60_000);
    return () => clearInterval(id);
  }, []);

  const s = servicoEmCurso(agora, horaInicio);
  const porcentagem = `${(s.fracao * 100).toFixed(2)}%`;

  return (
    <section className="fita" aria-label={`Serviço de ${formatarDataBR(s.diaDoServico)}: ${s.horasCumpridas} de 24 horas cumpridas`}>
      <div className="fita-cabecalho">
        <span className="fita-titulo">Serviço de {formatarDataBR(s.diaDoServico)}</span>
        <span className="fita-contagem dado">{s.horasCumpridas}h de 24h</span>
      </div>
      <div className="fita-trilho">
        <div className="fita-cumprido" style={{ width: porcentagem }} />
        <div className="fita-agora" style={{ left: porcentagem }} />
      </div>
      <div className="fita-horas dado" aria-hidden="true">
        {MARCAS.map((h) => (
          <span key={h} style={{ left: `${(h / 24) * 100}%` }}>
            {String((horaInicio + h) % 24).padStart(2, "0")}h
          </span>
        ))}
      </div>
    </section>
  );
}
