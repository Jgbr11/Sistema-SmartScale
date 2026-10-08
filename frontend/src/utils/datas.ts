export interface MesAno {
  ano: number;
  mes: number;
}

const doisDigitos = (n: number) => String(n).padStart(2, "0");

export function hojeISO(agora: Date = new Date()): string {
  return `${agora.getFullYear()}-${doisDigitos(agora.getMonth() + 1)}-${doisDigitos(agora.getDate())}`;
}

export function mesISO(ano: number, mes: number): string {
  return `${ano}-${doisDigitos(mes + 1)}`;
}

export function dataDoMes(ano: number, mes: number, dia: number): string {
  return `${mesISO(ano, mes)}-${doisDigitos(dia)}`;
}

export function deslocarMes({ ano, mes }: MesAno, delta: number): MesAno {
  const d = new Date(ano, mes + delta, 1);
  return { ano: d.getFullYear(), mes: d.getMonth() };
}

export function celulasDoMes(ano: number, mes: number): (number | null)[] {
  const deslocamento = new Date(ano, mes, 1).getDay();
  const dias = new Date(ano, mes + 1, 0).getDate();
  return [...Array(deslocamento).fill(null), ...Array.from({ length: dias }, (_, i) => i + 1)];
}

export function nomeDoMes(ano: number, mes: number): string {
  const texto = new Date(ano, mes, 1).toLocaleDateString("pt-BR", { month: "long", year: "numeric" });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
