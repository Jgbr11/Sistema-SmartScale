// Mesma ordem do Boletim Interno do 5º BSup (BI nº 107) - usada em
// qualquer tela que mostre o roster de um dia (Escala do mês e Escala
// do dia), pra sempre aparecer na mesma sequência que o pessoal já
// conhece do boletim de papel.
export const ORDEM_TIPOS = [
  "Oficial de Dia",
  "Graduado de Dia",
  "Comandante da Guarda",
  "Cabo da Guarda",
  "Cabo de Dia",
  "Motorista de Dia",
  "Monitoramento",
  "Plantão ao Alojamento",
  "Graduado do Rancho",
  "Cozinheiro de Dia",
  "Rancheiro de Dia",
  "Guardas ao Quartel",
];

export function ordenarPorTipo<T extends { tipoServico: { nome: string }; militar?: { nomeGuerra: string } | null }>(lista: T[]): T[] {
  return lista.slice().sort((a, b) => {
    const ia = ORDEM_TIPOS.indexOf(a.tipoServico.nome);
    const ib = ORDEM_TIPOS.indexOf(b.tipoServico.nome);
    const comparaTipo = (ia === -1 ? 999 : ia) - (ib === -1 ? 999 : ib);
    if (comparaTipo !== 0) return comparaTipo;
    // Dentro da mesma função, ordena por nome de guerra (alfabético).
    // Vaga em aberto (sem militar) vai por último dentro do grupo.
    const nomeA = a.militar?.nomeGuerra ?? "\uffff";
    const nomeB = b.militar?.nomeGuerra ?? "\uffff";
    return nomeA.localeCompare(nomeB, "pt-BR");
  });
}
