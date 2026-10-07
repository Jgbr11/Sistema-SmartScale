import { act, fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { FeedbackProvider, useFeedback } from "./Feedback";

function Botao({ onResultado }: { onResultado: (v: boolean) => void }) {
  const { confirmar } = useFeedback();
  return <button onClick={async () => onResultado(await confirmar("Remover este boletim?"))}>abrir</button>;
}

describe("Feedback", () => {
  it("confirmar resolve true ao confirmar e false ao voltar", async () => {
    const resultados: boolean[] = [];
    render(<FeedbackProvider><Botao onResultado={(v) => resultados.push(v)} /></FeedbackProvider>);

    fireEvent.click(screen.getByText("abrir"));
    expect(screen.getByText("Remover este boletim?")).toBeTruthy();
    await act(async () => fireEvent.click(screen.getByText("Confirmar")));

    fireEvent.click(screen.getByText("abrir"));
    await act(async () => fireEvent.click(screen.getByText("Voltar")));

    expect(resultados).toEqual([true, false]);
  });
});
