import type { RequisitoServico } from "../api/types";

export function descreverRequisito(r: RequisitoServico): string {
  let texto = r.posto.sigla;
  if (r.subunidade) texto += ` lotado no ${r.subunidade.sigla}`;
  if (r.qualificacao) texto += ` com ${r.qualificacao.nome}`;

  const excecoes: string[] = [];
  if (r.qualificacoesExcluidas.length > 0) {
    const nomes = r.qualificacoesExcluidas.map((q) => q.nome).sort((a, b) => a.localeCompare(b, "pt-BR"));
    excecoes.push(`quem tem ${juntarComOu(nomes)}`);
  }
  if (r.subunidadeExcluida) excecoes.push(`lotados no ${r.subunidadeExcluida.sigla}`);
  if (excecoes.length > 0) texto += `, exceto ${excecoes.join(" e ")}`;
  return texto;
}

function juntarComOu(itens: string[]): string {
  if (itens.length <= 1) return itens.join("");
  return `${itens.slice(0, -1).join(", ")} ou ${itens[itens.length - 1]}`;
}
