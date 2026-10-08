import { describe, expect, it } from "vitest";
import { servicoEmCurso } from "./servico";

describe("servicoEmCurso", () => {
  it("às 14h30 o serviço do dia está com 6h cumpridas", () => {
    const s = servicoEmCurso(new Date(2026, 8, 25, 14, 30));
    expect(s.diaDoServico).toBe("2026-09-25");
    expect(s.horasCumpridas).toBe(6);
    expect(s.fracao).toBeCloseTo(6.5 / 24, 5);
  });

  it("às 05h o serviço ainda é o do dia anterior", () => {
    const s = servicoEmCurso(new Date(2026, 8, 26, 5, 0));
    expect(s.diaDoServico).toBe("2026-09-25");
    expect(s.horasCumpridas).toBe(21);
  });

  it("às 08h em ponto começa o serviço novo", () => {
    const s = servicoEmCurso(new Date(2026, 8, 26, 8, 0));
    expect(s.diaDoServico).toBe("2026-09-26");
    expect(s.fracao).toBe(0);
  });
});
