import { describe, expect, it } from "vitest";
import { secaoDaRota } from "./secoes";

describe("secaoDaRota", () => {
  it("usa o grupo do menu", () => {
    expect(secaoDaRota("/trocas")).toBe("Escala");
    expect(secaoDaRota("/militares/12")).toBe("Pessoal");
    expect(secaoDaRota("/regras")).toBe("Configuração");
    expect(secaoDaRota("/auditoria")).toBe("Administração");
  });

  it("rotas de uso pessoal ficam sem seção", () => {
    expect(secaoDaRota("/minha-conta")).toBeNull();
  });
});
