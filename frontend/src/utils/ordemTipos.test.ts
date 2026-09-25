import { describe, expect, it } from "vitest";
import { ordenarPorTipo } from "./ordemTipos";

describe("ordenarPorTipo", () => {
  it("segue a ordem do Boletim Interno e, dentro da função, o nome de guerra; vaga aberta por último", () => {
    const lista = [
      { tipoServico: { nome: "Guardas ao Quartel" }, militar: { nomeGuerra: "Prado" } },
      { tipoServico: { nome: "Oficial de Dia" }, militar: { nomeGuerra: "Zeni" } },
      { tipoServico: { nome: "Guardas ao Quartel" }, militar: null },
      { tipoServico: { nome: "Guardas ao Quartel" }, militar: { nomeGuerra: "Andrade" } },
    ];
    expect(ordenarPorTipo(lista).map((s) => `${s.tipoServico.nome}:${s.militar?.nomeGuerra ?? "-"}`)).toEqual([
      "Oficial de Dia:Zeni",
      "Guardas ao Quartel:Andrade",
      "Guardas ao Quartel:Prado",
      "Guardas ao Quartel:-",
    ]);
  });
});
