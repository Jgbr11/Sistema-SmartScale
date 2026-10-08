import { createContext, useContext, useState, type ReactNode } from "react";
import { api, ApiError } from "../api/client";
import type { Usuario } from "../api/types";
import { useAoMudar } from "../hooks/useAoMudar";

interface AuthContextValue {
  usuario: Usuario | null;
  carregando: boolean;
  entrar: (login: string, senha: string) => Promise<void>;
  sair: () => Promise<void>;
  recarregar: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);

  async function carregarSessao() {
    try {
      const me = await api.get<Usuario>("/api/auth/me");
      setUsuario(me);
    } catch {
      setUsuario(null);
    } finally {
      setCarregando(false);
    }
  }

  useAoMudar(carregarSessao);

  async function entrar(login: string, senha: string) {
    await api.login(login, senha);
    await carregarSessao();
  }

  async function sair() {
    await api.logout();
    setUsuario(null);
  }

  return (
    <AuthContext.Provider value={{ usuario, carregando, entrar, sair, recarregar: carregarSessao }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth deve ser usado dentro de um AuthProvider");
  return ctx;
}

export { ApiError };
