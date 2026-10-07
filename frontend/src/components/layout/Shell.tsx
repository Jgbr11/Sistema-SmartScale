import { NavLink, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { NotificacaoSino } from "./NotificacaoSino";
import { PERFIL_LABEL } from "../../utils/perfis";

interface Item {
  label: string;
  to: string;
}
interface Grupo {
  label: string | null;
  itens: Item[];
}

const MENU_POR_PERFIL: Record<string, { topo?: Item[]; grupos: Grupo[]; rodape?: Item }> = {
  SARGENTEANTE: {
    topo: [{ label: "Painel", to: "/painel" }, { label: "Boletim", to: "/boletim" }],
    grupos: [
      { label: "ESCALA", itens: [
        { label: "Escala do mês", to: "/escala" },
        { label: "Trocas de serviço", to: "/trocas" },
      ]},
      { label: "PESSOAL", itens: [
        { label: "Militares", to: "/militares" },
        { label: "Qualificações", to: "/qualificacoes" },
        { label: "Missões e dispensas", to: "/missoes" },
      ]},
      { label: "CONFIGURAÇÃO", itens: [
        { label: "Tipos de serviço", to: "/tipos-servico" },
        { label: "Regras da escala", to: "/regras" },
        { label: "Feriados", to: "/feriados" },
        { label: "Postos e graduações", to: "/postos-graduacao" },
        { label: "Subunidades", to: "/subunidades" },
      ]},
      { label: "ADMINISTRAÇÃO", itens: [
        { label: "Perfis e permissões", to: "/perfis" },
        { label: "Log de auditoria", to: "/auditoria" },
      ]},
    ],
    rodape: { label: "Histórico", to: "/historico" },
  },
  CABO_SARGENTEACAO: {
    topo: [{ label: "Painel", to: "/painel" }, { label: "Boletim", to: "/boletim" }],
    grupos: [
      { label: "PESSOAL", itens: [
        { label: "Militares", to: "/militares" },
        { label: "Qualificações", to: "/qualificacoes" },

        { label: "Missões e dispensas", to: "/missoes" },
      ]},
      { label: "ESCALA", itens: [
        { label: "Escala do mês", to: "/escala" },
        { label: "Trocas de serviço", to: "/trocas" },
      ]},
    ],
  },
  SD_EP_SARGENTEACAO: {
    grupos: [
      { label: "MINHA ROTINA", itens: [
        { label: "Boletim", to: "/boletim" },
        { label: "Minha escala", to: "/minha-escala" },
        { label: "Escala do dia", to: "/escala" },
        { label: "Avisos", to: "/avisos" },
        { label: "Meu histórico", to: "/historico" },
      ]},
      { label: "SARGENTEAÇÃO (LEITURA)", itens: [
        { label: "Militares", to: "/militares" },
        { label: "Escala do mês", to: "/escala" },
        { label: "Trocas de serviço", to: "/trocas" },
        { label: "Histórico completo", to: "/historico" },
      ]},
    ],
  },
  MILITAR_ESCALADO: {
    grupos: [
      { label: null, itens: [
        { label: "Boletim", to: "/boletim" },
        { label: "Minha escala", to: "/minha-escala" },
        { label: "Escala do dia", to: "/escala" },
        { label: "Avisos", to: "/avisos" },
        { label: "Minhas trocas", to: "/trocas" },
        { label: "Meu histórico", to: "/historico" },
      ]},
    ],
  },
};

export function Shell({ children }: { children: React.ReactNode }) {
  const { usuario, sair } = useAuth();
  const menu = usuario && !usuario.trocarSenha ? MENU_POR_PERFIL[usuario.perfil] : undefined;
  const [menuAberto, setMenuAberto] = useState(false);
  const local = useLocation();

  useEffect(() => {
    setMenuAberto(false);
  }, [local.pathname]);

  return (
    <div className="app-shell">
      <div className="mobile-topbar">
        <button className="hamburger" onClick={() => setMenuAberto(true)} aria-label="Abrir menu">☰</button>
        <span className="brand-mini" style={{ flex: 1 }}>MilScale</span>
        {usuario && !usuario.trocarSenha && <NotificacaoSino />}
      </div>
      <div className={"sidebar-backdrop" + (menuAberto ? " open" : "")} onClick={() => setMenuAberto(false)} />
      <aside className={"sidebar" + (menuAberto ? " open" : "")}>
        <div className="brand" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
            <div className="crest">M</div>
            <div>
              <h1>MilScale</h1>
              <span>Escala de serviço</span>
            </div>
          </div>
          {usuario && !usuario.trocarSenha && <NotificacaoSino />}
        </div>
        <nav style={{ flex: 1 }}>
          {menu?.topo?.map((item) => <ItemLink key={item.to} item={item} />)}
          {menu?.grupos.map((g, i) => (
            <div key={i} style={{ marginTop: i === 0 && !menu.topo ? 0 : 14 }}>
              {g.label && <div className="section-label">{g.label}</div>}
              {g.itens.map((item) => <ItemLink key={item.to} item={item} />)}
            </div>
          ))}
          {menu?.rodape && (
            <div style={{ marginTop: 14 }}>
              <ItemLink item={menu.rodape} />
            </div>
          )}
        </nav>
        {usuario && (
          <NavLink to="/minha-conta" className="user-box" style={{ cursor: "pointer" }}>
            <strong>{usuario.nomeExibicao}</strong>
            <small>{PERFIL_LABEL[usuario.perfil] ?? usuario.perfil}</small>
          </NavLink>
        )}
        <button className="logout" onClick={sair}>
          ↩ Log out
        </button>
      </aside>
      <div className="content">{children}</div>
    </div>
  );
}

function ItemLink({ item }: { item: Item }) {
  return (
    <NavLink to={item.to} className={({ isActive }) => (isActive ? "active" : "")}>
      {item.label}
    </NavLink>
  );
}

