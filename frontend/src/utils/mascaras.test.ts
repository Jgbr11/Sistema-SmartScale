import { describe, expect, it } from "vitest";
import { mascararCpf, mascararFusex, mascararTelefone, somenteDigitos } from "./mascaras";

describe("máscaras", () => {
  it("CPF encaixa pontuação enquanto digita", () => {
    expect(mascararCpf("0000")).toBe("000.0");
    expect(mascararCpf("00000000001")).toBe("000.000.000-01");
    expect(mascararCpf("000000000019999")).toBe("000.000.000-01");
  });

  it("telefone celular e fixo", () => {
    expect(mascararTelefone("41999998888")).toBe("(41) 99999-8888");
    expect(mascararTelefone("4133334444")).toBe("(41) 3333-4444");
  });

  it("FUSEX", () => {
    expect(mascararFusex("12345")).toBe("123-45");
  });

  it("somenteDigitos tira a máscara", () => {
    expect(somenteDigitos("000.000.000-01")).toBe("00000000001");
  });
});
