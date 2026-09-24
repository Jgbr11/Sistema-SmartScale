import type { Afastamento } from "../api/types";

export const TIPO_AFASTAMENTO_LABEL: Record<Afastamento["tipo"], string> = {
  MISSAO: "Missão",
  DISPENSA: "Dispensa",
  FERIAS: "Férias",
  LICENCA: "Licença",
  CURSO: "Curso",
  OUTRO: "Outro",
};

export const TIPOS_AFASTAMENTO: { valor: Afastamento["tipo"]; label: string }[] = [
  { valor: "MISSAO", label: "Missão" },
  { valor: "DISPENSA", label: "Dispensa" },
  { valor: "FERIAS", label: "Férias" },
  { valor: "LICENCA", label: "Licença" },
  { valor: "CURSO", label: "Curso" },
  { valor: "OUTRO", label: "Outro" },
];
