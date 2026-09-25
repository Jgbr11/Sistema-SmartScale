import { describe, expect, it } from "vitest";
import { capitalizar, formatarCpf, formatarDataBR, formatarPeriodo } from "./formatadores";

describe("formatadores", () => {
  it("formata data ISO como dd/mm/aaaa", () => {
    expect(formatarDataBR("2026-09-05")).toBe("05/09/2026");
  });

  it("período de um dia só mostra a data uma vez", () => {
    expect(formatarPeriodo("2026-09-05", "2026-09-05")).toBe("05/09/2026");
    expect(formatarPeriodo("2026-09-05", "2026-09-07")).toBe("05/09/2026 a 07/09/2026");
  });

  it("formata CPF de 11 dígitos e devolve o resto como veio", () => {
    expect(formatarCpf("00000000001")).toBe("000.000.000-01");
    expect(formatarCpf("123")).toBe("123");
  });

  it("capitaliza só a primeira letra", () => {
    expect(capitalizar("setembro de 2026")).toBe("Setembro de 2026");
  });
});
