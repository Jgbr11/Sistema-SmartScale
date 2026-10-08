import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../../api/client";
import type { Notificacao } from "../../api/types";
import { Esqueleto } from "../ui/Esqueleto";

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
    <div className="sino">
      <button
        ref={botaoRef}
        onClick={abrirPainel}
        aria-label="Notificações"
        className="sino-botao"
      >
        🔔
        {contagem > 0 && (
          <span
            className="sino-contador"
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
          <div className="sino-cabecalho">
            <strong className="texto-125">Notificações</strong>
            {contagem > 0 && (
              <button onClick={marcarTodasLidas} className="link-pequeno">
                Marcar tudo como lido
              </button>
            )}
          </div>
          {carregando ? (
            <Esqueleto />
          ) : notificacoes.length === 0 ? (
            <div className="vazio">Nenhuma notificação ainda.</div>
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
                <div className="nota-mini mt-2">{formatarQuando(n.dataCriacao)}</div>
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
