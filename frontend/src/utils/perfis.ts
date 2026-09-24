/** Nomes de exibição dos perfis de acesso — o backend guarda o nome do
 *  perfil igual ao ROLE do Spring Security (ex.: "SARGENTEANTE"), então
 *  toda tela que mostra perfil pra gente ler usa esse mapa. */
export const PERFIL_LABEL: Record<string, string> = {
  MILITAR_ESCALADO: "Militar Escalado",
  SD_EP_SARGENTEACAO: "Sd EP da Sargenteação",
  CABO_SARGENTEACAO: "Cabo da Sargenteação",
  SARGENTEANTE: "Sargenteante",
};
