import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from "react";

type TipoAviso = "sucesso" | "erro" | "info";

interface Aviso {
  id: number;
  tipo: TipoAviso;
  mensagem: string;
}

interface Dialogo {
  tipo: "confirmar" | "perguntar";
  mensagem: string;
  obrigatorio: boolean;
  resolver: (valor: boolean | string | null) => void;
}

interface Feedback {
  avisar: (mensagem: string, tipo?: TipoAviso) => void;
  confirmar: (mensagem: string) => Promise<boolean>;
  perguntar: (mensagem: string, opcoes?: { obrigatorio?: boolean }) => Promise<string | null>;
}

const FeedbackContext = createContext<Feedback | undefined>(undefined);

export function FeedbackProvider({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const [dialogo, setDialogo] = useState<Dialogo | null>(null);
  const [resposta, setResposta] = useState("");
  const proximoId = useRef(1);

  const avisar = useCallback((mensagem: string, tipo: TipoAviso = "info") => {
    const id = proximoId.current++;
    setAvisos((atual) => [...atual, { id, tipo, mensagem }]);
    setTimeout(() => setAvisos((atual) => atual.filter((a) => a.id !== id)), 5000);
  }, []);

  const confirmar = useCallback(
    (mensagem: string) =>
      new Promise<boolean>((resolve) =>
        setDialogo({ tipo: "confirmar", mensagem, obrigatorio: false, resolver: (v) => resolve(v === true) })),
    []);

  const perguntar = useCallback(
    (mensagem: string, opcoes?: { obrigatorio?: boolean }) =>
      new Promise<string | null>((resolve) => {
        setResposta("");
        setDialogo({ tipo: "perguntar", mensagem, obrigatorio: opcoes?.obrigatorio ?? false,
          resolver: (v) => resolve(typeof v === "string" ? v : null) });
      }),
    []);

  const fechar = useCallback((valor: boolean | string | null) => {
    setDialogo((atual) => {
      atual?.resolver(valor);
      return null;
    });
  }, []);

  useEffect(() => {
    if (!dialogo) return;
    const aoTeclar = (e: KeyboardEvent) => {
      if (e.key === "Escape") fechar(dialogo.tipo === "confirmar" ? false : null);
    };
    document.addEventListener("keydown", aoTeclar);
    return () => document.removeEventListener("keydown", aoTeclar);
  }, [dialogo, fechar]);

  const podeConfirmar = dialogo?.tipo !== "perguntar" || !dialogo.obrigatorio || resposta.trim() !== "";

  return (
    <FeedbackContext.Provider value={{ avisar, confirmar, perguntar }}>
      {children}
      {dialogo && (
        <div className="dialogo-fundo" onClick={() => fechar(dialogo.tipo === "confirmar" ? false : null)}>
          <div className="dialogo" role="dialog" aria-modal="true" aria-label={dialogo.mensagem} onClick={(e) => e.stopPropagation()}>
            <p>{dialogo.mensagem}</p>
            {dialogo.tipo === "perguntar" && (
              <textarea autoFocus rows={3} value={resposta} onChange={(e) => setResposta(e.target.value)} />
            )}
            <div className="dialogo-acoes">
              <button className="btn btn-outline" onClick={() => fechar(dialogo.tipo === "confirmar" ? false : null)}>
                Voltar
              </button>
              <button className="btn btn-primary" autoFocus={dialogo.tipo === "confirmar"} disabled={!podeConfirmar}
                onClick={() => fechar(dialogo.tipo === "confirmar" ? true : resposta.trim())}>
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
      <div className="avisos" aria-live="polite">
        {avisos.map((a) => (
          <div key={a.id} className={`aviso aviso-${a.tipo}`}>{a.mensagem}</div>
        ))}
      </div>
    </FeedbackContext.Provider>
  );
}

export function useFeedback() {
  const ctx = useContext(FeedbackContext);
  if (!ctx) throw new Error("useFeedback deve ser usado dentro de um FeedbackProvider");
  return ctx;
}
