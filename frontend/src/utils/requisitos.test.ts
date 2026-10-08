import { describe, expect, it } from "vitest";
import type { RequisitoServico } from "../api/types";
import { descreverRequisito } from "./requisitos";

const posto = (sigla: string) => ({ id: 1, sigla, descricao: sigla, nivelHierarquico: 1 });
const sub = (sigla: string) => ({ id: 1, sigla, nome: sigla, ativo: true });
const curso = (nome: string) => ({ id: nome.length, nome });

describe("descreverRequisito", () => {
  it("só posto", () => {
    const r: RequisitoServico = { id: 1, posto: posto("Ten"), qualificacoesExcluidas: [] };
    expect(descreverRequisito(r)).toBe("Ten");
  });

  it("posto com curso exigido", () => {
    const r: RequisitoServico = { id: 1, posto: posto("Sd EP"), qualificacao: curso("CFC"), qualificacoesExcluidas: [] };
    expect(descreverRequisito(r)).toBe("Sd EP com CFC");
  });

  it("posto lotado numa subunidade", () => {
    const r: RequisitoServico = { id: 1, posto: posto("Sd EV"), subunidade: sub("Aprov"), qualificacoesExcluidas: [] };
    expect(descreverRequisito(r)).toBe("Sd EV lotado no Aprov");
  });

  it("exceções de curso (ordem alfabética) e de subunidade", () => {
    const r: RequisitoServico = {
      id: 1, posto: posto("Sd EP"),
      qualificacoesExcluidas: [curso("Motorista"), curso("CFC")],
      subunidadeExcluida: sub("Aprov"),
    };
    expect(descreverRequisito(r)).toBe("Sd EP, exceto quem tem CFC ou Motorista e lotados no Aprov");
  });
});
