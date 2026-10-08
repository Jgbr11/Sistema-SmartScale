import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useOrganizacao } from "../hooks/useOrganizacao";
import { mascararCpf, somenteDigitos } from "../utils/mascaras";
import { FitaDoServico } from "../components/ui/FitaDoServico";

export function LoginPage() {
  const { entrar } = useAuth();
  const organizacao = useOrganizacao();
  const navigate = useNavigate();
  const [login, setLogin] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await entrar(somenteDigitos(login), senha);
      navigate("/");
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Servidor indisponível. Tente de novo em instantes.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login-wrap">
      <div className="login-ident">
        <span className="sobrelinha sobrelinha-clara">{organizacao ? organizacao.nome : "Escala de serviço"}</span>
        <h1>MilScale</h1>
        <p>Escala de serviço de 24 horas, montada pela ordem de quem está há mais tempo sem tirar serviço.</p>
        <FitaDoServico />
      </div>
      <div className="login-form-wrap">
        <form className="login-card" onSubmit={handleSubmit}>
          <h2>Entrar</h2>
          <p className="sub">Use seu CPF e a senha do batalhão.</p>

          {erro && <div className="error-box">{erro}</div>}

          <div className="field">
            <label>CPF</label>
            <input
              value={login}
              onChange={(e) => setLogin(mascararCpf(e.target.value))}
              placeholder="000.000.000-00"
              autoFocus
            />
          </div>
          <div className="field">
            <label>Senha</label>
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="••••••••"
            />
          </div>
          <button className="btn btn-primary largura-total" disabled={enviando}>
            {enviando ? "Entrando…" : "Entrar"}
          </button>
          {import.meta.env.DEV && (
            <p className="login-demo">
              Contas de demonstração (senha <code>milscale123</code>): 000.000.000-01
              (sargenteante), 000.000.000-02 (cabo), 000.000.000-03 (soldado),
              000.000.000-04 (nogueira).
            </p>
          )}
        </form>
      </div>
    </div>
  );
}
