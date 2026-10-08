import { Navigate, Route, BrowserRouter, Routes, useLocation } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { Shell } from "./components/layout/Shell";
import { LoginPage } from "./pages/Login";
import { MilitaresPage } from "./pages/Militares";
import { TiposServicoPage } from "./pages/TiposServico";
import { RegrasEscalaPage } from "./pages/RegrasEscala";
import { EscalaDoMesPage } from "./pages/EscalaDoMes";
import { EscalaDoDiaPage } from "./pages/EscalaDoDia";
import { MinhaEscalaPage } from "./pages/MinhaEscala";

import { QualificacoesPage } from "./pages/Qualificacoes";
import { PostosGraduacaoPage } from "./pages/PostosGraduacao";
import { SubunidadesPage } from "./pages/Subunidades";
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
import { NaoEncontradaPage } from "./pages/NaoEncontrada";
import type { Usuario } from "./api/types";

type Perfil = Usuario["perfil"];

const SARGENTEACAO: Perfil[] = ["SARGENTEANTE", "CABO_SARGENTEACAO", "SD_EP_SARGENTEACAO"];
const GESTAO: Perfil[] = ["SARGENTEANTE", "CABO_SARGENTEACAO"];
const SARGENTEANTE: Perfil[] = ["SARGENTEANTE"];

function RotaProtegida({ children, perfis }: { children: React.ReactNode; perfis?: Perfil[] }) {
  const { usuario, carregando } = useAuth();
  const local = useLocation();
  if (carregando) return <div className="carregando-rota">Carregando…</div>;
  if (!usuario) return <Navigate to="/login" replace />;
  if (usuario.trocarSenha && local.pathname !== "/minha-conta") return <Navigate to="/minha-conta" replace />;
  if (perfis && !perfis.includes(usuario.perfil)) return <Navigate to="/" replace />;
  return <Shell>{children}</Shell>;
}

function RotaProtegidaSemMenu({ children }: { children: React.ReactNode }) {
  const { usuario, carregando } = useAuth();
  if (carregando) return <div className="carregando-rota">Carregando…</div>;
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

          <Route path="/militares" element={<RotaProtegida perfis={SARGENTEACAO}><MilitaresPage /></RotaProtegida>} />
          <Route path="/tipos-servico" element={<RotaProtegida perfis={SARGENTEANTE}><TiposServicoPage /></RotaProtegida>} />
          <Route path="/regras" element={<RotaProtegida perfis={SARGENTEANTE}><RegrasEscalaPage /></RotaProtegida>} />
          <Route path="/escala" element={<RotaProtegida><EscalaRoute /></RotaProtegida>} />
          <Route path="/escala/pdf/:data" element={<RotaProtegidaSemMenu><EscalaPdfPage /></RotaProtegidaSemMenu>} />
          <Route path="/minha-escala" element={<RotaProtegida><MinhaEscalaPage /></RotaProtegida>} />
          <Route path="/qualificacoes" element={<RotaProtegida perfis={GESTAO}><QualificacoesPage /></RotaProtegida>} />

          <Route path="/painel" element={<RotaProtegida perfis={GESTAO}><PainelPage /></RotaProtegida>} />
          <Route path="/avisos" element={<RotaProtegida><AvisosPage /></RotaProtegida>} />
          <Route path="/trocas" element={<RotaProtegida><TrocasPage /></RotaProtegida>} />
          <Route path="/missoes" element={<RotaProtegida perfis={GESTAO}><MissoesDispensasPage /></RotaProtegida>} />
          <Route path="/feriados" element={<RotaProtegida perfis={SARGENTEANTE}><FeriadosPage /></RotaProtegida>} />
          <Route path="/postos-graduacao" element={<RotaProtegida perfis={SARGENTEANTE}><PostosGraduacaoPage /></RotaProtegida>} />
          <Route path="/subunidades" element={<RotaProtegida perfis={SARGENTEANTE}><SubunidadesPage /></RotaProtegida>} />
          <Route path="/perfis" element={<RotaProtegida perfis={SARGENTEANTE}><PerfisPermissoesPage /></RotaProtegida>} />
          <Route path="/minha-conta" element={<RotaProtegida><MinhaContaPage /></RotaProtegida>} />
          <Route path="/militares/:id" element={<RotaProtegida perfis={SARGENTEACAO}><FichaMilitarPage /></RotaProtegida>} />
          <Route path="/auditoria" element={<RotaProtegida perfis={SARGENTEANTE}><AuditoriaPage /></RotaProtegida>} />
          <Route path="/boletim" element={<RotaProtegida><BoletimPage /></RotaProtegida>} />
          <Route path="/historico" element={<RotaProtegida><HistoricoPage /></RotaProtegida>} />
          <Route path="*" element={<RotaProtegida><NaoEncontradaPage /></RotaProtegida>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
