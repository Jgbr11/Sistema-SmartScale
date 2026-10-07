import { celulasDoMes, dataDoMes, hojeISO } from "../../utils/datas";

const DIAS_SEMANA = ["DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"];

export interface InfoDoDia {
  rotulo?: string;
  destaque?: "atencao" | "positivo" | "servico";
  habilitado?: boolean;
  progresso?: number;
}

interface Props {
  ano: number;
  mes: number;
  diaSelecionado?: string | null;
  onSelecionar?: (dataISO: string) => void;
  infoDoDia?: (dataISO: string) => InfoDoDia;
}

export function CalendarioMensal({ ano, mes, diaSelecionado = null, onSelecionar, infoDoDia }: Props) {
  const hoje = hojeISO();
  return (
    <div className="calendar-grid">
      {DIAS_SEMANA.map((d) => <div key={d} className="dow">{d}</div>)}
      {celulasDoMes(ano, mes).map((dia, i) => {
        if (dia === null) return <div key={`vazio-${i}`} className="calendar-cell empty" />;
        const dataISO = dataDoMes(ano, mes, dia);
        const info = infoDoDia?.(dataISO) ?? {};
        const classes = [
          "calendar-cell",
          info.destaque && `destaque-${info.destaque}`,
          diaSelecionado === dataISO && "selecionado",
          dataISO === hoje && "hoje",
        ].filter(Boolean).join(" ");
        return (
          <button
            key={dataISO}
            className={classes}
            disabled={info.habilitado === false || !onSelecionar}
            aria-pressed={diaSelecionado === dataISO}
            onClick={() => onSelecionar?.(dataISO)}
          >
            {dia}
            {info.rotulo && <span className="tipo">{info.rotulo}</span>}
            {info.progresso !== undefined && info.progresso > 0 && (
              <span className="dia-fita" aria-hidden="true">
                <span style={{ width: `${Math.min(1, info.progresso) * 100}%` }} />
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}
