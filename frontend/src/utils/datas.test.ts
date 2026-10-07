import { describe, expect, it } from "vitest";
import { celulasDoMes, dataDoMes, deslocarMes, hojeISO, mesISO } from "./datas";

describe("datas", () => {
  it("hoje é o dia local, mesmo tarde da noite", () => {
    expect(hojeISO(new Date(2026, 8, 25, 23, 30))).toBe("2026-09-25");
  });

  it("mês no formato da API", () => {
    expect(mesISO(2026, 0)).toBe("2026-01");
  });

  it("desloca mês atravessando o ano", () => {
    expect(deslocarMes({ ano: 2026, mes: 11 }, 1)).toEqual({ ano: 2027, mes: 0 });
    expect(deslocarMes({ ano: 2026, mes: 0 }, -1)).toEqual({ ano: 2025, mes: 11 });
  });

  it("células do mês começam no dia da semana certo", () => {
    const celulas = celulasDoMes(2026, 8);
    expect(celulas.slice(0, 3)).toEqual([null, null, 1]);
    expect(celulas.filter((c) => c !== null)).toHaveLength(30);
    expect(dataDoMes(2026, 8, 5)).toBe("2026-09-05");
  });
});
