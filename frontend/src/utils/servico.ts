import { hojeISO } from "./datas";

export interface ServicoEmCurso {
  diaDoServico: string;
  inicio: Date;
  fim: Date;
  fracao: number;
  horasCumpridas: number;
}

export function servicoEmCurso(agora: Date = new Date(), horaInicio = 8): ServicoEmCurso {
  const inicio = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate(), horaInicio);
  if (agora < inicio) inicio.setDate(inicio.getDate() - 1);
  const fim = new Date(inicio);
  fim.setDate(fim.getDate() + 1);
  const fracao = (agora.getTime() - inicio.getTime()) / (fim.getTime() - inicio.getTime());
  return { diaDoServico: hojeISO(inicio), inicio, fim, fracao, horasCumpridas: Math.floor(fracao * 24) };
}

export function progressoDoDia(dataISO: string): number {
  const emCurso = servicoEmCurso();
  if (dataISO < emCurso.diaDoServico) return 1;
  return dataISO === emCurso.diaDoServico ? emCurso.fracao : 0;
}
