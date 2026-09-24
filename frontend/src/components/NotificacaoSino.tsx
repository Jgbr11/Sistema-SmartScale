import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Notificacao } from "../api/types";

/**
 * O sininho — badge com a contagem de não lidas, atualizado a cada
 * 30s (sem websocket nesse projeto, então é sondagem mesmo). Clicar
 * abre um painel com as mais recentes; clicar numa notificação marca
 * como lida e leva pra tela relacionada.
 */
export function NotificacaoSino() {
  const [contagem, setContagem] = useState(0);
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [aberto, setAberto] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [posicao, setPosicao] = useState({ top: 0, left: 0 });
  const botaoRef = useRef<HTMLButtonElement>(null);
  const painelRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  async function buscarContagem() {
    try {
      const r = await api.get<{ total: number }>("/api/notificacoes/nao-lidas/contagem");
      setContagem(r.total);
    } catch {
      // silencioso — o sininho não pode quebrar o resto da tela
    }
  }

  useEffect(() => {
    buscarContagem();
    const intervalo = setInterval(buscarContagem, 30000);
    return () => clearInterval(intervalo);
  }, []);

  useEffect(() => {
    function aoClicarFora(e: MouseEvent) {
      if (
        painelRef.current && !painelRef.current.contains(e.target as Node) &&
        botaoRef.current && !botaoRef.current.contains(e.target as Node)
      ) {
        setAberto(false);
      }
    }
    document.addEventListener("mousedown", aoClicarFora);
    return () => document.removeEventListener("mousedown", aoClicarFora);
  }, []);

  async function abrirPainel() {
    if (!aberto && botaoRef.current) {
      const r = botaoRef.current.getBoundingClientRect();
      // Painel fixo na tela (não dentro do sidebar) pra não ser cortado
      // pelo overflow-y:auto do sidebar quando passa da largura dele.
      const larguraPainel = 320;
      let left = r.left;
      if (left + larguraPainel > window.innerWidth - 12) left = window.innerWidth - larguraPainel - 12;
      setPosicao({ top: r.bottom + 8, left });
    }
    setAberto((v) => !v);
    if (!aberto) {
      setCarregando(true);
      const lista = await api.get<Notificacao[]>("/api/notificacoes?limite=20");
      setNotificacoes(lista);
      setCarregando(false);
    }
  }

  async function clicarNotificacao(n: Notificacao) {
    if (!n.lida) {
      await api.post(`/api/notificacoes/${n.id}/marcar-lida`, {});
      setContagem((c) => Math.max(0, c - 1));
      setNotificacoes((atual) => atual.map((x) => (x.id === n.id ? { ...x, lida: true } : x)));
    }
    setAberto(false);
    if (n.link) navigate(n.link);
  }

  async function marcarTodasLidas() {
    await api.post("/api/notificacoes/marcar-todas-lidas", {});
    setContagem(0);
    setNotificacoes((atual) => atual.map((x) => ({ ...x, lida: true })));
  }

  return (
    <div style={{ position: "relative" }}>
      <button
        ref={botaoRef}
        onClick={abrirPainel}
        aria-label="Notificações"
        style={{
          position: "relative", background: "none", border: "1px solid var(--footer-border)",
          borderRadius: 6, width: 34, height: 34, color: "#fff", fontSize: 15, cursor: "pointer",
        }}
      >
        🔔
        {contagem > 0 && (
          <span
            style={{
              position: "absolute", top: -5, right: -5, background: "var(--red-text)", color: "#fff",
              borderRadius: 999, fontSize: 10, fontWeight: 700, minWidth: 16, height: 16,
              display: "flex", alignItems: "center", justifyContent: "center", padding: "0 3px",
            }}
          >
            {contagem > 9 ? "9+" : contagem}
          </span>
        )}
      </button>

      {aberto && (
        <div
          ref={painelRef}
          style={{
            position: "fixed", top: posicao.top, left: posicao.left, width: 320, maxHeight: 400, overflowY: "auto",
            background: "#fff", border: "1px solid var(--border)", borderRadius: 8, boxShadow: "0 8px 24px rgba(0,0,0,0.18)",
            zIndex: 500,
          }}
        >
          <div style={{ padding: "10px 14px", borderBottom: "1px solid var(--border-2)", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <strong style={{ fontSize: 12.5 }}>Notificações</strong>
            {contagem > 0 && (
              <button onClick={marcarTodasLidas} style={{ background: "none", border: "none", color: "var(--sidebar-active)", fontSize: 11, cursor: "pointer" }}>
                Marcar tudo como lido
              </button>
            )}
          </div>
          {carregando ? (
            <div style={{ padding: 16, fontSize: 12, color: "var(--grey)" }}>Carregando…</div>
          ) : notificacoes.length === 0 ? (
            <div style={{ padding: 16, fontSize: 12, color: "var(--grey)" }}>Nenhuma notificação ainda.</div>
          ) : (
            notificacoes.map((n) => (
              <button
                key={n.id}
                onClick={() => clicarNotificacao(n)}
                style={{
                  display: "block", width: "100%", textAlign: "left", padding: "10px 14px",
                  border: "none", borderBottom: "1px solid var(--border-2)", cursor: "pointer",
                  background: n.lida ? "#fff" : "var(--amber-bg)",
                }}
              >
                <div style={{ fontSize: 12, color: "var(--dark)", fontWeight: n.lida ? 400 : 600 }}>{n.mensagem}</div>
                <div style={{ fontSize: 10.5, color: "var(--grey)", marginTop: 2 }}>{formatarQuando(n.dataCriacao)}</div>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}

function formatarQuando(iso: string) {
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" });
}
