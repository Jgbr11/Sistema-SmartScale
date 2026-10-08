import { useEffect, useState } from "react";
import { api } from "../api/client";

export interface Organizacao {
  nome: string;
  sigla: string;
  sistema: string;
}

let pedido: Promise<Organizacao> | null = null;

function buscarOrganizacao(): Promise<Organizacao> {
  if (!pedido) {
    pedido = api.get<Organizacao>("/api/organizacao").catch((erro) => {
      pedido = null;
      throw erro;
    });
  }
  return pedido;
}

export function useOrganizacao(): Organizacao | null {
  const [organizacao, setOrganizacao] = useState<Organizacao | null>(null);

  useEffect(() => {
    let ativo = true;
    buscarOrganizacao()
      .then((o) => { if (ativo) setOrganizacao(o); })
      .catch(() => {});
    return () => { ativo = false; };
  }, []);

  return organizacao;
}
