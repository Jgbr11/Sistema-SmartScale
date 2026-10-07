import { useCallback, useEffect, useEffectEvent, useState } from "react";
import { ApiError } from "../api/client";

export function useCarregamento<T>(buscar: () => Promise<T>, chave: unknown = null) {
  const [dados, setDados] = useState<T | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [versao, setVersao] = useState(0);
  const buscarAgora = useEffectEvent(buscar);

  useEffect(() => {
    let ativo = true;
    buscarAgora()
      .then((resultado) => {
        if (!ativo) return;
        setDados(resultado);
        setErro(null);
      })
      .catch((e) => {
        if (ativo) setErro(e instanceof ApiError ? e.message : "Não foi possível carregar os dados.");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [chave, versao]);

  const recarregar = useCallback(() => {
    setCarregando(true);
    setVersao((v) => v + 1);
  }, []);

  return { dados, carregando, erro, recarregar };
}
