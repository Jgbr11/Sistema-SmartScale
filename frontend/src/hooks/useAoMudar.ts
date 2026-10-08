import { useEffect, useEffectEvent } from "react";

export function useAoMudar(acao: () => void, chave: unknown = null) {
  const executar = useEffectEvent(acao);
  useEffect(() => {
    executar();
  }, [chave]);
}
