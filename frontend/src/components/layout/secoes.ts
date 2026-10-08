const SECAO_POR_ROTA: Record<string, string> = {
  escala: "Escala",
  trocas: "Escala",
  "minha-escala": "Escala",
  avisos: "Escala",
  militares: "Pessoal",
  qualificacoes: "Pessoal",
  missoes: "Pessoal",
  historico: "Pessoal",
  "tipos-servico": "Configuração",
  regras: "Configuração",
  feriados: "Configuração",
  "postos-graduacao": "Configuração",
  subunidades: "Configuração",
  perfis: "Administração",
  auditoria: "Administração",
  painel: "Visão geral",
  boletim: "Comunicados",
};

export function secaoDaRota(caminho: string): string | null {
  const primeiro = caminho.split("/").filter(Boolean)[0] ?? "";
  return SECAO_POR_ROTA[primeiro] ?? null;
}
