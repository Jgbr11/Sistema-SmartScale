import { Navigate, Route, BrowserRouter, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { Shell } from "./components/Shell";
import { LoginPage } from "./pages/Login";
import { MilitaresPage } from "./pages/Militares";
import { TiposServicoPage } from "./pages/TiposServico";
import { RegrasEscalaPage } from "./pages/RegrasEscala";
import { EscalaDoMesPage } from "./pages/EscalaDoMes";
import { EscalaDoDiaPage } from "./pages/EscalaDoDia";
import { MinhaEscalaPage } from "./pages/MinhaEscala";

import { QualificacoesPage } from "./pages/Qualificacoes";
import { MissoesDispensasPage } from "./pages/MissoesDispensas";
import { TrocasPage } from "./pages/Trocas";
import { FeriadosPage } from "./pages/Feriados";
import { PainelPage } from "./pages/Painel";
import { AvisosPage } from "./pages/Avisos";
import { MinhaContaPage } from "./pages/MinhaConta";
import { PerfisPermissoesPage } from "./pages/PerfisPermissoes";
import { FichaMilitarPage } from "./pages/FichaMilitar";
import { AuditoriaPage } from "./pages/Auditoria";
import { BoletimPage } from "./pages/Boletim";
import { EscalaPdfPage } from "./pages/EscalaPdf";
import { HistoricoPage } from "./pages/Historico";

function RotaProtegida({ children }: { children: React.ReactNode }) {
  const { usuario, carregando } = useAuth();
  if (carregando) return <div style={{ padding: 40 }}>Carregando…</div>;
  if (!usuario) return <Navigate to="/login" replace />;
  return <Shell>{children}</Shell>;
}

/** Igual à protegida, mas sem o Shell (sem menu lateral) — usada pela
 *  página de impressão, que precisa ficar limpa pro PDF sair sem o menu. */
function RotaProtegidaSemMenu({ children }: { children: React.ReactNode }) {
  const { usuario, carregando } = useAuth();
  if (carregando) return <div style={{ padding: 40 }}>Carregando…</div>;
  if (!usuario) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function RaizPorPerfil() {
  const { usuario } = useAuth();
  if (usuario?.perfil === "MILITAR_ESCALADO" || usuario?.perfil === "SD_EP_SARGENTEACAO") {
    return <Navigate to="/minha-escala" replace />;
  }
  if (usuario?.perfil === "SARGENTEANTE" || usuario?.perfil === "CABO_SARGENTEACAO") {
    return <Navigate to="/painel" replace />;
  }
  return <Navigate to="/escala" replace />;
}

/** Militar Escalado só pode ver UM dia por vez (RF13/RF14 com escopo restrito).
 *  Os outros perfis usam a visão completa do mês, com calendário e geração. */
function EscalaRoute() {
  const { usuario } = useAuth();
  if (usuario?.perfil === "MILITAR_ESCALADO") return <EscalaDoDiaPage />;
  return <EscalaDoMesPage />;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<RotaProtegida><RaizPorPerfil /></RotaProtegida>} />

          {/* Funcionais nesta fatia */}
          <Route path="/militares" element={<RotaProtegida><MilitaresPage /></RotaProtegida>} />
          <Route path="/tipos-servico" element={<RotaProtegida><TiposServicoPage /></RotaProtegida>} />
          <Route path="/regras" element={<RotaProtegida><RegrasEscalaPage /></RotaProtegida>} />
          <Route path="/escala" element={<RotaProtegida><EscalaRoute /></RotaProtegida>} />
          <Route path="/escala/pdf/:data" element={<RotaProtegidaSemMenu><EscalaPdfPage /></RotaProtegidaSemMenu>} />
          <Route path="/minha-escala" element={<RotaProtegida><MinhaEscalaPage /></RotaProtegida>} />
          <Route path="/qualificacoes" element={<RotaProtegida><QualificacoesPage /></RotaProtegida>} />

          {/* No menu (igual ao Figma), próxima fatia de implementação */}
          <Route path="/painel" element={<RotaProtegida><PainelPage /></RotaProtegida>} />
          <Route path="/avisos" element={<RotaProtegida><AvisosPage /></RotaProtegida>} />
          <Route path="/trocas" element={<RotaProtegida><TrocasPage /></RotaProtegida>} />
          <Route path="/missoes" element={<RotaProtegida><MissoesDispensasPage /></RotaProtegida>} />
          <Route path="/bloqueio" element={<RotaProtegida><EscalaDoMesPage /></RotaProtegida>} />
          <Route path="/feriados" element={<RotaProtegida><FeriadosPage /></RotaProtegida>} />
          <Route path="/perfis" element={<RotaProtegida><PerfisPermissoesPage /></RotaProtegida>} />
          <Route path="/minha-conta" element={<RotaProtegida><MinhaContaPage /></RotaProtegida>} />
          <Route path="/militares/:id" element={<RotaProtegida><FichaMilitarPage /></RotaProtegida>} />
          <Route path="/auditoria" element={<RotaProtegida><AuditoriaPage /></RotaProtegida>} />
          <Route path="/boletim" element={<RotaProtegida><BoletimPage /></RotaProtegida>} />
          <Route path="/historico" element={<RotaProtegida><HistoricoPage /></RotaProtegida>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
